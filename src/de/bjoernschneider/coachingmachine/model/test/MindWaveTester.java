package de.bjoernschneider.coachingmachine.model.test;

import com.neurosky.thinkgear.ThinkGear;
import com.neurosky.thinkgear.ThinkGearDevice;
import com.neurosky.thinkgear.ThinkGearDevice.DataType;

import de.bjoernschneider.coachingmachine.logger.Logger;

public class MindWaveTester extends Tester {

	private ThinkGearDevice headset = null;
    private Thread dataThread = null;
    private DataCollector dataTask = null;
    private EegDataListener edl = null;
    private Thread machineTestingThread;
    private boolean captureOn=false;
    private long eegSum=0;
    
	public MindWaveTester(TestCompletionListener tl, EegDataListener listener) {
		super(tl);
		edl=listener;
	}

	@Override
	public boolean init() {
		try {
			System.loadLibrary("../../../../CM/libs/NeuroSky_MindWave/thinkgear");
			return true;
		} catch (UnsatisfiedLinkError e) {
			Logger.getInstance().logError("ThinkGear Native code library failed to load.\n" + e);
		}
		return false;
	}
	
	@Override
	public boolean start() {
		return connect();
	}

	@Override
	public void stop() {
		disconnect();
	}

	@Override
	public void test(boolean syncTest) throws InterruptedException {
		if (headset==null) return;
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
		for (int i=0; i<6; i++) {
			captureOn=true;
			Thread.sleep(95);
			captureOn=false;
			Thread.sleep(5);
		}
		//Logger.getInstance().logInfo("eegSum="+eegSum);
		testResult.setRawData(eegSum);
		edl.saveCurrentDataAsImage();	
	}
	
	// Following code was originally written by David Cheatham (dcheath@projectportfolio.info, http://dcheath.projectportfolio.info/)

	private boolean connect() {
		boolean success = false;
		captureOn = false;
		headset = new ThinkGearDevice();
    	if (dataTask == null && headset != null) {
        	success = headset.connect((byte) 5);
            if (success) {
                dataTask = new DataCollector();
                dataThread = new Thread(dataTask);
                dataThread.start();
            } else {
            	Logger.getInstance().logError("Headset could not be connected");
            }
        }
    	return success;
    }

    private void disconnect() {
		if (headset != null) {
            headset.disconnect();
			headset.dispose();
            dataTask = null;
            dataThread = null;
        }
    }

    /**
     * The data collection task waits for a packet to be read and passes all
     * data to the view. This task will end when the
     * associated {@link ThinkGearDevice#disconnect() } is called.
     */
    private class DataCollector implements Runnable {
        /**
         * The run method loops as long as
         * {@link ThinkGearDevice#waitForPacket() } returns true, pulling data
         * and passing it to the view
         */
        @Override
        public void run() {
            while (headset.waitForPacket()) {
                int[] data = new int[DataType.values().length];
                int i = 0;
                for (DataType type : DataType.values()) {
                    data[i++] = (int) headset.getValue(type);
                }
                if (captureOn) eegSum+=Math.abs(data[ThinkGear.DATA_RAW]);
            	if (edl!=null) {
             	    // assemble string output
         	        StringBuilder output = new StringBuilder("");
        	        int v=0;
        	        for (DataType type : DataType.values()) {
        	    	   if (v!=ThinkGear.DATA_RAW) {
        		           output.append(type.toString());
        		           output.append("=");
        		           output.append(data[v]);
        		           output.append("\n");
        	    	   }
        	    	   v++;
        	        }
                	edl.visualizeData(data[ThinkGear.DATA_RAW], 0, output.toString(), captureOn);
                }
            }
        }
    }

	@Override
	public Tester.Kind testerKind() {
		return Kind.MINDWAVE;
	}

	@Override
	public String testerKindString() {
		return "MINDWAVE";
	}
}
