package com.hytl.mserver.model.message;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 呼叫响应
 */
@Data
public class CallResponseMessage {
    /**
     * 呼叫ID
     */
    @NotNull
    private String callId;

    /**
     * 响应类型
     */
    @NotNull
    private ResponseType type;

    public enum ResponseType {
        ACCEPT, REJECT
    }
}