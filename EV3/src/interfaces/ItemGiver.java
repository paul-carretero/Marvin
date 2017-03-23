package interfaces;

import shared.Item;
import shared.IntPoint;

public interface ItemGiver {
	public Item getNearestPallet();
	public Item getMarvinPosition();
	public boolean checkPallet(IntPoint position);
}
