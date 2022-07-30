package com.atticus.test;

import com.atticus.rpc.api.HelloObject;
import com.atticus.rpc.api.HelloService;
import com.atticus.rpc.serializer.CommonSerializer;
import com.atticus.rpc.transport.RpcClient;
import com.atticus.rpc.transport.RpcClientProxy;
import com.atticus.rpc.transport.socket.client.SocketClient;

/**
 * 测试用Socket客户端
 */
public class SocketTestClient {

    public static void main(String[] args) {
        RpcClient client = new SocketClient(CommonSerializer.KRYO_SERIALIZER);
        // 接口与代理对象之间的中介对象
        RpcClientProxy proxy = new RpcClientProxy(client);
        // 创建代理对象
        HelloService helloService = proxy.getProxy(HelloService.class);
        // 创建接口方法的参数对象
        HelloObject object = new HelloObject(12, "This is test message");
        // 由动态代理可知，代理对象调用hello()实际会执行invoke()
        String res = helloService.hello(object);
        System.out.println(res);
    }
}
