package interfaces;

import java.util.List;

import shared.Item;

public interface ItemGiver {
	public Item getNearestPallet();
	public List<Item> getItemsList();
}
