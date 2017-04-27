package goals;

import aiPlanner.Marvin;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.IntPoint;

/**
 * Effectue une tentative de Grab sur un palet donné de manière optimiste:
 * on considère qu'on est bien positionné par rapport au palet, on avance d'abord et on corrige éventuellement après.
 * @author paul.carretero
 */
public class GoalGrabOptimist extends GoalGrabPessimist {
	
	/**
	 * Nom du Goal
	 */
	protected final GoalType	NAME = GoalType.GRAB_OPTIMISTE;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param palet une position d'un palet possible
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 * @param eom EyeOfMarvin, permet de fournir les position
	 * @param radar un DistanceGiver permettant de donner des distance radar
	 * @see Pose
	 */
	public GoalGrabOptimist(final GoalFactory gf, final Marvin ia, final Point palet, final PoseGiver pg, final ItemGiver eom, final DistanceGiver radar) {
		super(gf, ia, palet, pg, eom, radar);
	}
	
	@Override
	public void start() {
		
		if(this.eom.checkpalet(new IntPoint(this.palet))){
			correctPosition();
			grabWrapper();
			decal();
		}
		else{
			failPoseHandler();
		}
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
