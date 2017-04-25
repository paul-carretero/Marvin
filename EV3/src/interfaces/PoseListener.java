package interfaces;

import lejos.robotics.navigation.Pose;

/**
 * permet de mettre � jour divers objet necessitant la pose du robot
 */
public interface PoseListener {
	
	/**
	 * @param p la derni�re pose du robot
	 */
	public void setPose(Pose p);
	
}
