package goals;

import aiPlanner.Marvin;

public class GoalFindEnnemy extends Goal {
	
	protected	final	String	NAME	=	"GoalFindEnnemy";

	public GoalFindEnnemy(Marvin ia, int timeout) {
		super(ia, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineDefault() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preConditionsFailHandler() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getName() {
		return NAME;
	}

}
