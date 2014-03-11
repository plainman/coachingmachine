package de.bjoernschneider.coachingmachine.model.test;

import java.io.IOException;
import java.util.ArrayList;

import com.openeeg.acquisition.EEGAcquisitionController;
import com.openeeg.acquisition.IRawSampleGenerator;
import com.openeeg.acquisition.IRawSampleListener;
import com.openeeg.acquisition.RawSample;

import de.bjoernschneider.coachingmachine.logger.Logger;

public class OpenEegTester extends Tester {

    private EegDataListener edl = null;
    private Process p1, p2;
    private EEGAcquisitionController eegAcquisitionController;
    private Thread machineTestingThread;
    private boolean captureOn=false;
    private long eegSum=0;
    private ArrayList<Integer> p300arr1, p300arr2;
    private static final int CAPTURE_TIME = 600; //ms
    
	public OpenEegTester(TestCompletionListener tl, EegDataListener edl) {
		super(tl);
		this.edl=edl;
		p300arr1=new ArrayList<Integer>();
		p300arr2=new ArrayList<Integer>();
	}

	@Override
	public boolean init() {
		try {
			// start NeuroServer
//		    Runtime rt = Runtime.getRuntime();
//		    String path="X:\\CM\\libs\\OpenEEG_NeuroServer\\";
//		    String[] cmd1 = {"cmd.exe","/c",path+"nsd.exe"};
//		    p1 = rt.exec(cmd1);
//		    String[] cmd2 = {"cmd.exe","/c",path+"modeegdriver.exe","-d COM4"};
//		    p2 = rt.exec(cmd2);
//		    if (p1==null||p2==null) { 
//		    	Logger.getInstance().logError("NeuroServer/OpenEEG-Driver could not be started");
//		    	return false;
//		    }
		    //InputStream in = p.getInputStream();
		    //OutputStream out = p.getOutputStream();
		    //InputStream err = p.getErrorStream();
		} catch(Exception e) {
			e.printStackTrace();
			stop();
			return false;
		}
		return true;
	}
	
	@Override
	public boolean start() {
        // setup acquisition
		eegAcquisitionController = EEGAcquisitionController.getInstance();
        IRawSampleGenerator sampleGenerator = eegAcquisitionController.getChannelSampleGenerator();
        // Listens to both channels 1 and 2
        SampleListener twoChannelListener = new SampleListener("Channel One and Two");
        sampleGenerator.addSampleListener(twoChannelListener, new int[]{1, 2});
        // and start
        try {
			eegAcquisitionController.startReading(false);
		} catch (IOException e) {
			e.printStackTrace();
			stop();
			return false;
		}
		return true;
	}

	@Override
	public void stop() {
		// stop reading data
		if (eegAcquisitionController!=null) eegAcquisitionController.stopReading();
	    // stop neuroserver
		if (p2!=null) p2.destroy() ;
	    if (p1!=null) p1.destroy() ;
	}

	@Override
	public void test(boolean syncTest) throws InterruptedException {
		testResult.setValue(Result.ERROR);
		machineTestingThread = new Thread() {
			  @Override public void run() {
				try {  
				  testWithMachine();
				} catch (InterruptedException e) {
					interrupt();
					Logger.getInstance().logInfo("Test cancelled");
					//e.printStackTrace();
				}
				testCompletionListener.testerComplete(testResult);
			  }
			};
		machineTestingThread.start();
		if (syncTest) machineTestingThread.join();
		
	}

	private void testWithMachine() throws InterruptedException {
		eegSum=0;
		captureOn=true;
		Thread.sleep(CAPTURE_TIME);
		captureOn=false;
		testResult.setRawData(eegSum);
		edl.saveCurrentDataAsImage();
	}

	
	class SampleListener implements IRawSampleListener
    {
        String name;

        SampleListener(String name)
        {
            this.name = name;
        }

        public void receiveSample(RawSample rawSample)
        {
//            Logger.getInstance().logInfo(name);
//            int packetNumber = rawSample.getPacketNumber();
//            Logger.getInstance().logInfo(" Packet number:" + packetNumber);
//            for (int i = 0, length = rawSample.getChannelNumbers().length; i < length; i++)
//            {
//                int channelNumber = rawSample.getChannelNumbers()[i];
//                int sample = rawSample.getSamples()[i];
//                Logger.getInstance().logInfo("  Channel #" + channelNumber + " = " + sample);
//            }
            
            // get data from sampler
        	int eeg1Data=rawSample.getSamples()[0];
            int eeg2Data=rawSample.getSamples()[1];
            // store data, if capturing is active
            if (captureOn) {
            	if (eegSum==0) { // called the first time during captureOn -> clear arrays
	            	p300arr1.clear();
	            	p300arr2.clear();
            	}
            	p300arr1.add(eeg1Data);
            	p300arr2.add(eeg2Data);
            }
            // calculate sum
        	if (captureOn) eegSum+=Math.abs(eeg1Data)+Math.abs(eeg2Data);
        	// draw and log data
        	edl.visualizeData(eeg1Data, eeg2Data, "OpenEEG active", captureOn);
            Logger.getInstance().logData(new int[]{eeg1Data, eeg2Data}, 2);
        }
    }
	
	public ArrayList<Integer> getCapturedData(int channel) {
		switch(channel) {
			case 1: return p300arr1;
			case 2: return p300arr2;
			default: return null;
		}
	}
	
	public int getCaptureTime() {
		return CAPTURE_TIME;
	}
	
	@Override
	public Tester.Kind testerKind() {
		return Kind.OPENEEG;
	}

	@Override
	public String testerKindString() {
		return "OPENEEG";
	}

}
