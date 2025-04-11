package com.hytl.mserver.model;

import java.util.ArrayList;
import java.util.List;

public class RTCConfiguration {
    private List<IceServer> iceServers;

    // 构造函数
    public RTCConfiguration() {
        this.iceServers = new ArrayList<>();
    }

    // 添加 ICE 服务器
    public void addIceServer(IceServer iceServer) {
        this.iceServers.add(iceServer);
    }

    // Getter 和 Setter 方法
    public List<IceServer> getIceServers() {
        return iceServers;
    }

    public void setIceServers(List<IceServer> iceServers) {
        this.iceServers = iceServers;
    }

    @Override
    public String toString() {
        return "RTCConfiguration{" +
                "iceServers=" + iceServers +
                '}';
    }
}