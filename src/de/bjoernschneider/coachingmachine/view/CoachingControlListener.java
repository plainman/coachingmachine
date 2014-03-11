package de.bjoernschneider.coachingmachine.view;

public interface CoachingControlListener {
	public void startCoaching();
	public void startOddball(boolean visual) throws InterruptedException;
	public void startNames() throws InterruptedException;
	public void proceed();
	public void abort();
	public void startCalib(int testid);
}
