package com.atticus.rpc.transport;

import com.atticus.rpc.annotation.Service;
import com.atticus.rpc.annotation.ServiceScan;
import com.atticus.rpc.enumeration.RpcError;
import com.atticus.rpc.exception.RpcException;
import com.atticus.rpc.provider.ServiceProvider;
import com.atticus.rpc.register.ServiceRegistry;
import com.atticus.rpc.util.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * 扫描服务类并进行服务注册
 */
public abstract class AbstractRpcServer implements RpcServer {

    protected Logger logger = LoggerFactory.getLogger(AbstractRpcServer.class);

    protected String host;
    protected int port;

    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;

    public void scanServices() {
        // 获取main()入口所在类的类名，即启动类
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            // 获取启动类对应的Class对象
            startClass = Class.forName(mainClassName);
            // 判断启动类是否存在@ServiceScan注解
            if (!startClass.isAnnotationPresent(ServiceScan.class)) {
                logger.error("启动类缺少@ServiceScan注解");
                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            logger.error("出现未知错误");
            throw new RpcException(RpcError.UNKNOWN_ERROR);
        }
        // 获取ServiceScan注解接口对应的value()值，默认为""
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();
        if ("".equals(basePackage)) {
            // 获取启动类所在的包，因为启动类也放在这个包下面
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf('.'));
        }
        // 获取包下面的所有类的Class对象
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for (Class<?> clazz : classSet) {
            // 利用Service注解判断该类是否为服务类
            if (clazz.isAnnotationPresent(Service.class)) {
                // 获取Service注解接口对应的name()值，默认为""
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try {
                    // 创建服务实现类的实例
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建" + clazz + "时有错误发生");
                    continue;
                }
                if ("".equals(serviceName)) {
                    // 一个服务实现类可能实现了多个服务接口
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface : interfaces) {
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    publishService(obj, serviceName);
                }
            }
        }
    }

    /**
     * 将服务保存在本地的注册表，同时注册到Nacos
     *
     * @param service     服务实体
     * @param serviceName 服务名称
     * @param <T>         泛型
     */
    @Override
    public <T> void publishService(T service, String serviceName) {
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }
}
