package de.bjoernschneider.coachingmachine.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import de.bjoernschneider.coachingmachine.logger.Logger;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

public class TestFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private Canvas canvas;
	
	private List<String> texts;
	

	/**
	 * Create the frame.
	 */
	public TestFrame() {
		setTitle("Test Window");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		canvas = new Canvas();
		canvas.setBackground(new Color(250, 240, 230));
		contentPane.add(canvas, BorderLayout.CENTER);
		

	}
	
	public void drawTexts(int big) {
		if (texts==null) { Logger.getInstance().logError("Do not forget to set texts before using."); return; }
		Graphics g=canvas.getGraphics();
		int w=canvas.getWidth();
		int h=canvas.getHeight();
		int count=texts.size();
		int h_sec=h/count;
		int sFont_height=h_sec/3;
		int bFont_height=h_sec/2;
		Font sFont=new Font("Arial", Font.PLAIN, sFont_height);
		Font bFont=new Font("Arial", Font.BOLD, bFont_height);
		int x_shift;
		int y_shift=0;
		for (int i=0; i<count; i++) {
			if (i==big) { 
				g.setFont(bFont);
				g.setColor(Color.black);
				y_shift=bFont_height/2;
			} else {
				g.setFont(sFont);
				g.setColor(Color.lightGray);
				y_shift=sFont_height/2;
			}
			x_shift=g.getFontMetrics().stringWidth(texts.get(i))/2;
			g.drawString(texts.get(i), w/2-x_shift, h_sec*i+h_sec/2+y_shift);
		}
	}
	
	public void drawBlueCircle() {
		int dia=canvas.getHeight()/2;
		int y=canvas.getHeight()/4;
		int x=canvas.getWidth()/2-dia/2;
		Graphics g=canvas.getGraphics();
		g.setColor(Color.BLUE);
		g.fillOval(x, y, dia, dia);
	}
	
	public void drawRedTriangle() {
		Graphics g=canvas.getGraphics();
		int w=canvas.getWidth();
		int h=canvas.getHeight();
		int[] xp={w/2,w/2+h/4,w/2-h/4};
		int[] yp={h/4,h/2+h/4,h/2+h/4};
		g.setColor(Color.RED);
		g.fillPolygon(xp, yp, 3);
	}
	
	public void clearCanvas() {
		Graphics g=canvas.getGraphics();
		g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	public void setTexts(List<String> texts) {
		this.texts = texts;
	}

}
