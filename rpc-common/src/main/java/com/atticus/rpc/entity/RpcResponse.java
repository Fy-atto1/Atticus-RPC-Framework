package com.atticus.rpc.entity;

import com.atticus.rpc.enumeration.ResponseCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 提供者（服务端）处理完成后，向消费者（客户端）返回的结果对象
 */
@Data
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {

    /**
     * 响应对应的请求号
     */
    private String requestId;

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 响应状态补充信息
     */
    private String message;

    /**
     * 成功时的响应数据
     */
    private T data;

    /**
     * 成功时服务端返回的结果对象
     *
     * @param data 响应数据
     * @param <T>  泛型
     * @return 结果对象
     */
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    /**
     * 失败时服务端返回的结果对象
     *
     * @param responseCode 响应状态码
     * @param <T>          泛型
     * @return 结果对象
     */
    public static <T> RpcResponse<T> fail(ResponseCode responseCode, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(responseCode.getCode());
        response.setMessage(responseCode.getMessage());
        return response;
    }

}
