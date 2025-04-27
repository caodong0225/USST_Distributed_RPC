package top.caodong0225.protocol;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import top.caodong0225.common.HealthChecker;
import top.caodong0225.servlet.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     服务注册中心启动
 * </p>
 * @author jyzxc
 */
public class RegistrationCenterServer {

    private HealthChecker healthChecker;
    private ScheduledExecutorService scheduler;

    public void start(String hostname, Integer port, String redisHost, Integer redisPort) {
        RedisClient.init(redisHost, redisPort);
        // 启动tomcat容器
        Tomcat tomcat = new Tomcat();
        Server server = tomcat.getServer();
        Service service = server.findService("Tomcat");
        Connector connector = new Connector();
        connector.setPort(port);
        Engine engine = new StandardEngine();
        engine.setDefaultHost(hostname);
        Host host = new StandardHost();
        host.setName(hostname);
        String contextPath = "";
        Context context = new StandardContext();
        context.setPath(contextPath);
        context.addLifecycleListener(new Tomcat.FixContextListener());
        host.addChild(context);
        engine.addChild(host);
        service.setContainer(engine);
        service.addConnector(connector);


        // 添加 CORS 过滤器
        FilterDef corsFilterDef = new FilterDef();
        corsFilterDef.setFilterName("CorsFilter");
        corsFilterDef.setFilterClass(top.caodong0225.filter.CorsFilter.class.getName());
        context.addFilterDef(corsFilterDef);

        FilterMap corsFilterMap = new FilterMap();
        corsFilterMap.setFilterName("CorsFilter");
        corsFilterMap.addURLPattern("/*"); // 拦截所有请求
        corsFilterMap.setDispatcher(DispatcherType.REQUEST.name());
        context.addFilterMap(corsFilterMap);


        tomcat.addServlet(contextPath, "unregisterServlet", new UnregisterServlet());
        context.addServletMappingDecoded("/unregister/*", "unregisterServlet");
        tomcat.addServlet(contextPath, "registerServlet", new RegisterServlet());
        context.addServletMappingDecoded("/register", "registerServlet");
        tomcat.addServlet(contextPath, "lookupServlet", new LookupServlet());
        context.addServletMappingDecoded("/lookup", "lookupServlet");
        tomcat.addServlet(contextPath, "heartbeatServlet", new HeartbeatServlet());
        context.addServletMappingDecoded("/heartbeat", "heartbeatServlet");
        tomcat.addServlet(contextPath, "proxyServlet", new ProxyServlet());
        context.addServletMappingDecoded("/call/*", "proxyServlet");
        // 路径格式: /call/{serviceName}/...
        try{
            // 启动tomcat
            tomcat.start();
            // 启动健康检查
            startHealthCheck();
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

    private void startHealthCheck() {
        healthChecker = new HealthChecker();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(
                healthChecker,
                0,    // 立即启动
                30,   // 间隔30秒
                TimeUnit.SECONDS
        );
    }

    public void stop() {
        if (healthChecker != null) {
            healthChecker.stop();
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
