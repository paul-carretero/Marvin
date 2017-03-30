package positionManager;

import aiPlanner.Main;
import interfaces.DistanceGiver;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class VisionSensor implements DistanceGiver{
	private	EV3UltrasonicSensor radarUS			= null;
	private	SampleProvider 		radar			= null;
	private SampleProvider		spy				= null;
	private	static	final float	RADAR_OFFSET	= 100f;
	private static 	final int	OUT_OF_RANGE	= 9999;
	
	public VisionSensor(){
		Port port	= LocalEV3.get().getPort(Main.US_SENSOR);
		radarUS		= new EV3UltrasonicSensor(port);
		
		radarUS.enable();
		
		radar		= radarUS.getDistanceMode();
		spy			= radarUS.getListenMode();
	}
	
	synchronized public int getRadarDistance(){
		if(radarUS.isEnabled()){
			float[] sample = new float[1];
			radar.fetchSample(sample, 0);
			float res = (sample[0] * 1000f) + RADAR_OFFSET;
			if(res < Main.RADAR_MAX_RANGE && res > Main.RADAR_MIN_RANGE){
				return (int) res;
			}
		}
		return OUT_OF_RANGE;
	}
	
	synchronized public boolean checkEnnemyRadar(){
		if(radarUS.isEnabled()){
			float[] sample = new float[1];
			spy.fetchSample(sample, 0);
			return sample[0] == 1;
		}
		return false;
	}
}