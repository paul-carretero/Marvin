package shared;

public class Item extends TimedPoint{
	
	private ItemType	type;
	private int			probability;

	public Item(int x, int y, int currentTime, ItemType t) {
		super(x, y, currentTime);
		this.type 			= t;
		this.probability 	= 100;
	}
	
	public Item(int x, int y, int currentTime) {
		super(x, y, currentTime);
		this.type			= ItemType.UNDEFINED;
	}
	
	public ItemType getType() {
		return this.type;
	}
	
	public void setType(ItemType t){
		this.type			= t;
	}

	@Override
	public String toString(){
		return "Item = [" + this.type.toString() + "] + POS : [" + this.x +"," + this.y + "] @ " + getReferenceTime();
	}
	
	public int getProbability(){
		return this.probability;
	}
	
	public void setProbability(int p){
		this.probability	= Math.min(100, Math.max(0, p));
	}
}
