package com.atticus.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用接口中客户端需要传递给服务端的参数
 */

// 自动提供类的get、set、equals、hashCode、canEqual、toString方法
@Data
// 添加一个含有所有已声明字段属性参数的构造函数
@AllArgsConstructor
public class HelloObject implements Serializable {
    // 实现Serializable接口，是因为在调用过程中HelloObject的对象需要从客户端传递给服务端。
    // Serializable序列化接口没有任何方法或者字段，只是用于标识可序列化的语义。
    // 实现了Serializable接口的类可以被ObjectOutputStream转换为字节流。
    // 同时也可以通过ObjectInputStream再将其解析为对象。
    // 序列化是指把对象转换为字节序列的过程；反序列化则是把持久化的字节文件数据恢复为对象的过程。

    private Integer id;
    private String message;

}
