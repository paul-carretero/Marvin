package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Point;

public class CenterArea extends Area {

	public CenterArea(int id) {
		super(id);
	}

	@Override
	public int getConsistency(Point p) {
		if(
				p.x() < (Main.X_RED_LINE + MARGE_ERREUR) && 
				p.x() > (Main.X_YELLOW_LINE - MARGE_ERREUR) &&
				p.y() < (Main.Y_GREEN_LINE + MARGE_ERREUR) &&
				p.y() > (Main.Y_BLUE_LINE - MARGE_ERREUR))
		{
			return 0;
		}
		else{
			return Math.max(
					Math.max( (p.x() - Main.X_RED_LINE) , (Main.X_YELLOW_LINE - p.x())),
					Math.max( (p.y() - Main.Y_GREEN_LINE) , (Main.Y_BLUE_LINE - p.y()))
			);
		}
	}

	@Override
	public Area colorChange(int Color, Pose p) {
		switch (Color) {
		case Main.COLOR_GREY:
			return this;
		case Main.COLOR_BLACK:
			// ici on soustrait bien la marge d'erreur au lieu de la rajouté, afin d'éviter les cas limites
			if(p.getX() < (Main.X_RED_LINE - MARGE_ERREUR) && p.getX() > (Main.X_YELLOW_LINE + MARGE_ERREUR) && 
				p.getY() < (Main.Y_GREEN_LINE - MARGE_ERREUR) && p.getY() > (Main.Y_BLUE_LINE + MARGE_ERREUR)){
				return this;
			}
			else{
				return Main.getArea(15);
			}
		case Main.COLOR_RED:
			if( Math.abs(p.getHeading()) < (90 - AMBIGUOUS_ANGLE) || Math.abs(p.getHeading()) > (90 + AMBIGUOUS_ANGLE) ){
				if(p.getY() < (Main.Y_GREEN_LINE) && p.getY() > (Main.Y_BLACK_LINE)){
					return Main.getArea(9);
				}
				if(p.getY() < (Main.Y_BLACK_LINE) && p.getY() > (Main.Y_BLUE_LINE)){
					return Main.getArea(7);
				}
			}
		case Main.COLOR_GREEN:
			if( Math.abs(p.getHeading()) > (0 + AMBIGUOUS_ANGLE) && Math.abs(p.getHeading()) < (180 - AMBIGUOUS_ANGLE) ){
				if(p.getX() < (Main.X_RED_LINE) && p.getX() > (Main.X_BLACK_LINE)){
					return Main.getArea(12);
				}
				if(p.getX() < (Main.X_BLACK_LINE) && p.getX() > (Main.X_YELLOW_LINE)){
					return Main.getArea(11);
				}
			}
		case Main.COLOR_BLUE:
			if( Math.abs(p.getHeading()) > (0 + AMBIGUOUS_ANGLE) && Math.abs(p.getHeading()) < (180 - AMBIGUOUS_ANGLE) ){
				if(p.getX() < (Main.X_RED_LINE) && p.getX() > (Main.X_BLACK_LINE)){
					return Main.getArea(3);
				}
				if(p.getX() < (Main.X_BLACK_LINE) && p.getX() > (Main.X_YELLOW_LINE)){
					return Main.getArea(2);
				}
			}
		case Main.COLOR_YELLOW:
			if( Math.abs(p.getHeading()) > (90 - AMBIGUOUS_ANGLE) && Math.abs(p.getHeading()) < (90 + AMBIGUOUS_ANGLE) ){
				if(p.getY() < (Main.Y_GREEN_LINE) && p.getY() > (Main.Y_BLACK_LINE)){
					return Main.getArea(8);
				}
				if(p.getY() < (Main.Y_BLACK_LINE) && p.getY() > (Main.Y_BLUE_LINE)){
					return Main.getArea(5);
				}
			}
		default:
			return Main.getArea(15);
		}
		
	}

}
