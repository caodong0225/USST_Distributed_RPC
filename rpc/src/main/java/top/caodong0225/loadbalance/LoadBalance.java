package top.caodong0225.loadbalance;

import java.util.List;
import java.util.Random;

public class LoadBalance {

    public static String random(List<String> urlList){
        Random rand = new Random();
        return urlList.get(rand.nextInt(urlList.size()));
    }
}
