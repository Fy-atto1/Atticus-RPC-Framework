package com.atticus.test;

import com.atticus.rpc.api.HelloService;
import com.atticus.rpc.serializer.CommonSerializer;
import com.atticus.rpc.transport.RpcServer;
import com.atticus.rpc.transport.netty.server.NettyServer;

/**
 * 测试用Netty服务端
 */
public class NettyTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer server = new NettyServer("127.0.0.1", 9999, CommonSerializer.PROTOBUF_SERIALIZER);
        server.publishService(helloService, HelloService.class);
    }
}
