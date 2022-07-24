package com.atticus.rpc.socket.util;

import com.atticus.rpc.entity.RpcRequest;
import com.atticus.rpc.enumeration.PackageType;
import com.atticus.rpc.serializer.CommonSerializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Socket方式将数据序列化并写入输出流中（编码）
 */
public class ObjectWriter {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static void writeObject(OutputStream out, Object object, CommonSerializer serializer)
            throws IOException {
        out.write(intToBytes(MAGIC_NUMBER));
        if (object instanceof RpcRequest) {
            out.write(intToBytes(PackageType.REQUEST_PACK.getCode()));
        } else {
            out.write(intToBytes(PackageType.RESPONSE_PACK.getCode()));
        }
        out.write(intToBytes(serializer.getCode()));
        byte[] bytes = serializer.serialize(object);
        out.write(intToBytes(bytes.length));
        out.write(bytes);
        out.flush();
    }

    /**
     * 将int类型的整数转换为字节数组
     *
     * @param value 整数
     * @return 字节数组
     */
    private static byte[] intToBytes(int value) {
        byte[] des = new byte[4];
        des[3] = (byte) ((value >> 24) & 0xFF);
        des[2] = (byte) ((value >> 16) & 0xFF);
        des[1] = (byte) ((value >> 8) & 0xFF);
        des[0] = (byte) (value & 0xFF);
        return des;
    }
}
