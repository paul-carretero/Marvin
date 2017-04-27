package itemManager;

import java.util.List;
import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.PoseListener;
import interfaces.ServerListener;
import lejos.robotics.navigation.Pose;
import shared.Item;
import shared.ItemType;
import shared.IntPoint;
import shared.Color;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * gestionnaire de la mastermap des items du terrain.<br/>
 * D�finit et permet d'acc�der aux Item de type ME (le robot) ou Palet.<br/>
 * Les op�rations de recherche ou d'insertion dans la map doivent �tre r�alis�es de mani�re atomique (donc synchronized)
 * @see Item
 * @author paul.carretero
 */
public class EyeOfMarvin implements ServerListener, ItemGiver, PoseListener {

	/**
	 * Map principace regroupant les diff�rent Item du terrain.
	 * Cette carte est mise � jour par le serveur et est utilis�e pour r�cup�rer la position des items.
	 */
	private final List<Item>			masterList;
	
	/**
	 * Pr�cision de la carte (permet d'arrondir les position obtenues)
	 * Permet �galement de minimiser le bruit dans la reception des donn�es
	 */
	private	static final int			MAP_PRECISION 	= 30; // on arrondi au multiple de MAP_PRECISION
	
	/**
	 * repr�sente une distance infinie
	 */
	private static final int			OUT_OF_RANGE	= 9999;
	
	/**
	 * Dur�e minimum pour qu'un item n'ayant pas boug� soit consid�r� comme un palet
	 */
	private static final int			MIN_LIFE		= 1500;
	
	/**
	 * Distance minium en bordure des lignes blanches o� l'on ne cherchera pas de palet a attraper
	 */
	private static final int			MIN_PALET_MARGE	= 100;
	
	/**
	 * Distance minium du robot en dessous de laquelle on ne recherche pas de palet
	 */
	private static final int			MIN_PALET_DIST	= 500;
	
	/**
	 * marge de tol�rance lors de la recherche d'un item
	 */
	private static final int			MAX_SEARCH		= 150;
	
	/**
	 * Derni�re pose connue du robot
	 */
	private volatile Pose myPose;
	
	/**
	 * Cr�er une nouvelle instance du gestionnaire de la mastermap des items du terrain
	 */
	public EyeOfMarvin() {
		
		this.masterList 	= new ArrayList<Item>();
		this.myPose 		= new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL);
		
