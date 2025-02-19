package com.imserver.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表示一个通话会话的类
 * 包含通话的基本信息，如会话ID、呼叫方、被呼叫方、通话类型、通话状态、开始时间和结束时间
 */
@Data
public class CallSession {
    /**
     * 呼叫ID
     */
    private final String callId;

    /**
     * 通话类型（音频或视频）
     */
    private final CallType type;

    /**
     * 呼叫方
     */
    private final String caller;

    /**
     * 被呼叫方
     */
    private final String callee;

    /**
     * 呼叫方sessionId（为支持多设备同时被呼叫，但仅有一个接受）
     */
    private final String callerSessionId;

    /**
     * 被呼叫方sessionId（为支持多设备同时被呼叫，但仅有一个接受）
     */
    private String calleeSessionId;

    /**
     * 当前通话状态
     */
    private volatile CallStatus status;

    /**
     * 通话开始时间
     */
    private final LocalDateTime startTime;

    /**
     * 通话结束时间
     */
    private LocalDateTime endTime;

    /**
     * 通话类型的枚举
     * 表示通话是音频通话还是视频通话
     */
    public enum CallType {
        /**
         * 音频通话
         */
        AUDIO,

        /**
         * 视频通话
         */
        VIDEO
    }

    /**
     * 通话状态的枚举
     * 表示通话的当前状态，如初始化、振铃、已连接、已结束或被拒绝
     */
    public enum CallStatus {
        /**
         * 通话正在初始化
         */
        INITIATING,

        /**
         * 被呼叫方正在振铃
         */
        RINGING,

        /**
         * 通话已连接
         */
        CONNECTED,

        /**
         * 呼叫超时
         */
        TIMEOUT,

        /**
         * 通话已结束
         */
        ENDED,

        /**
         * 通话被拒绝
         */
        REJECTED
    }

    public CallSession(String callId, CallType type, String caller, String callee, String callerSessionId) {
        this.status = CallStatus.INITIATING;
        this.callId = callId;
        this.type = type;
        this.caller = caller;
        this.callee = callee;
        this.callerSessionId = callerSessionId;
        this.startTime = LocalDateTime.now();
    }
}