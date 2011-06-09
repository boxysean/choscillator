package dev.boxy.choscillator.brain;

import processing.core.PApplet;
import processing.core.PGraphics;
import controlP5.CheckBox;
import controlP5.ControlP5;
import controlP5.ControlWindow;
import controlP5.Textlabel;

class Monitor {
	int x, y, w, h, currentValue, targetValue, backgroundColor;
	Channel sourceChannel;
	CheckBox showGraph;
	Textlabel label;
	BrainGrapher parent;

	Monitor(Channel _sourceChannel, int _x, int _y, int _w, int _h,
			BrainGrapher _parent, ControlP5 controlP5) {
		sourceChannel = _sourceChannel;
		x = _x;
		y = _y;
		w = _w;
		h = _h;
		parent = _parent;
		currentValue = 0;
		backgroundColor = parent.color(255);
		showGraph = controlP5.addCheckBox("showGraph", x + 16, y + 34);
		showGraph.addItem("GRAPH", 0);
		showGraph.activate(0);
		showGraph.setColorForeground(sourceChannel.drawColor);
		showGraph.setColorActive(parent.color(0));

		label = new Textlabel(parent, sourceChannel.name, x + 16, y + 16);
		label.setFont(controlP5.grixel);
		label.setColorValue(0);

	}

	void update() {

	}

	void draw(PGraphics buf) {
		// this technically only neds to happen on the packet, not every frame
		if (showGraph.getItem(0).value() == 0) {
			sourceChannel.graphMe = false;
		} else {
			sourceChannel.graphMe = true;
		}

		buf.pushMatrix();
		buf.translate(x, y);
		// Background
		buf.noStroke();
		buf.fill(backgroundColor);
		buf.rect(0, 0, w, h);

		// border line
		buf.strokeWeight(1);
		buf.stroke(220);
		buf.line(w - 1, 0, w - 1, h);

		if (sourceChannel.points.size() > 0) {

			Point targetPoint = (Point) sourceChannel.points
					.get(sourceChannel.points.size() - 1);
			targetValue = PApplet.round(PApplet.map(targetPoint.value, sourceChannel.minValue,
					sourceChannel.maxValue, 0, h));

			if ((parent.scaleMode.equals("Global")) && sourceChannel.allowGlobal) {
				targetValue = (int) PApplet.map(targetPoint.value, 0, parent.globalMax, 0, h);
			}

			// Calculate the new position on the way to the target with easing
			currentValue = currentValue
					+ PApplet.round((float) (targetValue - currentValue) * .08f);

			// Bar
			buf.noStroke();
			buf.fill(sourceChannel.drawColor);
			buf.rect(0, h - currentValue, w, h);
		}

		// Draw the checkbox matte

		buf.noStroke();
		buf.fill(255, 150);
		buf.rect(10, 10, w - 20, 40);

		buf.popMatrix();

		label.draw(parent);
	}

}
