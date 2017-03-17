package aiPlanner;

import shared.Point;

public class GoalGoToPosition extends Goal {
	
	protected final String NAME = "GoalGoToPosition";
	protected Point positionPoint = null;

	public GoalGoToPosition(Marvin ia, int timeout) {
		super(ia, timeout);
	}
	
	public GoalGoToPosition(Marvin ia, int timeout, Point p) {
		super(ia, timeout);
		this.positionPoint = p;
	}

	@Override
	protected void defineDefault() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
				
	}

	@Override
	public void preConditionsFailHandler() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected String getName() {
		return NAME;
	}

}
