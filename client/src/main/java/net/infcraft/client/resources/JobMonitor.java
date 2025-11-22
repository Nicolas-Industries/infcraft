package net.infcraft.client.resources;

public interface JobMonitor {

	boolean isFinished();
	boolean isCancelled();
	boolean endedWithErrors();
	
}
