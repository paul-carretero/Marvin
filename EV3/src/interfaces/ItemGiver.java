package interfaces;

import shared.Item;

import java.util.List;

import shared.IntPoint;

public interface ItemGiver {
	public Item getNearestpalet();
	public Item getMarvinPosition();
	public Item getPossibleEnnemy();
	public Item getNearestItem(IntPoint searchPoint);
	public boolean checkpalet(IntPoint position);
	public List<IntPoint> searchPosition(int range);
	public List<IntPoint> searchPosition(IntPoint startPoint, int minRange, int maxRange);
}
