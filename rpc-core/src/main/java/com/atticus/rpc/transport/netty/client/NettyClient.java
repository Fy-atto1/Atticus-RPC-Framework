package com.atticus.rpc.transport.netty.client;

import com.atticus.rpc.entity.RpcRequest;
import com.atticus.rpc.entity.RpcResponse;
import com.atticus.rpc.enumeration.RpcError;
import com.atticus.rpc.exception.RpcException;
import com.atticus.rpc.register.NacosServiceDiscovery;
import com.atticus.rpc.register.ServiceDiscovery;
import com.atticus.rpc.serializer.CommonSerializer;
import com.atticus.rpc.transport.RpcClient;
import com.atticus.rpc.util.RpcMessageChecker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Netty方式客户端
 */
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static final EventLoopGroup group;
    private static final Bootstrap bootstrap;

    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                // 启用该功能时，TCP会主动探测空闲连接的有效性
                // 可以将此功能视为TCP的心跳机制，默认的心跳间隔是7200s，即2小时
                .option(ChannelOption.SO_KEEPALIVE, true);
    }

    private final ServiceDiscovery serviceDiscovery;

    private CommonSerializer serializer;

    public NettyClient() {
        serviceDiscovery = new NacosServiceDiscovery();
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        // 保证自定义实体类变量的原子性和共享性的线程安全，此处应用于RpcResponse
        AtomicReference<Object> result = new AtomicReference<>(null);
        try {
            // 从Nacos获取提供对应服务的服务端地址
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            // 创建Netty通道连接
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }
            // 向服务端发送请求，并设置监听
            // 关于writeAndFlush()的具体实现可以参考如下网址
            // https://blog.csdn.net/qq_34436819/article/details/103937188
            channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                if (future1.isSuccess()) {
                    logger.info(String.format("客户端发送消息：%s", rpcRequest.toString()));
                } else {
                    logger.error("发送消息时有错误发生：", future1.cause());
                }
            });
            channel.closeFuture().sync();
            // AttributeMap<AttributeKey, AttributeValue>是绑定在Channel上的
            // 可以设置用来获取通道对象
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
            // get()阻塞获取value
            RpcResponse rpcResponse = channel.attr(key).get();
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            result.set(rpcResponse.getData());
        } catch (InterruptedException e) {
            logger.error("发送消息时有错误发生", e);
            // interrupt()这里作用是给受阻塞的当前线程发出一个中断信号，让当前线程退出阻塞状态，以继续执行然后结束
            Thread.currentThread().interrupt();
        }
        return result.get();
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
