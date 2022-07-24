package com.atticus.rpc.socket.server;

import com.atticus.rpc.RequestHandler;
import com.atticus.rpc.entity.RpcRequest;
import com.atticus.rpc.registry.ServiceRegistry;
import com.atticus.rpc.serializer.CommonSerializer;
import com.atticus.rpc.socket.util.ObjectReader;
import com.atticus.rpc.socket.util.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * IO传输模式|处理客户端RpcRequest的工作线程
 */
public class RequestHandlerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

    private Socket socket;
    private RequestHandler requestHandler;
    private ServiceRegistry serviceRegistry;
    private CommonSerializer serializer;

    public RequestHandlerThread(Socket socket, RequestHandler requestHandler,
                                ServiceRegistry serviceRegistry, CommonSerializer serializer) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceRegistry = serviceRegistry;
        this.serializer = serializer;
    }

    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(inputStream);
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object response = requestHandler.handle(rpcRequest, service);
            ObjectWriter.writeObject(outputStream, response, serializer);
        } catch (IOException e) {
            logger.info("调用或发送时发生错误" + e);
        }
    }

}
