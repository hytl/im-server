package com.imserver.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SessionResponseMessage {
    /**
     * 返回给客户端当前其的sessionId，为了订阅session域消息
     */
    private String sessionId;
}
