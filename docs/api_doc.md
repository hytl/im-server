# 用户身份验证

## WebSocket

1. WebSocket URL格式规范

   https://{domain}:{port}/imserver/ws?userId={userId}   // SockJS

   wss://{domain}:{port}/imserver/ws?userId={userId}    // 原生WebSocket

2. CONNECT帧添加ide头传递token

前端参考代码：

```js
const socket = new SockJS('https://{domain}:{port}/imserver/ws?userId={userId}'); // SockJS
const socket = new WebSocket('wss://{domain}:{port}/imserver/ws?userId={userId}'); // 原生WebSocket
const stompClient = Stomp.over(socket);
const headers = {
    ide:your-auth-token
};
stompClient.connect(headers, function(frame) {
    console.log('Connected: ' + frame);
    // 连接成功后的逻辑
}, function(error) {
    console.log('Error: ' + error);
    // 连接失败后的逻辑
});
```

## HTTP

请求头添加ide，示例：ide:123

接口响应code：小于1000和http状态一致，常用的200（ok）、400（BAD_REQUEST 通常是传参有误）、401（UNAUTHORIZED）、403（FORBIDDEN）、404（NOT_FOUND）、500（INTERNAL_SERVER_ERROR）；

大于1000为业务错误【http状态码还是200】（如Call-Request阶段1001是当用户呼叫时，用户忙；微信处理策略是打过去直接就断开了，呼叫方的客户端有“用户忙”提示）

# 用户在线状态

- SEND帧地址：/app/user/status

  说明：建议定时拉取（如果需要长期展示用户在线状态）

  **报文示例：**

  ```json
  ["userId1","userId2"] // 用户id数组，最大长度200
  ```

- 客户端订阅地址：/topic/user/status

  说明：此订阅信息，如果有用户上线/离线，会作主动推送。

  **报文示例：**

  ```json
  [
    {
      "userId": "userId1", // 用户ID
      "status": "ONLINE" // 状态，枚举值：ONLINE(在线)、OFFLINE(离线)
    }
  ]
  ```


# 音视频通话(WebRTC)

## WebRTC 视频通话时序图

![WebSocket视频通话时序图](.\api_doc.assets\WebSocket视频通话时序图.png)

## **流程详解**

