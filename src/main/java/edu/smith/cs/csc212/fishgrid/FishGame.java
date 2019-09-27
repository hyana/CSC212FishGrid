package edu.smith.cs.csc212.fishgrid;

import java.awt.Color;
import java.lang.reflect.Array;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


import me.jjfoley.gfx.IntPoint;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 *
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;
	/**
	 * These are fish we've found!
	 */
	List<Fish> found;
	/**
	 * These are fish we've found!
	 */
	List<Fish> sentHome;
	/**
	 * These are the fast fish!
	 */
	List<Fish> fastScaredFish;
	/**
	 * These are the normal fish!
	 */
	List<Fish> normalFish;
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	/**
	 * Score!
	 */
	int score;
	/**
	 * Total number of Fish!
	 */
	final int totalFish;
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);
		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		sentHome = new ArrayList<Fish>();
		fastScaredFish = new ArrayList<Fish>();
		normalFish = new ArrayList<Fish>();
		totalFish = Fish.COLORS.length - 1;
		// Add a home!
		home = world.insertFishHome();
				
		final int num = 15;
		
				
		for (int i=0; i < num; i++) {
			world.insertRockRandomly();
		}
			world.insertSnailRandomly();
		
		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
		} 

	}
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * This method is how the Main app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	
	public void findHome() {
		// question
		if (player.getPosition().equals(this.home.getPosition())) {
			for (Fish f : found){
					sentHome.add(f);
					world.remove(f);
				} found = new ArrayList<Fish>();
			}
		int size = missing.size();
		for (int i = 0; i < size; i++){
			Fish fish = missing.get(i);
			if (fish.getPosition().equals(this.home.getPosition())){
				missing.remove(fish);
				sentHome.add(fish);
				world.remove(fish);
				size--;
			}
		}
	}	
		
	public boolean gameOver() {
		return missing.isEmpty() && sentHome.size() == totalFish;
	}


	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				// Remove this fish from the missing list.
				missing.remove(wo);
				found.add((Fish) wo);
				// change wo to fish variable
				Fish color = (Fish) wo;
				if (color.getColor().equals(Color.green) || color.getColor().equals(Color.yellow)) {
					score += 10;
					}
				// Increase score when you find a fish!
				score += 10;
			}
		} if (stepsTaken % 20 == 0) {
			for (int j = found.size() - 1; j > 0; j--) {
				Fish boredFish = found.get(j);
				found.remove(boredFish);
				missing.add(boredFish);
				}
			}
	
		// Make sure missing fish *do* something.
		wanderMissingFish();
		findHome();
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (int i = 1; i < missing.size() - 1; i++) {
			if (normalFish.size() + fastScaredFish.size() <= totalFish) {
				normalFish.add(missing.get(i - 1));
				fastScaredFish.add(missing.get(i + 1));
		}
		
		for (Fish fsf : fastScaredFish) {
			if(rand.nextDouble() < 0.8) {
				fsf.moveRandomly();
			} if (fsf.getPosition() == home.getPosition()) {
				missing.remove(fsf);
				sentHome.add(fsf);
				world.remove(fsf);
			}
		} for (Fish norm : normalFish) {
			if(rand.nextDouble() < 0.2) {
				norm.moveRandomly();
			} if (norm.getPosition() == home.getPosition()) {
				missing.remove(norm);
				sentHome.add(norm);
				world.remove(norm);
				}
			}
		}
	}


	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	
	public void click(int x, int y) {		
		List<WorldObject> atPoint = world.find(x, y);
		for(WorldObject wo : atPoint) {
			// instanceof => only detect object from Rock class
			if (world.canSwim(player, x, y) == false && wo instanceof Rock) {
				world.remove((Rock) wo);
				}
			}
	}
	
}