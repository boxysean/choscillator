package dev.boxy.choscillator.brain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Channel { 

	String name;
	int drawColor;
	String description;
	boolean graphMe;
	boolean relative;
	int maxValue;
	int minValue;
	ArrayList<Point> points;
	boolean allowGlobal;
	int value;
	
	protected static Map<String, Channel> channelMap = new HashMap<String, Channel>();
	
	/**
	 * Method for finding values by name
	 * @param key name of the channel
	 * @return the channel data
	 */
	public static int get(String key) {
		return channelMap.get(key).value;
	}
	
	Channel(String _name, int _drawColor, String _description) {
		name = _name;
		drawColor = _drawColor;
		description = _description;
		allowGlobal = true;
		points = new ArrayList<Point>();
		
		channelMap.put(name, this);
	}
	
	
	void addDataPoint(int value) {
		
		long time = System.currentTimeMillis();
		this.value = value;
		
		if(value > maxValue) maxValue = value;
		if(value < minValue) minValue = value;
		
		points.add(new Point(time, value));
		
		// tk max length handling
	}
	
	Point getLatestPoint() {
		if(points.size() > 0) {
			return (Point)points.get(points.size() - 1);
		}
		else {
			return new Point(0, 0);
		}
	}


}