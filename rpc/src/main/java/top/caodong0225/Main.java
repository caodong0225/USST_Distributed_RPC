package top.caodong0225;

import top.caodong0225.protocol.RegistrationCenterServer;


public class Main {
    public static void main(String[] args) {
        RegistrationCenterServer server = new RegistrationCenterServer();
        server.start("localhost",8080, "localhost",6379);
    }
}