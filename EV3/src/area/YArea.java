package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Symbolise une division du terrain en fonction des lignes en X (bleue ou vertes).
 * Propose une implémentation de Area afin de mettre à jour la position
 * Deprecated car très peu précise du fait des nombreux faux-positif...
 * @author paul.carretero
 */
public class YArea extends Area {

	/**
	 * Y de la ligne associée a cette Area
	 */
	private final int	yLine;

	/**
	 * Initialise cette Area avec la couleur d'une ligne en x (jaune ou rouge)
	 * @param color la couleur d'une ligne en y, soit bleu, soit verte
	 * @param am AreaManager permettant d'obtenir une position et un objectif de position du robot
	 */
	public YArea(final Color color, final AreaManager am) {
		super(color, am);
		
		if(color == Color.BLUE){
			this.yLine = Main.Y_BLUE_LINE;
		}
		else{
			this.yLine = Main.Y_GREEN_LINE;
		}
		
		this.isConsistent = false;
	}

	@Override
	public void updateAreaWithPosition(final Pose p) {
		this.smallerThan = (p.getY() < this.yLine);
		this.isConsistent = (Math.abs(p.getY() - this.yLine) > Area.MIN_ERREUR);
	}

	@Override
	public void updatePose(final Pose p) {
		if(Math.abs(p.getY() - this.yLine) < MAX_ERREUR){
			if(this.isConsistent){
				if((this.smallerThan && p.getY() > this.yLine) || (!this.smallerThan && p.getY() < this.yLine)){
					float newY = p.getY() * (1f-PERCENT) + this.yLine * PERCENT;
					p.setLocation(p.getX(),newY);
				}
			}
		}
		else{
			this.isConsistent = false;
		}
	}
	

	@Override
	protected boolean checkConsistantAngle(final float h){
		return (Math.abs(h) > (0 + AMBIGUOUS_ANGLE)) && (Math.abs(h) < (180 - AMBIGUOUS_ANGLE));
	}
	
	@Override
	protected boolean checkColorValidity(float distance) {
		Pose myPose = this.am.getLastPose();
		float yInit = myPose.getY();
		
		// si on est proche au départ de cette ligne
		if(Math.abs(myPose.getY() - this.yLine) < MAX_ERREUR){
			return true;
		}

		// si la position de départ est incohérente avec cette ligne
		if((this.smallerThan && myPose.getY() > this.yLine) || (!this.smallerThan && myPose.getY() < this.yLine)){
			this.isConsistent = false;
			return false;
		}
		
		float fixDistance = distance + MAX_ERREUR;
		myPose.moveUpdate(fixDistance);
		
		// si on ne devrait pas avoir franchi cette ligne
		if((this.smallerThan && yInit < this.yLine && myPose.getY() < this.yLine) || (!this.smallerThan && yInit > this.yLine && myPose.getY() > this.yLine)){
			this.isConsistent = false;
			return false;
		}
		
		// si on devrait avoir franchi cette ligne alors OKs
		if((this.smallerThan && yInit < this.yLine && myPose.getY() > this.yLine) || (!this.smallerThan && yInit > this.yLine && myPose.getY() < this.yLine)){
			return true;
		}
		
		return false;
	}

}
