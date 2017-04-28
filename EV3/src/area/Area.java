package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Regroupe les informations correspondant à la position du robot par rapport à une ligne de couleur
 * @author paul.carretero, florent.chastagner
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
	protected final static int 	AMBIGUOUS_ANGLE	= 20;
	
	/**
	 * Marge d'erreur en dessous de laquelle ne mettre pas à jour
	 */
	protected static final int 	MIN_ERREUR		= 100;
	
	/**
	 * Marge d'erreur au dessus de laquelle on préfèrera se fier à la position du gestionnaire de position (erreur du capteur)
	 * et ou on ne mettra donc pas à jour
	 * Les couleurs plantent souvent, très peu fiable...
	 */
	protected static final int	MAX_ERREUR		= 250;
	
	/**
	 * Taux de correction de la pose
	 * Les couleurs plantent souvent, peu fiable...
	 * 30% max en tout en safe
	 */
	protected static final float PERCENT		= 0.15f;
	
	/**
	 * AreaManager permettant d'obtenir une position et un objectif de position du robot
	 */
	protected final AreaManager am;
	
	/**
	 * @param color couleur de la ligne associée
	 * @param am AreaManager permettant d'obtenir une position et un objectif de position du robot
	 */
	public Area(final Color color,final AreaManager am){
		this.lineColor	= color;
		this.am			= am;
	}
	
	@Override
	public String toString(){
		return "A"+this.lineColor + " && < = " + this.smallerThan + " && valid = " + this.isConsistent;
	}
	
	/**
	 * @param currentColor la couleur que l'on vient de détecter
	 * @param heading la direction du robot
	 * @param distance distance que le robot parcoure en avant
	 */
	public void colorChange(final Color currentColor, final float heading, final float distance){
		if(currentColor == this.lineColor && this.isConsistent){
			this.isConsistent = this.isConsistent && checkConsistantAngle(heading) && checkColorValidity(distance);
			if(this.isConsistent){
				this.smallerThan = !this.smallerThan;
			}
			Main.printf("[AREA MANAGER]          : color = "+ currentColor + " && " + this.toString());
		}
	}
	
	/**
	 * met a jour les donnees de l'area avec la pose actuelle
	 * @param p la position actuelle du robot.
	 * @param force force la mise à jour en fonction de la pose, si faux, ne met à jour que si inconsistance
	 */
	public void updateAreaWithPosition(final Pose p, final boolean force){
		if(force || !this.isConsistent){
			updateAreaWithPosition(p);
			Main.printf("[AREA MANAGER]          : " + this.toString());
		}
	}

	/**
	 * @param p la pose du robot sur laquelle mettre à jour
	 */
	protected abstract void updateAreaWithPosition(final Pose p);

	/**
	 * met à jour la pose en fonction des données de cette area
	 * @param p une pose
	 */
	public abstract void updatePose(final Pose p);
	
	/**
	 * Vérifie si l'angle est succeptible d'entrainer un doute lorsque l'on rencontre une ligne en X OU en y (selon l'area)
	 * @param h la pose du robot
	 * @return vrai si l'angle permet de définir convenablement la direction du robot
	 */
	protected abstract boolean checkConsistantAngle(final float h);
	
	/**
	 * @param distance une distance que le robot est en train de parcourir en avant
	 * @return vrai si il est théoriquement possible de rencontrer la couleur, faux sinon
	 */
	protected abstract boolean checkColorValidity(final float distance);

}
