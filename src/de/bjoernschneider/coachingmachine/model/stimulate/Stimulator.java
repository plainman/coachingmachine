package de.bjoernschneider.coachingmachine.model.stimulate;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import de.bjoernschneider.coachingmachine.logger.Logger;


public class Stimulator {
	
	private StimulateCompletionListener stimulateListener;

	private static final long WavDuration = 167; //ms
	private static final long SilenceDuration = 10; //ms
	
	private boolean clipReady;
	
	public Stimulator(StimulateCompletionListener sl) {
		stimulateListener=sl;
	}
	
	public void stimulate(long duration, boolean syncCall) throws InterruptedException {
		Logger.getInstance().logInfo("Start Stimulate");
		try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File("X:\\CM\\Soundfiles\\links rechts rauschen -10dB 2x167+2x10ms.wav"));
            final Clip clip = AudioSystem.getClip();
            clip.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent le) {
					if (le.getType() == LineEvent.Type.STOP) { 
						clipReady=true; 
						stimulateListener.stimulateComplete(); 
						clip.close();
						Logger.getInstance().logInfo("Finished Stimulate");
					}
				}
			});
            clip.open(ais);
            int count = (int)(duration * 1000 / ( (WavDuration+SilenceDuration) * 2));
            clipReady=false;
       		clip.loop(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (syncCall) {
          	while (!clipReady){
       			Thread.sleep(100);
          	}
        }   
	}
}
