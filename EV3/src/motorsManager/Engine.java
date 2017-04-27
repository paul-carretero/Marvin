package motorsManager;

import aiPlanner.Main;
import interfaces.WaitProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MovePilot;

/**
 * Fournit des primitives pour les mouvements (marche avant/arri�re et rotation) du robot.
 * Sa base principalement sur les primitives LeJos existantes.
 * @author paul.carretero
 */
public class Engine{
	
	/**
	 * Marge d'erreur maximum d'errur lors de l'attente
	 */
	private static final int MARGE_MS = 150;
	/**
	 * Roue gauche du robot
	 */
	private Wheel					leftWheel;
	/**
	 * roue droite du robor
	 */
	private Wheel					rightWheel;
	/**
	 * Servomoteur gauche
	 */
	private EV3LargeRegulatedMotor	leftMotor;
	/**
	 * Servomoteur droite
	 */
	private EV3LargeRegulatedMotor	rightMotor;
	/**
	 * pilot LeJos du robot, repr�sente le chassis et la structure des roues
	 */
	private MovePilot				pilot;
	/**
	 * Moniteur sur lequelle attendre lors des d�placement
	 */
	private WaitProvider			waitProvider;
	
	/**
	 * Permet de stabiliser la trajectoire en fonction des irr�gularit�s de sym�trie des roues
	 */
	private static final float 		RIGHT_WHEEL_CORRECTION 	= -0.25f; // positif ou negatif...
	
	/**
	 * Cr�er un nouvel Engine (un seul en tout sinon erreur).
	 * @param marvin l'ia qui g�re les attentes (et les interruptions donc).
	 */
	public Engine(WaitProvider marvin){
		this.leftMotor	= new EV3LargeRegulatedMotor(LocalEV3.get().getPort(Main.LEFT_WHEEL));
		this.rightMotor	= new EV3LargeRegulatedMotor(LocalEV3.get().getPort(Main.RIGHT_WHEEL));
		
		updateWheelOffset();
		
		Chassis chassis	= new WheeledChassis(new Wheel[]{this.leftWheel, this.rightWheel},  WheeledChassis.TYPE_DIFFERENTIAL);
		
		chassis.setSpeed(Main.CRUISE_SPEED, Main.ROTATION_SPEED);
		chassis.setAcceleration(Main.LINEAR_ACCELERATION, Main.LINEAR_ACCELERATION);
		
		this.pilot		= new MovePilot(chassis);
		
		this.pilot.setLinearAcceleration(Main.LINEAR_ACCELERATION);
		this.pilot.setAngularSpeed(Main.ROTATION_SPEED);
		this.pilot.setMinRadius(0);
		
		this.waitProvider = marvin;
		
		Main.printf("[ENGINE]                : Initialized");
	}
	
	/**
	 * Permet de modifier les donn�es utilis�es par l'odom�tre en fonction de la pr�sence ou non d'un palet.
	 * Si le robot a grab un palet alors il tournera l�g�rement moins.
	 * Corriger la distance des roues par rapport au centre permet de corriger ce probl�me
	 */
	public final void updateWheelOffset(){
		if(Main.HAVE_PALET){
			this.leftWheel	= WheeledChassis.modelWheel(this.leftMotor, Main.WHEEL_DIAMETER).offset(-1*Main.DISTANCE_TO_CENTER_P);
			this.rightWheel	= WheeledChassis.modelWheel(this.rightMotor, Main.WHEEL_DIAMETER + RIGHT_WHEEL_CORRECTION).offset(Main.DISTANCE_TO_CENTER_P);
		}
		else{
			this.leftWheel	= WheeledChassis.modelWheel(this.leftMotor, Main.WHEEL_DIAMETER).offset(-1*Main.DISTANCE_TO_CENTER);
			this.rightWheel	= WheeledChassis.modelWheel(this.rightMotor, Main.WHEEL_DIAMETER + RIGHT_WHEEL_CORRECTION).offset(Main.DISTANCE_TO_CENTER);
		}
	}
	
	/**
	 * @return le pilot utilis� pour g�rer les d�placement
	 */
	public MovePilot getPilot(){
		return this.pilot;
	}

	/**
	 * @param ml un MoveListener qui sera informer a chaque d�but et fin de d�placement
	 */
	public void addMoveListener(MoveListener ml){
		this.pilot.addMoveListener(ml);
	}

	/**
	 * Fait rouler le robot en avant sur une distance d�finie
	 * @param distance distance � parcourir en millim�tre
	 * @param speed vitesse a utiliser pour ce parcours
	 * @return vrai si l'on a attendu pendant toute la duree, faux sinon
	 */
	public boolean goForward(float distance, float speed){
		int waitTime = (int) (distance * 1000f/speed);
		this.pilot.setLinearSpeed(speed);
		this.pilot.forward();
		boolean res = syncWait(waitTime);
		this.pilot.stop();
		return res;
	}
	
	/**
	 * Fait rouler le robot en arri�re sur une distance d�finie
	 * @param distance distance � parcourir en millim�tre
	 * @param speed vitesse a utiliser pour ce parcours
	 * @return vrai si l'on a attendu pendant toute la duree, faux sinon
	 */
	public boolean goBackward(float distance, float speed){
		final int waitTime = (int) ((distance * 1000f)/speed);
		this.pilot.setLinearSpeed(speed);
		this.pilot.backward();
		boolean res = syncWait(waitTime);
		this.pilot.stop();
		return res;
	}
	
	/**
	 * @param f le robot effectura une rotation de Angle (positif ou n�gatif)
	 * @param speed vitesse de la rotation
	 */
	public void turnHere(float f, int speed){
		this.pilot.setAngularSpeed(speed);
		this.pilot.rotate(f);
	}
	
	/**
	 * Arr�te les moteurs imm�diatement
	 */
	public void stop() {
		this.pilot.stop();
	}
	
	/**
	 * Demande au moniteur d'attente d'attendre pendant une dur�e d�termin� (�ventuellemnt interruptible).
	 * @param ms une dur�e en ms pendant laquelle attendre
	 * @return vrai si l'on a attendu pendant toute la duree specifie, faux sinon
	 */
	public boolean syncWait(int ms){
		
		int start = Main.TIMER.getElapsedMs();
		this.waitProvider.syncWait(ms);
		int end = Main.TIMER.getElapsedMs();
		
		System.out.println();
		
		return Main.areApproximatelyEqual(end - start, ms, MARGE_MS);
	}
}
