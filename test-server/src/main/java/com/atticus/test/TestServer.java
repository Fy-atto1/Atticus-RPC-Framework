package com.atticus.test;

import com.atticus.rpc.api.HelloService;
import com.atticus.rpc.server.RpcServer;

/**
 * 用于测试的服务提供方（服务端）
 */
public class TestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();
        // 注册HelloServiceImpl服务
        rpcServer.register(helloService, 9000);
    }

}
