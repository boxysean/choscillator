package dev.boxy.choscillator;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import processing.core.*;
import controlP5.*;
import dev.boxy.choscillator.brain.*;

public class Choscillator extends PApplet implements BrainGrapherListener,
		BrainGrapherCharter, TransitionListener {

	public static final String[] CHANNELS = new String[] {
			// "Attention",
			// "Meditation",
			// "Delta",
			// "Theta",
			"Low Alpha", "High Alpha", "Low Beta", "High Beta",
	// "Low Gamma",
	// "High Gamma",
	};

	static final int RESET_UPDATES = 5;
	
	static final int SCORE = 15;
	
	static final int CALLIBRATION_ROUNDS = 30;
//	static final int CALLIBRATION_ROUNDS = 4;

	ControlP5 controlP5;
	int colour = 100;

	BrainGrapher brainGrapher;
	TransitionManager trans = new TransitionManager();
	ChoscillatorGraphics graphics = new ChoscillatorGraphics(this, trans);

	PhraseManager phrases = new PhraseManager(this);

	// recording areas

	double relaxedVector[] = new double[CHANNELS.length];
	long relaxedVectorRecords = 0;

	double attentiveVector[] = new double[CHANNELS.length];
	long attentiveVectorRecords = 0;

	int attentiveCount = 0;
	int totalCount = 0;

	boolean connectionLost = false;
	boolean lastPaused = false;
	boolean paused = false;
	boolean reset = false;
	int resetFrames = 0;
	
	boolean callibration = false;
	int callibrationFrame = 0;
	
	public void setup() {
		graphics.setup();

		brainGrapher = new BrainGrapher(1024, 768);
		brainGrapher.addListener(this);
		brainGrapher.addCharter(this);

		// Pack brain grapher interface in a separate window

		PFrame pframe = new PFrame(brainGrapher);
		pframe.setVisible(true);
		
		trans.addListener(this);
		
		reset();
	}

	public void draw() {
		if (paused) {
			if (!lastPaused) {
				lastPaused = true;
				fill(0, 0, 0, 128);
				rect(0, 0, width, height);
			}
		} else {
			trans.transition();
			graphics.draw();
			phrases.draw();
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "dev.boxy.choscillator.Choscillator" });
//		PApplet.main(new String[] { "dev.boxy.choscillator.Choscillator" });
	}

//	@Override
	public void keyTyped(KeyEvent e) {

	}

//	@Override
	public void onBrainUpdate() {
		if (reset) {
			reset();
		}
		
		if (trans.inTransition()) {
			return;
		}
		
		// DEBUG

		String s = "";

		for (int i = 0; i < CHANNELS.length; i++) {
			String channel = CHANNELS[i];
			int val = Channel.getValue(channel);
			s += channel + " " + val + " ";
		}

		// System.out.println(s);

		boolean okay = true;

		for (int i = 0; i < CHANNELS.length; i++) {
			String channel = CHANNELS[i];
			int val = Channel.getValue(channel);
			if (val == 0) {
				okay = false;
				break;
			}
		}

		boolean relaxedState = trans.isRelaxedState();
		
		if (okay) {
			for (int i = 0; i < CHANNELS.length; i++) {
				String channel = CHANNELS[i];
				int val = Channel.getValue(channel);
				
				if (relaxedState) {
					relaxedVector[i] += value(val);
				} else {
					attentiveVector[i] += value(val);
				}
			}

			if (relaxedState) {
				relaxedVectorRecords++;
			} else {
				attentiveVectorRecords++;
			}
		}

		// Mock the framerate...

		if (callibration && callibrationFrame == CALLIBRATION_ROUNDS / 2) {
			trans.startTransition();
		} else if (callibration && callibrationFrame == CALLIBRATION_ROUNDS) {
			callibration = false;
			totalCount = 0;
			attentiveCount = 0;
			trans.startTransition();
		}
		
		if (callibration) {
			double frame = callibrationFrame++ % CALLIBRATION_ROUNDS;
	
			if (frame > CALLIBRATION_ROUNDS / 2.0) {
				frame = CALLIBRATION_ROUNDS - frame;
			}
	
			frame /= (CALLIBRATION_ROUNDS / 2.0);
			
			graphics.setPos(frame);
		} else {
			double percent = 0;
			
			if (totalCount > 0) {
				System.out.printf("rel %d att %d total %d score %d\n", totalCount - attentiveCount, attentiveCount, totalCount, SCORE);
				
				if (relaxedState) {
					percent = (double) (totalCount - attentiveCount) / SCORE;
					if (totalCount - attentiveCount >= SCORE) {
						trans.startTransition();
					}
				} else {
					percent = (double) attentiveCount / SCORE;
					if (attentiveCount >= SCORE) {
						trans.startTransition();
					}
					
					percent = 1.0 - percent;
				}
			} else {
				if (relaxedState) {
					percent = 1.0;
				} else {
					percent = 0;
				}
			}
			
			graphics.setPos(percent);
		}
	}

	public double value(int x) {
		return Math.log10(x) / Math.log10(2);
		// return x;
	}

	public double dist(double A, double B) {
		return l1(A, B);
	}

	public double l2(double A, double B) {
		return Math.sqrt((A - B) * (A - B));
	}

	public double l1(double A, double B) {
		return Math.abs(A - B);
	}

