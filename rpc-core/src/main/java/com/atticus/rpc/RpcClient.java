package com.atticus.rpc;

import com.atticus.rpc.entity.RpcRequest;
import com.atticus.rpc.serializer.CommonSerializer;

/**
 * 客户端类通用接口
 */
public interface RpcClient {

    Object sendRequest(RpcRequest rpcRequest);

    void setSerializer(CommonSerializer serializer);
}
