package com.atticus.rpc.util;

import com.atticus.rpc.entity.RpcRequest;
import com.atticus.rpc.entity.RpcResponse;
import com.atticus.rpc.enumeration.ResponseCode;
import com.atticus.rpc.enumeration.RpcError;
import com.atticus.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查响应和请求
 */
public class RpcMessageChecker {

    private static final Logger logger = LoggerFactory.getLogger(RpcMessageChecker.class);

    private static final String INTERFACE_NAME = "interfaceName";

    private RpcMessageChecker() {
    }

    public static void check(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        if (rpcResponse == null) {
            logger.error("调用服务失败，serviceName:{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE,
                    INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        // 如果响应与请求的请求号不一致
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcError.RESPONSE_NOT_MATCH,
                    INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        // 如果调用失败
        if (rpcResponse.getStatusCode() == null
                || !(rpcResponse.getStatusCode().equals(ResponseCode.SUCCESS.getCode()))) {
            logger.error("调用服务失败，serviceName:{}，RpcResponse:{}",
                    rpcRequest.getInterfaceName(), rpcResponse);
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE,
                    INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
