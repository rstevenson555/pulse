package com.bos.art.logServer.Queues.MBeanInterface;

/**
 * Created with IntelliJ IDEA.
 * User: i0360b6
 * Date: 1/28/14
 * Time: 8:52 AM
 * To change this template use File | Settings | File Templates.
 */
public interface DisruptorMBean {

    long getBufferSize();
    long getCursor();

}
