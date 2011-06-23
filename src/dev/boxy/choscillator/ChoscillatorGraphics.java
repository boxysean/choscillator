package dev.boxy.choscillator;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class ChoscillatorGraphics {
	
	final static int MAX_SIZE = 600;
	final static int MIN_SIZE = 100;
	final static double SPEED = 0.5;
	final static int TRANSITION_FRAMES = 30;
	final static int BANDS = 50;
	final static int BANDWIDTH = 5;
	final static double BAND_SPEED = 0.1;
	final static int BAND_SPEED_DELAY = 30;
	final static int GLINT_WIDTH = 5;

	int state = 0;

	double currP = 0.0;
	double nextP = 0.0;
	int frames = 0;

	int transitionFrames = 0;
	boolean inTransition = false;
	boolean transitionUp = false;

	float bandColours[] = new float[BANDS];

	Choscillator chos;
	
	List<TransitionListener> transitionListeners = new ArrayList<TransitionListener>();
	
	double pos;
	boolean transitionNext = false;
	int transitionNextFrames = 0;
	
	public ChoscillatorGraphics(Choscillator choscillator) {
		chos = choscillator;
		transitionListeners.add(chos);
	}

	void setup() {
		chos.size(1024, 768);
		chos.noStroke();
		chos.frameRate(30);
	}

	void draw() {
//		if (transitionNext && transitionNextFrames == 0) {
//			transitionNextFrames = 30;
//		}
		
		if (transitionNext /*&& --transitionNextFrames == 0*/) {
			transitionUp = !transitionUp;
			inTransition = true;
			transitionNext = false;
		}
		
		if (inTransition) {
			if (transitionFrames == TRANSITION_FRAMES / 2) {
				state = 1 - state;
				for (TransitionListener listener : transitionListeners) {
					listener.onTransition(state == 0);
				}
			}

			if (transitionUp) {
				if (++transitionFrames == TRANSITION_FRAMES) {
					inTransition = false;
				}
			} else {
				if (--transitionFrames == 0) {
					inTransition = false;
				}
			}
		}

		if (chos.frameCount % 30 == 0 && !inTransition) {
			nextP = pos;
			frames = 0;
		}

		if (++frames >= 30) {
			frames = 30;
		}

		// higher = more smooth and less responsive
		int smoothing = 12;

		double l = Math.sin((frames / 30.0) * PApplet.PI / smoothing);

		currP = nextP * l + currP * (1 - l);

		float transition = (float) transitionFrames / TRANSITION_FRAMES;
		float transitionColour = transition * 255;

		if (transitionFrames < TRANSITION_FRAMES / 2) {
			chos.background(transitionColour);
			drawSquare(currP, transitionColour, false);

//			if (currP < 0.05 && !inTransition) {
//				transitionUp = true;
//				inTransition = true;
//			}
		} else if (transitionFrames >= TRANSITION_FRAMES / 2) {
			chos.background(transitionColour);
			drawSquare(currP, transitionColour, true);

//			if (currP >= 0.95 && !inTransition) {
//				transitionUp = false;
//				inTransition = true;
//			}
		}
	}
	
	void drawSquare(double p, double colour, boolean forward) {
		double l = 0;

		// if (forward) {
		l = p * MAX_SIZE + (1 - p) * MIN_SIZE;
		// } else {
		// l = p * MAX_SIZE + (1 - p) * MIN_SIZE;
		// }

		int b = BANDS;

		double x = (chos.width - l) / 2.0;
		double y = (chos.height - l) / 2.0;

		double mpColour = (colour + 128) / 2.0;
		mpColour = 255 - colour;

		// float bandWidth = BANDWIDTH * p;
		float bandWidth = BANDWIDTH;

		int fc = chos.frameCount % ((int) (b * BAND_SPEED) + BAND_SPEED_DELAY);

		if (!forward) {
			fc = ((int) (b * BAND_SPEED) + BAND_SPEED_DELAY) - fc;
		}

		for (int i = b - 1; i >= 0; i--) {
			double q = (double) i / (b - 1);

			double bandColour = q * colour + (1 - q) * mpColour;

			if (i != 0 && i != b - 1) {
				double glintX = (double) fc / BAND_SPEED - i;
				glintX = (glintX + GLINT_WIDTH) / (2 * GLINT_WIDTH);
				glintX = Math.min(1.0, Math.max(glintX, 0.0));
				glintX = Math.sin(glintX * PApplet.PI) * ((float) (b - i) / b);

				bandColour = (1.0 - glintX) * bandColour + glintX * (255 - colour);
			}

			chos.fill((int) bandColour);

			double xx = x - bandWidth * i;
			double yy = y - bandWidth * i;
			double ll = l + 2 * bandWidth * i;

			chos.rect((float) xx, (float) yy, (float) ll, (float) ll);
		}
	}
	
	public void transition() {
		if (!inTransition) {
			System.out.println("TRANSITION");
			transitionNext = true;
		}
	}
	
	public void setPos(double x) {
		pos = x;
	}
	
	public void setPosHard(double x) {
		nextP = x;
		currP = x;
	}
}
