package motorsManager;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

public class CalibrateGraber {
	
	private static EV3LargeRegulatedMotor graber;
	private static Port port;
	private static long start;

	public static void main(String[] args) {
		port   = LocalEV3.get().getPort("D");
		graber = new EV3LargeRegulatedMotor(port);
		
		startCalibrate(false);
		Button.ENTER.waitForPress();
		stopCalibrate();
		Button.ENTER.waitForPress();
	}

	/**
	 * Lance la calibration
	 * @param open vrai pour l'ouverture
	 */
	public static void startCalibrate(boolean open){
		graber.setSpeed(800);
		if(open){
			graber.forward();
		}else{
			graber.backward();
		}
		start = System.currentTimeMillis();
	}

	/**
	 * Arrête la calibration
	 * @param open
	 */
	public static void stopCalibrate(){
		graber.stop();
		System.out.println("TOTAL TIME : " + String.valueOf(System.currentTimeMillis() - start));
	}

}
