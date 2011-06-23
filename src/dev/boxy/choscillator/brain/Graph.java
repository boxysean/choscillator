package dev.boxy.choscillator.brain;

import processing.core.PApplet;
import processing.core.PGraphics;
import controlP5.ControlP5;
import controlP5.RadioButton;
import controlP5.Slider;

class Graph {
	int x, y, w, h, pixelsPerSecond, gridColor, gridX, originalW, originalX;
	long leftTime, rightTime, gridTime;
	boolean scrollGrid;
	String renderMode;
	float gridSeconds;
	Slider pixelSecondsSlider;
	RadioButton renderModeRadio;
	RadioButton scaleRadio;
	BrainGrapher parent;

	Graph(int _x, int _y, int _w, int _h, BrainGrapher parent, ControlP5 controlP5) {
		x = _x;
		y = _y;
		w = _w;
		h = _h;
		this.parent = parent;
		pixelsPerSecond = 50;
		gridColor = parent.color(0);
		gridSeconds = 1; // seconds per grid line
		scrollGrid = false;

		// temporary overdraw kludge to keep graph smooth
		originalW = w;
		originalX = x;

		w += (pixelsPerSecond * 2);
		x -= pixelsPerSecond;

		pixelSecondsSlider = controlP5.addSlider("PIXELS PER SECOND", 10,
				parent.width, 50, 16, 16, 100, 10);
		pixelSecondsSlider.setColorForeground(parent.color(180));
		pixelSecondsSlider.setColorActive(parent.color(180));

		renderModeRadio = controlP5.addRadioButton("RENDER MODE", 16, 36);
		// renderModeRadio.setSpacingColumn(40);

		renderModeRadio.setColorForeground(parent.color(255));
		renderModeRadio.setColorActive(parent.color(0));
		renderModeRadio.addItem("Lines", 1);
		renderModeRadio.addItem("Curves", 2);
		renderModeRadio.addItem("Shaded", 3);
		renderModeRadio.addItem("Triangles", 4);
		renderModeRadio.activate(0);
		// triangles, too?

		scaleRadio = controlP5.addRadioButton("SCALE MODE", 104, 36);
		scaleRadio.setColorForeground(parent.color(255));
		scaleRadio.setColorActive(parent.color(0));
		scaleRadio.addItem("Local Maximum", 1);
		scaleRadio.addItem("Global Maximum", 2);
		scaleRadio.activate(0);
	}

	void update() {
	}

	void draw(PGraphics buf) {

		pixelsPerSecond = PApplet.round(pixelSecondsSlider.value());

		switch (PApplet.round(renderModeRadio.value())) {
		case 1:
			renderMode = "Lines";
			break;
		case 2:
			renderMode = "Curves";
			break;
		case 3:
			renderMode = "Shaded";
			break;
		case 4:
			renderMode = "Triangles";
			break;
		}

		switch (PApplet.round(scaleRadio.value())) {
		case 1:
			parent.scaleMode = "Local";
			break;
		case 2:
			parent.scaleMode = "Global";
			break;
		}

		w = originalW;
		x = originalX;

		w += (pixelsPerSecond * 2);
		x -= pixelsPerSecond;

		// Figure out the left and right time bounds of the graph, based on
		// the pixels per second value
		rightTime = System.currentTimeMillis();
		leftTime = rightTime - ((w / pixelsPerSecond) * 1000);

		buf.pushMatrix();
		buf.translate(x, y);

		// Background
		buf.fill(220);
		buf.rect(0, 0, w, h);

		// Draw the background graph
		buf.strokeWeight(1);
		buf.stroke(255);

		if (scrollGrid) {
			// Start from the first whole second and work right
			gridTime = (rightTime / (long) (1000 * gridSeconds))
					* (long) (1000 * gridSeconds);
		} else {
			gridTime = rightTime;
		}

		while (gridTime >= leftTime) {
			int gridX = (int) parent.mapLong(gridTime, leftTime, rightTime, 0L,
					(long) w);
			buf.line(gridX, 0, gridX, h);
			gridTime -= (long) (1000 * gridSeconds);
		}

		// Draw square horizontal grid for now
		int gridY = h;
		while (gridY >= 0) {
			gridY -= pixelsPerSecond * gridSeconds;
			buf.line(0, gridY, w, gridY);
		}

		// Draw each channel (pass in as constructor arg?)

		buf.noFill();
		if (renderMode.equalsIgnoreCase("Shaded") || renderMode.equalsIgnoreCase("Triangles"))
			buf.noStroke();
		if (renderMode.equalsIgnoreCase("Curves") || renderMode.equalsIgnoreCase("Lines"))
			buf.strokeWeight(2);

		for (Channel thisChannel : parent.channels) {
			if (thisChannel.graphMe) {

				// Draw the line
				if (renderMode.equalsIgnoreCase("Lines") || renderMode.equalsIgnoreCase("Curves"))
					parent.stroke(thisChannel.drawColor);

				if (renderMode.equalsIgnoreCase("Shaded") || renderMode.equalsIgnoreCase("Triangles")) {
					buf.noStroke();
					buf.fill(thisChannel.drawColor, 120);
				}

				if (renderMode.equalsIgnoreCase("Triangles")) {
					buf.beginShape(PApplet.TRIANGLES);
				} else {
					buf.beginShape();
				}

				if (renderMode.equalsIgnoreCase("Curves") || renderMode.equalsIgnoreCase("Shaded"))
					buf.vertex(0, h);

				for (int j = 0; j < thisChannel.points.size(); j++) {
					Point thisPoint = (Point) thisChannel.points.get(j);

					// check bounds
					if ((thisPoint.time >= leftTime)
							&& (thisPoint.time <= rightTime)) {

						int pointX = (int) parent.mapLong(thisPoint.time, leftTime,
								rightTime, 0L, (long) w);

						int pointY = 0;
						if ((parent.scaleMode.equalsIgnoreCase("Global")) && thisChannel.allowGlobal) {
							pointY = (int) PApplet.map(thisPoint.value, 0, parent.globalMax,
									h, 0);
						} else {
							// Local scale
							pointY = (int) PApplet.map(thisPoint.value,
									thisChannel.minValue, thisChannel.maxValue,
									h, 0);
						}

						// ellipseMode(CENTER);
						// ellipse(pointX, pointY, 5, 5);

						if (renderMode.equalsIgnoreCase("Curves")) {
							buf.curveVertex(pointX, pointY);
						} else {
							buf.vertex(pointX, pointY);
						}
					}
				}
			}

			if (renderMode.equalsIgnoreCase("Curves") || renderMode.equalsIgnoreCase("Shaded"))
				buf.vertex(w, h);
			if (renderMode.equalsIgnoreCase("Lines") || renderMode.equalsIgnoreCase("Curves")
					|| renderMode.equalsIgnoreCase("Triangles"))
				buf.endShape();
			if (renderMode.equalsIgnoreCase("Shaded"))
				buf.endShape(PApplet.CLOSE);
		}

		buf.popMatrix();

		// gui matte
		buf.noStroke();
		buf.fill(255, 150);
		buf.rect(10, 10, 195, 81);

	}

}
