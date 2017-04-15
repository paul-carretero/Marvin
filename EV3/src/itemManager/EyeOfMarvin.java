package itemManager;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import interfaces.ServerListener;
import lejos.robotics.navigation.Pose;
import shared.Item;
import shared.ItemType;
import shared.IntPoint;

import shared.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * gestionnaire de la mastermap des items du terrain.
 * Définit et permet d'accéder aux Item de type ME (le robot) ou Palet.
 * @see Item
 */
public class EyeOfMarvin implements ServerListener, ItemGiver {
	
	/**
	 * Un poseGiver permettant d'obtenir la position du robot (entre autre)
	 */
	private final PoseGiver 			poseGiver;
	/**
	 * Map principace regroupant les différent Item du terrain.
	 * Cette carte est mise à jour par le serveur et est utilisée pour récupérer la position des items.
	 */
	private final Map<IntPoint,Item>	masterMap;
	
	/**
	 * Précision de la carte (permet d'arrondir les position obtenues)
	 * Permet également de minimiser le bruit dans la reception des données
	 */
	private	static final int			MAP_PRECISION 	= 40; // on arrondi au multiple de MAP_PRECISION
	
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
	 * marge de tolérance lors de la recherche d'un item
	 */
	private static final int			MAX_SEARCH		= 100;
	
	/**
	 * Créer une nouvelle instance du gestionnaire de la mastermap des items du terrain
	 * @param pg Un poseGiver permettant d'obtenir la position du robot (entre autre)
	 */
	public EyeOfMarvin(PoseGiver pg) {
		
		this.poseGiver	= pg;
		this.masterMap 	= new HashMap<IntPoint,Item>();
		
		Main.printf("[EYE OF MARVIN]         : Initialized");
	}
	
	/**
	 * Calcul la différences moyennes des coordonnées fournies par le serveur avec les coordonnées connues de départ des palets.
	 * Définit les écart moyen en X et en Y directement à la reception serveur afin d'obtenir une carte plus précise des palet.
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
	private static void averagize(Item i){
		i.silentUpdate((i.x()/MAP_PRECISION)*MAP_PRECISION,(i.y()/MAP_PRECISION)*MAP_PRECISION);
	}
	
	/**
	 * Arrondi la postion d'un Intpoint
	 * @param i un Intpoint a arrondir
	 */
	private static void averagize(IntPoint i){
		i.update((i.x()/MAP_PRECISION)*MAP_PRECISION,(i.y()/MAP_PRECISION)*MAP_PRECISION);
	}
	
	/**
	 * Ajoute un nouvel item dans la mastermap ou met à jour le temps de référence de celui ci ainsi que son type (si définit).
	 * @param i un item a ajouter dans la mastermap
	 */
	synchronized private void putInHashMap(Item i){
		averagize(i);
		IntPoint key = new IntPoint(i.x(), i.y());
		if(this.masterMap.containsKey(key)){
			this.masterMap.get(key).updateTimeStamp(i.getReferenceTime());
			if(i.getType() != ItemType.UNDEFINED){
				this.masterMap.get(key).setType(i.getType());
			}
		}
		else{
			this.masterMap.put(key,i);
		}
	}
	
	/**
	 * Définit un unique item comme le robot si un item est suffisament proche de la position théorique de celui ci.
	 */
	synchronized private void defineMe(){
		Pose p = this.poseGiver.getPosition();
		Main.poseRealToSensor(p);
		IntPoint myPosition = new IntPoint(p);
		averagize(myPosition);
		
		if(this.masterMap.containsKey(myPosition)){
			this.masterMap.get(myPosition).setType(ItemType.ME);
		}
		else{
			int distance = OUT_OF_RANGE;
			IntPoint posOnMap = null;
			for (Entry<IntPoint, Item> entry : this.masterMap.entrySet()){
				if(entry.getValue().getDistance(myPosition) < distance){
					posOnMap = entry.getValue();
					distance = entry.getValue().getDistance(myPosition);
				}
			}
			
			if(posOnMap != null && this.masterMap.get(posOnMap) != null && this.masterMap.get(posOnMap).getDistance(myPosition) < MAX_SEARCH){
				this.masterMap.get(posOnMap).setType(ItemType.ME);
			}
		}
	}
	
	/**
	 * supprime les référence obsolète de la mastermap
	 * @param timeout temps au dessous duquel on supprime les référence de la map
	 */
	synchronized private void cleanHashMap(int timeout) {
		for(Iterator<Map.Entry<IntPoint, Item>> it = this.masterMap.entrySet().iterator(); it.hasNext(); ) {
			Entry<IntPoint, Item> entry = it.next();
			if(entry.getValue().getReferenceTime() < timeout){
				it.remove();
			}
			else if(entry.getValue().getLifeTime() > MIN_LIFE){
				entry.getValue().setType(ItemType.PALET);
			}
		}
	}
	
