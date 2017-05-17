package com.three.nsq.trendrr;
/**
 *
 */


import java.util.List;


/**
 * An interface to the nsq lookup.  We keep this as an interface because it depends on
 * some json parsing library and we dont want to force a dependancy on a specific lib.
 *
 * @author Dustin Norlander
 * @created Jan 14, 2013
 */
public interface NSQLookup {
    /**
     * add an address to a nsq lookup server.
     *
     * @param addr
     * @param port
     */
    void addAddr(String addr, int port);

    /**
     * Lookup topic addresses
     *
     * @param topic
     * @return
     */
    List<ConnectionAddress> lookup(String topic);
}
