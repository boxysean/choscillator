package dev.boxy.choscillator;

import java.util.LinkedList;
import java.util.Queue;

import processing.core.PApplet;

class PhraseManager {
	Choscillator chos;
	
	final int FRAMES_PER_PHRASE = 60;
	int frames = 0;
	Queue<String> phrases = new LinkedList<String>();
	String phrase = null;
	
	public PhraseManager(Choscillator chos) {
		this.chos = chos;
	}
	
	public void add(String s){
		phrases.add(s);
	}
	
	public void draw() {
		frames++;
		
		if (frames == FRAMES_PER_PHRASE) {
			phrase = null;
			frames = 0;
		}
		
		if (frames == 0 && phrase == null && !phrases.isEmpty()) {
			phrase = phrases.poll();
		}
		
		int r = 0;
		int g = 0;
		int b = 0;
		
		if (phrase != null) {
			if (phrase.equals("red")) {
				r = 255;
			} else if (phrase.equals("green")) {
				g = 255;
			} else if (phrase.equals("blue")) {
				b = 255;
			}
			
			double alpha = Math.sin(((float) frames / FRAMES_PER_PHRASE) * PApplet.PI) * 64;
			chos.fill(r, g, b, (float) alpha);
			chos.rect(0, 0, chos.width, chos.height);
		}
	}
}
