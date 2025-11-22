
package net.infcraft.client;

import static net.infcraft.client.InfCraft.*;

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
