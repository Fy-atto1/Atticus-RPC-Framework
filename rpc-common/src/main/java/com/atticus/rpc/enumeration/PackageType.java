package com.atticus.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 包类型（调用请求包/响应结果包）
 */
@Getter
@AllArgsConstructor
public enum PackageType {

    REQUEST_PACK(0),
    RESPONSE_PACK(1);

    private final int code;
}
