package com.atticus.rpc.transport;

import com.atticus.rpc.serializer.CommonSerializer;

/**
 * 服务端通用接口
 */
public interface RpcServer {

    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    void start();

    /**
     * 向Nacos注册服务
     *
     * @param service     服务实体
     * @param serviceName 服务名称
     * @param <T>         泛型
     */
    <T> void publishService(T service, String serviceName);
}
