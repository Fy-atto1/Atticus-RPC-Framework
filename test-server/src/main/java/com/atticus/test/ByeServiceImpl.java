package com.atticus.test;

import com.atticus.rpc.annotation.Service;
import com.atticus.rpc.api.ByeService;

/**
 * 服务实现类
 */
@Service
public class ByeServiceImpl implements ByeService {

    @Override
    public String bye(String name) {
        return "bye," + name;
    }
}
