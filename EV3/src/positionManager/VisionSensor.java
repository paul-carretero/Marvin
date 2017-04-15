package positionManager;

import aiPlanner.Main;
import interfaces.DistanceGiver;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

/**
 * Vision Sensor permet d'abstraire la gestion du radar et offre quelque primitives de base pour le traitement des informations en provenance de celui ci.
 */
public class VisionSensor implements DistanceGiver{
	
	/**
	 * Repr�sentation du radar � ultra-son physique du robot
	 */
	private	EV3UltrasonicSensor radarUS			= null;
	
	/**
	 * Repr�sente la distance lu par le radar
	 */
	private	SampleProvider 		radar			= null;
	
	/**
	 * Repr�sente les donn�es re�u sur la pr�sence ou non de radar ennemi.
	 */
	private SampleProvider		spy				= null;
	
	/**
	 * d�placement du radar par rapport au centre de position du robot, les donn�es fournit seront fonction du centre de direcetion du robot
	 */
	private	static	final float	RADAR_OFFSET	= 100f;
	
	/**
	 * fix de la position du radar en fonction des donn�es obtenue � l'initialisation
	 */
	private	float				dynamicOffset	= 0;
	
	/**
	 * Symbolise l'absence de donn�es exploitable
	 */
	private static 	final int	OUT_OF_RANGE	= 9999;
	
	/**
	 * Distance entre l'item en face du robot et son point de d�par au d�but.
	 */
	private static 	final int	START_DISTANCE	= 600;
	
	/**
	 * Distance maximal au d�l� dela de laquelle on consid�re que le radar est en d�faut (il faut red�marer...)
	 */
	private static 	final int	MAX_RADAR_BIAS	= 300;
	
	/**
	 * Cr�� une nouvelle instance du controlleur du radar
	 * Exception InvalidSensorMode soulev�e al�atoirement par le biblioth�que Lejos, il suffit de red�marer le programme.
	 */
	public VisionSensor(){
		Port port			= LocalEV3.get().getPort(Main.US_SENSOR);
		this.radarUS		= new EV3UltrasonicSensor(port);
		
		this.radarUS.enable();
		
		this.radar			= this.radarUS.getDistanceMode();
		this.spy			= this.radarUS.getListenMode();
		
		setDynamicOffset();
		
		Main.printf("[VISION SENSOR]         : Initialized");
	}
	
	/**
	 * Utilis� au d�marrage afin de d�termin� dynamiquement le biais de calibration du radar 
	 * en fonction de la distance initiale connue du palet le plus proche.
	 * Termine le programme si la distance n'est pas coh�rente avec les donn�es initiales.
	 */
	private void setDynamicOffset() {
		int dist = getRadarDistance();
		if(Main.areApproximatelyEqual(dist, START_DISTANCE, MAX_RADAR_BIAS)){
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
			if(res >= RADAR_OFFSET && res <= Main.RADAR_MAX_RANGE){
				return (int) Math.round(0.94f * res + 29.6 + this.dynamicOffset);
			}
		}
		return OUT_OF_RANGE;
	}
	
	/**
	 * @return vrai si le radar detecte un radar ennemi, faux sinon.
	 */
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
		return dist < Main.RADAR_MAX_RANGE && dist > Main.RADAR_MIN_RANGE;
	}
}