package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Symbolise une division du terrain en fonction des lignes en X (jaune ou rouge).
 * Propose une implémentation de Area afin de mettre à jour la position
 */
public class XArea extends Area {
	
	/**
	 * X de la ligne associée a cette Area
	 */
	private final int	xLine;
	
	/**
	 * Initialise cette Area avec la couleur d'une ligne en x (jaune ou rouge)
	 * @param color la couleur d'une ligne en x, soit rouge, soit jaune
	 * @param am AreaManager permettant d'obtenir une position et un objectif de position du robot
	 */
	public XArea(final Color color, final AreaManager am) {
		super(color,am);
		
		if(color == Color.YELLOW){
			this.xLine = Main.X_YELLOW_LINE;
		}
		else{
			this.xLine = Main.X_RED_LINE;
		}
		
		this.isConsistent = false;
	}

	@Override
	public void updateAreaWithPosition(final Pose p) {		
		this.smallerThan = (p.getX() < this.xLine);
		this.isConsistent = (Math.abs(p.getX() - this.xLine) > Area.MIN_ERREUR);
	}
	
	/**
	 * fonction de mise à jour de la pose p en fonction des donnees sur la position du robot par rapport à la lignes
	 * @param p une pose a mettre à jour
	 */
	private void actualUpdate(final Pose p){
		if(this.isConsistent){
			if((this.smallerThan && p.getX() > this.xLine) || (!this.smallerThan && p.getX() < this.xLine)){
				
				Main.printf("[AREA MANAGER]          : [color = "+this.lineColor+"] mise à jour de la pose, ancienne : " + p);
				
				float newX = p.getX() * (1f-PERCENT) + this.xLine * PERCENT;
				p.setLocation(newX, p.getY());
				
				Main.printf("[AREA MANAGER]          : [color = "+this.lineColor+"] mise à jour de la pose, nouvelle : " + p);
			}
		}
	}

	/**
	 * Met à jour si les coordonnees sont coherentes<br/>
	 * si le robot ne se trouve pas dans les zone d'enbut (il n'y a pas de ligne ici)<br/>
	 * ou si il se trouve dans les zone d'enbut mais parfaitement parralèle et loin de la ligne
	 */
	@Override
	public void updatePose(final Pose p) {
		if(Math.abs(p.getX() - this.xLine) < MAX_ERREUR){
			this.isConsistent = false;
		}
		else if((p.getY() > Main.Y_BOTTOM_WHITE && p.getY() < Main.Y_TOP_WHITE)){
			actualUpdate(p);
		}
		else if(Math.abs(p.getHeading()) < (90 + AMBIGUOUS_ANGLE) && Math.abs(p.getHeading()) > (90 - AMBIGUOUS_ANGLE) && Math.abs(p.getX() - this.xLine) > MAX_ERREUR ){
			actualUpdate(p);
		}
		else{
			this.isConsistent = false;
		}
	}
	
	@Override
	protected boolean checkConsistantAngle(final float h){
		return Math.abs(h) > (90 + AMBIGUOUS_ANGLE) || Math.abs(h) < (90 - AMBIGUOUS_ANGLE);
	}

	@Override
	protected boolean checkColorValidity(final float distance) {
		Pose myPose = this.am.getLastPose();
		float xInit = myPose.getX();
		
		// si on est proche au départ de cette ligne
		if(Math.abs(myPose.getX() - this.xLine) < MAX_ERREUR){
			return true;
		}

		// si la position de départ est incohérente avec cette ligne
		if((this.smallerThan && myPose.getX() > this.xLine) || (!this.smallerThan && myPose.getX() < this.xLine)){
			this.isConsistent = false;
			return false;
		}
		
		float fixDistance = distance + MAX_ERREUR;
		myPose.moveUpdate(fixDistance);
		
		// si on ne devrait pas avoir franchi cette ligne
		if((this.smallerThan && xInit < this.xLine && myPose.getX() < this.xLine) || (!this.smallerThan && xInit > this.xLine && myPose.getX() > this.xLine)){
			this.isConsistent = false;
			return false;
		}
		
		// si on devrait avoir franchi cette ligne alors OKs
		if((this.smallerThan && xInit < this.xLine && myPose.getX() > this.xLine) || (!this.smallerThan && xInit > this.xLine && myPose.getX() < this.xLine)){
			return true;
		}
		
		return false;
	}

}
