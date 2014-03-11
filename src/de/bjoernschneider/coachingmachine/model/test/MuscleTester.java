package de.bjoernschneider.coachingmachine.model.test;
import com.labjack.labjackud.LJUD;
// importing the LJUD class


import de.bjoernschneider.coachingmachine.logger.Logger;



public class MuscleTester extends Tester {
	
		private Thread machineTestingThread; // test execution thread
		private double maxVoltage=10.0; //V
		private double holdVoltage=5.0; //V
		private long slope=100; //ms
		private int ljHandle; // labjack handle
		
		public MuscleTester(TestCompletionListener tcl) {
			super(tcl);
		}
		
		@Override
		public boolean init() {
			try {
				System.loadLibrary("LJUDJava");
				return true;
			} catch (UnsatisfiedLinkError e) {
				Logger.getInstance().logError("LabJack Native code library failed to load.\n" + e);
			}
			return false;
		}

		@Override
		public boolean start() {
			// open labjack to test if it is there
			int[] handleArr = { 0 };
			int errorcode = LJUD.OpenLabJack(LJUD.LJ_dtU3, LJUD.LJ_ctUSB, "1", 1, handleArr);
			if (errorcode != LJUD.LJE_NOERROR) return false;
			ljHandle=handleArr[0];
			return true;
		}

		@Override
		public void stop() {
			// nothing to do here	
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
		
		public static void ErrorHandler(int Errorcode, int Iteration, Exception excep) {
			byte[] err = new byte[255];

			if (Errorcode != LJUD.LJE_NOERROR) {
				LJUD.ErrorToString(Errorcode, err);
				System.err.println("Error number = " + Errorcode);
				System.err.println("Error string = " + new String(err));
				System.err.println("Iteration = " + Iteration);
				System.err.println("Stack Trace : ");
				excep.printStackTrace();
				if (Errorcode > LJUD.LJE_MIN_GROUP_ERROR) {
					// Quit if this is a group error.
					System.exit(1);
				}
			}
		}

		private void testWithMachine() throws InterruptedException {
			//long zstVorher = System.currentTimeMillis();
			// initialize hardware
			int[] handleArr = { 0 };
			int errorcode = LJUD.OpenLabJack(LJUD.LJ_dtU3, LJUD.LJ_ctUSB, "1", 1, handleArr);
			ErrorHandler(errorcode, 0, new Exception());
			ljHandle=handleArr[0];
			// sensor data
			double v_sum = 0;
			double[] value = { 0.0 };
			// actuator data
			double t_ges=250; //ms     250ms dauern auf meinem PC mit Programmlogik ~500ms
			double t_step=5; //ms
			double t_slope=(double)slope; //ms
			double t_plat=t_ges-(2*t_slope); //ms
			double v_step=holdVoltage*t_step/t_slope; //V
			double v_act=0.0; //V
			int phase=0; //0=rise,1=plat,2=fall 
			//long zst=System.currentTimeMillis()-zstVorher; Logger.getInstance().logInfo("Init fertig. "+zst+"ms vergangen.");
			for (double t=t_step; t<=t_ges; t+=t_step) {
				// actuator
				switch (phase) {
				case 0: v_act+=v_step; break;
				case 1: v_act=holdVoltage;   break;
				case 2: v_act-=v_step; break;
				}
				if (t>=(t_slope+t_plat)) phase=2;
				else if (t>=t_slope) phase=1;
				//Logger.getInstance().logInfo(t+" ms   "+v_act+" V");
				errorcode=LJUD.eDAC(ljHandle, 0, v_act, 0, 0, 0);
				ErrorHandler(errorcode, 0, new Exception());
				// sensor
				int Errorcode=LJUD.eAIN(ljHandle, 0, 31, value, 0, 0, 0, 1, 0, 0);
				ErrorHandler(Errorcode, 0, new Exception());
				v_sum+=value[0];
				// wait
				Thread.sleep((long)t_step);
			}
			//zst=System.currentTimeMillis()-zstVorher; Logger.getInstance().logInfo("Test fertig. "+zst+"ms vergangen.");
			// set actuator to 0 anyway
			errorcode=LJUD.eDAC(ljHandle, 0, 0.0, 0, 0, 0);
			ErrorHandler(errorcode, 0, new Exception());
			// calculate and evaluate test result
			long result=(long)((v_sum-1665000)/10000);
			if (result>25) { 
				Logger.getInstance().logInfo("Ergebnis: Schwach / "+result); //+"  ("+v_sum+")"); 
				testResult.setValue(Result.WEAK);
			}
			else {
				Logger.getInstance().logInfo("Ergebnis: Stark / "+result); //+"  ("+v_sum+")"); 
				testResult.setValue(Result.STRONG);
			}
			testResult.setRawData((long)v_sum);
				
		}

		public boolean calibPower() throws InterruptedException {
			Logger.getInstance().logInfo("Teste maximale haltbare Kraft... ");
			// initialize hardware
			int[] handleArr = { 0 };
			int errorcode = LJUD.OpenLabJack(LJUD.LJ_dtU3, LJUD.LJ_ctUSB, "1", 1, handleArr);
			ErrorHandler(errorcode, 0, new Exception());
			ljHandle=handleArr[0];
			// sensor data
			double[] value = { 0.0 };
			// actuator data
			long t_step=100; //ms
			double v_step=0.1; //V
			double v_act=0.0; //V
			// increase voltage until client cannot hold it
			boolean found=false;
			while ((v_act<=maxVoltage)&&(!found)) {
				// actuator
				errorcode=LJUD.eDAC(ljHandle, 0, v_act, 0, 0, 0);
				ErrorHandler(errorcode, 0, new Exception());
				// sensor
				errorcode=LJUD.eAIN(ljHandle, 0, 31, value, 0, 0, 0, 1, 0, 0);
				ErrorHandler(errorcode, 0, new Exception());
				//evaluation and next step
				Logger.getInstance().logInfo("Teste "+v_act+" V  =>  Ergebnis: "+value[0]);
				if (value[0]>45000) found=true;
				Thread.sleep(t_step);
				v_act+=v_step;
			}
			Logger.getInstance().logInfo("Test fertig.  Ergebnis: "+v_act+" V");
			// hold a bit to prove it and then set actuator to 0 again - relax :-)
			Thread.sleep(500);
			errorcode=LJUD.eDAC(ljHandle, 0, 0.0, 0, 0, 0);
			ErrorHandler(errorcode, 0, new Exception());
			// evaluate result
			if (found) holdVoltage=v_act;
			return found;
		}
		public double getHoldVoltage() {
			return holdVoltage;
		}
		public void setHoldVoltage(double voltage) {
			this.holdVoltage=voltage;
		}
		public void setSlope(long slope) {
			this.slope=slope;
		}

		@Override
		public Tester.Kind testerKind() {
			return Kind.MUSCLE;
		}

		@Override
		public String testerKindString() {
			return "MUSCLE";
		}
}
