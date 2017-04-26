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
	 */
	public XArea(Color color) {
		super(color);
		
		if(color == Color.YELLOW){
			this.xLine = Main.X_YELLOW_LINE;
		}
		else{
			this.xLine = Main.X_RED_LINE;
		}
		
		this.isConsistent = false;
	}

	@Override
	public void updateAreaWithPosition(Pose p) {		
		this.smallerThan = (p.getX() < this.xLine);
		this.isConsistent = (Math.abs(p.getX() - this.xLine) > Area.MIN_ERREUR);
	}

	@Override
	public void updatePose(Pose p) {
		if(Math.abs(p.getX() - this.xLine) < MAX_ERREUR){
			if(this.isConsistent){
				if((this.smallerThan && p.getX() > this.xLine) || (!this.smallerThan && p.getX() < this.xLine)){
					float newX = p.getX() * (1f-PERCENT) + this.xLine * PERCENT;
					p.setLocation(newX, p.getY());
				}
			}
		}
		else{
			this.isConsistent = false;
		}
	}
	
	@Override
	protected boolean checkConsistantAngle(final float h){
		return Math.abs(h) > (90 + AMBIGUOUS_ANGLE) || Math.abs(h) < (90 - AMBIGUOUS_ANGLE);
	}

}
