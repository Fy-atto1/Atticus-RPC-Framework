package com.atticus.rpc.serializer;

import com.atticus.rpc.enumeration.SerializerCode;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * protostuff序列化器
 */
public class ProtostuffSerializer implements CommonSerializer {

    /**
     * 用来存放对象序列化之后的数据，避免每次序列化都重新申请buffer空间
     * 如果设置的空间不足，会自动进行扩展
     * 但是空间大小需要设置一个合适的值，因为过大浪费空间，过小会自动扩展浪费时间
     */
    private LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    /**
     * 缓存类对应的Schema
     * 由于构造Schema需要获得对象的类和字段信息，会用到反射机制
     * 这是一个很耗时的过程，因此进行缓存很有必要，下次遇到相同的类直接从缓存中获取
     */
    private Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public byte[] serialize(Object obj) {
        Class clazz = obj.getClass();
        Schema schema = getSchema(clazz);
        byte[] data;
        try {
            // 序列化操作，将对象转换为字节数组
            data = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        Schema schema = getSchema(clazz);
        Object obj = schema.newMessage();
        // 反序列化操作，将字节数组转换为对应的对象
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }

    @Override
    public int getCode() {
        return SerializerCode.PROTOBUF.getCode();
    }

    /**
     * 获取Schema
     *
     * @param clazz 对象的类型
     * @return 对应的Schema
     */
    @SuppressWarnings("unchecked")
    private Schema getSchema(Class clazz) {
        // 首先尝试从Map缓存中获取类对应的Schema
        Schema schema = schemaCache.get(clazz);
        if (Objects.isNull(schema)) {
            // 创建一个新的Schema，RuntimeSchema就是将schema繁琐的创建过程进行了封装
            // 它的创建过程是线程安全的，采用懒创建的方式，即当需要schema时才创建
            schema = RuntimeSchema.getSchema(clazz);
            if (Objects.nonNull(schema)) {
                // 缓存schema，方便下次直接使用
                schemaCache.put(clazz, schema);
            }
        }
        return schema;
    }
}
