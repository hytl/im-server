package com.imserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Data
public class IceServer {
    private String url;
    private String username;
    private String credential;
}