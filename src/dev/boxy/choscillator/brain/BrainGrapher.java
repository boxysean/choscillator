package dev.boxy.choscillator.brain;

import java.util.ArrayList;
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
	
	Channel signalQualityChannel;
	Monitor signalQualityMonitor;
	
	List<Channel> channels = new ArrayList<Channel>();
	List<Monitor> monitors = new ArrayList<Monitor>();
	Graph graph;
	ConnectionLight connectionLight;
	int packetCount = 0;
	int globalMax;
	String scaleMode;
	
	List<BrainGrapherListener> listeners = new LinkedList<BrainGrapherListener>();
	List<BrainGrapherCharter> charters = new LinkedList<BrainGrapherCharter>();
	
	int width;
	int height;
	
	public BrainGrapher() {
		this(1024, 768);
	}
	
	public BrainGrapher(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public void addListener(BrainGrapherListener listener) {
		listeners.add(listener);
	}
	
	public void addCharter(BrainGrapherCharter charter) {
		charters.add(charter);
	}
	
	public void buildMonitors() {
		monitors.clear();
		
		for (int i = 0; i < channels.size(); i++) {
			Channel channel = channels.get(i);
			
			monitors.add(new Monitor(channel, i * (width / channels.size()), 
					height / 2, width / channels.size(), height / 2, this, controlP5));
		}

		monitors.get(monitors.size()-1).w += width % monitors.size();
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
			String list[] = Serial.list();
			serial = new Serial(this, list[0], 9600);
			serial.bufferUntil(10);
		} catch (Exception e) {
			System.err.println("Unable to connect...");
			e.printStackTrace();
		}

		// Create the channel objects
		// yellow to purple and then the space in between, grays for the alphas
		
		signalQualityChannel = new Channel("Signal Quality", color(0), "");
		signalQualityChannel.minValue = 0;
		signalQualityChannel.maxValue = 200;
		signalQualityChannel.allowGlobal = false;
		
		Channel attentionChannel = new Channel("Attention", color(100), "");
		attentionChannel.minValue = 0;
		attentionChannel.maxValue = 100;
		attentionChannel.allowGlobal = false;
		channels.add(attentionChannel);
		
		Channel meditationChannel = new Channel("Meditation", color(50), "");
		meditationChannel.minValue = 0;
		meditationChannel.maxValue = 100;
		meditationChannel.allowGlobal = false;
		channels.add(meditationChannel);
		
		channels.add(new Channel("Delta", color(219, 211, 42), "Dreamless Sleep"));
		channels.add(new Channel("Theta", color(245, 80, 71), "Drowsy"));
		channels.add(new Channel("Low Alpha", color(237, 0, 119), "Relaxed"));
		channels.add(new Channel("High Alpha", color(212, 0, 149), "Relaxed"));
		channels.add(new Channel("Low Beta", color(158, 18, 188), "Alert"));
		channels.add(new Channel("High Beta", color(116, 23, 190), "Alert"));
		channels.add(new Channel("Low Gamma", color(39, 25, 159), "???"));
		channels.add(new Channel("High Gamma", color(23, 26, 153), "???"));
		
		// Add external channels
		
		for (BrainGrapherCharter charter : charters) {
			channels.addAll(charter.addBrainGraphChannels());
		}

		buildMonitors();

		// Set up the graph
		graph = new Graph(0, 0, width, height / 2, this, controlP5);

		connectionLight = new ConnectionLight(width - 98, 10, 20, this);

		globalMax = 0;
	}

	public void draw() {

		PGraphics buf = this.g;
		
		// find the global max
		if (scaleMode != null && scaleMode.equalsIgnoreCase("Global")) {
			for (Channel channel : channels) {
				if (channel.allowGlobal && channel.maxValue > globalMax) {
					globalMax = channel.maxValue;
				}
			}
		}

		buf.background(255);

		graph.update();
		graph.draw(buf);

		connectionLight.update();
		connectionLight.draw(buf);

		for (Monitor monitor : monitors) {
			monitor.update();
			monitor.draw(buf);
		}

	}

	public void serialEvent(Serial p) {
		String[] incomingValues = split(p.readString(), ',');

//		println(incomingValues);

		// Add the data to the logs
		if (incomingValues.length > 1) {
			packetCount++;

			// Wait till the third packet or so to start recording to avoid
			// initialization garbage.
			if (packetCount > 3) {
				signalQualityChannel.addDataPoint(Integer.parseInt(incomingValues[0].trim()));
				
				for (int i = 1, j = 0; i < incomingValues.length; i++, j++) {
					int newValue = Integer.parseInt(incomingValues[i].trim());

					// Zero the EEG power values if we don't have a signal.
					// Can be useful to leave them in for development.
					if ((Integer.parseInt(incomingValues[0]) == 200) && (i > 2)) {
						newValue = 0;
					}

					channels.get(j).addDataPoint(newValue);
				}
				
				// Do some work on the artificial channels
				
				for (BrainGrapherCharter charter : charters) {
					charter.updateBrainGraphChannels();
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