package positionManager;

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

public class ColorSensor {
	
	public static final int COLOR_BLUE 				= 0;
	public static final int COLOR_BLACK 			= 1;
	public static final int COLOR_WHITE 			= 2;
	public static final int COLOR_GREY 				= 3;
	public static final int COLOR_YELLOW 			= 4;
	public static final int COLOR_RED 				= 5;
	public static final int COLOR_GREEN 			= 6;
	
	private float[][] colors;
	private Port port;
	private EV3ColorSensor colorSensor;
	private SampleProvider average;
	
	public ColorSensor(){
		port        = LocalEV3.get().getPort(Main.COLOR_SENSOR);
		colorSensor = new EV3ColorSensor(port);
		colors      = new float[16][0];
		average		= new MeanFilter(colorSensor.getRGBMode(), 1);
	}
	
	public void lightOn(){
		colorSensor.setFloodlight(Color.WHITE);
	}
	
	public void lightOff(){
		colorSensor.setFloodlight(false);
	}

	/**
	 * Renvoie la couleur connue la plus proche.
	 * Pour que cette fonction ne renvoie pas -1 il conviens de calibrer les 
	 * couleurs en amont.
	 * 
	 * @return la couleur (Color.EXAMPLE) ou -1 si aucune couleur n'a été
	 * calibrée
	 */
	public shared.Color getCurrentColor(){
		float[]        sample  = new float[average.sampleSize()];
		double         minscal = Double.MAX_VALUE;
		int            color   = -1;

		average.fetchSample(sample, 0);

		for(int i= 0; i< 7; i++){
			if(colors[i].length > 0){
				double scalaire = scalaire(sample, colors[i]);
				if (scalaire < minscal) {
					minscal = scalaire;
					color = i;
				}
			}
		}
		return getRealColor(color);
	}
	
	private shared.Color getRealColor(int color) {
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
	 * @param v1 la preière couleur
	 * @param v2 la seconde couleur
	 * @return la distance entre les deux couleurs.
	 */
	protected static double scalaire(float[] v1, float[] v2) {
		return Math.sqrt (Math.pow(v1[0] - v2[0], 2.0) +
				Math.pow(v1[1] - v2[1], 2.0) +
				Math.pow(v1[2] - v2[2], 2.0));
	}
	
	public void setCalibration(){
		try{
			File fichierRead =  new File("conf.txt") ;
			ObjectInputStream ois =  new ObjectInputStream(new FileInputStream(fichierRead)) ;
			colors = (float[][])ois.readObject() ;
			ois.close();
		}
		catch (Exception e) {
			Main.printf("[COLOR SENSOR]          : Impossible de charger le fichier de calibration");
		}
	}
}
