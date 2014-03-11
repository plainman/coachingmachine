package de.bjoernschneider.coachingmachine.model.test;

public interface EegDataListener {
	public void visualizeData(int eegData1, int eegData2, String addData, boolean captureOn);
	public void saveCurrentDataAsImage();
}
