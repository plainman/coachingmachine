package de.bjoernschneider.coachingmachine.controller;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JOptionPane;

import de.bjoernschneider.coachingmachine.logger.Logger;
import de.bjoernschneider.coachingmachine.model.speak.SpeakCompletionListener;
import de.bjoernschneider.coachingmachine.model.speak.SpeakControlListener;
import de.bjoernschneider.coachingmachine.model.speak.Speaker;
import de.bjoernschneider.coachingmachine.model.stimulate.StimulateCompletionListener;
import de.bjoernschneider.coachingmachine.model.stimulate.StimulateControlListener;
import de.bjoernschneider.coachingmachine.model.stimulate.Stimulator;
import de.bjoernschneider.coachingmachine.model.test.MuscleTester;
import de.bjoernschneider.coachingmachine.model.test.OpenEegTester;
import de.bjoernschneider.coachingmachine.model.test.StressDB;
import de.bjoernschneider.coachingmachine.model.test.StressDB.StressEntry;
import de.bjoernschneider.coachingmachine.model.test.TestCompletionListener;
import de.bjoernschneider.coachingmachine.model.test.TestControlListener;
import de.bjoernschneider.coachingmachine.model.test.TestExecuterListener;
import de.bjoernschneider.coachingmachine.model.test.Tester;
import de.bjoernschneider.coachingmachine.model.test.TesterManager;
import de.bjoernschneider.coachingmachine.model.test.TextDB;
import de.bjoernschneider.coachingmachine.view.CoachingControlListener;
import de.bjoernschneider.coachingmachine.view.MainFrame;


