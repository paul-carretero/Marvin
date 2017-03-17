package positionManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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

public class ColorCalibrator {
	
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
		colors[Main.COLOR_BLUE] = blue;
		
		System.out.println("Press enter to calibrate red...");
		Button.ENTER.waitForPressAndRelease();
		float[] red = new float[average.sampleSize()];
		average.fetchSample(red, 0);
		colors[Main.COLOR_RED] = red;
		
		System.out.println("Press enter to calibrate green...");
		Button.ENTER.waitForPressAndRelease();
		float[] green = new float[average.sampleSize()];
		average.fetchSample(green, 0);
		colors[Main.COLOR_GREEN] = green;

		System.out.println("Press enter to calibrate black...");
		Button.ENTER.waitForPressAndRelease();
		float[] black = new float[average.sampleSize()];
		average.fetchSample(black, 0);
		colors[Main.COLOR_BLACK] = black;
		
		System.out.println("Press enter to calibrate grey...");
		Button.ENTER.waitForPressAndRelease();
		float[] grey = new float[average.sampleSize()];
		average.fetchSample(grey, 0);
		colors[Main.COLOR_GREY] = grey;
		
		System.out.println("Press enter to calibrate white...");
		Button.ENTER.waitForPressAndRelease();
		float[] white = new float[average.sampleSize()];
		average.fetchSample(white, 0);
		colors[Main.COLOR_WHITE] = white;
		
		System.out.println("Press enter to calibrate yellow...");
		Button.ENTER.waitForPressAndRelease();
		float[] yellow = new float[average.sampleSize()];
		average.fetchSample(yellow, 0);
		colors[Main.COLOR_YELLOW] = yellow;
		
		colorSensor.setFloodlight(false);
		
		File fichier =  new File("conf.txt") ;
		
		PrintWriter writer = new PrintWriter(fichier);
		writer.print("");
		writer.close();
		
		ObjectOutputStream oos =  new ObjectOutputStream(new FileOutputStream(fichier)) ;
		oos.writeObject(colors) ;
		oos.close();
		
		ObjectInputStream ois =  new ObjectInputStream(new FileInputStream(fichier)) ;
		float[][] readColors = (float[][])ois.readObject() ;
		ois.close();
		
		/*BufferedWriter outputWritter =  new BufferedWriter(new FileWriter("conf.txt"));
		
		for(float f[] : colors){
			for(float c : f){
				outputWritter.write(Float.toString(c) + ";");
			}
			outputWritter.newLine();
		}
		outputWritter.flush();
		outputWritter.close();
		
		BufferedReader inputReader = new BufferedReader(new FileReader("conf.txt"));
		
		float[][] readColors = new float[7][average.sampleSize()];
		
		int i = 0;
		int j = 0;
		String l = inputReader.readLine();
		while(l != null){
			j = 0;
			for(String s : l.split(";")){
				readColors[i][j] = Float.parseFloat(s);
				j = j+1;
			}
			l = inputReader.readLine();
			i = i+1;
		}*/
		
		boolean again = true;
		
		while (again) {
			float[] sample = new float[average.sampleSize()];
			System.out.println("\nPress enter to detect a color...");
			Button.ENTER.waitForPressAndRelease();
			average.fetchSample(sample, 0);
			double minscal = Double.MAX_VALUE;
			String color = "";
			
			double scalaire = ColorSensor.scalaire(sample, readColors[Main.COLOR_BLUE]);	
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "blue";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[Main.COLOR_RED]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "red";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[Main.COLOR_GREEN]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "green";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[Main.COLOR_BLACK]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "black";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[Main.COLOR_GREY]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "grey";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[Main.COLOR_YELLOW]);
			if (scalaire < minscal) {
				minscal = scalaire;
				color = "yellow";
			}
			
			scalaire = ColorSensor.scalaire(sample, readColors[Main.COLOR_WHITE]);
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
		}
	}

}
