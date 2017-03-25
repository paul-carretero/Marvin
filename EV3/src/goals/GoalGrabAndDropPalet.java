package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.ItemGiver;
import lejos.robotics.geometry.Point;
import shared.IntPoint;

public class GoalGrabAndDropPalet extends Goal {
	
	protected 	final String NAME = "GoalGrabAndDropPalet";
	private 	ItemGiver eom;

	public GoalGrabAndDropPalet(GoalFactory gf,  Marvin ia, int timeout, ItemGiver eom) {
		super(gf, ia, timeout);
		this.eom = eom;
	}

	@Override
	protected void defineDefault() {
		preConditions.add(Main.HAND_OPEN);
	}

	@Override
	public void start() {
		IntPoint palet = eom.getNearestPallet();
		Main.printf("point trouve : " + palet);
		if(palet != null){
			ia.pushGoal(gf.goalDrop(timeout));
			ia.pushGoal(gf.goalGrab(timeout, palet.toLejosPoint()));
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

}
