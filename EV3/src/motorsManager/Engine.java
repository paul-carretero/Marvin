package motorsManager;

import aiPlanner.Main;
import eventManager.EventHandler;
import interfaces.EngineUpdateListener;
import interfaces.WaitProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class Engine implements EngineUpdateListener{
	
	private Wheel     leftWheel;
	private Wheel     rightWheel;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private Chassis   chassis;
	private MovePilot pilot;
	private float coeffCorrectionBackward = 1;
	private float coeffCorrectionForward = 1;
	WaitProvider waitProvider;
	
	private final float right_wheel_correction = -0.2f; // positif ou negatif...
	
	public Engine(EventHandler eventManager, WaitProvider marvin){
		leftMotor	= new EV3LargeRegulatedMotor(LocalEV3.get().getPort(Main.LEFT_WHEEL));
		rightMotor	= new EV3LargeRegulatedMotor(LocalEV3.get().getPort(Main.RIGHT_WHEEL));
		leftWheel	= WheeledChassis.modelWheel(leftMotor, Main.WHEEL_DIAMETER).offset(-1*Main.DISTANCE_TO_CENTER);
		rightWheel	= WheeledChassis.modelWheel(rightMotor, Main.WHEEL_DIAMETER + right_wheel_correction).offset(Main.DISTANCE_TO_CENTER);
		chassis		= new WheeledChassis(new Wheel[]{leftWheel, rightWheel},  WheeledChassis.TYPE_DIFFERENTIAL);
		chassis.setSpeed(Main.CRUISE_SPEED, Main.ROTATION_SPEED);
		chassis.setAcceleration(Main.LINEAR_ACCELERATION, Main.LINEAR_ACCELERATION);
		pilot		= new MovePilot(chassis);
		pilot.addMoveListener(eventManager);
		pilot.setLinearAcceleration(Main.LINEAR_ACCELERATION);
		pilot.setAngularSpeed(Main.ROTATION_SPEED);
		this.waitProvider = marvin;
		Main.printf("[ENGINE]                : Initialized");
	}
	
	public MovePilot getPilot(){
		return pilot;
	}


	public void goForward(float distance, float speed){
		int waitTime = (int) ((distance * 10000f/speed) * coeffCorrectionForward);
		pilot.setLinearSpeed(speed);
		Main.printf("[ENGINE]                : waitTime = " + waitTime);
		pilot.forward();
		syncWait(waitTime);
		pilot.stop();
	}
	
	public void goBackward(float distance, float speed){
		int waitTime = (int) (((distance*10000)/speed) * coeffCorrectionBackward);
		pilot.setLinearSpeed(speed);
		pilot.backward();
		syncWait(waitTime);
		pilot.stop();
	}
	
	public static int normalize(int angle){
		angle = angle % 360; // on normalise;
		if(angle > 180){
			angle = (-1) * (360 - angle);
		}
		return angle;
	}
	
	public void turnHere(int angle, int speed){
		angle = normalize(angle);
		pilot.setAngularSpeed(speed);
		pilot.rotate(angle);
	}
	
	// a calibrer...
	public void turnSmooth(int angle){
		angle = normalize(angle);
		int coeff = 1;
		
		if(angle < 0){
			coeff = -1;
		}
		
		pilot.setLinearSpeed(Main.CRUISE_SPEED);

		int waitTime = coeff * angle * 10;
						
		pilot.arcForward(coeff * 400);
		waitTime = 2200;
		syncWait(waitTime);
		pilot.stop();
	}
	
	
	
	public void stop() {
		pilot.stop();
	}
	
	public void syncWait(int ms){
		waitProvider.syncWait(ms);
	}


	public void backwardUpdateCoef(boolean add) {
		if(add){
			coeffCorrectionBackward = coeffCorrectionBackward + 0.01f;
		}
		else if(coeffCorrectionBackward > 0.1){
			coeffCorrectionBackward = coeffCorrectionBackward - 0.01f;
		}
	}


	public void forwardUpdateCoef(boolean add) {
		if(add){
			coeffCorrectionForward = coeffCorrectionForward + 0.01f;
		}
		else if(coeffCorrectionForward > 0.1){
			coeffCorrectionForward = coeffCorrectionForward - 0.01f;
		}
	}


	public void turnHereUpdateCoef(boolean add) {
		// TODO Auto-generated method stub
		
	}


	public void turnSmoothForwardUpdateCoef(boolean add) {
		// TODO Auto-generated method stub
		
	}


	public void turnSmoothBackwardUpdateCoef(boolean add) {
		// TODO Auto-generated method stub
		
	}
}
