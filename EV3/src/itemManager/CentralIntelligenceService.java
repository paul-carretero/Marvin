package itemManager;

import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.ModeListener;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import shared.IntPoint;
import shared.Item;
import shared.Mode;

public class CentralIntelligenceService extends Thread implements ModeListener{
	private ItemGiver			eom;
	private PoseGiver 			pg;
	private IntPoint			interceptionTarget;
	private Item				firstContact;
	private Item				confirmationContact;
	private Mode				mode;
	private volatile Object		notifyOnUptate;
	private static final int	SIGNIFICATIVE_DISTANCE = 70; // 7cm
	
	public CentralIntelligenceService(ItemGiver eom, PoseGiver pg){
		this.eom 					= eom;
		this.pg						= pg;
		this.interceptionTarget		= null;
		this.firstContact 			= null;
		this.confirmationContact	= null;
		this.notifyOnUptate			= null;
	}
	
	@Override
	public void run(){
		this.setPriority(MIN_PRIORITY);
		while(this.mode != Mode.END && !isInterrupted()){
			
			// si on a pas de premier contact alors on tente d'en repérer un.
			if(firstContact == null){
				updateInterceptionTarget(null);
				firstContact = eom.getPossibleEnnemy();
			}
			else{
				confirmationContact = eom.getPossibleEnnemy();
				
				// si le contact n'a pas bougé ou si il a disparu, on reset
				if(confirmationContact != firstContact && confirmationContact != null){
					
					// on vérifie que la distance est significative pour pouvoir extrapoler à une droite
					if(firstContact.getDistance(confirmationContact) > SIGNIFICATIVE_DISTANCE){
						IntPoint intersection = confirmationContact.getIntersection(firstContact.computeVector(confirmationContact));
						// on vérifie que l'ennemy avance vers l'intersection et que l'intersection est bien dans le terrain
						if(intersection.x() > 0 && intersection.x() < 3000 && confirmationContact.getDistance(intersection) < firstContact.getDistance(intersection)){
							updateInterceptionTarget(intersection);
						}
					}
				}
				
				// on réinitialise firstcontact à confirmationcontact (si il change de trajectoire ou autre)
				firstContact = confirmationContact;
				confirmationContact = null;
			}
			Main.printf("Ennemy target = " + interceptionTarget);
			syncWait();
		}
	}
	
	public void addEnnemyMoveNotifier(Object o){
		notifyOnUptate = o;
	}
	
	public void resetEnnemyMoveNotifier(){
		this.notifyOnUptate = null;
	}
	
	private void updateInterceptionTarget(IntPoint target){
		if(target != null){
			if(!target.equals(interceptionTarget)){
				interceptionTarget = target;
				if(notifyOnUptate != null){
					synchronized (notifyOnUptate) {
						notifyOnUptate.notify();
					}
				}
			}
			Main.printf("ennemy = " + firstContact.toString());
		}
		else{
			interceptionTarget = null;
		}
	}
	
	private void syncWait(){
		synchronized (this) {
			try {
				this.wait(800);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public Point getInterceptionLocation(){
		Point ennemy = eom.getPossibleEnnemy().toLejosPoint();
		if(interceptionTarget != null && ennemy != null){
			if(pg.getPosition().distanceTo(interceptionTarget.toLejosPoint()) < ennemy.distance(interceptionTarget.toLejosPoint())){
				return interceptionTarget.toLejosPoint();
			}
		}
		return null;
	}

	public void setMode(Mode m) {
		this.mode = m;
	}
}
