package shared;

public class Item extends TimedPoint{
	
	private ItemType type;
	private int probability;

	public Item(int x, int y, int currentTime, ItemType t) {
		super(x, y, currentTime);
		type = t;
		probability = 100;
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
	
	public int getProbability(){
		return probability;
	}
	
	public void setProbability(int p){
		this.probability = Math.min(100, Math.max(0, p));
	}
}
