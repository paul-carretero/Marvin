package shared;

import aiPlanner.Main;

/**
 * repr�sente un objet sur le plateau, cet objet est repr�sent� par son type, son emplacement et sa dur�e de vie
 * @see IntPoint
 * @author paul.carretero
 */
public class Item extends IntPoint{
	
	/**
	 * Repr�sente le type de l'item (UNDEFINED, ME ou PALET)
	 * @see ItemType
	 */
	private ItemType	type;
	
	/**
	 * repr�sente le moment o� l'objet � �t� "trouv�"
	 */
	private final int 	createdTime;
	
	
	/**
	 * repr�sente le moment o� l'objet � �t� mis � jour ou (ou confirm�)
	 */
	private int 		referenceTime;

	/**
	 * @param x la position de l'item sur l'axe x
	 * @param y la position de l'item sur l'axe y
	 * @param currentTime le moment de r�f�rence ou l'item a �t� trouv�
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
	 * @param t d�fini le type de l'item
	 */
	public void setType(final ItemType t){
		this.type			= t;
	}

	@Override
	public String toString(){
		return "Item = [" + this.type.toString() + "] + POS : [" + this.x +"," + this.y + "]";
	}
	
	/**
	 * @param x la nouvelle coordonn� de l'item sur l'axe x
	 * @param y la nouvelle coordonn� de l'item sur l'axe y
	 * @param currentTime le nouveau temps de r�f�rence de l'item (pas de sa cr�ation)
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
	 * @param x la nouvelle coordonn� de l'item sur l'axe x
	 * @param y la nouvelle coordonn� de l'item sur l'axe y
	 */
	public void silentUpdate(final int x, final int y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * @param currentTime le nouveau temps de r�f�rence de l'item (pas de sa cr�ation)
	 */
	public void updateTimeStamp(final int currentTime) {
		this.referenceTime	= currentTime;
	}

	/**
	 * @return le temps de r�f�rence de l'item
	 */
	public int getReferenceTime() {
		return this.referenceTime;
	}
	
	/**
	 * @return la dur�e de vie de l'item (de sa cr�ation jusqu'a la derni�re confirmation).
	 */
	public int getLifeTime(){
		return this.referenceTime - this.createdTime;
	}
}
