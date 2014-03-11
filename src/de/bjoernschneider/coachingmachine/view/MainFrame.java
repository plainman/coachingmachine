package de.bjoernschneider.coachingmachine.view;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import de.bjoernschneider.coachingmachine.logger.Logger;
import de.bjoernschneider.coachingmachine.model.speak.SpeakControlListener;
import de.bjoernschneider.coachingmachine.model.stimulate.StimulateControlListener;
import de.bjoernschneider.coachingmachine.model.test.TestControlListener;
import de.bjoernschneider.coachingmachine.model.test.Tester.Kind;
import de.bjoernschneider.coachingmachine.model.test.TesterManager;


public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField tfStimulationDuration;
	private JTextField tfSpeakText;
	private JLabel lblTestresult;
	private JCheckBox chckbxSpeakPrelude;
	private Canvas csStimulate;
	private Canvas csTest;
	private Canvas csSpeak;
	private CoachingControlListener coachingControlListener;
	private SpeakControlListener speakControlListener; 
	private StimulateControlListener stimulateControlListener; 
	private TestControlListener testControlListener;
	private JButton btnCancel;
	private JButton btnProceed;
	private JButton btnStart;
	private JTextField tfTestDelay;
	private JSlider slTempo;
	private JTextField tfSlope;
	private JCheckBox chckbxSpeakSynthetically;
	private JButton btnCalib1;
	private JButton btnCalib2;
	private JTextField tfMaxVoltage;
	private JRadioButton rdbtnManual;
	private JRadioButton rdbtnMuscle;
	private JRadioButton rdbtnMindWave;
	private JRadioButton rdbtnOpenEeg;
	private TestFrame tf;
	private JPanel panelOpenEegTest;
	private EegDataPanel edp; 
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JCheckBox chckbxChannel1;
	private JCheckBox chckbxChannel2;

	private class MyDispatcher implements KeyEventDispatcher {
		boolean pressed=false;
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
        	boolean processed=false;
        	switch(e.getKeyCode()) {
        	case KeyEvent.VK_SPACE:
        		if (e.getID() == KeyEvent.KEY_PRESSED && !pressed) {
        			pressed=true;
        			Logger.getInstance().logMarker("Personal test START");        			
        		}
        		if (e.getID() == KeyEvent.KEY_RELEASED) {
        			pressed=false;
        			Logger.getInstance().logMarker("Personal test END");
        		}
        		processed=true;
        		break;
        	case KeyEvent.VK_S:
        		if (e.getID() == KeyEvent.KEY_RELEASED) {
        			Logger.getInstance().logMarker("Stimulate Marker");
        			processed=true;
        		}
        		break;
        	}
            return processed;
        }
    }	
	
	
	
	public MainFrame(CoachingControlListener ccl, TestControlListener tcl, SpeakControlListener spcl, StimulateControlListener stcl) {
		coachingControlListener = ccl;
		testControlListener = tcl;
		speakControlListener = spcl;
		stimulateControlListener = stcl;

		//
		// General windows setup
		//
		setTitle("Coaching Machine");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(200, 100, 776, 808);
		
		contentPane = new JPanel();
		contentPane.setFocusable(false);
		contentPane.setFocusTraversalKeysEnabled(false);
		contentPane.setEnabled(false);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				TesterManager.getInstance().stopTester();
				TesterManager.getInstance().setLoggingOn(false);
			}
		});

		tf=new TestFrame();
		tf.setVisible(true);
		tf.setLocation(getX()+getWidth(), getY());
		
		//
		// EEG Panel
		//
		edp = new EegDataPanel();		
		contentPane.add(edp);
		TesterManager.getInstance().setEegDataListener(edp);
		
		//
		// Auto Coaching
		//
		JPanel panelAutoCoaching = new JPanel();
		panelAutoCoaching.setBorder(new TitledBorder(null, "AutoCoaching", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelAutoCoaching.setBounds(10, 11, 344, 147);
		contentPane.add(panelAutoCoaching);
		panelAutoCoaching.setLayout(null);
		
		btnStart = new JButton("START");
		btnStart.setBounds(10, 27, 100, 40);
		panelAutoCoaching.add(btnStart);
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				coachingControlListener.startCoaching();
			}
		});
		
		btnProceed = new JButton("Proceed");
		btnProceed.setBounds(120, 27, 100, 40);
		panelAutoCoaching.add(btnProceed);
		btnProceed.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			coachingControlListener.proceed();
			}
		});
		btnProceed.setEnabled(false);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBounds(230, 27, 100, 40);
		panelAutoCoaching.add(btnCancel);
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				coachingControlListener.abort();
			}
		});
		btnCancel.setEnabled(false);

		slTempo = new JSlider(0, 10, 5);
		slTempo.setBounds(10, 100, 187, 31);
		panelAutoCoaching.add(slTempo);
		slTempo.setMajorTickSpacing(10);
		slTempo.setMinorTickSpacing(1);
		slTempo.setPaintTicks(true);
		
		JLabel lblTempo = new JLabel("Tempo:");
		lblTempo.setBounds(10, 78, 46, 14);
		panelAutoCoaching.add(lblTempo);
		
		JLabel lblDelay = new JLabel("Delay S>T [%]:");
		lblDelay.setBounds(211, 115, 96, 14);
		panelAutoCoaching.add(lblDelay);
		
		tfTestDelay = new JTextField();
		tfTestDelay.setBounds(296, 112, 34, 20);
		panelAutoCoaching.add(tfTestDelay);
		tfTestDelay.setColumns(10);
		tfTestDelay.setText("50");

		//
		// Speaking
		//
		chckbxSpeakPrelude = new JCheckBox("Speak prelude");
		chckbxSpeakPrelude.setBounds(207, 85, 131, 23);
		panelAutoCoaching.add(chckbxSpeakPrelude);
		
		JPanel panelSpeak = new JPanel();
		panelSpeak.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Speak", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelSpeak.setBounds(364, 11, 314, 83);
		contentPane.add(panelSpeak);
		panelSpeak.setLayout(null);
		
		JLabel lblText = new JLabel("Text:");
		lblText.setBounds(11, 18, 42, 23);
		panelSpeak.add(lblText);
		
		tfSpeakText = new JTextField();
		tfSpeakText.setBounds(48, 19, 257, 20);
		panelSpeak.add(tfSpeakText);
		tfSpeakText.setColumns(10);
		
		JButton btnTestspeak = new JButton("Speak");
		btnTestspeak.setBounds(11, 48, 100, 23);
		panelSpeak.add(btnTestspeak);
		btnTestspeak.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					speakControlListener.startSpeak(tfSpeakText.getText(), false);
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			}
		});
		
		csSpeak = new Canvas();
		csSpeak.setBounds(117, 48, 23, 23);
		panelSpeak.add(csSpeak);
		csSpeak.setBackground(Color.WHITE);
		
		chckbxSpeakSynthetically = new JCheckBox("Speak synthetically");
		chckbxSpeakSynthetically.setBounds(166, 48, 143, 23);
		panelSpeak.add(chckbxSpeakSynthetically);

		//
		// Stimulate
		//
		JPanel panelStimulate = new JPanel();
		panelStimulate.setBorder(new TitledBorder(null, "Stimulate", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelStimulate.setBounds(364, 101, 314, 57);
		contentPane.add(panelStimulate);
		panelStimulate.setLayout(null);
		
		JButton btnTeststimulation = new JButton("Stimulate");
		btnTeststimulation.setBounds(10, 23, 100, 23);
		panelStimulate.add(btnTeststimulation);
		
		csStimulate = new Canvas();
		csStimulate.setBounds(116, 23, 23, 23);
		panelStimulate.add(csStimulate);
		csStimulate.setBackground(Color.WHITE);
		
		JLabel lblDuration = new JLabel("Stimulate [s]:");
		lblDuration.setBounds(170, 27, 78, 14);
		panelStimulate.add(lblDuration);
		
		tfStimulationDuration = new JTextField();
		tfStimulationDuration.setBounds(258, 24, 33, 20);
		panelStimulate.add(tfStimulationDuration);
		tfStimulationDuration.setColumns(10);
		tfStimulationDuration.setText("10");
		
		btnTeststimulation.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					stimulateControlListener.startStimulate(false);
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			}
		});

		//
		// Test
		//
		JPanel panelTest = new JPanel();
		panelTest.setBorder(new TitledBorder(null, "Test", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelTest.setBounds(10, 169, 391, 151);
		contentPane.add(panelTest);
		panelTest.setLayout(null);
		
		JLabel lblResult = new JLabel("Last Test:");
		lblResult.setBounds(109, 59, 65, 14);
		panelTest.add(lblResult);
		
		lblTestresult = new JLabel("---");
		lblTestresult.setBounds(184, 59, 46, 14);
		panelTest.add(lblTestresult);
		
		JButton btnTest = new JButton("Test");
		btnTest.setBounds(109, 29, 104, 23);
		btnTest.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					testControlListener.startTest(false);
				} catch (InterruptedException e1) {
					// e1.printStackTrace();
				}
			}
		});
		panelTest.add(btnTest);
		
		csTest = new Canvas();
		csTest.setBounds(219, 29, 23, 23);
		panelTest.add(csTest);
		csTest.setBackground(Color.WHITE);
		
		btnCalib2 = new JButton("Cablibration");
		btnCalib2.setBounds(109, 110, 104, 23);
		panelTest.add(btnCalib2);
		btnCalib2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				coachingControlListener.startCalib(2);
			}
		});
		
		rdbtnManual = new JRadioButton("Manual");
		rdbtnManual.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				switch (e.getStateChange()) {
				case ItemEvent.SELECTED:
					TesterManager.getInstance().startTester(Kind.MANUAL);
					break;
				case ItemEvent.DESELECTED:
					TesterManager.getInstance().stopTester();
					break;
				}
				
			}
		});
		buttonGroup.add(rdbtnManual);
		rdbtnManual.setSelected(true);
		rdbtnManual.setBounds(6, 29, 97, 23);
		panelTest.add(rdbtnManual);
		
		rdbtnMuscle = new JRadioButton("Muscle");
		rdbtnMuscle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				switch (e.getStateChange()) {
				case ItemEvent.SELECTED:
					if (!TesterManager.getInstance().startTester(Kind.MUSCLE)) {
						rdbtnManual.setSelected(true);
					}
					break;
				case ItemEvent.DESELECTED:
					TesterManager.getInstance().stopTester();
					break;
				}

			}
		});
		buttonGroup.add(rdbtnMuscle);
		rdbtnMuscle.setBounds(6, 55, 97, 23);
		panelTest.add(rdbtnMuscle);
		
		rdbtnMindWave = new JRadioButton("MindWave");
		rdbtnMindWave.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				switch (e.getStateChange()) {
				case ItemEvent.SELECTED:
					if (!TesterManager.getInstance().startTester(Kind.MINDWAVE)) {
						rdbtnManual.setSelected(true);
					}
					break;
				case ItemEvent.DESELECTED:
					TesterManager.getInstance().stopTester();
					break;
				}
				edp.reset();
			}
		});
		buttonGroup.add(rdbtnMindWave);
		rdbtnMindWave.setBounds(6, 81, 104, 23);
		panelTest.add(rdbtnMindWave);
		
		rdbtnOpenEeg = new JRadioButton("OpenEEG");
		rdbtnOpenEeg.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				switch (e.getStateChange()) {
				case ItemEvent.SELECTED:
					if (!TesterManager.getInstance().startTester(Kind.OPENEEG)) {
						rdbtnManual.setSelected(true);
					}
					break;
				case ItemEvent.DESELECTED:
					TesterManager.getInstance().stopTester();
					break;
				}
				edp.reset();
			}
		});
		buttonGroup.add(rdbtnOpenEeg);
		rdbtnOpenEeg.setBounds(6, 110, 97, 23);
		panelTest.add(rdbtnOpenEeg);
		
		JCheckBox chckbxLoggingOn = new JCheckBox("Logging on");
		chckbxLoggingOn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				switch (e.getStateChange()) {
				case ItemEvent.SELECTED:
					TesterManager.getInstance().setLoggingOn(true);
					break;
				case ItemEvent.DESELECTED:
					TesterManager.getInstance().setLoggingOn(false);
					break;
				}
			}
		});
		chckbxLoggingOn.setSelected(true);
		chckbxLoggingOn.setBounds(109, 80, 97, 23);
		panelTest.add(chckbxLoggingOn);
		
		JButton btnOddballVis = new JButton("Oddball Visual");
		btnOddballVis.setBounds(256, 29, 125, 23);
		panelTest.add(btnOddballVis);
		btnOddballVis.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					coachingControlListener.startOddball(true);
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}				
			}
		});
		
		JButton btnOddballAcu = new JButton("Oddball Tones");
		btnOddballAcu.setBounds(256, 65, 125, 23);
		panelTest.add(btnOddballAcu);
		btnOddballAcu.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					coachingControlListener.startOddball(false);
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}	
			}
		});
		
		JButton btnNames = new JButton("Names");
		btnNames.setBounds(256, 99, 125, 23);
		panelTest.add(btnNames);
		btnNames.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					coachingControlListener.startNames();
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			}
		});
		
		//
		// OpenEEG test settings
		//
		panelOpenEegTest = new JPanel();
		panelOpenEegTest.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "OpenEEG Test", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelOpenEegTest.setBounds(403, 169, 181, 152);
		contentPane.add(panelOpenEegTest);
		panelOpenEegTest.setLayout(null);
		
		chckbxChannel1 = new JCheckBox("Channel 1");
		chckbxChannel1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				edp.setChannel1Active(e.getStateChange()==ItemEvent.SELECTED);
			}
		});
		chckbxChannel1.setSelected(true);
		chckbxChannel1.setBounds(18, 26, 97, 23);
		panelOpenEegTest.add(chckbxChannel1);
		
		chckbxChannel2 = new JCheckBox("Channel 2");
		chckbxChannel2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				edp.setChannel2Active(e.getStateChange()==ItemEvent.SELECTED);
			}
		});
		chckbxChannel2.setSelected(true);
		chckbxChannel2.setBounds(18, 54, 97, 23);
		panelOpenEegTest.add(chckbxChannel2);
		
		// 
		// Muscle test settings
		//
		JPanel panelMuscleTest = new JPanel();
		panelMuscleTest.setBounds(587, 169, 163, 152);
		contentPane.add(panelMuscleTest);
		panelMuscleTest.setBorder(new TitledBorder(null, "Muscle Test", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelMuscleTest.setLayout(null);
		
		JLabel lblSlope = new JLabel("Test slope:");
		lblSlope.setBounds(10, 28, 78, 14);
		panelMuscleTest.add(lblSlope);
		
		JLabel lblMaxVoltage = new JLabel("Max. Voltage:");
		lblMaxVoltage.setBounds(10, 70, 78, 14);
		panelMuscleTest.add(lblMaxVoltage);
		
		tfSlope = new JTextField();
		tfSlope.setBounds(110, 25, 33, 20);
		panelMuscleTest.add(tfSlope);
		tfSlope.setText("100");
		tfSlope.setColumns(10);
		
		tfMaxVoltage = new JTextField();
		tfMaxVoltage.setBounds(110, 67, 33, 20);
		panelMuscleTest.add(tfMaxVoltage);
		tfMaxVoltage.setText("5.0");
		tfMaxVoltage.setColumns(10);
		
		btnCalib1 = new JButton("Voltage Calib.");
		btnCalib1.setBounds(10, 102, 133, 23);
		panelMuscleTest.add(btnCalib1);
		btnCalib1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
					coachingControlListener.startCalib(1);
			}
		});
		
	}
	
	public EegDataPanel getEegDataPanel() {
		return edp;
	}
	public JSlider getSlTempo() {
		return slTempo;
	}
	public JTextField getTfStimulationDuration() {
		return tfStimulationDuration;
	}
	public JTextField getTfSpeakText() {
		return tfSpeakText;
	}
	public JTextField getTfTestDelay() {
		return tfTestDelay;
	}
	public JLabel getLblTestresult() {
		return lblTestresult;
	}

	public Canvas getCsStimulate() {
		return csStimulate;
	}

	public Canvas getCsTest() {
		return csTest;
	}

	public Canvas getCsSpeak() {
		return csSpeak;
	}

	public void disableMuscleTester() {
		rdbtnMuscle.setSelected(false);
		rdbtnMuscle.setEnabled(false);
	}
	public void disableManualTester() {
		rdbtnManual.setSelected(false);
		rdbtnManual.setEnabled(false);
	}
	public void disableMindWaveTester() {
		rdbtnMindWave.setSelected(false);
		rdbtnMindWave.setEnabled(false);
	}
	public void disableOpenEegTester() {
		rdbtnOpenEeg.setSelected(false);
		rdbtnOpenEeg.setEnabled(false);
	}
	
	
	public JCheckBox getChckbxSpeakPrelude() {
		return chckbxSpeakPrelude;
	}

	public JButton getBtnCancel() {
		return btnCancel;
	}

	public JButton getBtnProceed() {
		return btnProceed;
	}

	public JButton getBtnStart() {
		return btnStart;
	}
	
	public JTextField getTfSlope() {
		return tfSlope;
	}
	public JCheckBox getChckbxSpeakSynthetically() {
		return chckbxSpeakSynthetically;
	}
	public JButton getBtnCalib1() {
		return btnCalib1;
	}
	public JButton getBtnCalib2() {
		return btnCalib2;
	}
	public JTextField getTfMaxVoltage() {
		return tfMaxVoltage;
	}
	public TestFrame getTestFrame() {
		return tf;
	}
}
