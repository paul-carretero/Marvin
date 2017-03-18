package area;

import lejos.robotics.navigation.Pose;
import shared.Point;

public class DefaultArea extends Area {

	public DefaultArea(int id) {
		super(id);
	}

	@Override
	public int getConsistency(Point p) {
		return -1;
	}

	@Override
	public Area colorChange(int Color, Pose p) {
		return this;
	}

}
