package com.atticus.rpc;

import com.atticus.rpc.entity.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 实际执行方法调用的处理器
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    public Object handle(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("服务：{}成功调用方法：{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            logger.error("调用或发送时有错误发生：" + e);
        }
        return result;
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service)
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        Method method = null;
        try {
            // getClass()获取的是实例对象的类型
            method = service.getClass()
                    .getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            logger.error("调用或发送时有错误发生：" + e);
        }
        if (method != null) {
            return method.invoke(service, rpcRequest.getParameters());
        } else {
            logger.info("未找到指定方法");
            return null;
        }
    }

}
