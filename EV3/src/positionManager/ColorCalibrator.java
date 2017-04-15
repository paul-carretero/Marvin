package positionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import aiPlanner.Main;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;

@SuppressWarnings("javadoc")
/**
 * Classe utilitaire permettant de générer un fichier de calibration des couleurs.
 */
public class ColorCalibrator {
	
	public static final int COLOR_BLUE 		= 0;
	public static final int COLOR_BLACK 	= 1;
	public static final int COLOR_WHITE 	= 2;
	public static final int COLOR_GREY 		= 3;
	public static final int COLOR_YELLOW 	= 4;
	public static final int COLOR_RED 		= 5;
	public static final int COLOR_GREEN 	= 6;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Port port = LocalEV3.get().getPort(Main.COLOR_SENSOR);
		EV3ColorSensor colorSensor = new EV3ColorSensor(port);
		SampleProvider average = new MeanFilter(colorSensor.getRGBMode(), 1);
		colorSensor.setFloodlight(Color.WHITE);
		
		float[][] colors = new float[7][average.sampleSize()];
		
		System.out.println("Press enter to calibrate blue...");
		Button.ENTER.waitForPressAndRelease();
		float[] blue = new float[average.sampleSize()];
		average.fetchSample(blue, 0);
		colors[COLOR_BLUE] = blue;
		
		Delay.msDelay(500);
		
		System.out.println("Press enter to calibrate red...");
		Button.ENTER.waitForPressAndRelease();
		float[] red = new float[average.sampleSize()];
		average.fetchSample(red, 0);
		colors[COLOR_RED] = red;
		
		Delay.msDelay(500);
		
		System.out.println("Press enter to calibrate green...");
		Button.ENTER.waitForPressAndRelease();
		float[] green = new float[average.sampleSize()];
		average.fetchSample(green, 0);
		colors[COLOR_GREEN] = green;
		
		Delay.msDelay(500);

		System.out.println("Press enter to calibrate black...");
		Button.ENTER.waitForPressAndRelease();
		float[] black = new float[average.sampleSize()];
		average.fetchSample(black, 0);
		colors[COLOR_BLACK] = black;
		
		Delay.msDelay(500);
		
		System.out.println("Press enter to calibrate grey...");
		Button.ENTER.waitForPressAndRelease();
		float[] grey = new float[average.sampleSize()];
		average.fetchSample(grey, 0);
		colors[COLOR_GREY] = grey;
		
		Delay.msDelay(500);
		
		System.out.println("Press enter to calibrate white...");
		Button.ENTER.waitForPressAndRelease();
		float[] white = new float[average.sampleSize()];
		average.fetchSample(white, 0);
		colors[COLOR_WHITE] = white;
		
		Delay.msDelay(500);
		
		System.out.println("Press enter to calibrate yellow...");
		Button.ENTER.waitForPressAndRelease();
		float[] yellow = new float[average.sampleSize()];
		average.fetchSample(yellow, 0);
		colors[COLOR_YELLOW] = yellow;
		
		colorSensor.setFloodlight(false);
		
		final File fichier =  new File("conf.txt") ;
		
		PrintWriter writer = new PrintWriter(fichier);
		writer.print("");
		writer.close();
		
		ObjectOutputStream oos =  new ObjectOutputStream(new FileOutputStream(fichier)) ;
		oos.writeObject(colors) ;
		oos.close();
		
		ObjectInputStream ois =  new ObjectInputStream(new FileInputStream(fichier)) ;
		float[][] readColors = (float[][])ois.readObject() ;
		ois.close();
		
		boolean again = true;
		
		while (again) {
			float[] sample = new float[average.sampleSize()];
			System.out.println("\nPress enter to detect a color...");
			Button.ENTER.waitForPressAndRelease();
			average.fetchSample(sample, 0);
			double minscal = Double.MAX_VALUE;
			String color = "";
			
			double scalaire = ColorSensor.scalaire(sample, readColors[COLOR_BLUE]);	
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "blue";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[COLOR_RED]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "red";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[COLOR_GREEN]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "green";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[COLOR_BLACK]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "black";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[COLOR_GREY]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "grey";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[COLOR_YELLOW]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "yellow";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[COLOR_WHITE]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "white";
			}
			
			System.out.println("The color is " + color + " \n");
			System.out.println("Press ENTER to continue \n");
			System.out.println("ESCAPE to exit");
			Button.waitForAnyPress();
			if(Button.ESCAPE.isDown()) {
				colorSensor.setFloodlight(false);
				again = false;
			}
			Delay.msDelay(500);
		}
		colorSensor.close();
	}

}
