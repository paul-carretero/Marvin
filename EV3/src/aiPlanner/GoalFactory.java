package aiPlanner;

import java.util.ArrayDeque;
import java.util.Deque;

public class GoalFactory {
	
	private Marvin ia;
	
	public GoalFactory(Marvin ia){
		this.ia = ia;
	}
	
	public Deque<Goal> initializeStartGoals(){
		Deque<Goal> goals = new ArrayDeque<Goal>();
		//goals.push(new GoalPlay(ia));
		return goals;
	}
	
	public Goal play(int timeout){
		return new GoalPlay(ia, timeout);
	}
}
