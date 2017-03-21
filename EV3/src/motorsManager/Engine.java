package motorsManager;

import aiPlanner.Main;
import eventManager.EventHandler;
import interfaces.EngineUpdateListener;
import interfaces.WaitProvider;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class Engine implements EngineUpdateListener{
	
	private Wheel					leftWheel				= null;
	private Wheel					rightWheel				= null;
	private EV3LargeRegulatedMotor	leftMotor				= null;
	private EV3LargeRegulatedMotor	rightMotor				= null;
	private Chassis					chassis					= null;
	private MovePilot				pilot					= null;
	private float					coeffCorrectionBackward = 1;
	private float					coeffCorrectionForward 	= 1;
	private WaitProvider			waitProvider 			= null;
	
	private final float 			right_wheel_correction 	= -0.2f; // positif ou negatif...
	
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
		pilot.setMinRadius(0);
		
		this.waitProvider = marvin;
		
		Main.printf("[ENGINE]                : Initialized");
	}
	
	public MovePilot getPilot(){
		return pilot;
	}


	//DISTANCE EN MM
	public void goForward(float distance, float speed){
		int waitTime = (int) ((distance * 1000f/speed) * coeffCorrectionForward);
		pilot.setLinearSpeed(speed);
		pilot.forward();
		syncWait(waitTime);
		pilot.stop();
	}
	
	//DISTANCE EN MM
	public void goBackward(float distance, float speed){
		int waitTime = (int) (((distance * 1000f)/speed) * coeffCorrectionBackward);
		pilot.setLinearSpeed(speed);
		pilot.backward();
		syncWait(waitTime);
		pilot.stop();
	}
	
	public void turnHere(int angle, int speed){
		//angle = normalize(angle);
		pilot.setAngularSpeed(speed);
		pilot.rotate(angle);
	}
	
	// a calibrer...
	public void turnSmoothForward(int angle){
		pilot.setLinearSpeed(Main.CRUISE_SPEED);
		pilot.arc(55, 90);
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
