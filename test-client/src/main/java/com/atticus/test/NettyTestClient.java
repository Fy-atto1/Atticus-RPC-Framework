package com.atticus.test;

import com.atticus.rpc.api.HelloObject;
import com.atticus.rpc.api.HelloService;
import com.atticus.rpc.serializer.ProtostuffSerializer;
import com.atticus.rpc.transport.RpcClient;
import com.atticus.rpc.transport.RpcClientProxy;
import com.atticus.rpc.transport.netty.client.NettyClient;

/**
 * 测试用Netty客户端
 */
public class NettyTestClient {

    public static void main(String[] args) {
        RpcClient client = new NettyClient();
        client.setSerializer(new ProtostuffSerializer());
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject helloObject = new HelloObject(12, "this is netty style");
        String res = helloService.hello(helloObject);
        System.out.println(res);
    }
}
