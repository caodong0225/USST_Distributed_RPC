package top.caodong0225.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class URL implements Serializable {
    private static final long serialVersionUID = 1L;
    private String serviceName;      // 接口类名
    private String hostname;
    private Integer port;
    private long lastHeartbeatTime;  // 用于存储最后的心跳时间
    private boolean isAlive;  // 服务是否存活

    public URL(String serviceName, String hostname, Integer port) {
        this.serviceName = serviceName;
        this.hostname = hostname;
        this.port = port;
    }
    public URL(){

    }
}
