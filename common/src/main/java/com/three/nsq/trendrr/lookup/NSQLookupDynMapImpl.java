/**
 *
 */
package com.three.nsq.trendrr.lookup;

import com.three.nsq.trendrr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Lookup implementation based on trendrr-oss DynMap
 *
 * @author Dustin Norlander
 * @created Jan 23, 2013
 */
public class NSQLookupDynMapImpl implements NSQLookup {

    protected static Logger log = LoggerFactory.getLogger(NSQLookupDynMapImpl.class);

    Set<String> addresses = new HashSet<String>();


    public void addAddr(String addr, int port) {
        if (!addr.startsWith("http")) {
            addr = "http://" + addr;
        }
        addr = addr + ":" + port;
        this.addresses.add(addr);
    }

    public List<ConnectionAddress> lookup(String topic) {
        HashMap<String, ConnectionAddress> addresses = new HashMap<String, ConnectionAddress>();

        for (String addr : this.addresses) {
            String url = null;
            try {
                url = addr + "/lookup?topic=" + URLEncoder.encode(topic, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("lookup error , topic:{}", topic, e);
                continue;
            }

            DynMap mp = DynMap.instance(this.getHTML(url), new DynMap());
            for (DynMap node : mp.getListOrEmpty(DynMap.class, "data.producers")) {
                String host = node.getString("broadcast_address", node.getString("address"));
                String key = host + ":" + node.getInteger("tcp_port");
                ConnectionAddress address = new ConnectionAddress();
                address.setHost(host);
                address.setPort(node.getInteger("tcp_port"));
                addresses.put(key, address);
            }
        }
        return new ArrayList<ConnectionAddress>(addresses.values());
    }

    public String getHTML(String url) {
        URL u;
        HttpURLConnection conn = null;
        BufferedReader rd = null;
        String line;
        String result = "";
        try {
            u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }

        } catch (Exception e) {
            log.error("Caught", e);
        } finally {
            try {
                if (rd != null) {
                    rd.close();
                }
            } catch (Exception e) {
                log.error("Caught", e);
            }

            // Release memory and underlying resources on the HttpURLConnection otherwise we may run out of file descriptors and leak memory
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }

}
