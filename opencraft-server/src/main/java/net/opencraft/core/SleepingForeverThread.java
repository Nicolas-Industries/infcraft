
package net.opencraft.core;

import static net.opencraft.core.OpenCraft.oc;

public class SleepingForeverThread extends Thread {

    public SleepingForeverThread(final String threadName) {
        super(threadName);
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while (oc.running) {
        	try {
                Thread.sleep(2147483647L);
            } catch (InterruptedException ex) {
            }
        }
    }
}
