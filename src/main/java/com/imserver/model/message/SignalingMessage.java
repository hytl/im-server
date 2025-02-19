package com.imserver.model.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignalingMessage {
    /**
     * 信令类型
     */
    @NotNull
    private SignalingType type;
    /**
     * 呼叫ID
     */
    @Size(max = 100)
    @NotBlank
    private String callId;
    /**
     * 信令内容，如SDP或ICE候选信息
     */
    @NotNull
    private Object payload;

    /**
     * 信令类型
     */
    public enum SignalingType {
        OFFER, ANSWER, ICE_CANDIDATE
    }
}