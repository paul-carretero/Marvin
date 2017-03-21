package positionManager;

import aiPlanner.Main;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class VisionSensor {
	private	EV3UltrasonicSensor radarUS			= null;
	private	SampleProvider 		radar			= null;
	private	static	final	int	RADAR_OFFSET	= 6; // faudra surement faire une fonction de calibration...
	
	public VisionSensor(){
		Port port  = LocalEV3.get().getPort(Main.US_SENSOR);
		radarUS = new EV3UltrasonicSensor(port);
		Delay.msDelay(300);
		radarUS.disable();
		Delay.msDelay(300);
		radarUS.enable();
		radar = radarUS.getDistanceMode();
	}
	
	public int getNearItemDistance(){
		if(radarUS.isEnabled()){
			float[] sample = new float[1];
			radar.fetchSample(sample, 0);
			int res = Math.round((sample[0] * 1000) + RADAR_OFFSET*10);
			if(res < 3001){
				return res;
			}
			return 9999;
		}
		else{
			return 9999;
		}
	}
}