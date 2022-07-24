package com.atticus.rpc.socket.client;

import com.atticus.rpc.RpcClient;
import com.atticus.rpc.entity.RpcRequest;
import com.atticus.rpc.entity.RpcResponse;
import com.atticus.rpc.enumeration.RpcError;
import com.atticus.rpc.exception.RpcException;
import com.atticus.rpc.serializer.CommonSerializer;
import com.atticus.rpc.socket.util.ObjectReader;
import com.atticus.rpc.socket.util.ObjectWriter;
import com.atticus.rpc.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Socket方式进行远程调用的客户端
 */
public class SocketClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private CommonSerializer serializer;

    private final String host;
    private final int port;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        // 使用socket套接字实现TCP网络传输
        // 在try()中一般进行对资源的申请，若{}出现异常，()资源会自动关闭
        try (Socket socket = new Socket(host, port)) {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            ObjectWriter.writeObject(outputStream, rpcRequest, serializer);
            Object obj = ObjectReader.readObject(inputStream);
            RpcResponse rpcResponse = (RpcResponse) obj;
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            return rpcResponse.getData();
        } catch (IOException e) {
            logger.error("调用时有错误发生：" + e);
            throw new RpcException("服务调用失败：", e);
        }
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
