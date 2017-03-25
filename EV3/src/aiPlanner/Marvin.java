package aiPlanner;

import eventManager.EventHandler;
import goals.Goal;
import goals.GoalFactory;
import goals.GoalRecalibrate;
import interfaces.ModeListener;
import interfaces.SignalListener;
import interfaces.WaitProvider;
import itemManager.EyeOfMarvin;
import itemManager.FakeServer;
import itemManager.Server;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.robotics.navigation.Pose;
import motorsManager.Engine;
import motorsManager.GraberManager;
import positionManager.AreaManager;
import positionManager.DirectionCalculator;
import positionManager.PositionCalculator;
import shared.Mode;
import shared.SignalType;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Deque;

public class Marvin implements SignalListener, WaitProvider{
	
	private	Deque<Goal> 		goals;
	private	EyeOfMarvin 		itemManager;
	private	EventHandler 		eventManager;
	private	PositionCalculator 	positionManager;
	private	Mode 				currentMode 		= Mode.PASSIVE;
	private	Server				server;
	private	GraberManager 		graber;
	private	Engine 				engine;
	private GoalFactory 		GFactory;
	private	AreaManager			areaManager;
	private	List<ModeListener>	modeListener;
	private SoundManager		audio;
	private boolean 			allowInterrupt 		= false;
	private DirectionCalculator	directionCalculator = null; // pour calculer angle
	private int					rotationSpeed		= Main.ROTATION_SPEED;
	private int					linearSpeed			= Main.CRUISE_SPEED;
	
	public Marvin(){
		
		
		Main.printf("Not that anyone cares what I say, but the restaurant is at the *other* end of the Universe.");
		
		/**********************************************************/
		
		Main.setState(Main.HAS_MOVED, false);
		Main.setState(Main.HAND_OPEN, true);
		Main.setState(Main.HAVE_PALET, false);
		Main.setState(Main.PRESSION, false);
		
		modeListener		= new ArrayList<ModeListener>();
		currentMode 		= Mode.PASSIVE;
		
		/**********************************************************/
		
		eventManager 		= new EventHandler(this);
		engine 				= new Engine(eventManager,this);
		graber 				= new GraberManager();
		directionCalculator = new DirectionCalculator();
		
		try {
			positionManager	= new PositionCalculator(engine.getPilot(), directionCalculator);
		} catch (Exception e) {
			Main.printf(e.getMessage());
			System.exit(1);
		}
		
		itemManager 		= new EyeOfMarvin(positionManager);
		areaManager			= new AreaManager(positionManager);
		server 				= new Server(itemManager);
		audio				= new SoundManager();

		/**********************************************************/
		
		modeListener.add(eventManager);
		modeListener.add(areaManager);
		modeListener.add(graber);
		modeListener.add(positionManager);
		
		directionCalculator.addEom(itemManager);
		
		/**********************************************************/
		
		eventManager.start();
		positionManager.start();
		graber.start();
		server.start();
		areaManager.start();
		audio.start();
		
		/**********************************************************/
		
		GFactory 				= new GoalFactory(this,positionManager, this.itemManager);
		goals 					= GFactory.initializeStartGoals();
		
		/**********************************************************/
		LocalEV3.get().getLED().setPattern(3);
		for(int i = 0; i< 5; i++){
			System.out.print("###");
			syncWait(300);
		}
		System.out.print("##");
		
		System.out.println("MARVIN : STAND-BY");
		System.out.println(" AWAITING ORDERS");
		
		LocalEV3.get().getLED().setPattern(1);
		Button.ENTER.waitForPressAndRelease();
		Sound.beep();
		Main.TIMER.resetTimer();
		
		/**********************************************************/
				
		while(!goals.isEmpty() && !(Main.TIMER.getElapsedMin() > 5)){
			goals.pop().startWrapper();
			setAllowInterrupt(false);
		}
		
		//goBackward(600);
				
		syncWait(1000);
		/**********************************************************/

		updateMode(Mode.END);
		cleanUp();
	}

	public void updateMode(Mode m) {
		if(currentMode != m){
			currentMode = m;
			Main.printf("[MARVIN]                : Mode : " + m.toString());
			for(ModeListener ml : modeListener){
				synchronized (ml) {
					ml.notify();
					ml.setMode(m);
				}	
			}
		}
	}
	
	// TODO : a refaire avec des enum une fois finalisé
	private boolean noTypeOfGoal(String name){ 
		for(Iterator<Goal> itr = goals.iterator();itr.hasNext();)  {
			if(itr.next().getName().equals(name)){
				return false;
			}
		}
		return true;
	}
	
	public void tryInterruptEngine(){
		if(allowInterrupt){
			synchronized(this){
				engine.stop();
				notify();
			}
		}
	}
	
	synchronized public void signal(SignalType e) {
		Main.printf("[MARVIN]                : Signal received : " + e.toString());
		switch (e) {
		case LOST:
			if(noTypeOfGoal("GoalRecalibrate")){
				goals.push(null);
			}
			break;
		case PRESSION_PUSHED:
			tryInterruptEngine();
			break;
		case STOP:
			synchronized(this){
				updateMode(Mode.END);
				notify();
			}
			engine.stop();
			graber.stopGrab();
			goals.clear();
			break;
		default:
			break;
		}
	}
	
	public synchronized void cleanUp(){
		LocalEV3.get().getLED().setPattern(3);
		goals.clear();
		audio.addVictoryTheme();
		syncWait(2000);
		Main.printf("[MARVIN]                : I'm just trying to die.");
		try {
			
			server.interrupt();
			server.join();
			positionManager.interrupt();
			positionManager.join();
			graber.interrupt();
			graber.join();
			eventManager.interrupt();
			eventManager.join();
			areaManager.interrupt();
			areaManager.join();
			audio.interrupt();
			audio.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		Main.printf("[MARVIN]                : I told you this would all end in tears.");
		syncWait(1000);
	}

	protected PositionCalculator getPositionManager() {
		return positionManager;
	}
	
	synchronized public void pushGoal(Goal g){
		Main.printf("[MARVIN]                : Why should I want to make anything up? Life's bad enough as it is without wanting to invent any more of it.");
		if(currentMode != Mode.END){
			goals.push(g);
		}
	}
	
	public void syncWait(int ms){
		synchronized (this) {
			try {
				this.wait(ms);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public void goForward(int distance){
		if(distance > 0 && linearSpeed > 0 && currentMode != Mode.END){
			
			directionCalculator.startLine(true);
			
			engine.goForward(distance, linearSpeed);
			
			directionCalculator.reset();
			
		}
	}
	
	public void goBackward(int distance){
		if(distance > 0 && linearSpeed > 0 && currentMode != Mode.END){
			
			positionManager.swap();
			
			for(int i = 0; i<(distance/linearSpeed); i++){
				audio.addBip();
			}
			
			engine.goBackward(distance, linearSpeed);
			
			positionManager.swap();
		}
	}
	
	public void turnHere(int angle){
		if(angle != 0 && currentMode != Mode.END){
			engine.turnHere(angle, rotationSpeed);
		}
	}

	public void open() {
		graber.open();
	}

	public void grab() {
		graber.close();
	}
	
	public void setAllowInterrupt(boolean value){
		this.allowInterrupt = value;
	}
}