//	@Override
	public List<Channel> addBrainGraphChannels() {
		List<Channel> channels = new ArrayList<Channel>();
		channels.add(new Channel("Vector", color(173, 0, 173), "Vector"));
		return channels;
	}

//	@Override
	public void updateBrainGraphChannels() {
		// If we've recorded the state, then start doing distance calculations
		// So: attentive means higher score

		if (attentiveVector != null && relaxedVector != null
				&& attentiveVectorRecords > 0 && relaxedVectorRecords > 0) {
			// float vectorBias = 0.7f;

			double attentiveDist = 0;
			double relaxedDist = 0;

			for (int i = 0; i < CHANNELS.length; i++) {
				String channel = CHANNELS[i];
				int val = Channel.getValue(channel);

				if (val != 0) {
					double channelVal = value(val);

					double attentiveVal = attentiveVector[i] / attentiveVectorRecords;
					double relaxedVal = relaxedVector[i] / relaxedVectorRecords;

					attentiveDist += dist(attentiveVal, channelVal);
					relaxedDist += dist(relaxedVal, channelVal);
				}
			}

			if (attentiveDist < relaxedDist) {
				attentiveCount++;
			}

			totalCount++;

			boolean relaxedState = trans.isRelaxedState();

			System.out.printf("[state %s] attentive %3.2f - %3.2f relaxed, %10s wins! (is less) a %4d / %4d t\n",
							relaxedState ? "relaxed" : "attentive",
							attentiveDist, relaxedDist,
							attentiveDist > relaxedDist ? "relaxed"
									: "attentive", attentiveCount, totalCount);

			Channel vectorChannel = Channel.get("Vector");
			float vectorVal;

			if (relaxedState) {
				vectorVal = (1 - (float) (totalCount - attentiveCount)
						/ SCORE) * 10000;
			} else {
				vectorVal = (float) attentiveCount / SCORE * 10000;
			}

			vectorChannel.addDataPoint((int) vectorVal);
		}

		// Data to plot...

		String s = "";

		for (int i = 0; i < CHANNELS.length; i++) {
			String channel = CHANNELS[i];
			int channelVal = Channel.getValue(channel);
			s += channelVal + " ";
		}

		int sigQual = Channel.getValue("Signal Quality");
		
		if (sigQual == 200) {
			paused = true;
		} else if (paused && sigQual != 200) {
			if (++resetFrames == RESET_UPDATES) {
				paused = false;
				reset = true;
				resetFrames = 0;
			}
		}

		// System.out.println(s);
	}
	
	public void reset() {
		attentiveCount = 0;
		totalCount = 0;
		Arrays.fill(relaxedVector, 0);
		Arrays.fill(attentiveVector, 0);
		relaxedVectorRecords = 0;
		attentiveVectorRecords = 0;
		graphics.setPosHard(0);
		reset = false;
		callibration = true;
		callibrationFrame = 0;
		trans = new TransitionManager();
		trans.addListener(this);
		graphics = new ChoscillatorGraphics(this, trans);
		resetFrames = 0;
	}

//	@Override
	public void onTransition(boolean relaxedState) {
		String stateName = "";
		String otherStateName = "";

		if (relaxedState) {
			stateName = "relaxed  ";
			otherStateName = "attentive";
		} else {
			stateName = "attentive";
			otherStateName = "relaxed  ";
		}

		// System.out.printf("[state %s] %s finished, %.2f attentive\n",
		// otherStateName, otherStateName, (float) attentiveCount / totalCount);

		attentiveCount = 0;
		totalCount = 0;
	}

}

class PFrame extends Frame {
	public PFrame(PApplet papplet) {
		setBounds(100, 100, 1024, 768);
		add(papplet);
		papplet.init();
	}
}