		Main.printf("[EYE OF MARVIN]         : Initialized");
	}
	
	/********************************************************
	 * Fonctions utilitaires de calibration
	 *******************************************************/
	
	/**
	 * Calcul la diff�rences moyennes des coordonn�es fournies par le serveur avec les coordonn�es connues de d�part des palets.<br/>
	 * D�finit les �carts moyen en X et en Y directement � la reception serveur afin d'obtenir une carte plus pr�cise des palets.
	 */
	public void calibrateSensor(){
		int sensorMarge = 200;
		int xTotal = 0;
		int yTotal = 0;
		int total = 0;
		
		for(IntPoint pFix : Main.INITIAL_PALETS){
			IntPoint pSensor = getNearestItem(pFix);
			if(pSensor != null && pFix.getDistance(pSensor) < sensorMarge){
				xTotal += pFix.x() - pSensor.x();
				yTotal += pFix.y() - pSensor.y();
				total++;
			}
			else{
				Main.printf("[EYE OF MARVIN]         : Error, no item in the expected position : " + pFix);
			}
		}
		
		IntPoint meStart = new IntPoint(Main.X_INITIAL,Main.Y_INITIAL);
		IntPoint pSensor = getNearestItem(meStart);
		if(pSensor != null && meStart.getDistance(pSensor) < sensorMarge){
			xTotal += meStart.x() - pSensor.x();
			yTotal += meStart.y() - pSensor.y();
			total++;
		}
		else{
			Main.printf("[EYE OF MARVIN]         : Error, no Robot in the expected position : " + meStart);
		}
		
		if(total > 0){
			Server.defineOffset(xTotal/total, yTotal/total);
		}
		else{
			Main.printf("[EYE OF MARVIN]         : Le serveur n'est probablement pas lanc�...");
		}
		
	}
	
	/**
	 * Arrondi la postion d'un Item, sans mettre � jour son temps de r�f�rence
	 * @param i un Item a arrondir
	 */
	private static void averagize(final Item i){
		i.silentUpdate((i.x()/MAP_PRECISION)*MAP_PRECISION,(i.y()/MAP_PRECISION)*MAP_PRECISION);
	}
	
	/**
	 * Arrondi la postion d'un Intpoint
	 * @param i un Intpoint a arrondir
	 */
	private static void averagize(final IntPoint i){
		i.update((i.x()/MAP_PRECISION)*MAP_PRECISION,(i.y()/MAP_PRECISION)*MAP_PRECISION);
	}
	
	/********************************************************
	 * Mise a jour de la carte avec le donnees serveurs
	 *******************************************************/
	
	/**
	 * Ajoute un nouvel item dans la mastermap ou met � jour le temps de r�f�rence de celui ci ainsi que son type (si d�finit).
	 * @param item un item a ajouter dans la mastermap
	 */
	synchronized private void putInMasterList(final Item item){
		averagize(item);
		int index = this.masterList.indexOf(item);
		if(index != -1){
			this.masterList.get(index).updateTimeStamp(item.getReferenceTime());
			if(item.getType() != ItemType.UNDEFINED){
				this.masterList.get(index).setType(item.getType());
			}
		}
		else{
			this.masterList.add(item);
		}
	}
	
	/**
	 * supprime les r�f�rences obsol�tes de la mastermap
	 * @param timeout temps en dessous duquel on supprime les r�f�rences de la map
	 */
	synchronized private void cleanHashMap(final int timeout) {
		for(Iterator<Item> it = this.masterList.iterator(); it.hasNext(); ) {
			Item entry = it.next();
			if(entry.getReferenceTime() < timeout){
				it.remove();
			}
			else if(entry.getLifeTime() > MIN_LIFE){
				entry.setType(ItemType.PALET);
			}
		}
	}
	
	synchronized public void receiveRawPoints(final int timeout, final List<Item> PointsList) {
			for(Item tp : PointsList){
				putInMasterList(tp);
			}
			cleanHashMap(timeout);
	}
	
	/********************************************************
	 * Primitive pour la recherche d'item
	 *******************************************************/
	
	synchronized public Item getNearestpalet() {
		IntPoint myIntPose = new IntPoint(this.myPose);
		int distance = OUT_OF_RANGE;
		Item res = null;
		
		for (Item entry : this.masterList){
			if((entry.getDistance(myIntPose) < distance) 
					&& entry.getType() == ItemType.PALET
					&& entry.y() > (Main.Y_BOTTOM_WHITE + MIN_PALET_MARGE)
					&& entry.y() < (Main.Y_TOP_WHITE - MIN_PALET_MARGE)
					&& entry.getDistance(myIntPose) > MIN_PALET_DIST){
				res = entry;
				distance = entry.getDistance(myIntPose);
			}
		}
		return res;
	}
	
	synchronized public Item getNearestItem(final IntPoint searchPoint) {
		
		int distance = OUT_OF_RANGE;
		Item res = null;
		
		for (Item entry : this.masterList){
			if(searchPoint != null && (entry.getDistance(searchPoint) < distance)){
				res			= entry;
				distance	= entry.getDistance(searchPoint);
			}
		}
		return res;
	}
	
	synchronized public Item getMarvinPosition(){
		Main.printf("[EYE OF MARVIN]                          :" + this.myPose);
		Main.poseRealToSensor(this.myPose);
		IntPoint myPosition = new IntPoint(this.myPose);
		averagize(myPosition);
		
		int distance = OUT_OF_RANGE;
		int myIndex = -1;
		int testDist = OUT_OF_RANGE;
		
			for(int i = 0; i < this.masterList.size(); i++){
				testDist = this.masterList.get(i).getDistance(myPosition);
				if(testDist < distance){
					myIndex = i;
					distance = testDist;
				}
			}

			if(myIndex > -1){
				this.masterList.get(myIndex).setType(ItemType.ME);
				return this.masterList.get(myIndex);
			}
		return null;
	}
	
	synchronized public boolean checkpalet(final IntPoint position){
		return this.masterList.indexOf(position) != -1;
	}
	
	synchronized public boolean canPlayAgain() {
		
		for(Item item : this.masterList){
			if(item.getType() == ItemType.PALET 
					&& item.y() > (Main.Y_BOTTOM_WHITE + MIN_PALET_MARGE)
					&& item.y() < (Main.Y_TOP_WHITE - MIN_PALET_MARGE) 
			){
				return true;
			}
		}
		return false;
	}
	
	/********************************************************
	 * Aide a la detection d'ennemies
	 *******************************************************/
	
	/**
	 * @param type Type d'un item a compter
	 * @return le nombre d'item dans la mastermap ayant le type type.
	 */
	synchronized private int count(final ItemType type){
		int res = 0;
		for (Item item : this.masterList){
			if(item.getType() == type){
				res++;
			}
		}
		return res;
	}
	
	/**
	 * @return la position d'un ennemy �ventuel ou null si plusieurs ennemie possible ou non trouv�
	 */
	synchronized public Item getPossibleEnnemy(){
		if(count(ItemType.UNDEFINED) == 1){
			for (Item item : this.masterList){
				if(item.getType() == ItemType.UNDEFINED){
					return item;
				}
			}
		}
		return null;
	}
	
	/********************************************************
	 * Aide a la recalibration
	 *******************************************************/

	synchronized public List<IntPoint> searchPosition(final Color color){
		List<IntPoint> resList = new ArrayList<IntPoint>();
		
		if(color == Color.YELLOW || color == Color.RED){
			// x fix�
			int x = 0;
			
			if(color == Color.YELLOW){
				x = Main.X_YELLOW_LINE;
			}
			else{
				x = Main.X_RED_LINE;
			}
			
			for (Item entry : this.masterList){
				if(entry.y() < Main.Y_TOP_WHITE && entry.y() > Main.Y_BOTTOM_WHITE && Main.areApproximatelyEqual(entry.x(), x, MAX_SEARCH)){
					resList.add(entry);
				}
			}
			
		}
		else if(color == Color.BLUE || color == Color.GREEN){
			
			int y = 0;
			
			if(color == Color.BLUE){
				y = Main.Y_BLUE_LINE;
			}
			else{
				y = Main.Y_GREEN_LINE;
			}
			
			for (Item entry : this.masterList){
				if(Main.areApproximatelyEqual(entry.y(), y, MAX_SEARCH)){
					resList.add(entry);
				}
			}
		}
		return resList;
	}
	
	synchronized public List<IntPoint> searchPosition(final IntPoint start, final int minRange, final int maxRange){
		List<IntPoint> resList = new ArrayList<IntPoint>();
		for (Item entry : this.masterList){
			if(start.getDistance(entry) < maxRange && start.getDistance(entry) > minRange){
				resList.add(entry);
			}
		}
		return resList;
	}
	
	/********************************************************
	 * Divers
	 *******************************************************/
	
	/**
	 * Affiche la mastermap
	 */
	@SuppressWarnings("unused")
	synchronized private void printMasterList(){
		Main.printf("--------------------------------" + this.masterList.size() + "Elements");
		for(Item item : this.masterList) {
			Main.printf("[EYE OF MARVIN]         : " + item.toString());
		}
		Main.printf("------------------------------------------------------");
	}

	public void setPose(Pose p) {
		this.myPose = p;
	}

}
