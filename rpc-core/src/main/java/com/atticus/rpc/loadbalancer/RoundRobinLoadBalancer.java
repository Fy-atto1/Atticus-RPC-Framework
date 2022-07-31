package com.atticus.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 轮转算法，依次按顺序选择
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    /**
     * index表示当前选到了第几个服务器，每次选择后都会自增加一
     */
    private int index = 0;

    @Override
    public Instance select(List<Instance> instances) {
        if (index >= instances.size()) {
            index %= instances.size();
        }
        return instances.get(index++);
    }
}
