package com.hytl.mserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Data
public class IceServer {
    private String urls;
    private String username;
    private String credential;
}