package dev.boxy.choscillator;

import java.awt.Frame;

import processing.core.PApplet;
import controlP5.ControlP5;
import dev.boxy.choscillator.brain.BrainGrapher;
import dev.boxy.choscillator.brain.BrainGrapherListener;
import dev.boxy.choscillator.brain.Channel;

public class Choscillator extends PApplet implements BrainGrapherListener {

	ControlP5 controlP5;
	int colour = 100;
	
	BrainGrapher brainGrapher;
	
	public void setup() {
		size(1024, 768);
		background(255);

		controlP5 = new ControlP5(this);
		controlP5.addSlider("colour", 0, 255, 128, 900, 100, 100, 600);
		
		brainGrapher = new BrainGrapher(1024, 768);
		
		// Pack brain grapher interface in a separate window
		
		PFrame pframe = new PFrame(brainGrapher);
		pframe.setVisible(true);
	}

	public void draw() {
		background(colour);
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "dev.boxy.choscillator.Choscillator" });
	}

	@Override
	public void onBrainUpdate() {
		// relaxed values
		int lowAlpha = Channel.get("Low Alpha");
		int highAlpha = Channel.get("High Alpha");
		
		// alert values
		int lowBeta = Channel.get("Low Beta");
		int highBeta = Channel.get("High Beta");
		
		System.out.printf("alpha %d beta %d\n", lowAlpha + highAlpha, lowBeta + highBeta);
	}

}

class PFrame extends Frame {
    public PFrame(PApplet papplet) {
        setBounds(100, 100, 1024, 768);
        add(papplet);
        papplet.init();
    }
}
