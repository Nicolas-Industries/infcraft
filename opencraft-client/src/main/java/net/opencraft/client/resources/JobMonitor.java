package net.opencraft.client.resources;

public interface JobMonitor {

	boolean isFinished();
	boolean isCancelled();
	boolean endedWithErrors();
	
}
