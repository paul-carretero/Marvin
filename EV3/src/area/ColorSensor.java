package area;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import aiPlanner.Main;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;

/**
 * Classe représentant le capteur de couleur du robot.
 * Principalement utilisé pour obtenir la couleur "vue" par le robot.
 * @author paul.carretero
 */
public class ColorSensor {

	/**
	 * indice de la couleur bleu dans le tableau
	 */
	public static final int COLOR_BLUE 		= 0;
	/**
	 * indice de la couleur noire dans le tableau
	 */
	public static final int COLOR_BLACK 	= 1;
	/**
	 * indice de la couleur blanche dans le tableau
	 */
	public static final int COLOR_WHITE 	= 2;
	/**
	 * indice de la couleur grise dans le tableau
	 */
	public static final int COLOR_GREY 		= 3;
	/**
	 * indice de la couleur jaune dans le tableau
	 */
	public static final int COLOR_YELLOW 	= 4;
	/**
	 * indice de la couleur rouge dans le tableau
	 */
	public static final int COLOR_RED 		= 5;
	/**
	 * indice de la couleur verte dans le tableau
	 */
	public static final int COLOR_GREEN 	= 6;
	
	/**
	 * Tableau contenant l'indice de la couleur ainsi qu'un échantillon des valeurs couleurs récupérés
	 */
	private float[][]				colors;
	
	/**
	 * Capteur de couleur physique du robot.
	 */
	private final EV3ColorSensor	colorSensor;
	
	/**
	 * représnte les données fournit par le capteur de couleur sous forme standard
	 */
	private final SampleProvider	average;
	
	/**
	 * Créer une nouvelle instance du capteur de couleur et initialize le SampleProvider et charge le fichier de configuration
	 */
	public ColorSensor(){
		Port port        = LocalEV3.get().getPort(Main.COLOR_SENSOR);
		this.colorSensor = new EV3ColorSensor(port);
		this.average	 = new MeanFilter(this.colorSensor.getRGBMode(), 1);
		
		setCalibration();
		
		Main.printf("[COLOR SENSOR]          : Initialized");
	}
	
	/**
	 * Allume le capteur de couleur
	 */
	public void lightOn(){
		this.colorSensor.setFloodlight(Color.WHITE);
	}
	
	/**
	 * Termine le capteur de couleur
	 */
	public void lightOff(){
		this.colorSensor.setFloodlight(false);
	}

	/**
	 * Renvoie la couleur connue la plus proche.
	 * 
	 * @return la couleur (Color.EXAMPLE)
	 */
	public shared.Color getCurrentColor(){
		float[]        sample  = new float[this.average.sampleSize()];
		double         minscal = Double.MAX_VALUE;
		int            color   = -1;

		this.average.fetchSample(sample, 0);

		for(int i= 0; i< 7; i++){
			if(this.colors[i].length > 0){
				double scalaire = scalaire(sample, this.colors[i]);
				if (scalaire < minscal) {
					minscal = scalaire;
					color = i;
				}
			}
		}
		return getRealColor(color);
	}
	
	/**
	 * Effectue la convertion entre l'indice dans le tableau de couleur et la couleur sous forme d'Enum pour le reste du programme
	 * @param color un entier associé à une couleur
	 * @return Color.COLOR la couleur associé à l'entier en entrée
	 */
	private static shared.Color getRealColor(final int color) {
		switch (color) {
		case COLOR_BLACK:
			return shared.Color.BLACK;
		case COLOR_BLUE:
			return shared.Color.BLUE;
		case COLOR_GREEN:
			return shared.Color.GREEN;
		case COLOR_GREY:
			return shared.Color.GREY;
		case COLOR_RED:
			return shared.Color.RED;
		case COLOR_WHITE:
			return shared.Color.WHITE;
		case COLOR_YELLOW:
			return shared.Color.YELLOW;
		default:
			return null;
		}
	}

	/**
	 * Calcule la distance entre deux couleurs.
	 * @param v1 la preiÃ¨re couleur
	 * @param v2 la seconde couleur
	 * @return la distance entre les deux couleurs.
	 */
	public static double scalaire(final float[] v1, final float[] v2) {
		return Math.sqrt (Math.pow(v1[0] - v2[0], 2.0) +
				Math.pow(v1[1] - v2[1], 2.0) +
				Math.pow(v1[2] - v2[2], 2.0));
	}
	
	/**
	 * Tente de lire et charger le fichier de calibration.
	 */
	private void setCalibration(){
		try{
			File fichierRead =  new File("conf.txt") ;
			ObjectInputStream ois =  new ObjectInputStream(new FileInputStream(fichierRead)) ;
			this.colors = (float[][])ois.readObject() ;
			ois.close();
		}
		catch (Exception e) {
			Main.printf("[COLOR SENSOR]          : Impossible de charger le fichier de calibration");
		}
	}
}
