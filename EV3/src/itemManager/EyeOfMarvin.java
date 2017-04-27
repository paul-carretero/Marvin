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
 * Définit et permet d'accéder aux Item de type ME (le robot) ou Palet.<br/>
 * Les opérations de recherche ou d'insertion dans la map doivent être réalisées de manière atomique (donc synchronized)
 * @see Item
 * @author paul.carretero
 */
public class EyeOfMarvin implements ServerListener, ItemGiver, PoseListener {

	/**
	 * Map principace regroupant les différent Item du terrain.
	 * Cette carte est mise à jour par le serveur et est utilisée pour récupérer la position des items.
	 */
	private final List<Item>			masterList;
	
	/**
	 * Précision de la carte (permet d'arrondir les position obtenues)
	 * Permet également de minimiser le bruit dans la reception des données
	 */
	private	static final int			MAP_PRECISION 	= 30; // on arrondi au multiple de MAP_PRECISION
	
	/**
	 * représente une distance infinie
	 */
	private static final int			OUT_OF_RANGE	= 9999;
	
	/**
	 * Durée minimum pour qu'un item n'ayant pas bougé soit considéré comme un palet
	 */
	private static final int			MIN_LIFE		= 1500;
	
	/**
	 * Distance minium en bordure des lignes blanches où l'on ne cherchera pas de palet a attraper
	 */
	private static final int			MIN_PALET_MARGE	= 100;
	
	/**
	 * Distance minium du robot en dessous de laquelle on ne recherche pas de palet
	 */
	private static final int			MIN_PALET_DIST	= 500;
	
	/**
	 * marge de tolérance lors de la recherche d'un item
	 */
	private static final int			MAX_SEARCH		= 150;
	
	/**
	 * Dernière pose connue du robot
	 */
	private volatile Pose myPose;
	
	/**
	 * Créer une nouvelle instance du gestionnaire de la mastermap des items du terrain
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
	 * Calcul la différences moyennes des coordonnées fournies par le serveur avec les coordonnées connues de départ des palets.<br/>
	 * Définit les écarts moyen en X et en Y directement à la reception serveur afin d'obtenir une carte plus précise des palets.
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
			Main.printf("[EYE OF MARVIN]         : Le serveur n'est probablement pas lancé...");
		}
		
	}
	
	/**
	 * Arrondi la postion d'un Item, sans mettre à jour son temps de référence
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
	 * Ajoute un nouvel item dans la mastermap ou met à jour le temps de référence de celui ci ainsi que son type (si définit).
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
	 * supprime les références obsolètes de la mastermap
	 * @param timeout temps en dessous duquel on supprime les références de la map
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
	 * @return la position d'un ennemy éventuel ou null si plusieurs ennemie possible ou non trouvé
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
			// x fixé
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
