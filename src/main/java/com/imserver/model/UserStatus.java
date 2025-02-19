package com.imserver.model;

public record UserStatus(String userId, Status status) {

    public enum Status {
        OFFLINE, ONLINE
    }
}
