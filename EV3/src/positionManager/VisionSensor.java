package positionManager;

import aiPlanner.Main;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RangeFinderAdapter;

public class VisionSensor {
	private EV3UltrasonicSensor radarUS;
	private RangeFinderAdapter radar;
	
	public VisionSensor(){
		Port port  = LocalEV3.get().getPort(Main.US_SENSOR);
		radarUS = new EV3UltrasonicSensor(port);
		radar = new RangeFinderAdapter(radarUS);
	}
	
	public int getVisionData() {
		if(radarUS.isEnabled()){
			float[] sample = new float[1];
			radar.fetchSample(sample, 0);
			return (int) Math.round((sample[0] * 100) + Main.RADAR_CALIBRATION);
		}
		else{
			return -1;
		}
	}
	
	public int getNearItemDistance(){
		return Math.round(radar.getRange());
	}
}
