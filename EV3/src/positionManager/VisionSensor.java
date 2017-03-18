package positionManager;

import aiPlanner.Main;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class VisionSensor {
	private EV3UltrasonicSensor radarUS;
	private SampleProvider radar;
	
	public VisionSensor(){
		Port port  = LocalEV3.get().getPort(Main.US_SENSOR);
		radarUS = new EV3UltrasonicSensor(port);
		Delay.msDelay(500);
		radarUS.disable();
		Delay.msDelay(500);
		radarUS.enable();
		radar = radarUS.getDistanceMode();
	}
	
	public int getNearItemDistance(){
		if(radarUS.isEnabled()){
			float[] sample = new float[1];
			radar.fetchSample(sample, 0);
			return Math.round((sample[0] * 100));
		}
		else{
			return -1;
		}
	}
}
