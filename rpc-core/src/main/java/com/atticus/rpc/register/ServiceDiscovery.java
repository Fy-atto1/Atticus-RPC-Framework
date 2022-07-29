package com.atticus.rpc.register;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名称查找服务端地址
     *
     * @param serviceName 服务名称
     * @return 提供该服务的服务端地址
     */
    InetSocketAddress lookupService(String serviceName);
}
