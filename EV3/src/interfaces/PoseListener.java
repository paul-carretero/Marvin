package interfaces;

import lejos.robotics.navigation.Pose;

/**
 * permet de mettre à jour divers objets necessitant la pose du robot
 * @author paul.carretero, florent.chastagner
 */
public interface PoseListener {
	
	/**
	 * @param p la dernière pose du robot
	 */
	public void setPose(Pose p);
	
}
