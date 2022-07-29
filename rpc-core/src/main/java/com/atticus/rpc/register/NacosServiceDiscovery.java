package com.atticus.rpc.register;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.atticus.rpc.util.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务发现
 */
public class NacosServiceDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceDiscovery.class);

    private final NamingService namingService;

    public NacosServiceDiscovery() {
        namingService = NacosUtil.getNacosNamingService();
    }

    /**
     * 根据服务名称从注册中心获取到一个服务提供者的地址
     *
     * @param serviceName 服务名称
     * @return 提供该服务的服务端地址
     */
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            // 利用列表获取某个服务的提供者
            List<Instance> instances = NacosUtil.getAllInstance(namingService, serviceName);
            Instance instance = instances.get(0);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            logger.error("获取服务时有错误发生");
        }
        return null;
    }
}
