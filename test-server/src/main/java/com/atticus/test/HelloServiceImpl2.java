package com.atticus.test;

import com.atticus.rpc.api.HelloObject;
import com.atticus.rpc.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端api接口实现
 */
public class HelloServiceImpl2 implements HelloService {

    /**
     * 使用HelloServiceImpl初始化日志对象，方便在日志输出的时候，可以打印出日志信息所属的类。
     */
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl2.class);

    @Override
    public String hello(HelloObject object) {
        // 使用{}可以直接将getMessage()内容输出
        logger.info("接收到消息：{}", object.getMessage());
        return "本次处理来自Socket服务";
    }

}
