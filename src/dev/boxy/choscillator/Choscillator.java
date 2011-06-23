package dev.boxy.choscillator;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.core.PApplet;
import controlP5.ControlP5;
import dev.boxy.choscillator.brain.BrainGrapher;
import dev.boxy.choscillator.brain.BrainGrapherCharter;
import dev.boxy.choscillator.brain.BrainGrapherListener;
import dev.boxy.choscillator.brain.Channel;

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

	// static final float BIAS = 0.95f;
	static final int SCORE = 10;

	ControlP5 controlP5;
	int colour = 100;

	BrainGrapher brainGrapher;
	ChoscillatorGraphics graphics = new ChoscillatorGraphics(this);

	// Memory lowAlphaMemExp = new Memory(BIAS);
	// Memory highAlphaMemExp = new Memory(BIAS);
	// Memory lowBetaMemExp = new Memory(BIAS);
	// Memory highBetaMemExp = new Memory(BIAS);

	PhraseManager phrases = new PhraseManager(this);

	// recording areas

	double relaxedVector[] = new double[CHANNELS.length];
	long relaxedVectorRecords = 0;

	double attentiveVector[] = new double[CHANNELS.length];
	long attentiveVectorRecords = 0;

	int attentiveCount = 0;
	int totalCount = 0;

	boolean relaxedState = false;
	boolean knownState = false;

	boolean connectionLost = false;
	boolean lastPaused = false;
	boolean paused = false;
	boolean reset = false;
	boolean callibration = false;
	int callibrationFrame = 0;
	
	static final int CALLIBRATION_ROUNDS = 20;

	public void setup() {
		graphics.setup();

		brainGrapher = new BrainGrapher(1024, 768);
		brainGrapher.addListener(this);
		brainGrapher.addCharter(this);

		// Pack brain grapher interface in a separate window

		PFrame pframe = new PFrame(brainGrapher);
		pframe.setVisible(true);
		
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
			graphics.draw();
			phrases.draw();
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present",
				"dev.boxy.choscillator.Choscillator" });
		// PApplet.main(new String[] { "dev.boxy.choscillator.Choscillator" });
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// switch (e.getKeyChar()) {
		// case 'a':
		// if (!recordRelaxed) {
		// if (recordAttentive) {
		// recordAttentive = false;
		// // Average out the values
		// for (int i = 0; i < CHANNELS.length; i++) {
		// attentiveVector[i] /= recordSteps;
		// }
		//
		// System.out.println("done recording attentive");
		// } else {
		// recordSteps = 0;
		// attentiveVector = new double[CHANNELS.length];
		// recordAttentive = true;
		//
		// System.out.println("start recording attentive");
		// }
		// }
		//
		// break;
		//
		// case 'r':
		// if (!recordAttentive) {
		// if (recordRelaxed) {
		// recordRelaxed = false;
		// // Average out the values
		// for (int i = 0; i < CHANNELS.length; i++) {
		// relaxedVector[i] /= recordSteps;
		// }
		//
		// System.out.println("done recording relaxed");
		// } else {
		// recordSteps = 0;
		// relaxedVector = new double[CHANNELS.length];
		// recordRelaxed = true;
		//
		// System.out.println("start recording relaxed");
		// }
		// }
		// }
	}

	@Override
	public void onBrainUpdate() {

		if (reset) {
			reset();
		}

		// DEBUG

		String s = "";

		for (int i = 0; i < CHANNELS.length; i++) {
			String channel = CHANNELS[i];
			int val = Channel.getValue(channel);
			s += channel + " " + val + " ";
		}

		// System.out.println(s);

		if (knownState) {
			boolean okay = true;

			for (int i = 0; i < CHANNELS.length; i++) {
				String channel = CHANNELS[i];
				int val = Channel.getValue(channel);
				if (val == 0) {
					okay = false;
					break;
				}
			}

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
		}

		// Mock the framerate...

		if (callibration && callibrationFrame == CALLIBRATION_ROUNDS / 2) {
			graphics.transition();
		} else if (callibration && callibrationFrame == CALLIBRATION_ROUNDS) {
			callibration = false;
			graphics.transition();
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
				System.out.printf("rel %d att %d total %d score %d", totalCount - attentiveCount, attentiveCount, totalCount, SCORE);
				
				if (relaxedState) {
					percent = (double) (totalCount - attentiveCount) / SCORE;
					if (totalCount - attentiveCount >= SCORE) {
						graphics.transition();
					}
				} else {
					percent = (double) attentiveCount / SCORE;
					if (attentiveCount >= SCORE) {
						graphics.transition();
					}
					
					percent = 1.0 - percent;
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

	@Override
	public List<Channel> addBrainGraphChannels() {
		List<Channel> channels = new ArrayList<Channel>();

		// channels.add(new Channel("Low Alpha Smooth", color(33, 36, 163),
		// "Relaxed, smooth"));
		// channels.add(new Channel("Alpha vs Beta", color(43, 46, 173),
		// "Alpha vs Beta"));
		channels.add(new Channel("Vector", color(173, 0, 173), "Vector"));

		return channels;
	}

	@Override
	public void updateBrainGraphChannels() {
		// float lowAlphaSmoothBias = 0.7f;
		// float lowAlphaSmooth = Channel.getValue("Low Alpha Smooth");
		// float lowAlpha = Channel.getValue("Low Alpha");
		// float newLowAlphaSmooth = lowAlphaSmooth * lowAlphaSmoothBias +
		// lowAlpha * (1.0f - lowAlphaSmoothBias);
		// Channel.get("Low Alpha Smooth").addDataPoint((int)
		// newLowAlphaSmooth);
		//
		// float alphaVsBetaBias = 0.7f;
		// float alphaVsBeta = Channel.getValue("Alpha vs Beta");
		// float alphaSum = Channel.getValue("Low Alpha") +
		// Channel.getValue("High Alpha");
		// float betaSum = Channel.getValue("Low Beta") +
		// Channel.getValue("High Beta");
		// float newAlphaVsBeta = alphaVsBeta * alphaVsBetaBias + (alphaSum >
		// betaSum ? 10000 : 0) * (1 - alphaVsBetaBias);
		// Channel.get("Alpha vs Beta").addDataPoint((int) newAlphaVsBeta);

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

					double attentiveVal = attentiveVector[i]
							/ attentiveVectorRecords;
					double relaxedVal = relaxedVector[i] / relaxedVectorRecords;

					attentiveDist += dist(attentiveVal, channelVal);
					relaxedDist += dist(relaxedVal, channelVal);
				}
			}

			if (attentiveDist < relaxedDist) {
				attentiveCount++;
			}

			totalCount++;

			System.out.printf("[state %s] attentive %3.2f - %3.2f relaxed, %10s wins! (is less) a %4d / %4d t\n",
							relaxedState ? "relaxed" : "attentive",
							attentiveDist, relaxedDist,
							attentiveDist > relaxedDist ? "relaxed"
									: "attentive", attentiveCount, totalCount);

			if (knownState) {
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
			paused = false;
			reset = true;
		}

		// System.out.println(s);
	}
	
	public void reset() {
		attentiveCount = 0;
		totalCount = 0;
		Arrays.fill(relaxedVector, 0);
		Arrays.fill(attentiveVector, 0);
		graphics.setPosHard(0);
		reset = false;
		callibration = true;
		callibrationFrame = 0;
		graphics = new ChoscillatorGraphics(this);
	}

	@Override
	public void onTransition(boolean relaxedState) {
		String stateName = "";
		String otherStateName = "";

		this.relaxedState = relaxedState;
		this.knownState = true;

		if (relaxedState) {
			stateName = "relaxed  ";
			otherStateName = "attentive";

//			phrases.add("blue");
		} else {
			stateName = "attentive";
			otherStateName = "relaxed  ";

//			phrases.add("green");
		}

		// System.out.printf("[state %s] %s finished, %.2f attentive\n",
		// otherStateName, otherStateName, (float) attentiveCount / totalCount);

		attentiveCount = 0;
		totalCount = 0;
	}

}

// class Memory {
// float bias;
// float value;
//
// public Memory(float bias) {
// this.bias = bias;
// }
//
// public void add(float x) {
// value = ((1.0f - bias) * value) + (bias * x);
// }
//
// public float get() {
// return value;
// }
// }

class PFrame extends Frame {
	public PFrame(PApplet papplet) {
		setBounds(100, 100, 1024, 768);
		add(papplet);
		papplet.init();
	}
}
