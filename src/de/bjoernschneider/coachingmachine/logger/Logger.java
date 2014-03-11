package de.bjoernschneider.coachingmachine.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Logger {

	private static Logger instance=null;
	private final static String FILENAME="C:/Temp/CMLOG";
	private final static String ENDING=".txt";
	private File file;
	private FileWriter writer;
	private final static String SEPARATOR=" "; 
	boolean isOn = false;
		
	private Logger() {} // singleton

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
    
	public void logData(int[] values, int count) {
		StringBuilder str = new StringBuilder("D");
		for (int i=0; i<count; i++) {
			str.append(SEPARATOR);
			str.append(values[i]);
		}
		log(str.toString(), false);
	}

	public void logMarker(String text) {
		log("M"+SEPARATOR+text, true);
	}

	public void logInfo(String text) {
		log("I"+SEPARATOR+text, true);
	
	}
	
	public void logError(String text) {
		log("E"+SEPARATOR+text, true);
	
	}
	private void log(String text, boolean stdout) {
		if (isOn) {
			text = getTimestamp() + SEPARATOR + text;
			if (stdout) System.out.println("LOG "+text);
			if (writer!=null) {
				try {
					writer.write(text+"\r\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return sdf.format(System.currentTimeMillis());
	}

	public void logOn(boolean on) {
		if (on) {
			if (isOn) logOn(false);
			isOn=on;
			file = new File(FILENAME+getTimestamp()+ENDING);
			try {
				writer = new FileWriter(file ,true);
			} catch (IOException e) {
				writer=null;
				logInfo("Logging could not be started!");
				e.printStackTrace();
			}
			logInfo("Logging started");
		}
		else if (isOn) {
			logInfo("Logging stopped");
			if (writer!=null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					logInfo("Logging could not be flushed and closed!");
					e.printStackTrace();
				}
				writer=null;
			}
			isOn = on;
		}
	
	}

}