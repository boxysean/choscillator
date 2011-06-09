package dev.boxy.choscillator;

import processing.core.PApplet;
import controlP5.ControlP5;

public class Choscillator extends PApplet {

	ControlP5 controlP5;
	int myColor = color(0,0,0);

	int colour = 100;
	
	public void setup() {
		size(1024, 768);
		background(255);

		controlP5 = new ControlP5(this);

		controlP5.addSlider("colour", 0, 255, 128, 900, 100, 100, 600);
	}

	public void draw() {
		background(colour);
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "dev.boxy.choscillator.Choscillator" });
	}

}