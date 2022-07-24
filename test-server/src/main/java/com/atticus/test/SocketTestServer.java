package com.atticus.test;

import com.atticus.rpc.RpcServer;
import com.atticus.rpc.api.HelloService;
import com.atticus.rpc.registry.DefaultServiceRegistry;
import com.atticus.rpc.registry.ServiceRegistry;
import com.atticus.rpc.serializer.HessianSerializer;
import com.atticus.rpc.socket.server.SocketServer;

/**
 * 用于测试的服务提供方（服务端）
 */
public class SocketTestServer {

    public static void main(String[] args) {
        // 创建服务对象
        HelloService helloService = new HelloServiceImpl();
        // 创建服务容器
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        // 注册服务对象到服务容器中
        serviceRegistry.register(helloService);
        // 将服务容器纳入到服务端
        RpcServer server = new SocketServer(serviceRegistry);
        server.setSerializer(new HessianSerializer());
        // 启动服务端
        server.start(9999);
    }

}
