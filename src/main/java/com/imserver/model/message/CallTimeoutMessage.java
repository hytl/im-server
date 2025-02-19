package com.imserver.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 呼叫超时消息
 */
@AllArgsConstructor
@Data
public class CallTimeoutMessage {
    private String callId;
}
