package interfaces;

import lejos.robotics.navigation.Pose;

/**
 * Interface impl�ment� par un gestionnaire de position permettant au autre objet d'obtenir la position courrante 
 * et d'informer le gestionnaire de position de changement d'�tat int�ressant.
 */
public interface PoseGiver {
	/**
	 * @return une Pose repr�sentant l'�tat (position + direction) du robot au moment de l'appel.
	 */
	public Pose getPosition();
	
	/**
	 * Met � jour la position en consid�rant un X d�tect� de mani�re certaine.
	 * @param x une coordonn�e x s�re.
	 */
	public void sendFixX(int x);
	
	/**
	 * Met � jour la position en consid�rant un X d�tect� de mani�re certaine.
	 * @param y une coordonn�e y s�re.
	 */
	public void sendFixY(int y);
	
	/**
	 * @return l'ID de l'aire courante bas� sur le gestionnaire d'Area
	 */
	public int getAreaId();
	
	/**
	 * permet � un autre objet de d�finir la Pose courrante.
	 * @param p une nouvelle Pose.
	 * @see Pose
	 */
	public void setPose(Pose p);
}
