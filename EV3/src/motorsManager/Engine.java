package motorsManager;

import aiPlanner.Main;
import eventManager.EventHandler;
import interfaces.WaitProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class Engine{
	
	private Wheel					leftWheel;
	private Wheel					rightWheel;
	private EV3LargeRegulatedMotor	leftMotor;
	private EV3LargeRegulatedMotor	rightMotor;
	private Chassis					chassis;
	private MovePilot				pilot;
	private WaitProvider			waitProvider;
	
	private static final float 		RIGHT_WHEEL_CORRECTION 	= -0.25f; // positif ou negatif...
	
	public Engine(EventHandler eventManager, WaitProvider marvin){
		this.leftMotor	= new EV3LargeRegulatedMotor(LocalEV3.get().getPort(Main.LEFT_WHEEL));
		this.rightMotor	= new EV3LargeRegulatedMotor(LocalEV3.get().getPort(Main.RIGHT_WHEEL));
		this.leftWheel	= WheeledChassis.modelWheel(this.leftMotor, Main.WHEEL_DIAMETER).offset(-1*Main.DISTANCE_TO_CENTER);
		this.rightWheel	= WheeledChassis.modelWheel(this.rightMotor, Main.WHEEL_DIAMETER + RIGHT_WHEEL_CORRECTION).offset(Main.DISTANCE_TO_CENTER);
		
		this.chassis	= new WheeledChassis(new Wheel[]{this.leftWheel, this.rightWheel},  WheeledChassis.TYPE_DIFFERENTIAL);
		this.chassis.setSpeed(Main.CRUISE_SPEED, Main.ROTATION_SPEED);
		this.chassis.setAcceleration(Main.LINEAR_ACCELERATION, Main.LINEAR_ACCELERATION);
		
		this.pilot		= new MovePilot(this.chassis);
		this.pilot.addMoveListener(eventManager);
		this.pilot.setLinearAcceleration(Main.LINEAR_ACCELERATION);
		this.pilot.setAngularSpeed(Main.ROTATION_SPEED);
		this.pilot.setMinRadius(0);
		
		this.waitProvider = marvin;
		
		Main.printf("[ENGINE]                : Initialized");
	}
	
	public MovePilot getPilot(){
		return this.pilot;
	}


	//DISTANCE EN MM
	public void goForward(float distance, float speed){
		int waitTime = (int) (distance * 1000f/speed);
		this.pilot.setLinearSpeed(speed);
		this.pilot.forward();
		syncWait(waitTime);
		this.pilot.stop();
	}
	
	//DISTANCE EN MM
	public void goBackward(float distance, float speed){
		int waitTime = (int) ((distance * 1000f)/speed);
		this.pilot.setLinearSpeed(speed);
		this.pilot.backward();
		syncWait(waitTime);
		this.pilot.stop();
	}
	
	public void turnHere(int angle, int speed){
		//angle = normalize(angle);
		this.pilot.setAngularSpeed(speed);
		this.pilot.rotate(angle);
	}
	
	public void stop() {
		this.pilot.stop();
	}
	
	public void syncWait(int ms){
		this.waitProvider.syncWait(ms);
	}
}
