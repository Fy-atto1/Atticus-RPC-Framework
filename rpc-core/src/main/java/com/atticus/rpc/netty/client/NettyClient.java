package com.atticus.rpc.netty.client;

import com.atticus.rpc.RpcClient;
import com.atticus.rpc.codec.CommonDecoder;
import com.atticus.rpc.codec.CommonEncoder;
import com.atticus.rpc.entity.RpcRequest;
import com.atticus.rpc.entity.RpcResponse;
import com.atticus.rpc.enumeration.RpcError;
import com.atticus.rpc.exception.RpcException;
import com.atticus.rpc.serializer.CommonSerializer;
import com.atticus.rpc.util.RpcMessageChecker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty方式客户端
 */
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static final Bootstrap bootstrap;

    private CommonSerializer serializer;

    static {
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);
    }

    private String host;
    private int port;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new CommonDecoder())
                        .addLast(new CommonEncoder(serializer))
                        .addLast(new NettyClientHandler());
            }
        });
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            logger.info("客户端连接到服务端{}：{}", host, port);
            Channel channel = future.channel();
            if (channel != null) {
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
                return rpcResponse.getData();
            }
        } catch (InterruptedException e) {
            logger.error("发送消息时有错误发生", e);
        }
        return null;
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
