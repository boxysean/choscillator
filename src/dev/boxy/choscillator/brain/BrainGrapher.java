package dev.boxy.choscillator.brain;

import java.util.LinkedList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.serial.Serial;
import controlP5.ControlFont;
import controlP5.ControlP5;

public class BrainGrapher extends PApplet {

	ControlP5 controlP5;
	ControlFont font;

	Serial serial;
	int lf = 10; // ASCII linefeed
	Channel[] channels = new Channel[11];
	Monitor[] monitors = new Monitor[10];
	Graph graph;
	ConnectionLight connectionLight;
	int packetCount = 0;
	int globalMax;
	String scaleMode;
	
	List<BrainGrapherListener> listeners = new LinkedList<BrainGrapherListener>();
	
	int width;
	int height;
	
	public BrainGrapher(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public void addListener(BrainGrapherListener listener) {
		listeners.add(listener);
	}
	
	public void setup() {
		size(width, height);
		smooth();

		// Set up the knobs and dials
		controlP5 = new ControlP5(this);
		
		controlP5.setColorLabel(color(0));
		// controlP5.setColorValue(color(0));
		controlP5.setColorBackground(color(0));
		// controlP5.setColorForeground(color(130));
		// controlP5.setColorActive(color(0));

		font = new ControlFont(createFont("DIN-MediumAlternate", 12), 12);

		// Create each channel
		
		try {
			serial = new Serial(this, Serial.list()[0], 9600);
			serial.bufferUntil(10);
		} catch (Exception e) {
			System.err.println("Unable to connect...");
			e.printStackTrace();
		}

		// Creat the channel objects
		// yellow to purple and then the space in between, grays for the alphas
		channels[0] = new Channel("Signal Quality", color(0), "");
		channels[1] = new Channel("Attention", color(100), "");
		channels[2] = new Channel("Meditation", color(50), "");
		channels[3] = new Channel("Delta", color(219, 211, 42), "Dreamless Sleep");
		channels[4] = new Channel("Theta", color(245, 80, 71), "Drowsy");
		channels[5] = new Channel("Low Alpha", color(237, 0, 119), "Relaxed");
		channels[6] = new Channel("High Alpha", color(212, 0, 149), "Relaxed");
		channels[7] = new Channel("Low Beta", color(158, 18, 188), "Alert");
		channels[8] = new Channel("High Beta", color(116, 23, 190), "Alert");
		channels[9] = new Channel("Low Gamma", color(39, 25, 159), "???");
		channels[10] = new Channel("High Gamma", color(23, 26, 153), "???");

		// Manual override for a couple of limits.
		channels[0].minValue = 0;
		channels[0].maxValue = 200;
		channels[1].minValue = 0;
		channels[1].maxValue = 100;
		channels[2].minValue = 0;
		channels[2].maxValue = 100;
		channels[0].allowGlobal = false;
		channels[1].allowGlobal = false;
		channels[2].allowGlobal = false;

		// Set up the monitors, skip the signal quality

		for (int i = 0; i < monitors.length; i++) {
			monitors[i] = new Monitor(channels[i + 1], i * (width / 10),
					height / 2, width / 10, height / 2, this, controlP5);
		}

		monitors[monitors.length - 1].w += width % monitors.length;

		// Set up the graph
		graph = new Graph(0, 0, width, height / 2, this, controlP5);

		connectionLight = new ConnectionLight(width - 98, 10, 20, this);

		globalMax = 0;
	}

	public void draw() {

		PGraphics buf = this.g;
		
		// find the global max
		if (scaleMode != null && scaleMode.equalsIgnoreCase("Global")) {
			if (channels.length > 3) {
				for (int i = 3; i < channels.length; i++) {
					if (channels[i].maxValue > globalMax)
						globalMax = channels[i].maxValue;
				}
			}
		}

		buf.background(255);

		graph.update();
		graph.draw(buf);

		connectionLight.update();
		connectionLight.draw(buf);

		for (int i = 0; i < monitors.length; i++) {
			monitors[i].update();
			monitors[i].draw(buf);
		}

	}

	void serialEvent(Serial p) {
		String[] incomingValues = split(p.readString(), ',');

		println(incomingValues);

		// Add the data to the logs
		if (incomingValues.length > 1) {
			packetCount++;

			// Wait till the third packet or so to start recording to avoid
			// initialization garbage.
			if (packetCount > 3) {
				for (int i = 0; i < incomingValues.length; i++) {
					int newValue = Integer.parseInt(incomingValues[i].trim());

					// Zero the EEG power values if we don't have a signal.
					// Can be useful to leave them in for development.
					if ((Integer.parseInt(incomingValues[0]) == 200) && (i > 2))
						newValue = 0;

					channels[i].addDataPoint(newValue);
				}
				
				// Update listeners
				
				for (BrainGrapherListener listener : listeners) {
					listener.onBrainUpdate();
				}
			}

		}
	}

	// Extend core's Map function to the Long datatype.
	long mapLong(long x, long in_min, long in_max, long out_min, long out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}

	long constrainLong(long value, long min_value, long max_value) {
		if (value > max_value)
			return max_value;
		if (value < min_value)
			return min_value;
		return value;
	}
	
	public static void main(String[] args) {
		PApplet.main(new String[] { "--present", "dev.boxy.choscillator.brain.BrainGrapher" });
	}

}