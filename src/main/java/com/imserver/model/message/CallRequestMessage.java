package com.imserver.model.message;

import com.imserver.model.CallSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 呼叫请求
 */
@Data
public class CallRequestMessage {
    /**
     * 呼叫ID
     */
    @Size(max = 100)
    @NotBlank
    private String callId;

    /**
     * 通话类型，表示请求的通话是音频通话还是视频通话。
     */
    private CallSession.CallType type = CallSession.CallType.VIDEO;

    /**
     * 呼叫方
     */
    @Null
    private String caller;

    /**
     * 呼叫方用户名
     */
    @NotBlank
    private String callerUserName;

    /**
     * 被呼叫方
     */
    @Size(max = 100)
    @NotBlank
    private String callee;

    /**
     * 被呼叫方用户名
     */
    @NotBlank
    private String calleeUserName;
}