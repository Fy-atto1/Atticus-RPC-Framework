package com.atticus.rpc.handler;

import com.atticus.rpc.entity.RpcRequest;
import com.atticus.rpc.entity.RpcResponse;
import com.atticus.rpc.enumeration.ResponseCode;
import com.atticus.rpc.provider.ServiceProvider;
import com.atticus.rpc.provider.ServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 实际执行方法调用的处理器
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private static final ServiceProvider serviceProvider;

    static {
        serviceProvider = new ServiceProviderImpl();
    }

    public Object handle(RpcRequest rpcRequest) {
        Object result = null;
        // 从服务端本地注册表中获取服务实体
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
        try {
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("服务：{}成功调用方法：{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            logger.error("调用或发送时有错误发生：" + e);
        }
        // 方法调用成功
        return RpcResponse.success(result, rpcRequest.getRequestId());
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service)
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        Method method;
        try {
            // getClass()获取的是实例对象的类型
            method = service.getClass()
                    .getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            // 方法调用失败
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}
