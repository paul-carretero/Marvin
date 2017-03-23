package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;

public class GoalRecalibrate extends Goal {
	
	protected final String NAME = "GoalRecalibrate";

	public GoalRecalibrate(GoalFactory gf, Marvin ia, int timeout) {
		super(gf, ia, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineDefault() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		Main.printf("Sounds awful.");
		
	}
	
	@Override
	public String getName() {
		return NAME;
	}

}
