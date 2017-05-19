package positionManager;

import aiPlanner.Main;
import interfaces.DistanceGiver;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

/**
 * Vision Sensor permet d'abstraire la gestion du radar et offre quelque primitives de base pour le traitement des informations en provenance de celui ci.
 * @author paul.carretero, florent.chastagner
 */
public class VisionSensor implements DistanceGiver{
	
	/**
	 * Repr�sentation du radar � ultra-son physique du robot
	 */
	private	final EV3UltrasonicSensor	radarUS;
	
	/**
	 * Repr�sente la distance lu par le radar
	 */
	private	final SampleProvider		radar;
	
	/**
	 * Repr�sente les donn�es re�ues sur la pr�sence ou non de radar ennemi.
	 */
	private final SampleProvider		spy;
	
	
	/**
	 * fix de la position du radar en fonction des donn�es obtenues � l'initialisation
	 */
	private	float				dynamicOffset	= 0;
	
	/**
	 * d�placement du radar par rapport au centre de position du robot, les donn�es fournit seront fonction du centre de direcetion du robot
	 */
	private	static final float	RADAR_OFFSET	= 100f;
	
	/**
	 * Symbolise l'absence de donn�es exploitables.
	 */
	private static 	final int	OUT_OF_RANGE	= 9999;
	
	/**
	 * Distance entre l'item en face du robot et son point de d�part au d�but.
	 */
	private static 	final int	START_DISTANCE	= 700;
	
	/**
	 * Distance maximale au d�l� dela de laquelle on consid�re que le radar est en d�faut (il faut red�marer...)
	 */
	private static 	final int	MAX_RADAR_BIAS	= 400;
	
	/**
	 * Cr�e une nouvelle instance du controlleur du radar
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
	synchronized private void setDynamicOffset() {
		int dist = getRadarDistance();
		if(Main.areApproximatelyEqual(dist, START_DISTANCE, MAX_RADAR_BIAS)){
			this.dynamicOffset = START_DISTANCE - getRadarDistance();
		}
		else{
			Main.printf("[VISION SENSOR]         : This sensor is nothing but bugged, will try without...");
			Main.printf("[VISION SENSOR]         : Distance lue : " + dist);
			Sound.beep();
			Sound.beep();
			Sound.beep();
			Sound.beep();
			Sound.beep();
			Sound.beep();
			Sound.beep();
			Sound.beep();
			Sound.beep();
			Main.USE_RADAR = false;
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