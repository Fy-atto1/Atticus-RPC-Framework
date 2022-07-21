package com.atticus.rpc.entity;

import com.atticus.rpc.enumeration.ResponseCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 提供者（服务端）执行完成后或出错后向消费者（客户端）返回的结果对象
 */
@Data
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {

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
    public static <T> RpcResponse<T> success(T data) {
        RpcResponse<T> response = new RpcResponse<>();
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
    public static <T> RpcResponse<T> fail(ResponseCode responseCode) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(responseCode.getCode());
        response.setMessage(responseCode.getMessage());
        return response;
    }

}
