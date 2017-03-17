package shared;

import lejos.hardware.Audio;
import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;

public class Music {
		
	public Music(){
		EV3 ev3 = (EV3) BrickFinder.getDefault();
		Audio audio = ev3.getAudio();
		
		// Make EV3 beep
		audio.systemSound(0);
	}
	
}
