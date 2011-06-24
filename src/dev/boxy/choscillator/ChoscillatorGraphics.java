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

	float bandColours[] = new float[BANDS];

	Choscillator chos;
	TransitionManager trans;
	
	double pos;
	boolean transitionNext = false;
	int transitionNextFrames = 0;
	
	public ChoscillatorGraphics(Choscillator choscillator, TransitionManager transitionManager) {
		chos = choscillator;
		trans = transitionManager;
	}

	void setup() {
		chos.size(1024, 768);
		chos.noStroke();
		chos.frameRate(30);
		chos.noCursor();
	}

	void draw() {
		if (pos != nextP) {
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
		
		int transitionFrame = trans.getTransitionFrame();
		float transition = (float) transitionFrame / TRANSITION_FRAMES;
		float transitionColour = transition * 255;

		chos.background(transitionColour);
		drawSquare(currP, transitionColour, !trans.isRelaxedState());
	}
	
	void drawSquare(double p, double colour, boolean forward) {
		double l = 0;

		l = p * MAX_SIZE + (1 - p) * MIN_SIZE;
		int b = BANDS;

		double x = (chos.width - l) / 2.0;
		double y = (chos.height - l) / 2.0;

		double mpColour = (colour + 128) / 2.0;
		mpColour = 255 - colour;

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
	
	public void setPos(double x) {
		pos = x;
	}
	
	public void setPosHard(double x) {
		nextP = x;
		currP = x;
	}
}
