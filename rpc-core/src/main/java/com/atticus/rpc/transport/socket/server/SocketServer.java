package com.atticus.rpc.transport.socket.server;

import com.atticus.rpc.enumeration.RpcError;
import com.atticus.rpc.exception.RpcException;
import com.atticus.rpc.factory.ThreadPoolFactory;
import com.atticus.rpc.handler.RequestHandler;
import com.atticus.rpc.hook.ShutdownHook;
import com.atticus.rpc.provider.ServiceProvider;
import com.atticus.rpc.provider.ServiceProviderImpl;
import com.atticus.rpc.register.NacosServiceRegistry;
import com.atticus.rpc.register.ServiceRegistry;
import com.atticus.rpc.serializer.CommonSerializer;
import com.atticus.rpc.transport.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Socket方式进行远程调用连接的服务端
 */
public class SocketServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    private final ExecutorService threadPool;
    private final String host;
    private final int port;
    private CommonSerializer serializer;
    private RequestHandler requestHandler = new RequestHandler();

    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;

    public SocketServer(String host, int port) {
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        // 创建线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
    }

    /**
     * 将服务保存在本地的注册表，同时注册到Nacos注册中心
     *
     * @param service      服务实体
     * @param serviceClass 服务实体对应的类
     * @param <T>          泛型
     */
    @Override
    public <T> void publishService(T service, Class<T> serviceClass) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        serviceProvider.addServiceProvider(service, serviceClass);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }

    /**
     * 服务端启动
     */
    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("服务器启动......");
            // 添加钩子，服务端关闭时会注销服务
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            // 当未接收到连接请求时，accept()会一直阻塞
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接！{}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生：" + e);
        }
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