public class CoachingMachine implements CoachingControlListener, 
										SpeakControlListener, 
										SpeakCompletionListener, 
										StimulateControlListener,
										StimulateCompletionListener, 
										TestControlListener,
										TestCompletionListener, 
										TestExecuterListener 
{
	
	private Speaker sp;
	private Stimulator s;
	private MainFrame mf;
	private Thread thread;
	private StressDB sdb;
	
	private class CoachingContext {
		private int speakAndTestFlag;
		private int proceedFlag;
		private Tester.ResultValue lastTestResult;
		private ArrayList<StressEntry> stressResult;
		private boolean expectTarget;
		private ArrayList<Integer> tP300arr, ntP300arr;
		private CoachingContext() {
			stressResult=new ArrayList<StressEntry>();
			tP300arr = new ArrayList<Integer>();
			ntP300arr = new ArrayList<Integer>();
		}
	};
	private CoachingContext cc;
	
		
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CoachingMachine cm = new CoachingMachine();
					cm.init();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void init() {
		TesterManager.getInstance().setTestCompletionListener(this);
		TesterManager.getInstance().setTestExecuterListener(this);
		mf = new MainFrame(this, this, this, this);
		mf.setVisible(true);
		sp = new Speaker(this);
		s = new Stimulator(this);
		cc = new CoachingContext();
		sdb = new StressDB();
		
	}
	
	@Override
	public void startSpeak(String text, boolean syncCall) throws InterruptedException {
		mf.getCsSpeak().setBackground(Color.BLACK);	
		mf.getTfSpeakText().setText(text);
		sp.speak(text,syncCall,0);		
	}
	public void startSpeak(int textID, boolean syncCall) throws InterruptedException {
		mf.getCsSpeak().setBackground(Color.BLACK);
		String text=TextDB.getText(textID);
		mf.getTfSpeakText().setText(text);
		if (mf.getChckbxSpeakSynthetically().isSelected()) sp.speak(text,syncCall,0);
		else sp.speak(textID,syncCall,0);
	}

	@Override
	public void speakerComplete() {
		mf.getCsSpeak().setBackground(Color.WHITE);
		cc.speakAndTestFlag--;
	}

	@Override
	public void startTest(boolean syncCall) throws InterruptedException {
		// get tester
		Tester t=TesterManager.getInstance().getCurrentTester();
		if (t==null) {
			Logger.getInstance().logError("Cannot use selected Tester!");
			return;
		}
		// set tester active in View
		mf.getCsTest().setBackground(Color.BLACK);
		// special handling for muscle tester
		if (t.getClass()==MuscleTester.class) {
			MuscleTester mut=(MuscleTester)t;
			mut.setSlope(Long.parseLong(mf.getTfSlope().getText()));
			mut.setHoldVoltage(Double.parseDouble(mf.getTfMaxVoltage().getText().replace(',','.')));
		}
		// start test
		Logger.getInstance().logMarker("test start");
		t.test(syncCall);
	}

	public void startSpeakAndTest(int textID) throws InterruptedException {
		cc.speakAndTestFlag=2;
		mf.getCsSpeak().setBackground(Color.BLACK);
		String text=TextDB.getText(textID);
		mf.getTfSpeakText().setText(text);
		long millis=0;
		if (mf.getChckbxSpeakSynthetically().isSelected()) millis=sp.speak(text,false,Long.valueOf(mf.getTfTestDelay().getText()));
		else millis=sp.speak(textID,false,Long.valueOf(mf.getTfTestDelay().getText()));
		waitMillis(millis);
		startTest(false);
		while(cc.speakAndTestFlag>0) waitMillis(100);
	}

	@Override
	public void testerComplete(Tester.ResultValue result) {
		cc.lastTestResult=result;
		String res;
		if (result.isError()) res="Error";
		else if (result.isStrong()) res="Strong";
		else res="Weak";
		mf.getLblTestresult().setText(res);
		Logger.getInstance().logMarker("test result: "+res);
		mf.getCsTest().setBackground(Color.WHITE);
		cc.speakAndTestFlag--;
//todo: eliminate if statement through better design
		// if openeeg, integrate p300 results and visualize it
		if (TesterManager.getInstance().getCurrentTester().testerKind()==Tester.Kind.OPENEEG) {
			OpenEegTester tester=(OpenEegTester)TesterManager.getInstance().getCurrentTester();
			ArrayList<Integer> arr1=tester.getCapturedData(1);
			ArrayList<Integer> arr2=tester.getCapturedData(2);
			ArrayList<Integer> arr=(cc.expectTarget?cc.tP300arr:cc.ntP300arr);
			for(int i=0; i<Math.max(arr.size(), Math.max(arr1.size(),arr2.size())); i++) {
				int v=0;
				if (i<arr1.size()) v+=arr1.get(i);
				if (i<arr2.size()) v+=arr2.get(i);
				if (i<arr.size()) arr.set(i, arr.get(i)+v);
				else arr.add(v);
			}
			mf.getEegDataPanel().visualizeP300(cc.expectTarget, arr, tester.getCaptureTime());
		}
	}

	@Override
	public Tester.Result queryTestResult() {
		Object[] options = {"Weak",
                "Strong",
                "Error"};
		int r=JOptionPane.showOptionDialog(mf,
				"Choose test result",
				"Manual test simulation",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[2]);
		switch (r) {
			case 0: return Tester.Result.WEAK;
			case 1: return Tester.Result.STRONG;
		}
		return Tester.Result.ERROR;
	}

	@Override
	public void startStimulate(boolean syncCall) throws InterruptedException {
		mf.getCsStimulate().setBackground(Color.BLACK);
		String dur = mf.getTfStimulationDuration().getText();
		s.stimulate(Integer.parseInt(dur), syncCall);
	}

	@Override
	public void stimulateComplete() {
		mf.getCsStimulate().setBackground(Color.WHITE);
	}

	@Override
	public void startCoaching() {
		if (thread!=null) return;
		thread = new Thread() {
			  @Override public void run() {
				  Logger.getInstance().logInfo("Start Coaching");
				try {
					if (mf.getChckbxSpeakPrelude().isSelected()) { startSpeak(69, true); waitABit(500); }
					
					doCoaching();
				} catch (InterruptedException e) {
					interrupt();
					Logger.getInstance().logInfo("Cancelled Coaching");
					//e.printStackTrace();
				}
				Logger.getInstance().logInfo("Finished Coaching");
				thread=null;
			  }
			};
		thread.start();
	}

	public void doCoaching() throws InterruptedException {
		int l=0;
		boolean foundStress=true;
		StressEntry lastfoundStressEntry=null;
		boolean speakIntroText=false;
		boolean speakRepeatText=false;
		while (l<sdb.getLevelCount()) {
			if (foundStress) { speakIntroText=true; }
			else { speakRepeatText=true; }
			foundStress=false;
			sdb.reSortStressLevel(l);
			int prio=1;
			int highestprio=sdb.getPrioCount(l);
			StressEntry foundStressEntry=null;
			while (prio<=highestprio&&!foundStress) {
				for (int s=sdb.getStressStart(l); s<sdb.getStressEnd(l); s++) {
					StressEntry se=sdb.getStressEntry(s);
					StressEntry pse=se.getParentStressEntry();
					StressEntry vse=null;
					if (pse!=null) vse=lastfoundStressEntry;
					if (se.getPrio()==prio&&pse==vse) {
						if (speakIntroText) { waitABit(200); startSpeak(sdb.getIntroTextID(l), true); speakIntroText=false; speakRepeatText=false; waitABit(100); }
						else if (speakRepeatText) { waitABit(200); startSpeak(sdb.getRepeatTextID(l), true); speakIntroText=false; speakRepeatText=false; waitABit(100); }
						startSpeakAndTest(se.getTextID()); waitABit(200);
						if (cc.lastTestResult.isOk() && cc.lastTestResult.isWeak()) {
							foundStressEntry=se; 
							foundStress=true; 
						}
					}
				}
				prio++;
			}
			if (foundStress) {
				lastfoundStressEntry=foundStressEntry;
				cc.stressResult.add(foundStressEntry);
				Logger.getInstance().logInfo("Level "+l+": "+foundStressEntry.getText());
			}
			if (prio>highestprio||foundStress) l++;
		}
		// ...und Abschluss!
		waitABit(200);
		startSpeakAndTest(70); //"Jetzt wissen wir genug"
		if (cc.lastTestResult.isStrong()) {
			waitABit(200);
			startSpeak(71, true); //"Bitte denk weiter an deine problematische Situation und folge den Geräuschen"
			waitABit(100);
			startStimulate(true);
			waitToProceed();
			startSpeakAndTest(72); //"Die Situation ist erst mal okee so"
			if (cc.lastTestResult.isWeak()) {
				waitABit(100);
				startSpeak(73, true); //"Die Situation ist noch nicht okee, wir machen weiter."
				Logger.getInstance().logInfo("Coaching: Nochmal, weil noch nicht ok.");
				waitABit(300);
				doCoaching();
			} else {
				waitABit(100);
				startSpeakAndTest(74); //"Da ist noch etwas dahinter"
				if (cc.lastTestResult.isWeak()) {
					waitABit(100);
					startSpeak(75, true); //"Da ist noch weiterer Stress in dieser Situation, wir machen weiter."
					Logger.getInstance().logInfo("Coaching: Nochmal, weil noch was dahinter.");
					waitABit(300);
					doCoaching();
				}
			}
		} else {
			waitABit(100);
			startSpeakAndTest(76); //"Da ist noch mehr Stress."
			if (cc.lastTestResult.isWeak()) {
				Logger.getInstance().logInfo("Nochmal, weil wir noch nicht genug wissen.");
				waitABit(300);
				doCoaching();
			}
		}
		waitABit(100);
		startSpeak(77, true); //"Damit sind wir erst mal fertig. Vielen Dank!"
	}

	@Override
	public void startCalib(final int testid) {		
		if (thread!=null) return;
		thread = new Thread() {
			  @Override public void run() {
				Logger.getInstance().logInfo("Start Calib"+testid);
				try {
					switch (testid) {
					case 1:
						if (mf.getChckbxSpeakPrelude().isSelected()) { startSpeak(92, true); waitABit(200); }
						doCalib1();
						break;
					case 2:
						if (mf.getChckbxSpeakPrelude().isSelected()) { startSpeak(91, true); waitABit(200); }
						doCalib2();
						break;
					}
				} catch (InterruptedException e) {
					interrupt();
					Logger.getInstance().logInfo("Cancelled Calib"+testid);
					//e.printStackTrace();
				}
				Logger.getInstance().logInfo("End Calib"+testid);
				thread=null;
			  }
			};
		thread.start();
	}

	public void doCalib1() throws InterruptedException {
		// get tester
		Tester t=TesterManager.getInstance().getCurrentTester();
		if (t==null) {
			Logger.getInstance().logError("Cannot use Muscle Tester!");
			return;
		}
		if (t.getClass()==MuscleTester.class) {
			MuscleTester mut=(MuscleTester)t;		
			if (!mut.calibPower()) {
				JOptionPane.showMessageDialog(mf, "CoachingMachine is too weak for you!");
			} else {
				// set new hold voltage to View
				mf.getTfMaxVoltage().setText(String.format("%.1f",mut.getHoldVoltage()));
			}
		} else {
			Logger.getInstance().logError("Cannot use selected Tester for Calib.!");
			return;
		}
	}
	
	public void doCalib2() throws InterruptedException {
		for (int i=0; i<5; i++) {
			waitABit(500);
			int id = (int)(Math.random()*(91-83)+83); //Calib Texts are in range 79-90
			Logger.getInstance().logInfo("Calib2 test case: "+TextDB.getText(id));
			startSpeakAndTest(id);
		}
	}
	
	private void waitMillis(long millis) throws InterruptedException {
		Thread.sleep(millis);
	}
	private void waitABit(long millis) throws InterruptedException {
		millis=millis*(10-mf.getSlTempo().getValue());
		if (millis>0) waitMillis(millis);
	}

	private void waitToProceed() throws InterruptedException {
		int id=78;
		mf.getTfSpeakText().setText(TextDB.getText(id));
		cc.proceedFlag=1;
		mf.getBtnProceed().setEnabled(true);
		long counter=0;
		while (cc.proceedFlag>0) {
			waitMillis(100);
			counter++;
			if (counter>=100) { startSpeak(id, true); counter=0; }
		}
		mf.getBtnProceed().setEnabled(false);
	}
	@Override
	public void proceed() {
		if (thread!=null) cc.proceedFlag--;
	}	

	@Override
	public void abort() {
		if (thread!=null) thread.interrupt();
	}

	@Override
	public void startOddball(final boolean visual) throws InterruptedException {
		if (thread!=null) return;
		Tester.Kind k = TesterManager.getInstance().getCurrentTester().testerKind();
		if (!(k==Tester.Kind.MINDWAVE ||
			  k==Tester.Kind.OPENEEG)) { Logger.getInstance().logError("Start any EEG for Names experiment!"); return; }
		thread = new Thread() {
			  @Override 
			  public void run() {
				  Logger.getInstance().logInfo("Start Oddball");
				  try {
					doOddball(visual);
				} catch (InterruptedException e) {
					interrupt();
					Logger.getInstance().logInfo("Cancelled Oddball");
					//e.printStackTrace();
				}
				Logger.getInstance().logInfo("Finished Oddball");
				thread=null;
			  }
			};
		thread.start();
	}
		
	public void doOddball(boolean visual) throws InterruptedException {
		//todo: put following code in extra thread!
		waitMillis(2000);
		long ntSum=0; //nt = non target
		long ntAv=0;
		long ntCount=0;
		long tSum=0; // t = target
		long tAv=0;
		long tCount=0;
		long targetgapcount=0;
		for (int t=1; t<5; t++) { 
			for (int i=0; i<10; i++) {
				if (targetgapcount>2) {
					if (Math.random()<0.85) cc.expectTarget=false;
					else cc.expectTarget=true;
				} else {
					cc.expectTarget=false;
				}
				if (cc.expectTarget) { 
					Logger.getInstance().logInfo("   TARGET => ");
					targetgapcount=0;
					if (visual) {
						mf.getTestFrame().drawRedTriangle(); 
					} else {
						playTone(1500,1000);
					}
				} else {
					Logger.getInstance().logInfo("NONTARGET => "); 
					targetgapcount++;
					if (visual) {
						mf.getTestFrame().drawBlueCircle(); 
					} else {
						playTone(2000,1000);
					}
				}
				mf.getEegDataPanel().setTestTargetActive(cc.expectTarget);
				startTest(false);
				waitMillis(1000);
				if (visual) mf.getTestFrame().clearCanvas();
				waitMillis(2500+(long)(Math.random()*500));
				long eegData=cc.lastTestResult.getRawData();
				Logger.getInstance().logInfo("Value: "+eegData);
				if (cc.expectTarget) {
					tCount++; tSum+=eegData; tAv=tSum/tCount;
					if (ntAv>0) {
						if (eegData<ntAv) { Logger.getInstance().logInfo("=== target failed ==="); }
					}
				} else {
					ntCount++; ntSum+=eegData; ntAv=ntSum/ntCount;
					if (tAv>0) {
						if (eegData>tAv) { Logger.getInstance().logInfo(" === nontarget failed ==="); }
					}
				}
			}
			Logger.getInstance().logInfo("Averages: NONTARGET="+ntAv+" TARGET="+tAv);
		}
	}

	@Override
	public void startNames() throws InterruptedException {
		Tester.Kind k = TesterManager.getInstance().getCurrentTester().testerKind();
		if (!(k==Tester.Kind.MINDWAVE ||
			  k==Tester.Kind.OPENEEG)) { Logger.getInstance().logError("Start any EEG for Names experiment!"); return; }
//todo: put following code in extra thread!
		// sample texts
		List<String> texts = new ArrayList<String>();
		texts.add("Sabine"); texts.add("Björn"); texts.add("Fritz");
		mf.getTestFrame().setTexts(texts);
		mf.getTestFrame().drawTexts(-1);
		waitMillis(1000);

		// start test
		int count=texts.size();
		for (int i=0; i<20; i++) {
			int index=(int)(Math.random()*count);
			mf.getTestFrame().clearCanvas();
			mf.getTestFrame().drawTexts(index);
			startTest(false);
			waitMillis(1000);
			mf.getTestFrame().clearCanvas();
			mf.getTestFrame().drawTexts(-1);
			waitMillis(500);
			long eegData=cc.lastTestResult.getRawData();
			Logger.getInstance().logInfo(texts.get(index)+" = "+eegData);
		}
	}

    private byte[] getSinusTone(long frequency, AudioFormat af) {
 // todo: why fill 1s of waveform into data array, one sinus curve must be enough, or not?
        byte sample_size = (byte) (af.getSampleSizeInBits() / 8);
        byte[] data = new byte[(int) af.getSampleRate() * sample_size];
        double step_width = (2 * Math.PI) / af.getSampleRate();
        double x = 0;

        for (int i = 0; i < data.length; i += sample_size) {
            int sample_max_value = (int) Math.pow(2, af.getSampleSizeInBits()) / 2 - 1;
            int value = (int) (sample_max_value * Math.sin(frequency * x));
            for (int j = 0; j < sample_size; j++) {
                byte sample_byte = (byte) ((value >> (8 * j)) & 0xff);
                data[i + j] = sample_byte;
            }
            x += step_width;
        }
        return data;
    }
    
    private void playTone(long freq, long millis) {
        AudioFormat af = new AudioFormat(44100, 16, 1, true, false);
        byte[] data = getSinusTone(freq, af);
       
        try {
            Clip c = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));

            c.open(af, data, 0, data.length);
            int count=(int)(millis*1000/c.getMicrosecondLength())-1; if (count<0) count=0;
            c.start();
            c.loop(count);
//            while(c.isRunning()) {
//                try {
//                    Thread.sleep(50);
//                } catch (Exception ex) {}
//            }
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }
    

}
