package com.atticus.test;

import com.atticus.rpc.api.HelloService;
import com.atticus.rpc.netty.server.NettyServer;
import com.atticus.rpc.registry.DefaultServiceRegistry;
import com.atticus.rpc.registry.ServiceRegistry;

/**
 * 测试用Netty服务端
 */
public class NettyTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        serviceRegistry.register(helloService);
        NettyServer server = new NettyServer();
        server.start(9999);
    }
}
