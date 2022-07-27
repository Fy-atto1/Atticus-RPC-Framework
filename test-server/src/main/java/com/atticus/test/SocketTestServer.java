package com.atticus.test;

import com.atticus.rpc.api.HelloService;
import com.atticus.rpc.serializer.HessianSerializer;
import com.atticus.rpc.transport.RpcServer;
import com.atticus.rpc.transport.socket.server.SocketServer;

/**
 * 测试用Socket服务端
 */
public class SocketTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer server = new SocketServer("127.0.0.1", 9998);
        server.setSerializer(new HessianSerializer());
        // 启动服务端
        server.publishService(helloService, HelloService.class);
    }
}
