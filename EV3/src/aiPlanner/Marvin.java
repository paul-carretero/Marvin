package aiPlanner;

import eventManager.EventHandler;
import interfaces.ModeListener;
import interfaces.SignalListener;
import interfaces.WaitProvider;
import itemManager.EyeOfMarvin;
import itemManager.Server;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import motorsManager.Engine;
import motorsManager.GraberManager;
import positionManager.AreaManager;
import positionManager.PositionCalculator;
import shared.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Deque;

public class Marvin implements SignalListener, WaitProvider{
	
	private Deque<Goal> 		goals;
	private EyeOfMarvin 		itemManager;
	private EventHandler 		eventManager;
	private PositionCalculator 	positionManager;
	private Mode 				currentMode = Mode.PASSIVE;
	private Server 				server;
	private GraberManager 		graber;
	private Engine 				engine;
	private TimedPose 			currentPose;
	private AreaManager			areaManager;
	private List<ModeListener>	modeListener;
	
	public Marvin(int mode){
		
		Main.printf("Not that anyone cares what I say, but the restaurant is at the *other* end of the Universe.");
		
		/**********************************************************/
		
		Main.setState(Main.HAS_MOVED, false);
		Main.setState(Main.CALIBRATED, true);
		Main.setState(Main.HAND_OPEN, false);
		Main.setState(Main.HAVE_PALET, false);
		
		modeListener = new ArrayList<ModeListener>();
		
		/**********************************************************/
		
		GoalFactory GFactory = new GoalFactory(this);
		
		goals 			= GFactory.initializeStartGoals();
		
		/**********************************************************/
		currentMode 	= Mode.PASSIVE;
		System.out.println(" _____/_o_\\_____");
		eventManager 	= new EventHandler();
		engine 			= new Engine(eventManager,this);
		System.out.println("(==(/_______\\)==)");
		graber 			= new GraberManager();
		positionManager = new PositionCalculator();
		System.out.println(" \\==\\/     \\/==/");
		itemManager 	= new EyeOfMarvin(positionManager, eventManager);
		System.out.println("_________________");
		areaManager		= new AreaManager(positionManager);
		server 			= new Server(itemManager);

		eventManager.addSignalListener(this);
		eventManager.addSignalListener(positionManager);
		
		modeListener.add(eventManager);
		modeListener.add(areaManager);
		modeListener.add(graber);
		modeListener.add(positionManager);
		
		positionManager.addOdometryPoseProvider(engine.getPilot());
		System.out.println("MARVIN : STAND-BY");
		eventManager.start();
		positionManager.start();
		graber.start();
		server.start();
		areaManager.start();
		
		System.out.println(" AWAITING ORDERS");
		Button.ENTER.waitForPressAndRelease();
		Sound.beep();
		Main.TIMER.resetTimer();
		/**********************************************************/
				
		while(!goals.isEmpty() && !(Main.TIMER.getElapsedMin() > 5)){
			goals.pop().startWrapper();
		}
		syncWait(1000);
		updateMode(Mode.END);
		syncWait(1000);
		cleanUp();
	}

	protected void updateMode(Mode m) {
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
	
	synchronized public void signal(SignalType e) {
		Main.printf("[MARVIN]                : Signal received : " + e.toString());
		switch (e) {
		case LOST:
			if(noTypeOfGoal("GoalRecalibrate")){
				goals.push(new GoalRecalibrate(this));
			}
			break;
		case PRESSION_PUSHED:
			synchronized(this){
				notify();
			}
			break;
		case PRESSION_RELEASED:
			break;
		case STOP:
			synchronized(this){
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

	
	synchronized protected void cleanUp(){
		goals.clear();
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
		goals.push(g);
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
}
