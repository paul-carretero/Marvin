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
		Port port			= LocalEV3.get().getPort(Main.US_SENSOR);
		this.radarUS		= new EV3UltrasonicSensor(port);
		
		this.radarUS.enable();
		
		this.radar			= this.radarUS.getDistanceMode();
		this.spy			= this.radarUS.getListenMode();
		
		Main.printf("[VISION SENSOR]         : Initialized");
	}
	
	synchronized public int getRadarDistance(){
		if(this.radarUS.isEnabled()){
			float[] sample = new float[1];
			this.radar.fetchSample(sample, 0);
			float res = (sample[0] * 1000f) + RADAR_OFFSET;
			return (int) res;
		}
		return OUT_OF_RANGE;
	}
	
	synchronized public boolean checkEnnemyRadar(){
		if(this.radarUS.isEnabled()){
			float[] sample = new float[1];
			this.spy.fetchSample(sample, 0);
			return sample[0] == 1;
		}
		return false;
	}
	
	synchronized public boolean checkSomething(){
		int dist = getRadarDistance();
		return (dist < Main.RADAR_MAX_RANGE && dist > Main.RADAR_MIN_RANGE);
	}
}