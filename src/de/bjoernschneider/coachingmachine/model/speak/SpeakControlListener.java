package de.bjoernschneider.coachingmachine.model.speak;

public interface SpeakControlListener {

	public void startSpeak(String text, boolean syncCall) throws InterruptedException;

}
