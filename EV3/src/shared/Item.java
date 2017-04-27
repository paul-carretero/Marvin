package shared;

import aiPlanner.Main;

/**
 * représente un objet sur le plateau, cet objet est représenté par son type, son emplacement et sa durée de vie
 * @see IntPoint
 * @author paul.carretero
 */
public class Item extends IntPoint{
	
	/**
	 * Représente le type de l'item (UNDEFINED, ME ou PALET)
	 * @see ItemType
	 */
	private ItemType	type;
	
	/**
	 * représente le moment où l'objet à été "trouvé"
	 */
	private final int 	createdTime;
	
	
	/**
	 * représente le moment où l'objet à été mis à jour ou (ou confirmé)
	 */
	private int 		referenceTime;

	/**
	 * @param x la position de l'item sur l'axe x
	 * @param y la position de l'item sur l'axe y
	 * @param currentTime le moment de référence ou l'item a été trouvé
	 * @param t le type de l'objet
	 */
	public Item(final int x, final int y, final int currentTime, final ItemType t) {
		super(x, y);
		this.type 			= t;
		this.createdTime	= currentTime;
		this.referenceTime	= currentTime;
	}
	
	/**
	 * @return retourne le type de l'item
	 */
	public ItemType getType() {
		return this.type;
	}
	
	/**
	 * @param t défini le type de l'item
	 */
	public void setType(final ItemType t){
		this.type			= t;
	}

	@Override
	public String toString(){
		return "Item = [" + this.type.toString() + "] + POS : [" + this.x +"," + this.y + "]";
	}
	
	/**
	 * @param x la nouvelle coordonné de l'item sur l'axe x
	 * @param y la nouvelle coordonné de l'item sur l'axe y
	 * @param currentTime le nouveau temps de référence de l'item (pas de sa création)
	 */
	public void update(final int x, final int y, final int currentTime){
		this.x				= x;
		this.y				= y;
		this.referenceTime	= currentTime;
	}
	
	@Override
	public void update(final int x, final int y){
		this.x				= x;
		this.y				= y;
		this.referenceTime	= Main.TIMER.getElapsedMs();
	}
	
	/*@Override
	public boolean equals(final Object  o){
		if (!(o instanceof IntPoint)) {
	        return false;
	    }
		Item p = (Item)o;
		return (y() == p.y() && x() == p.x()) && this.type == p.getType() && this.referenceTime == p.getReferenceTime();
	}*/
	
	/**
	 * @param x la nouvelle coordonné de l'item sur l'axe x
	 * @param y la nouvelle coordonné de l'item sur l'axe y
	 */
	public void silentUpdate(final int x, final int y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * @param currentTime le nouveau temps de référence de l'item (pas de sa création)
	 */
	public void updateTimeStamp(final int currentTime) {
		this.referenceTime	= currentTime;
	}

	/**
	 * @return le temps de référence de l'item
	 */
	public int getReferenceTime() {
		return this.referenceTime;
	}
	
	/**
	 * @return la durée de vie de l'item (de sa création jusqu'a la dernière confirmation).
	 */
	public int getLifeTime(){
		return this.referenceTime - this.createdTime;
	}
}
