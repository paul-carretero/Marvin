package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;

public class GoalGrabAndDropPalet extends Goal {
	
	protected final String NAME = "GoalGrabAndDropPalet";

	public GoalGrabAndDropPalet(GoalFactory gf,  Marvin ia, int timeout) {
		super(gf, ia, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineDefault() {
		preConditions.add(Main.HAND_OPEN);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return NAME;
	}

}
