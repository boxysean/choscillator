package dev.boxy.choscillator.brain;

import processing.core.PApplet;
import processing.core.PGraphics;
import controlP5.ControlP5;
import controlP5.Textlabel;

class ConnectionLight {
	int x, y;
	int currentColor = 0;
	int goodColor;
	int badColor;
	int noColor;
	int diameter;
	int latestConnectionValue;
	Textlabel label;
	BrainGrapher parent;

	ConnectionLight(int _x, int _y, int _diameter, BrainGrapher _parent) {
		x = _x;
		y = _y;
		diameter = _diameter;
		parent = _parent;
		
 		label = new Textlabel(parent,"CONNECTION\nQUALITY", 32, 6);		
		label.setFont(ControlP5.standard58);
		label.setColorValue(_parent.color(0));
		
		goodColor = _parent.color(0, 255, 0);
		badColor = _parent.color(255, 255, 0);
		noColor = _parent.color(255, 0, 0);
	}
	
	void update() {
		latestConnectionValue = parent.channels[0].getLatestPoint().value;
		if(latestConnectionValue == 200) currentColor = noColor;
		if(latestConnectionValue < 200) currentColor = badColor;
		if(latestConnectionValue == 00) currentColor = goodColor;
	}
	
	void draw(PGraphics buf) {
		
		
		buf.pushMatrix();
		buf.translate(x, y);
		
		buf.noStroke();
		buf.fill(255, 150);
		buf.rect(0, 0, 88, 28);
		
		buf.noStroke();
		buf.fill(currentColor);
		buf.ellipseMode(PApplet.CORNER);
		buf.ellipse(5, 4, diameter, diameter);
		
		// TODO this is probably busted
 		label.draw(parent); 		
 		buf.popMatrix();
	}

}