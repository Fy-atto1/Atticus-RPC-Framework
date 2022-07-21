package com.atticus.test;

import com.atticus.rpc.api.HelloObject;
import com.atticus.rpc.api.HelloService;
import com.atticus.rpc.client.RpcClientProxy;

/**
 * 用于测试的客户端
 */
public class TestClient {

    public static void main(String[] args) {
        // 接口与代理对象之间的中介对象
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);
        // 创建代理对象
        HelloService helloService = proxy.getProxy(HelloService.class);
        // 创建接口方法的参数对象
        HelloObject object = new HelloObject(12, "This is a message");
        // 由动态代理可知，代理对象调用hello()实际会执行invoke()
        String res = helloService.hello(object);
        System.out.println(res);
    }

}
