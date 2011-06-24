package dev.boxy.choscillator;

import java.util.*;

public class TransitionManager {
	
	static final int TRANSITION_FRAMES = 30;
	
	boolean inTransition = false;
	boolean transitionToRelaxed = false;
	int transitionFrame = 0;
	
	List<TransitionListener> listeners = new LinkedList<TransitionListener>();
	
	public void startTransition() {
		if (!inTransition) {
			inTransition = true;
			transitionToRelaxed = !transitionToRelaxed;
		}
	}
	
	public void transition() {
		if (inTransition) {
			if (transitionToRelaxed) {
				if (++transitionFrame == TRANSITION_FRAMES) {
					inTransition = false;
				}
			} else {
				if (--transitionFrame == 0) {
					inTransition = false;
				}
			}
			
			if (transitionFrame == TRANSITION_FRAMES / 2) {
				for (TransitionListener listener : listeners) {
					listener.onTransition(transitionToRelaxed);
				}
			}
		}
	}
	
	public int getTransitionFrame() {
		return transitionFrame;
	}
	
	public boolean inTransition() {
		return inTransition;
	}
	
	public boolean isRelaxedState() {
		return transitionFrame < (TRANSITION_FRAMES / 2);
	}
	
	public void addListener(TransitionListener listener) {
		listeners.add(listener);
	}
	
}
