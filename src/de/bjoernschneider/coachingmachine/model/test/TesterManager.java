package de.bjoernschneider.coachingmachine.model.test;

import de.bjoernschneider.coachingmachine.logger.Logger;


public class TesterManager {
	
	private static TesterManager instance=null;
	private Tester tester=null;

	private TestCompletionListener testCompletionListener;
	private EegDataListener eegDataListener;
	private TestExecuterListener testExecuterListener;

	private TesterManager() {
	}
	
	public void setTestCompletionListener(TestCompletionListener testCompletionListener) {
		this.testCompletionListener = testCompletionListener;
	}
	public void setEegDataListener(EegDataListener eegDataListener) {
		this.eegDataListener = eegDataListener;
	}
	public void setTestExecuterListener(TestExecuterListener testExecuterListener) {
		this.testExecuterListener = testExecuterListener;
	}
	
	public void setLoggingOn(boolean on) {
		Logger.getInstance().logOn(on);
	}


	private boolean listenersSet() {
		return (testCompletionListener!=null &&
			    eegDataListener!=null &&
			    testExecuterListener!=null);
	}

	public boolean startTester(Tester.Kind kind) {
		boolean success=true;
		// listeners had to be set
		if (!listenersSet()) return false;
		// if a tester is running, stop it
		stopTester();
		// create new tester and initialize it
		switch (kind) {
		case MUSCLE:
			tester=new MuscleTester(testCompletionListener);
			break;
		case MANUAL:
			tester=new ManualTester(testCompletionListener, testExecuterListener);
			break;
		case MINDWAVE:
			tester=new MindWaveTester(testCompletionListener, eegDataListener);
			break;
		case OPENEEG:
			tester=new OpenEegTester(testCompletionListener, eegDataListener);
			break;
		default:
			success=false;
			break;
		}
		if (tester==null) success=false;
		else {
			success=tester.init();
			if (success) success=tester.start();
		}
		if (success) Logger.getInstance().logInfo("Started "+tester.testerKindString()+" Tester");
		else Logger.getInstance().logError("Tester "+tester.testerKindString()+" could not be started");
		return success;
	}
	
	public void stopTester() {
		if (tester!=null) {
			tester.stop();
			Logger.getInstance().logInfo("Stopped "+tester.testerKindString()+" Tester");
			tester=null;
		}
	}
	
	
	public Tester getCurrentTester() {
		return tester;
	}
	
    public static TesterManager getInstance() {
        if (instance == null) {
            instance = new TesterManager();
        }
        return instance;
    }

}
