package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Symbolise une division du terrain en fonction des lignes en X (bleue ou vertes).
 * Propose une implémentation de Area afin de mettre à jour la position
 */
public class YArea extends Area {

	/**
	 * Y de la ligne associée a cette Area
	 */
	private final int	yLine;

	/**
	 * Initialise cette Area avec la couleur d'une ligne en x (jaune ou rouge)
	 * @param color la couleur d'une ligne en y, soit bleu, soit verte
	 */
	public YArea(Color color) {
		super(color);
		
		if(color == Color.BLUE){
			this.yLine = Main.Y_BLUE_LINE;
		}
		else{
			this.yLine = Main.Y_GREEN_LINE;
		}
		
		this.isConsistent = false;
	}

	@Override
	public void updateAreaWithPosition(Pose p) {
		this.smallerThan = (p.getY() < this.yLine);
		this.isConsistent = (Math.abs(p.getY() - this.yLine) > Area.MIN_ERREUR);
	}

	@Override
	public void updatePose(Pose p) {
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

}
