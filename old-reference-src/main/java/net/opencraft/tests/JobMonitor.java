package net.opencraft.client.tests;

public interface JobMonitor {

	boolean isFinished();
	boolean isCancelled();
	boolean endedWithErrors();
	
}