	synchronized public void receiveRawPoints(int timeout, List<Item> PointsList) {
			for(Item tp : PointsList){
				putInHashMap(tp);
			}
			cleanHashMap(timeout);
			defineMe();
		//printHashMap(masterMap);
	}
	
	synchronized public Item getNearestpalet() {
		IntPoint myIntPose = new IntPoint(this.poseGiver.getPosition().getLocation());
		int distance = OUT_OF_RANGE;
		IntPoint res = null;
		for (Entry<IntPoint, Item> entry : this.masterMap.entrySet()){
			if((entry.getValue().getDistance(myIntPose) < distance) && entry.getValue().getType() == ItemType.PALET
					&& entry.getValue().y() > Main.Y_BOTTOM_WHITE + MIN_PALET_MARGE && entry.getValue().y() < (Main.Y_TOP_WHITE - MIN_PALET_MARGE)){
				res = entry.getKey();
				distance = entry.getValue().getDistance(myIntPose);
			}
		}
		if(res != null){
			return this.masterMap.get(res);
		}
		return null;
	}
	
	synchronized public Item getNearestItem(IntPoint searchPoint) {
		int distance = OUT_OF_RANGE;
		IntPoint res = null;
		for (Entry<IntPoint, Item> entry : this.masterMap.entrySet()){
			if(searchPoint != null){
				if((entry.getValue().getDistance(searchPoint) < distance)){
					res			= entry.getKey();
					distance	= entry.getValue().getDistance(searchPoint);
				}
			}
		}
		return this.masterMap.get(res);
	}
	
	synchronized public Item getMarvinPosition(){
		for (Entry<IntPoint, Item> entry : this.masterMap.entrySet())
		{
			if(entry.getValue().getType() == ItemType.ME){
				return entry.getValue();
			}
		}
		return null;
	}
	
	/**
	 * @param type Type d'un item a compter
	 * @return le nombre d'item dans la mastermap ayant le type type.
	 */
	synchronized private int count(ItemType type){
		int res = 0;
		for (Item item : this.masterMap.values()){
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
		if(count(ItemType.UNDEFINED) == 1 ){
			for (Item item : this.masterMap.values()){
				if(item.getType() == ItemType.UNDEFINED){
					return item;
				}
			}
		}
		return null;
	}
	
	synchronized public List<IntPoint> searchPosition(Color color){
		List<IntPoint> resList = new LinkedList<IntPoint>();
		
		if(color == Color.YELLOW || color == Color.RED){
			// x fixé
			int x = 0;
			
			if(color == Color.YELLOW){
				x = Main.X_YELLOW_LINE;
			}
			else{
				x = Main.X_RED_LINE;
			}
			
			for (IntPoint key : this.masterMap.keySet()){
				if(Main.areApproximatelyEqual(key.x(), x, MAX_SEARCH)){
					if(this.masterMap.get(key).getType() != ItemType.PALET){
						resList.add(key);
					}
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
			
			for (IntPoint key : this.masterMap.keySet()){
				if(Main.areApproximatelyEqual(key.y(), y, MAX_SEARCH)){
					if(this.masterMap.get(key).getType() != ItemType.PALET){
						resList.add(key);
					}
				}
			}
			
		}
		return resList;
	}
	
	/**
	 * Affiche la map passée en paramètre
	 * @param map une map d'item
	 */
	public static void printHashMap(Map<IntPoint,Item> map){
		Main.printf("--------------------------------" + map.size() + "Elements");
		for(Item item : map.values()) {
			Main.printf("[EYE OF MARVIN]         : " + item.toString());
		}
		Main.printf("------------------------------------------------------");
	}
	
	synchronized public boolean checkpalet(IntPoint position){
		if(this.masterMap.containsKey(position)){
			return this.masterMap.get(position).getType() == ItemType.PALET;
		}
		return false;
	}
	
	synchronized public List<IntPoint> searchPosition(IntPoint start, int minRange, int maxRange){
		List<IntPoint> resList = new LinkedList<IntPoint>();
		for (IntPoint key : this.masterMap.keySet()){
			if(start.getDistance(key) < maxRange && start.getDistance(key) > minRange && this.masterMap.get(key).getType() != ItemType.PALET ){
				resList.add(key);
			}
		}
		return resList;
	}
}
