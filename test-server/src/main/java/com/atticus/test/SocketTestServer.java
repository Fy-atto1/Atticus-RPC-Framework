package com.atticus.test;

import com.atticus.rpc.annotation.ServiceScan;
import com.atticus.rpc.serializer.CommonSerializer;
import com.atticus.rpc.transport.RpcServer;
import com.atticus.rpc.transport.socket.server.SocketServer;

/**
 * 测试用Socket服务端
 */
@ServiceScan
public class SocketTestServer {

    public static void main(String[] args) {
        RpcServer server = new SocketServer("127.0.0.1", 9998, CommonSerializer.HESSIAN_SERIALIZER);
        // 启动服务端
        server.start();
    }
}