1. **呼叫阶段 (Call-Request、Call-Response、Call-Hangup、Call-Timeout)**:

    - **呼叫发起 Call-Request**

        - Caller 通过信令服务器将Call-Request发送给 Callee

        - **WebSocket接口**

          *SEND帧地址：*/app/call/request  

          *订阅地址：*/user/queue/call/request

        - **HTTP接口**  POST  /imserver/call/request

          **请求报文示例**

          ```json
          {
              "callId": "F15D1A7F-907C-605E-9904-3B4A17688754", // 呼叫ID，客户端生成，唯一随机数，必填，长度1~100
              "type": "VIDEO", // 通话类型，缺省：VIDEO，可选，枚举值：VIDEO(视频通话)、AUDIO(语音通话)
              "callee": "calleeUserId", // 被呼叫方用户ID
            "callerUserName": "callerUserName", // 呼叫方用户名
              "calleeUserName": "calleeUserName" // 被呼叫方用户名
          }
          ```

          **HTTP响应报文示例**

          ```json
          {
            "code": 200, // 1001 被呼叫方忙
            "msg": "ok",
            "data": null
          }
          ```

    - **呼叫响应 Call-Response**

        - Callee 通过信令服务器将Call-Response发送给 Caller

        - **WebSocket接口**

          *SEND帧地址：*/app/call/response

          *订阅地址：*/user/queue/call/response、/user/queue/call/down（为单个用户支持多客户端准备的【如果不支持多客户端可不订阅】，场景：如微信支持PC端和手机端同时在线，当用户所有在线端侧收到呼叫时，其中一个客户端用户接受或拒绝呼叫请求时，其他客户端的呼叫窗口都要关闭）
          
        - **HTTP接口** POST /imserver/call/response
          
          **请求报文示例**
          
          ```json
          {
            "callId": "F15D1A7F-907C-605E-9904-3B4A17688754", // 呼叫ID，由Call-Request阶段获取
            "type": "ACCEPT", // 响应类型，必填，枚举值：ACCEPT(接受)、REJECT(拒绝)
          }
          ```
          
          **HTTP响应报文示例**
          
          ```json
          {
            "code": 200,
            "msg": "ok",
            "data": null
          }
          ```

    - **呼叫挂断 Call-Hangup**

        - Caller/Callee通过信令服务器将Call-Hangup发送给 Callee/Caller

        - **WebSocket接口**

          *SEND帧地址：*/app/call/hangup

          *订阅地址：*/user/queue/call/hangup 报文：{"callId": "F15D1A7F-907C-605E-9904-3B4A17688754", // 呼叫ID}
          
        - **HTTP接口** POST /imserver/call/hangup
          

        **请求报文示例：**空

        **HTTP响应报文示例**

          ```json
        {
          "code": 200,
          "msg": "ok",
          "data": null
        }
          ```

    - **呼叫超时 Call-Timeout**

        - 呼叫发起后，当超过一定时间（目前30秒）callee每个客户端和caller的呼叫端都会收到呼叫超时消息
        - 订阅地址：/user/queue/call/timeout 报文：{"callId": "F15D1A7F-907C-605E-9904-3B4A17688754", // 呼叫ID}

2. **WebRTC信令交换阶段 (Offer、Answer、ICE Candidate)**

    - **WebRTC信令**

      - 交换 **SDP（Session Description Protocol）** 信息（Offer 和 Answer），描述媒体能力和会话参数。
      - 交换 **ICE（Interactive Connectivity Establishment） Candidate**（网络地址信息），描述网络地址信息，用于建立直接连接。
      - SEND帧地址：/app/rtc/signaling

      **Offer报文示例：**

      ```json
      {
        "callId": "F15D1A7F-907C-605E-9904-3B4A17688754", // 呼叫ID，由Call-Request阶段获取
        "type": "OFFER", // 信令类型，必填，枚举值：OFFER, ANSWER, ICE_CANDIDATE
        "payload": "v=0\r\no=- 987654321 2 IN IP4 127.0.0.1\r\n..."
      }
      ```

      **Answer报文示例：**

      ```json
      {
           "callId": "F15D1A7F-907C-605E-9904-3B4A17688754", // 呼叫ID，由Call-Request阶段获取
           "type": "ANSWER", // 信令类型，必填，枚举值：OFFER, ANSWER, ICE_CANDIDATE
           "payload": "v=0\r\no=- 987654321 2 IN IP4 127.0.0.1\r\n..."
      }
      ```

      **ICE Candidate报文示例：**

       ```json
      {
           "callId": "F15D1A7F-907C-605E-9904-3B4A17688754", // 呼叫ID，由Call-Request阶段获取
           "type": "ICE_CANDIDATE", // 信令类型，必填，枚举值：OFFER, ANSWER, ICE_CANDIDATE
           "payload": {
                "candidate": "candidate:123456 1 udp 2113929471 192.168.1.1 12345 typ host",
                "sdpMid": "0",
                "sdpMLineIndex": 0
           }
      }
       ```

      - 客户端订阅地址：/user/queue/rtc/signaling



# STUN/TURN服务

> 在WebRTC中，STUN（Session Traversal Utilities for NAT）和TURN（Traversal Using Relays around NAT）是用于解决NAT（网络地址转换）穿透问题的协议，确保在不同网络环境下的设备能够直接通信。
>
> 说明：获取到的凭证是短期凭证，如果凭证过期，TURN 服务器会拒绝客户端的请求。

- **WebSocket接口**

  *SEND帧地址：*/app/turn/get-turn-credentials

  *订阅地址：*/user/queue/turn/get-turn-credentials

- **HTTP接口** GET /imserver/turn/get-turn-credentials
  

  **请求报文示例**

  ```json
  {}
  ```

  **WebSocket响应报文示例**

  ```json
  {
      "iceServers": [
          {
              "url": "stun:192.168.1.10:3478",
              "username": null,
              "credential": null
          },
          {
              "url": "turns:192.168.1.10:5349",
              "username": "1738936259:user1",
              "credential": "9x/Nusr5PJwRH7lva8FzX4dZIcs="
          }
      ]
  }
  ```

  **HTTP响应报文示例**

  ```json
  {
    "code": 200,
    "msg": "ok",
    "data": {
      "iceServers": [
        {
          "url": "stun:192.168.1.10:3478",
          "username": null,
          "credential": null
        },
        {
          "url": "turns:192.168.1.10:5349",
          "username": "1739285104:1",
          "credential": "sDt5LMC5oTsUnWCRUGv/nWeQWIc="
        }
      ]
    }
  }
  ```

  

- 前端处理凭证过期

  ```js
  // 伪代码
  let iceServers = [
      {
          "url": "stun:stun.example.com:3478"
      },
      {
          urls: "turns:turn.example.com:3478",
          username: "old_username",
          credential: "old_password",
      }
  ];
  
  const peerConnection = new RTCPeerConnection({ iceServers });
  
  peerConnection.oniceconnectionstatechange = () => {
      if (peerConnection.iceConnectionState === 'failed') {
          // 检测到连接失败，重新获取凭证
          fetchNewCredentials().then((resp) => {
              // 重新创建 PeerConnection
              peerConnection.close();
              const newPeerConnection = new RTCPeerConnection({ resp });
              // 重新发起协商
              renegotiate(newPeerConnection);
          });
      }
  };
  
  async function fetchNewCredentials() {
      const response = await fetch('/imserver/turn/get-turn-credentials');
      return response.json();
  }
  ```



# （可选）SEND帧服务端错误/业务错误信息获取

- 客户端订阅地址：/user/queue/errors

  说明：接口报错信息

  **报文示例：**

  ```json
  {
      "code": 400,
      "message": "Field error in object 'message' on field 'callerUserName': rejected value [null]; codes [NotBlank.message.callerUserName,NotBlank.callerUserName,NotBlank.java.lang.String,NotBlank]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [message.callerUserName,callerUserName]; arguments []; default message [callerUserName]]; default message [must not be blank]"
  }
  ```