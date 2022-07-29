package com.atticus.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.atticus.rpc.enumeration.RpcError;
import com.atticus.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 管理Nacos连接等工具类
 */
public class NacosUtil {

    private static final Logger logger = LoggerFactory.getLogger(NacosUtil.class);

    private static final String SERVER_ADDR = "127.0.0.1:8848";

    /**
     * 连接到Nacos创建命名空间
     *
     * @return 命名空间
     */
    public static NamingService getNacosNamingService() {
        try {
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            logger.error("连接到Nacos时有错误发生：" + e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    /**
     * 注册服务到Nacos
     *
     * @param namingService     命名空间
     * @param serviceName       服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    public static void registerService(NamingService namingService,
                                       String serviceName, InetSocketAddress inetSocketAddress) throws NacosException {
        namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
    }

    /**
     * 获取所有提供该服务的服务端地址
     *
     * @param namingService 命名空间
     * @param serviceName   服务名称
     * @return 所有提供该服务的服务端地址
     */
    public static List<Instance> getAllInstance(NamingService namingService, String serviceName) throws NacosException {
        return namingService.getAllInstances(serviceName);
    }
}
