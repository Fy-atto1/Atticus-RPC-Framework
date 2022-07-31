package com.atticus.rpc.register;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.atticus.rpc.loadbalancer.LoadBalancer;
import com.atticus.rpc.loadbalancer.RandomLoadBalancer;
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

    private final LoadBalancer loadBalancer;

    public NacosServiceDiscovery(LoadBalancer loadBalancer) {
        if (loadBalancer == null) {
            this.loadBalancer = new RandomLoadBalancer();
        } else {
            this.loadBalancer = loadBalancer;
        }
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
            List<Instance> instances = NacosUtil.getAllInstance(serviceName);
            // 负载均衡获取一个服务实体
            Instance instance = loadBalancer.select(instances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            logger.error("获取服务时有错误发生");
        }
        return null;
    }
}
