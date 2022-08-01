package com.atticus.rpc.transport.netty.client;

import com.atticus.rpc.entity.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未处理的请求（对所有Netty客户端请求进行统一管理）
 */
public class UnprocessedRequests {

    private static ConcurrentHashMap<String, CompletableFuture<RpcResponse>> unprocessedRequests
            = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprocessedRequests.put(requestId, future);
    }

    public void remove(String requestId) {
        unprocessedRequests.remove(requestId);
    }

    public void complete(RpcResponse rpcResponse) {
        // 请求完成，将请求从未完成的请求集合中移除
        CompletableFuture<RpcResponse> future = unprocessedRequests.remove(rpcResponse.getRequestId());
        if (future != null) {
            // 把响应对象放入future中
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
