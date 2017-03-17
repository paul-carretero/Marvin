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
	
	private float[][] colors;
	private Port port;
	private EV3ColorSensor colorSensor;
	
	public ColorSensor(){
		port        = LocalEV3.get().getPort(Main.COLOR_SENSOR);
		colorSensor = new EV3ColorSensor(port);
		colors      = new float[16][0];
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
	public int getCurrentColor(){
		SampleProvider average = new MeanFilter(colorSensor.getRGBMode(), 1);
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
		return color;
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
			Main.printf("[COLOR SENSOR]         : Impossible de charger le fichier de calibration");
		}
	}
}
