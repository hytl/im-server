package com.imserver.model.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100)
    @NotBlank
    private String callId;
}
