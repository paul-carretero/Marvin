package aiPlanner;

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
		preConditions.add(Main.STARTED);
		preConditions.add(Main.CALIBRATED);
	}

	@Override
	public void start() {
		Main.printf("This is the sort of thing you lifeforms enjoy, is it?");
		ia.pushGoal(new GoalGrab(ia, 30000));
	}

	@Override
	public void preConditionsFailHandler() {
		ia.cleanUp();
	}
	
	@Override
	protected String getName() {
		return NAME;
	}

}
