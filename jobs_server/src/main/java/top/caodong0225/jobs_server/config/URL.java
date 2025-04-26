package top.caodong0225.jobs_server.config;

/**
 * <p>
 *     服务自动注册
 * </p>
 * @author jyzxc
 */
public class URL {
    String serviceName;      // 接口类名
    private String hostname;
    private Integer port;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public URL(String serviceName, String hostname, Integer port) {
        this.serviceName = serviceName;
        this.hostname = hostname;
        this.port = port;
    }
}
