package com.atticus.test;

import com.atticus.rpc.RpcServer;
import com.atticus.rpc.api.HelloService;
import com.atticus.rpc.netty.server.NettyServer;
import com.atticus.rpc.registry.DefaultServiceRegistry;
import com.atticus.rpc.registry.ServiceRegistry;
import com.atticus.rpc.serializer.ProtostuffSerializer;

/**
 * 测试用Netty服务端
 */
public class NettyTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        serviceRegistry.register(helloService);
        RpcServer server = new NettyServer();
        server.setSerializer(new ProtostuffSerializer());
        server.start(9999);
    }
}
