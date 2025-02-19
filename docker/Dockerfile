FROM openjdk:21-slim
WORKDIR /app
COPY ./imserver-0.0.1-SNAPSHOT.jar ./app.jar

# 环境变量
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# 暴露端口
EXPOSE 8443

# 启动命令
ENTRYPOINT ["java", "-jar", "-XX:+UseZGC", "-XX:+ZGenerational", "app.jar", "--spring.config.location=file:/app/config/application.properties"]