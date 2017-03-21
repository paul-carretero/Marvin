package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;

public class GoalPlay extends Goal {
	
	protected final String NAME = "GoalPlay";

	public GoalPlay(Marvin ia, int timeout) {
		super(ia, timeout);
	}
	
	public GoalPlay(Marvin ia) {
		super(ia, 360000); // 6 min;
	}

	@Override
	protected void defineDefault() {
		preConditions.add(Main.CALIBRATED);
		preConditions.add(Main.HAND_OPEN);
	}

	@Override
	public void start() {
		Main.printf("This is the sort of thing you lifeforms enjoy, is it?");
	}

	@Override
	public void preConditionsFailHandler() {
		ia.cleanUp();
	}
	
	@Override
	public String getName() {
		return NAME;
	}

}
