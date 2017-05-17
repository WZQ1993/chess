/**
 *
 */
package com.three.nsq.trendrr;

import java.util.List;


/**
 * @author Dustin Norlander
 * @created Jan 22, 2013
 */
public interface BatchCallback {

    void batchSuccess(String topic, int num);

    void batchError(Exception ex, String topic, List<byte[]> messages);
}
