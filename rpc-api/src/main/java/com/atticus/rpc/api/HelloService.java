package com.atticus.rpc.api;

/**
 * 通用接口
 */
public interface HelloService {

    /**
     * 接口方法
     *
     * @param object HelloObject对象
     * @return 结果字符串
     */
    String hello(HelloObject object);

}
