package top.caodong0225.register;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRegister {

    // 本地注册的类名和类
    private static final Map<String, Class> map = new ConcurrentHashMap<>();

    // 注册类
    public static void register(String interfaceName, Class clazz) {
        map.put(interfaceName, clazz);
    }

    // 通过名称返回类
    public static Class get(String interfaceName) {
        return map.get(interfaceName);
    }
}
