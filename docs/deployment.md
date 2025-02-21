# 软件依赖

docker（安装过程省略）

coturn

## coturn安装

> coturn用来STUN/TURN服务。
>
> 在WebRTC中，STUN（Session Traversal Utilities for NAT）和TURN（Traversal Using Relays around NAT）是用于解决NAT（网络地址转换）穿透问题的协议，确保在不同网络环境下的设备能够直接通信。

1. 下载镜像

   docker pull coturn/coturn:4.6.3

2. 修改配置文件

   - turnserver.conf

     **external-ip**修改为服务器的公网IP

     **realm**修改为提供给外部的域名

3. 运行coturn

   ```shell
   docker run -d --network=host \
   --name=coturn \
   -v {path}/turnserver.conf:/etc/coturn/turnserver.conf \
   -v {path}/certificate.pem:/etc/ssl/certs/certificate.pem \
   -v {path}/private-key.pem:/etc/ssl/certs/private-key.pem \
   coturn/coturn:4.6.3
   ```

   **\* 每个{path}替换为宿主机的原文件路径**

4. 查看日志

   docker logs -f --tail 100 coturn

   **\* 没有明显报错日志即可，如果有通常是配置文件turnserver.conf、证书或私钥文件有误**

5. 开放端口（ubuntu版）

   ```shell
   sudo ufw allow 13478/udp
   sudo ufw allow 13478/tcp
   sudo ufw allow 15349/udp
   sudo ufw allow 15349/tcp
   sudo ufw allow 31000:32000/udp
   ```

   **\* 注意：如果是云服务器还需要配置安全组**



# 后端服务

1. 加载docker镜像

   docker load -i imserver1.1.tar
   
2. TLS证书和私钥合一

   ```shell
   openssl pkcs12 -export -out keystore.p12 -inkey private-key.pem -in certificate.pem
   ```

   **\* 记住输入的密码，后面服务运行时指定的运行参数需要用到，参数占位符{tls_password}**

3. 将需要用到的文件放置到同一个文件夹中

   application.properties、keystore.p12

4. 运行

   ```shell
   docker run -d -p 18443:8443 -e STUN_SERVER=personal.com.cn:13478 -e TURN_SERVER=personal.com.cn:15349 -v {path}/config:/app/config --name imserver imserver:v1.1
   ```

5. 开放端口（ubuntu版）

   ```shell
   sudo ufw allow 18443/udp
   ```

   **\* 注意：如果是云服务器还需要配置安全组**