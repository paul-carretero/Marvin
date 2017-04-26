package area;

import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Regroupe les informations correspondant à la position du robot par rapport à une ligne de couleur
 */
public abstract class Area {

	/**
	 * couleur de la ligne associée
	 */
	protected final Color 		lineColor;
	
	/**
	 * vrai si les données sont utilisable, faux sinon
	 */
	protected boolean			isConsistent;
	
	/**
	 * vrai si la robot possède une position en x plus petite que cette ligne, faux sinon
	 */
	protected boolean			smallerThan;
	
	/**
	 * Angle par rapport à une ligne de couleur en dessous duquel on ne peut pas considérer être fiable pour un changement d'Area
	 */
	protected final static int 	AMBIGUOUS_ANGLE	= 10;
	
	/**
	 * Marge d'erreur en dessous de laquelle ne mettre pas à jour
	 */
	protected static final int 	MIN_ERREUR		= 50;
	
	/**
	 * Marge d'erreur au dessus de laquelle on préfèrera se fier à la position du gestionnaire de position (erreur du capteur)
	 * et ou on ne mettra donc pas à jour
	 */
	protected static final int	MAX_ERREUR		= 400;
	
	/**
	 * Taux de correction de la pose
	 */
	protected static final float PERCENT		= 0.2f;
	
	/**
	 * @param color couleur de la ligne associée
	 */
	public Area(final Color color){
		this.lineColor = color;
	}
	
	@Override
	public String toString(){
		return "A"+this.lineColor + " @ " + this.smallerThan;
	}
	
	/**
	 * @param currentColor la couleur que l'on vient de détecter
	 * @param heading la direction du robot
	 */
	public void colorChange(Color currentColor, float heading){
		if(currentColor == this.lineColor){
			this.smallerThan = !this.smallerThan;
			this.isConsistent = checkConsistantAngle(heading);
		}
	}
	
	/**
	 * met a jour les donnees de l'area avec la pose actuelle
	 * @param p la position actuelle du robot.
	 * @param force force la mise à jour en fonction de la pose, si faux, ne met à jour que si inconsistance
	 */
	public void updateAreaWithPosition(final Pose p, boolean force){
		if(force || !this.isConsistent){
			updateAreaWithPosition(p);
		}
	}

	/**
	 * @param p la pose du robot sur laquelle mettre à jour
	 */
	protected abstract void updateAreaWithPosition(Pose p);

	/**
	 * met à jour la pose en fonction des données de cette area
	 * @param p une pose
	 */
	public abstract void updatePose(Pose p);
	
	/**
	 * Vérifie si l'angle est succeptible d'entrainer un doute lorsque l'on rencontre une ligne en X OU en y (selon l'area)
	 * @param h la pose du robot
	 * @return vrai si l'angle permet de définir convenablement la direction du robot
	 */
	protected abstract boolean checkConsistantAngle(final float h);

}
