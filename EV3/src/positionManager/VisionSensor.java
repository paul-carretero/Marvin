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
	private	float				dynamicOffset	= 0;
	private static 	final int	OUT_OF_RANGE	= 9999;
	private static 	final int	START_DISTANCE	= 600;
	
	public VisionSensor(){
		Port port			= LocalEV3.get().getPort(Main.US_SENSOR);
		this.radarUS		= new EV3UltrasonicSensor(port);
		
		this.radarUS.enable();
		
		this.radar			= this.radarUS.getDistanceMode();
		this.spy			= this.radarUS.getListenMode();
		
		setDynamicOffset();
		
		Main.printf("[VISION SENSOR]         : Initialized");
	}
	
	private void setDynamicOffset() {
		int dist = getRadarDistance();
		if(Main.areApproximatlyEqual(dist, START_DISTANCE, 100)){
			this.dynamicOffset = START_DISTANCE - getRadarDistance();
		}
		else{
			Main.printf("[VISION SENSOR]         : This sensor is nothing but bugged, better die now");
			System.exit(1);
		}
	}

	synchronized public int getRadarDistance(){
		if(this.radarUS.isEnabled()){
			float[] sample = new float[1];
			this.radar.fetchSample(sample, 0);
			float res = (sample[0] * 1000f) + RADAR_OFFSET;
			if(res >= 100 && res <= Main.RADAR_MAX_RANGE){
				return (int) Math.round(0.94f * res + 29.6 + this.dynamicOffset);
			}
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