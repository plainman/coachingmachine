package de.bjoernschneider.coachingmachine.model.stimulate;

public interface StimulateControlListener {

	public void startStimulate(boolean syncCall) throws InterruptedException;

}
