package shared;

public class Item extends TimedPoint{
	
	private ItemType type;

	public Item(int x, int y, int currentTime, ItemType t) {
		super(x, y, currentTime);
		type = t;
	}
	
	public Item(int x, int y, int currentTime) {
		super(x, y, currentTime);
		type = ItemType.UNDEFINED;
	}
	
	public ItemType getType() {
		return type;
	}
	
	public void setType(ItemType t){
		this.type = t;
	}

	public String toString(){
		return "Item = [" + type.toString() + "] + POS : [" + x +"," + y + "] @ " + getReferenceTime();
	}
}
