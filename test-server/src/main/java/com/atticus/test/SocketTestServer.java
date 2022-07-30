package com.atticus.test;

import com.atticus.rpc.api.HelloService;
import com.atticus.rpc.serializer.CommonSerializer;
import com.atticus.rpc.transport.RpcServer;
import com.atticus.rpc.transport.socket.server.SocketServer;

/**
 * 测试用Socket服务端
 */
public class SocketTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl2();
        RpcServer server = new SocketServer("127.0.0.1", 9998, CommonSerializer.HESSIAN_SERIALIZER);
        // 启动服务端
        server.publishService(helloService, HelloService.class);
    }
}
