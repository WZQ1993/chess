/**
 *
 */
package com.three.nsq.trendrr;


/**
 * @author Dustin Norlander
 * @created Jan 15, 2013
 */
public interface NSQMessageCallback {
    void message(NSQMessage message);
}
