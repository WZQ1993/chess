/**
 *
 */
package com.three.nsq.trendrr.frames;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;


/**
 * @author Dustin Norlander
 * @created Jan 14, 2013
 */
public class ResponseFrame extends NSQFrame {

    protected static Logger log = LoggerFactory.getLogger(ResponseFrame.class);

    public ResponseFrame() {
        this.frameId = 0;
    }

    public String getMessage() {
        try {
            return new String(this.data, "utf8");
        } catch (UnsupportedEncodingException e) {
            log.error("Caught", e);
        }
        return null;
    }

    public String toString() {
        return "RESPONSE: " + this.getMessage();
    }
}
