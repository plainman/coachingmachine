package de.bjoernschneider.coachingmachine.model.speak;
import java.io.File;
import java.io.FileNotFoundException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import de.bjoernschneider.coachingmachine.logger.Logger;
import de.bjoernschneider.coachingmachine.model.test.TextDB;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;


public class Speaker {
	
	private SpeakCompletionListener speakCompletionListener;
	
	private MaryInterface marytts = null;
	private AudioInputStream audio = null;
	private boolean clipReady=false;

		public Speaker(SpeakCompletionListener sl) {
			speakCompletionListener=sl;
			try {
				marytts = new LocalMaryInterface();
			} catch (MaryConfigurationException e) {
				e.printStackTrace();
			}
			marytts.setVoice("bits1-hsmm");
		}

		public long speak(String text, boolean syncCall, long percentage) throws InterruptedException {
			StringBuffer buf = new StringBuffer(text);
			if (buf.length()==0) buf.insert(0,TextDB.getText(0));
			try {
				audio = marytts.generateAudio(buf.toString());
			} catch (SynthesisException e) {
				e.printStackTrace();
			}
			AudioPlayer player = new AudioPlayer(audio, new LineListener() {
				@Override
				public void update(LineEvent event) {
					if (event.getType() == LineEvent.Type.STOP) speakCompletionListener.speakerComplete();					
				}
			});
			player.start();
			if (syncCall) player.join();
			
	        // calculate delay before potential tests starts:
			// found no f... way to get length of audio and calculate proper millis, because audio.getFrameLength(); always returns 0
			// that's way I count letters and estimate proper delay of test start, calculated with 50ms per letter
			long delay=(long)(buf.length()*50*percentage/100); //ms
			return delay; 
		}
		public long speak(int textID, boolean syncCall, long percentage) throws InterruptedException {
			long duration=0;
			if (!TextDB.isValidTextID(textID)) { return speak("", syncCall, percentage); }
			try {
	            AudioInputStream ais = AudioSystem.getAudioInputStream(new File("X:\\CM\\Soundfiles\\"+String.format("%03d", textID)+".wav"));
	            duration=(long)(ais.getFrameLength()/ais.getFormat().getFrameRate()*1000); //ms
	            final Clip clip = AudioSystem.getClip();
	            clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent le) {
						if (le.getType() == LineEvent.Type.STOP) { clipReady=true; speakCompletionListener.speakerComplete(); clip.close(); }
					}
				});
	            clip.open(ais);
	            clipReady=false;
	       		clip.loop(0);
	        } catch (FileNotFoundException e) {
	        	Logger.getInstance().logError("Text "+textID+" noch nicht eingesprochen. Verwende synthetische Stimme.");
	        	return speak(TextDB.getText(textID), syncCall, percentage);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        if (syncCall) {
	          	while (!clipReady){
	       			Thread.sleep(10);
	          	}
	        }   
	        // calculate delay before potential tests starts:
	        // delay of hotspot + delay of user desired percentage
	        long delay=TextDB.getDelay(textID); 
	        if (delay>duration) delay=duration;
	        else delay=(long)(delay+(duration-delay)*percentage/100); //ms
			return delay; 
		}

}
