package com.atticus.rpc.serializer;

import com.atticus.rpc.entity.RpcRequest;
import com.atticus.rpc.enumeration.SerializerCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 使用Json格式的序列化器
 */
public class JsonSerializer implements CommonSerializer {

    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    // ObjectMapper支持线程安全
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            logger.error("序列化时有错误发生：{}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            Object obj = objectMapper.readValue(bytes, clazz);
            if (obj instanceof RpcRequest) {
                obj = handlerRequest(obj);
            }
            return obj;
        } catch (IOException e) {
            logger.error("反序列化时有错误发生：{}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用JSON反序列化Object数组，无法保证反序列化后仍然为原实例类，通常直接被反序列化为String类型，因此要特殊处理
     *
     * @param obj RpcRequest对象
     * @return 对parameters字段重新进行反序列化后的RpcRequest对象
     */
    private Object handlerRequest(Object obj) throws IOException {
        RpcRequest rpcRequest = (RpcRequest) obj;
        // 根据RpcRequest中的另一个字段paramTypes来获取到Object数组中的每个实例的实际类，辅助反序列化
        for (int i = 0; i < rpcRequest.getParamTypes().length; i++) {
            Class<?> paramTypeClass = rpcRequest.getParamTypes()[i];
            if (!paramTypeClass.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                // 对parameters字段重新进行反序列化
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, paramTypeClass);
            }
        }
        return rpcRequest;
    }

    @Override
    public int getCode() {
        return SerializerCode.JSON.getCode();
    }
}
