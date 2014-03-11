package de.bjoernschneider.coachingmachine.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import de.bjoernschneider.coachingmachine.model.test.EegDataListener;
import javax.swing.JLabel;

public class EegDataPanel extends JPanel implements EegDataListener {

	private static final long serialVersionUID = 1L;

	boolean channel1Active=true;
	boolean channel2Active=true;
	boolean target=false;
	private JTextArea taEegCalMeas;
	
	private static final int YSCALE_TICKGAP=10;  //µV(?)
	private static final int XSCALE_TICKGAP=100; //ms
	private static final int TICK_WIDTH=3; //pixel
	
	private class EegContext {
		private BufferedImage image;
		private int drawingPos;
		private int lasty1;
		private int lasty2;
		private int maxValue=200;
		private int lastMaxValue=maxValue;
		private double scale;
		long lastMillis=0;		
	}
	EegContext ec=new EegContext();
	
	private class P300Context {
		private BufferedImage tImage, ntImage;
	}
	P300Context pc=new P300Context();

	public EegDataPanel() {
		setBorder(new TitledBorder(null, "EEG measurement", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setBounds(10, 332, 747, 418);
		setLayout(null);
		
		ec.image = new BufferedImage(583, 192, BufferedImage.TYPE_INT_ARGB);
		ec.drawingPos=5; // because of y-scale
		ec.lasty1=ec.lasty2=ec.image.getHeight()/2;
		ec.scale=(double)ec.lasty1/500;

		pc.tImage = new BufferedImage(360, 192, BufferedImage.TYPE_INT_ARGB);
		pc.ntImage = new BufferedImage(360, 192, BufferedImage.TYPE_INT_ARGB);

		taEegCalMeas = new JTextArea();
		taEegCalMeas.setFont(new Font("Monospaced", Font.PLAIN, 10));
		taEegCalMeas.setBounds(603, 15, 130, 192);
		taEegCalMeas.setEditable(false);
		taEegCalMeas.setOpaque(false);
		taEegCalMeas.setEnabled(false);
		taEegCalMeas.setDisabledTextColor(Color.black);
		add(taEegCalMeas);
		
		JLabel lblTarget = new JLabel("Target");
		lblTarget.setBounds(15, 215, 56, 16);
		add(lblTarget);
		
		JLabel lblNonTarget = new JLabel("Non Target");
		lblNonTarget.setBounds(380, 215, 78, 16);
		add(lblNonTarget);
		
		reset();
	}
	
	public void reset() {
		Graphics2D g;
		// erase eeg image area
		g = (Graphics2D) ec.image.getGraphics(); 
		g.setBackground(Color.WHITE);  
		g.clearRect(0, 0, ec.image.getWidth(), ec.image.getHeight());
		// erase p300 image areas
		g = (Graphics2D) pc.tImage.getGraphics(); 
		g.setBackground(Color.WHITE);  
		g.clearRect(0, 0, pc.tImage.getWidth(), pc.tImage.getHeight());
		g = (Graphics2D) pc.ntImage.getGraphics(); 
		g.setBackground(Color.WHITE);  
		g.clearRect(0, 0, pc.ntImage.getWidth(), pc.ntImage.getHeight());
		// set info text
		taEegCalMeas.setText("EEG not yet started...");
		// repaint all
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(ec.image, 10, 15, null);
		g.drawImage(pc.tImage, 10, 20+ec.image.getHeight(), null);
		g.drawImage(pc.ntImage, 15+pc.tImage.getWidth(), 20+ec.image.getHeight(), null);
	}

	@Override
	public void visualizeData(int eegData1, int eegData2, String addData, boolean captureOn) {
		Graphics2D g = (Graphics2D) ec.image.getGraphics(); 
		g.setBackground(Color.WHITE);
		int width=ec.image.getWidth();
		int height=ec.image.getHeight();
		int middle=height/2;
		long millis = System.currentTimeMillis();
		// draw eraser bar
		g.clearRect(ec.drawingPos, 0, 20, height);
		// mark capture on and draw x-ticks
		if (captureOn) {
			// mark capture on (different colors in regards to expected result)
			if (isTestTargetActive()) g.setColor(Color.YELLOW);
			else g.setColor(Color.LIGHT_GRAY);
			g.drawLine(ec.drawingPos,0,ec.drawingPos,height);
			// x-ticks
			if (ec.lastMillis==0 || millis-ec.lastMillis>=XSCALE_TICKGAP) {
				ec.lastMillis=millis;
				g.setColor(Color.GRAY);
				g.drawLine(ec.drawingPos, middle-TICK_WIDTH, ec.drawingPos, middle+TICK_WIDTH);
			}
		} else {
			ec.lastMillis=0;
		}
		// draw zero line
		g.setColor(Color.GRAY);
		g.drawLine(ec.drawingPos, middle, ec.drawingPos, middle);
		// draw channel1
		if (channel1Active) {
			g.setColor(Color.BLACK);
			int y1=middle-(int)(eegData1*ec.scale);
			g.drawLine(ec.drawingPos-1, ec.lasty1, ec.drawingPos, y1);
			ec.lasty1=y1;
		}
		// draw channel2
		if (channel2Active) {
			g.setColor(Color.BLUE);
			int y2=middle-(int)(eegData2*ec.scale);
			g.drawLine(ec.drawingPos-1, ec.lasty2, ec.drawingPos, y2);
			ec.lasty2=y2;
		}
		// remember highest absolute value
		int maxTmp=Math.abs(Math.max((channel1Active?eegData1:0), (channel2Active?eegData2:0)));
		if (maxTmp>ec.maxValue) ec.maxValue=maxTmp;
		// jump from right to left and adapt y-scale, if necessary
		if (ec.drawingPos++>width) {
			ec.drawingPos=TICK_WIDTH; // reset drawing position (consider width of y-scale)
			if (ec.lastMaxValue!=ec.maxValue) { // adapt y-scale
				ec.scale=(double)middle/ec.maxValue;
				g.setColor(Color.GRAY);
				g.clearRect(0, 0, TICK_WIDTH, height);
				for (int i=0; i<ec.maxValue/YSCALE_TICKGAP; i++) {
					g.drawLine(0, (int)(middle-i*YSCALE_TICKGAP*ec.scale), TICK_WIDTH, (int)(middle-i*YSCALE_TICKGAP*ec.scale));
					g.drawLine(0, (int)(middle+i*YSCALE_TICKGAP*ec.scale), TICK_WIDTH, (int)(middle+i*YSCALE_TICKGAP*ec.scale));
				}
				ec.lastMaxValue=ec.maxValue;
			}
			ec.maxValue=0; // reset maxValue for next round
		}
		// display given string
       taEegCalMeas.setText(addData);
       // repaint image
       repaint();
	}

	public void visualizeP300(boolean expectTarget, ArrayList<Integer> p300arr, int captureTime) {
		if (p300arr.size()==0) return;
		BufferedImage image = (expectTarget?pc.tImage:pc.ntImage);
		Graphics2D g = (Graphics2D) image.getGraphics(); 
		g.setBackground(Color.WHITE);
		int width=image.getWidth();
		int height=image.getHeight();
		int middle=height/2;
		// clear image
		g.clearRect(0, 0, width, height);
		// draw zero line
		g.setColor(Color.GRAY);
		g.drawLine(0, middle, width, middle);
		// draw x-ticks
		if (captureTime>=XSCALE_TICKGAP) {
			int tickgap=width*XSCALE_TICKGAP/captureTime;
			for (int dp=0; tickgap>0 && dp<=width; dp+=tickgap) {
				g.drawLine(dp, middle-TICK_WIDTH, dp, middle+TICK_WIDTH);
			}			
		}
		// calculate scales
		float xScale=(float)width/p300arr.size();
		Iterator<Integer> iter=p300arr.iterator();
		int maxValue=1;
		while(iter.hasNext()) { 
			int v=iter.next(); 
			if (Math.abs(v)>maxValue) maxValue=Math.abs(v);
		}
		float yScale=(float)middle/maxValue;
		// draw y-ticks
		g.setColor(Color.GRAY);
		int tickgap=middle*XSCALE_TICKGAP/maxValue;
		for (int dp=tickgap; tickgap>0 && dp<middle; dp+=tickgap) {
			g.drawLine(0, middle-dp, TICK_WIDTH, middle-dp);
			g.drawLine(0, middle+dp, TICK_WIDTH, middle+dp);
		}
		ec.lastMaxValue=ec.maxValue;
		// draw curve
		g.setColor(Color.BLACK);
		int x,y,lastx=0,lasty=0;
		for(int i=0; i<p300arr.size(); i++) {
			x=(int)(i*xScale);
			y=middle-(int)(p300arr.get(i)*yScale);
			if (i>0) g.drawLine(lastx, lasty, x, y);
			lastx=x; lasty=y;
		}
		// repaint image
        repaint();
	}

	
	private boolean isTestTargetActive() {
		return target;
	}
	public void setTestTargetActive(boolean isActive) {
		target=isActive;
	}
	public void setChannel1Active(boolean channel1Active) {
		this.channel1Active = channel1Active;
	}

	public void setChannel2Active(boolean channel2Active) {
		this.channel2Active = channel2Active;
	}
	@Override
	public void saveCurrentDataAsImage() {
		try {
			String ts=getTimestamp();
			ImageIO.write(ec.image, "png", new File("c:/temp/Raw EEG "+ts+".png"));	
			ImageIO.write(pc.tImage, "png", new File("c:/temp/P300 target "+ts+".png"));	
			ImageIO.write(pc.ntImage, "png", new File("c:/temp/P300 non target "+ts+".png"));	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String getTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return sdf.format(System.currentTimeMillis());
	}
}
