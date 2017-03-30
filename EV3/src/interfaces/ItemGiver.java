package interfaces;

import shared.Item;
import shared.IntPoint;

public interface ItemGiver {
	public Item getNearestPallet();
	public Item getMarvinPosition();
	public Item getPossibleEnnemy();
	public Item getNearestItem(IntPoint searchPoint);
	public boolean checkPallet(IntPoint position);
}
