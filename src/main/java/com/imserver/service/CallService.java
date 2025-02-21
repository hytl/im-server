package com.imserver.service;

import com.imserver.enums.ErrorCode;
import com.imserver.exception.ImServerException;
import com.imserver.model.CallSession;
import com.imserver.model.message.CallHangupMessage;
import com.imserver.model.message.CallRequestMessage;
import com.imserver.model.message.CallResponseMessage;
import com.imserver.model.message.CallTimeoutMessage;
import com.imserver.util.DelayedTaskManager;
import com.imserver.util.ThreadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 呼叫服务
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class CallService {
    // KEY:callId
    private final Map<String, CallSession> ACTIVE_CALL_SESSION_MAP = new ConcurrentHashMap<>();
    // KEY:userId VALUE:callId
    private final Map<String, String> ACTIVE_CALL_USER_MAP = new ConcurrentHashMap<>();
    // KEY:sessionId VALUE:callId
    private final Map<String, String> ACTIVE_SESSION_CALL_MAP = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();
    private final String delayedTaskCallKeyPrefix = "call_";

    private final MessageService messageService;
    private final ThreadUtil threadUtil;
    private final DelayedTaskManager delayedTaskManager;

    public CallSession getCallSession(String callId) {
        return ACTIVE_CALL_SESSION_MAP.get(callId);
    }

    public void request(CallRequestMessage message, String callerSessionId) {
        final String callId = message.getCallId();
        lock.lock();
        try {
            // 这种情况通常是前端单次多次点击，这里不做处理
            if (ACTIVE_CALL_SESSION_MAP.containsKey(callId)) {
                return;
            }

            // 判断是否被呼叫者占线
            if (ACTIVE_CALL_USER_MAP.containsKey(message.getCallee())) {
                throw new ImServerException(ErrorCode.CALL_USER_BUSY);
            }

            CallSession callSession = new CallSession(message.getCallId()
                    , message.getType()
                    , message.getCaller()
                    , message.getCallee()
                    , callerSessionId);

            callSession.setStatus(CallSession.CallStatus.RINGING);

            ACTIVE_CALL_SESSION_MAP.put(callId, callSession);

            delayedTaskManager.startDelayedTask(delayedTaskCallKeyPrefix + callId, () -> {
                String caller = callSession.getCaller();
                String callee = callSession.getCallee();
                log.info("Call-Timeout callId->{} caller->{} callee->{}", callId, caller, callee);
                CallTimeoutMessage callTimeoutMessage = new CallTimeoutMessage(callId);
                String callTimeoutDestination = "/queue/call/timeout";
                messageService.sendToUserAllOnlineClients(caller, callTimeoutDestination, callTimeoutMessage);
                messageService.sendToUserAllOnlineClients(callee, callTimeoutDestination, callTimeoutMessage);
                callSession.setStatus(CallSession.CallStatus.TIMEOUT);
                cleanByCallId(callId);
            }, 30, TimeUnit.SECONDS);

            // 发送消息给被呼叫者 消息发送给所有该用户的在线客户端
            messageService.sendToUserAllOnlineClients(message.getCallee()
                    , "/queue/call/request"
                    , message);
        } finally {
            lock.unlock();
        }
    }

    public void response(CallResponseMessage message, String sessionId) {
        final String callId = message.getCallId();
        lock.lock();
        try {
            CallSession callSession = ACTIVE_CALL_SESSION_MAP.get(callId);

            if (callSession == null || CallSession.CallStatus.RINGING != callSession.getStatus()) {
                return;
            }
            callSession.setCalleeSessionId(sessionId);
            final String dynamicTaskKey = delayedTaskCallKeyPrefix + callId;
            delayedTaskManager.cancelDelayedTask(dynamicTaskKey);
            threadUtil.execute(() -> // 发送响应结果消息给呼叫者的session域
                    messageService.sendToUserClient(callSession.getCallerSessionId()
                            , "/queue/call/response"
                            , message));

            threadUtil.execute(() ->  // 发送取消呼叫振铃消息给被呼叫的其他客户端会话user域，让其他客户端
                    // （实际是发送给所有被呼叫的客户端了，判断由客户端做）将呼叫拉起取消
                    messageService.sendToUserAllOnlineClients(callSession.getCallee()
                            , "/queue/call/down"
                            , message));

            if (CallResponseMessage.ResponseType.ACCEPT == message.getType()) {
                callSession.setStatus(CallSession.CallStatus.CONNECTED);

                // userId与callId绑定
                ACTIVE_CALL_USER_MAP.put(callSession.getCaller(), callId);
                ACTIVE_CALL_USER_MAP.put(callSession.getCallee(), callId);
                // sessionId与callId绑定
                ACTIVE_SESSION_CALL_MAP.put(callSession.getCallerSessionId(), callId);
                ACTIVE_SESSION_CALL_MAP.put(callSession.getCalleeSessionId(), callId);
            } else {
                callSession.setStatus(CallSession.CallStatus.REJECTED);
                cleanByCallId(callId);
            }
        } finally {
            lock.unlock();
        }
    }

    public void hangup(CallHangupMessage message) {
        lock.lock();
        try {
            String callId = message.getCallId();
            // 可能被呼叫方还没应答本次呼叫 或 拒绝应答了
            final CallSession callSession = ACTIVE_CALL_SESSION_MAP.get(callId);
            if (callSession == null || CallSession.CallStatus.REJECTED == callSession.getStatus()
                    || CallSession.CallStatus.ENDED == callSession.getStatus()) return;

            callSession.setEndTime(LocalDateTime.now());
            callSession.setStatus(CallSession.CallStatus.ENDED);

            // 将挂断消息发送给呼叫者和被呼叫者的user域
            threadUtil.execute(() -> messageService.sendToUserAllOnlineClients(callSession.getCaller()
                    , "/queue/call/hangup",
                    message
            ));

            threadUtil.execute(() -> messageService.sendToUserAllOnlineClients(callSession.getCallee()
                    , "/queue/call/hangup",
                    message
            ));

            // 清理数据
            cleanByCallId(callId);
        } finally {
            lock.unlock();
        }
    }

    public void cleanByCallId(String callId) {
        lock.lock();
        try {
            CallSession callSession = ACTIVE_CALL_SESSION_MAP.get(callId);
            if (callSession == null) return;
            ACTIVE_CALL_USER_MAP.remove(callSession.getCaller());
            ACTIVE_SESSION_CALL_MAP.remove(callSession.getCallerSessionId());

            if (callSession.getCallee() != null) {
                ACTIVE_CALL_USER_MAP.remove(callSession.getCallee());
            }
            if (callSession.getCalleeSessionId() != null) {
                ACTIVE_SESSION_CALL_MAP.remove(callSession.getCalleeSessionId());
            }

            ACTIVE_CALL_SESSION_MAP.remove(callSession.getCallId());
        } finally {
            lock.unlock();
        }
    }

    public void cleanByUserId(String userId) {
        lock.lock();
        try {
            String callId = ACTIVE_CALL_USER_MAP.get(userId);
            if (callId != null) {
                cleanByCallId(callId);
            }
        } finally {
            lock.unlock();
        }
    }
}