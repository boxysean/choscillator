package dev.boxy.choscillator;

import java.awt.Frame;

import processing.core.PApplet;
import controlP5.ControlP5;
import dev.boxy.choscillator.brain.BrainGrapher;

public class Choscillator extends PApplet {

	ControlP5 controlP5;
	int myColor = color(0,0,0);

	int colour = 100;
	
	public void setup() {
		size(1024, 768);
		background(255);

		controlP5 = new ControlP5(this);

		controlP5.addSlider("colour", 0, 255, 128, 900, 100, 100, 600);
		
		BrainGrapher brain = new BrainGrapher(1024, 768);
		PFrame pframe = new PFrame(brain);
	}

	public void draw() {
		background(colour);
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "dev.boxy.choscillator.Choscillator" });
	}

}

class PFrame extends Frame {
    public PFrame(PApplet papplet) {
        setBounds(100, 100, 1024, 768);
        add(papplet);
        papplet.init();
        show();
    }
}
