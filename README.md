# Atticus-RPC-Framework

Atticus-RPC-Framework 是一款基于 Nacos 实现的 RPC 框架。网络传输实现了基于 Java 原生 Socket 与 Netty 版本，并且实现了多种序列化方式与负载均衡算法。

## 架构

![简单架构.png](./images/简单架构.png)

服务提供端 Server 向注册中心 Nacos 注册服务，服务消费者通过注册中心 Nacos 获取服务相关信息，然后再通过网络向服务提供端Server发送服务请求。

![实现思路.jpg](./images/实现思路.jpg)

消费者调用提供者的方式取决于消费者的客户端选择，如果选用原生 Socket 则该步调用使用 BIO，如果选用 Netty 方式则该步调用使用 NIO。同理，如果该调用有返回值，则提供者向消费者发送返回值的方式取决于消费者的服务端选择。

## 特性

- 实现了并且统一使用自定义的传输协议
- 实现了基于 Java 原生 Socket 传输与 Netty 传输两种网络传输方式
- 实现了四种序列化方式，Json 方式、Kryo 算法、Hessian 算法与 Google Protobuf 方式
- 实现了两种负载均衡算法：随机算法与轮转算法
- 接口抽象良好，模块耦合度低，网络传输方式、序列化方式、负载均衡算法可配置
- 使用 Nacos 作为注册中心，管理服务提供者信息
- 消费端如果采用 Netty 方式，会复用 Channel 避免多次连接
- 如果消费端和提供者都采用 Netty 方式，会采用 Netty 的心跳机制，保证连接
- 服务提供端实现了基于注解的自动注册服务，并且处理了一个服务实现类实现了多个服务接口的情况

## 项目模块概览

- **rpc-api**    —— 通用接口
- **rpc-common**    —— 实体对象、工具类等公用类
- **rpc-core**    —— 框架的核心实现
- **test-client**    —— 用于测试的消费侧
- **test-server**    —— 用于测试的提供侧

## 传输协议（ARF协议）

服务请求与服务响应的传输采用了如下 ARF 协议（ Atticus-RPC-Framework 首字母）以防止粘包：

```
+---------------+---------------+-----------------+-------------+
|  Magic Number |  Package Type | Serializer Type | Data Length |
|    4 bytes    |    4 bytes    |     4 bytes     |   4 bytes   |
+---------------+---------------+-----------------+-------------+
|                          Data Bytes                           |
|                   Length: ${Data Length}                      |
+---------------------------------------------------------------+
```

| 字段            | 解释                                                         |
| :-------------- | :----------------------------------------------------------- |
| Magic Number    | 魔术字，表识一个 ARF 协议包，默认为0xCAFEBABE                |
| Package Type    | 包类型，指明这个包是一个调用请求包或者是服务响应包           |
| Serializer Type | 序列化器类型，指明这个包的数据部分的序列化方式               |
| Data Length     | 数据字节的长度                                               |
| Data Bytes      | 传输的对象，通常是一个`RpcRequest`或`RpcClient`对象，取决于`Package Type`字段，对象的序列化方式取决于`Serializer Type`字段。 |

### 运行项目

在此之前请确保 Nacos 运行在本地 `8848` 端口。

首先启动用于测试的服务提供侧，再启动用于测试的服务消费侧。如果成功运行，服务提供侧在控制台会打印服务请求信息，然后调用方法并返回执行结果，服务消费侧会接收到服务响应，控制台会打印服务响应信息以及执行服务方法后的返回结果。

## LICENSE

Atticus-RPC-Framework is under the MIT license. See
the [LICENSE](https://github.com/Fy-atto1/Atticus-RPC-Framework/blob/main/LICENSE) file for details.