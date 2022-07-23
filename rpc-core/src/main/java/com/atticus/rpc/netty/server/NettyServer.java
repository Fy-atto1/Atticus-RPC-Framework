package com.atticus.rpc.netty.server;

import com.atticus.rpc.RpcServer;
import com.atticus.rpc.codec.CommonDecoder;
import com.atticus.rpc.codec.CommonEncoder;
import com.atticus.rpc.serializer.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty方式服务端
 */
public class NettyServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Override
    public void start(int port) {
        // 用于处理客户端新连接的主“线程池”
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 用于连接后处理IO事件的从”线程池“
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 初始化Netty服务端启动器，作为服务端入口
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 将主从“线程池”初始化到启动器中
            serverBootstrap.group(bossGroup, workerGroup)
                    // 设置为服务端通道类型
                    .channel(NioServerSocketChannel.class)
                    // 日志打印方式
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 配置ServerChannel参数，服务端接收连接的最大队列长度
                    // 如果队列已满，那么客户端连接将被拒绝
                    .option(ChannelOption.SO_BACKLOG, 256)
                    // 启用该功能时，TCP会主动探测空闲连接的有效性
                    // 可以将此功能视为TCP的心跳机制，默认的心跳间隔是7200s，即2小时
                    // 理解可参考：https://blog.csdn.net/lclwjl/article/details/80154565
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // 配置Channel参数，nodelay没有延迟，true表示禁用Nagle算法，较小传输延迟
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 初始化Handler，设置Handler操作
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            // 初始化管道
                            ChannelPipeline pipeline = channel.pipeline();
                            // 向管道中添加Handler，注意入站Handler与出站Handler都必须按照实际执行顺序添加
                            // 比如先解码再进行Server处理，那么Decoder()就要放在前面
                            // 但入站和出站Handler之间互不影响，这里就是先添加的出站Handler再添加入站Handler
                            pipeline.addLast(new CommonEncoder(new KryoSerializer()))
                                    .addLast(new CommonDecoder())
                                    .addLast(new NettyServerHandler());
                        }
                    });
            // 绑定端口，启动Netty，sync()代表阻塞主线程，以执行Netty线程
            // 如果不阻塞，那么Netty会被直接shutdown
            ChannelFuture future = serverBootstrap.bind(port).sync();
            // 等到确定通道关闭了，关闭future回到主Server线程
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("启动服务器时有错误发生", e);
        } finally {
            // 优雅关闭Netty服务端并且清理掉内存
            // shutdownGracefully()执行逻辑参考：https://www.icode9.com/content-4-797057.html
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
