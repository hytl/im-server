package com.imserver.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 呼叫挂断
 */
@AllArgsConstructor
@Data
public class CallHangupMessage {
    /**
     * 呼叫ID
     */
    private String callId;
}
