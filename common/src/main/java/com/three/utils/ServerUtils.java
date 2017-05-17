package com.three.utils;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

public class ServerUtils {
    private static Logger logger = LoggerFactory.getLogger(ServerUtils.class);

    /**
     * <p>获取本机IP</p>
     * @return
     */
    public static String getHostAddress(int index) {
        if(index<0){
            return "0.0.0.0";
        }
        List<String> ips = Lists.newArrayList();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ia = addresses.nextElement();
                    if (ia.isSiteLocalAddress()
                            && !ia.isLoopbackAddress()
                            && ia.getHostAddress().indexOf(":") == -1) {
                        ips.add(ia.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        if(ips.size()>=index+1){
            return ips.get(index);
        }
        return "0.0.0.0";
    }
}
