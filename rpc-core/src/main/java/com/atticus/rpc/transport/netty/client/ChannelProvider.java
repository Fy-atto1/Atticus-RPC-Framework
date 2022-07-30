package com.atticus.rpc.transport.netty.client;

import com.atticus.rpc.codec.CommonDecoder;
import com.atticus.rpc.codec.CommonEncoder;
import com.atticus.rpc.enumeration.RpcError;
import com.atticus.rpc.exception.RpcException;
import com.atticus.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 用于获取Channel对象
 */
public class ChannelProvider {

    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);

    private static EventLoopGroup eventLoopGroup;
    private static Bootstrap bootstrap = initializeBootstrap();

    private static final int MAX_RETRY_COUNT = 5;
    private static Channel channel;

    private static Bootstrap initializeBootstrap() {
        eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                // 连接的超时时间，超过这个时间连接仍然建立失败的话，则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 启用该功能时，TCP会主动探测空闲连接的有效性
                // 可以将此功能视为TCP的心跳机制，默认的心跳间隔是7200s，即2小时
                .option(ChannelOption.SO_KEEPALIVE, true)
                // 配置Channel参数，nodelay没有延迟，true代表禁用Nagle算法，减小传输延迟
                // 理解可参考：https://blog.csdn.net/lclwjl/article/details/80154565
                .option(ChannelOption.TCP_NODELAY, true);
        return bootstrap;
    }

    public static Channel get(InetSocketAddress inetSocketAddress, CommonSerializer serializer) {
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline()
                        .addLast(new CommonEncoder(serializer))
                        // 设定IdleStateHandler心跳检测每5秒进行一次写检测
                        // 如果5秒内write()方法未被调用，则触发一次userEventTrigger()方法，实现客户端每5秒向服务端发送一次消息
                        .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                        .addLast(new CommonDecoder())
                        .addLast(new NettyClientHandler());
            }
        });
        // 设置计数器值为1
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            connect(bootstrap, inetSocketAddress, countDownLatch);
            // 阻塞当前线程，直到计数器的值为0
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("获取Channel时有错误发生", e);
        }
        return channel;
    }

    public static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress,
                               CountDownLatch countDownLatch) {
        connect(bootstrap, inetSocketAddress, MAX_RETRY_COUNT, countDownLatch);
    }

    /**
     * Netty客户端创建通道连接，实现连接失败重试机制
     *
     * @param bootstrap         客户端启动器
     * @param inetSocketAddress socket地址
     * @param retry             剩余重试次数
     * @param countDownLatch    计数器
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress,
                                int retry, CountDownLatch countDownLatch) {
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("客户端连接成功！");
                channel = future.channel();
                // 计数器减1
                countDownLatch.countDown();
                return;
            }
            if (retry == 0) {
                logger.error("客户端连接失败：重试次数已用完，放弃连接！");
                throw new RpcException(RpcError.CLIENT_CONNECT_SERVER_FAILURE);
            }
            // 计算第几次重连
            int order = (MAX_RETRY_COUNT - retry) + 1;
            // 重连的时间间隔，相当于1乘以2的order次方
            int delay = 1 << order;
            logger.error("{}:连接失败，第{}次重连......", new Date(), order);
            // 利用schedule()在给定的延迟时间后执行connect()重连
            bootstrap.config().group()
                    .schedule(() -> connect(bootstrap, inetSocketAddress, retry - 1, countDownLatch),
                            delay, TimeUnit.SECONDS);
        });
    }
}
