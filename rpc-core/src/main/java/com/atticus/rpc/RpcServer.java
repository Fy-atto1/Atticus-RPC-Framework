package com.atticus.rpc;

import com.atticus.rpc.serializer.CommonSerializer;

/**
 * 服务端通用接口
 */
public interface RpcServer {

    void start(int port);

    void setSerializer(CommonSerializer serializer);
}
