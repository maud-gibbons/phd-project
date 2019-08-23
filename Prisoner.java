package GameTheory;

import java.util.*;

public class Prisoner extends Agent implements Comparable<Prisoner>
{
	private int x, y; //position in grid
	private String strategy; //cooperator or defector
	private int fitnessScore; //gained through interactions with other prisoners
	
	private boolean defectorNear, cooperatorNear; //flags for movement function
	private ArrayList<Prisoner> nearby = new ArrayList<Prisoner>(); //prisoners in one prisoner's field of vision
	public int locations[][] = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}}; //generic grid locations in my field of vision

	public int clusterNo = 0; //Keeps track of the current cluster its in
	public int state = 0; //Only used in cluster analysis 0/1/2 waiting/in queue/finished


	public Prisoner(World world, int x, int y, int[] gene, String strategy)
	{
		super(world, gene);
		this.x=x;
		this.y=y;
		this.strategy = strategy;
		this.fitnessScore = 0;
	}

	public Prisoner(World world, int x, int y, int[] gene)
	{
		super(world, gene);
		this.x=x;
		this.y=y;
		this.fitnessScore = 0;
	}
	public Prisoner(World world, int x, int y, String strategy)
	{
		super(world);
		this.x=x;
		this.y=y;
		this.strategy = strategy;
		this.fitnessScore = 0;
	}

	public Prisoner(World world, int x, int y)
	{
		super(world);
		this.x=x;
		this.y=y;
		this.fitnessScore = 0;
	}

	public int compareTo(Prisoner p){
		int compareFitness = ((Prisoner) p).getFitnessScore();
		return this.fitnessScore - compareFitness;
	}

	public void takeTurn()
	{
		//check for other prisoners nearby and play a single round with each of those neighbours 
		scanHorizon();
		play();

		if (nearby.size() < 8)//If there is space to move
		{
			//move to a new location based on the strategy and position of your neighbours
			move(nearby);
		}
		if(nearby.size() == 0)
		{
			//if you have no neighbours take a step in a random direction 
			randomWalk();
		}
	}

	public void play(){
		//for each other prisoner in your neighbourhood play the Prisoner's Dilemma

		for(Prisoner p : nearby){
			if(this.strategy == "C"){
		 		
				if(p.strategy == "C"){
					
					//Reward for Mutual Cooperation
		 			fitnessScore += 3.0;
		 			p.fitnessScore += 3.0;
		 		}
		 		else
		 		{
		 			//The Sucker's Payoff
		 			fitnessScore += 0.0;
		 			p.fitnessScore += 5.0;
		 		}
		 	}
		 	else
		 	{
		 		if(p.strategy == "C")
		 		{
		 			//The Temptation to defect
		 			fitnessScore += 5.0;
		 			p.fitnessScore += 0.0;
		 		}
		 		else
		 		{
		 			//Punishment for Mutual Defection
		 			fitnessScore += 1.0;
		 			p.fitnessScore += 1.0;
		 		}
		 	}
		 }
	}

	//Identifying other prisoners and their strategies in the neighbourhood
	public void scanHorizon()
	{
		//rest the neighbourhood
		nearby.clear(); 
		cooperatorNear = false;
		defectorNear = false;

		for(int i[] : locations){

			//Translate the generic neighbourhood to this agent's neighbourhood
			int[] loc = new int[2];
			loc[0] = modulo(x + i[0], world.worldSizeX); //wrapping if on the grid edge
			loc[1] = modulo(y + i[1], world.worldSizeY);

			//updating the nearby list
			if (world.getAt(loc[0], loc[1]) != null){

				nearby.add(world.getAt(loc[0], loc[1]));

				//updating the C/D flags. Only one agent of either type need to be nearby
				if(cooperatorNear == false && world.getAt(loc[0], loc[1]).getStrategy().equals("C")){
					cooperatorNear = true;
				}
				
				if(defectorNear == false && world.getAt(loc[0], loc[1]).getStrategy().equals("D")){
					defectorNear = true;
				}
			}
		}
	}

	public ArrayList<Prisoner> findCooperators()
	{
		//returns the list of all cooperators in an agent's neighbourhood
		
		ArrayList<Prisoner> neighbours = new ArrayList<Prisoner>();
		for(int i[] : locations){

			int[] loc = new int[2];
			loc[0] = modulo(x + i[0], world.worldSizeX);
			loc[1] = modulo(y + i[1], world.worldSizeY);

			//check for valid locations and correct strategy
			if (world.getAt(loc[0], loc[1]) != null && world.getAt(loc[0], loc[1]).getStrategy().equals("C")){

				neighbours.add(world.getAt(loc[0], loc[1]));
			}
		}
		return neighbours;
	}

	private int[][] score(ArrayList<Prisoner> prisoners, int cellVal){
		//method to assign a value to potential movement locations
		// agent moves to the highest scoring cell
		// cellVal [-1,0,1] is determined by the gene and represents [flee, stay, follow]  
		
		int relX, relY;
		int scores[][] = {{0,0,0},{0,0,0},{0,0,0}};
		
		//Action: stay still
		if(cellVal==0){
			scores[0][0] = -1;
			scores[0][1] = -1;
			scores[0][2] = -1;
			scores[1][0] = -1;
			scores[1][1] = 1;
			scores[1][2] = -1;
			scores[2][0] = -1;
			scores[2][1] = -1;
			scores[2][2] = -1;
		}

		for(Prisoner p : prisoners){
			
			//determine the relative location of each neighbour
			relX = p.x -this.x;
			relY = p.y -this.y;

			if(relX > 1)
				relX = -1;
			if(relX < -1)
				relX = 1;

			if(relY > 1)
				relX = -1;
			if(relX < -1)
				relX = 1;

			//For each nearby prisoner, each grid position is given a cumulative score
			//cells adjacent to neighbours are incremented by cellVal [-1,1]
			if(relX == 0){
				if (relY == 1){
					scores[0][1] += cellVal;
					scores[0][2] += cellVal;
					scores[1][1] += cellVal;
					scores[1][2] += -100;
					scores[2][1] += cellVal;
					scores[2][2] += cellVal;
				}
				else if(relY == -1){
					scores[1][0] += -100;
					scores[1][1] += cellVal;
					scores[2][0] += cellVal;
					scores[2][1] += cellVal;
				}
			}
			else if (relX == 1){
				if(relY == 0){
					scores[1][0] += cellVal;
					scores[1][1] += cellVal;
					scores[1][2] += cellVal;
					scores[2][0] += cellVal;
					scores[2][1] += -100;
					scores[2][2] += cellVal;
				}
				else if (relY == 1){
					scores[1][1] += cellVal;
					scores[1][2] += cellVal;
					scores[2][1] += cellVal;
					scores[2][2] += -100;
				}
				else if(relY == -1){
					scores[1][0] += cellVal;
					scores[1][1] += cellVal;
					scores[2][0] += -100;
					scores[2][1] += cellVal;

				}
			}
			else if(relX == -1){
				if(relY == 0){
					scores[0][0] += cellVal;
					scores[0][1] += -100;
					scores[0][2] += cellVal;
					scores[1][0] += cellVal;
					scores[1][1] += cellVal;
					scores[1][2] += cellVal;
				}
				else if (relY == 1){
					scores[0][1] += cellVal;
					scores[0][2] += -100;
					scores[1][1] += cellVal;
					scores[1][2] += cellVal;
				}
				else if(relY == -1){
					scores[0][0] += -100;
					scores[0][1] += cellVal;
					scores[1][0] += cellVal;
					scores[1][1] += cellVal;
				}
			}
		}
		return scores;
	}

	public void move(ArrayList<Prisoner> prisoners){

		int scores[][] = {{0,0,0}, {0,0,0}, {0,0,0}};
		ArrayList<Prisoner> cooperators = new ArrayList<Prisoner>();
		ArrayList<Prisoner> defectors = new ArrayList<Prisoner>();

		//Separate the nearby prisoners into cooperators and defectors
		for(Prisoner p: prisoners){

			if ((defectorNear == true) && (cooperatorNear == true))
			{
				if(p.getStrategy().equals("C")){
					cooperators.add(p);
				}
				else if(p.getStrategy().equals("D")){
					defectors.add(p);
				}

				int cScores[][] = {{0,0,0}, {0,0,0}, {0,0,0}};
				int dScores[][] = {{0,0,0}, {0,0,0}, {0,0,0}};

				//Cooperator locations are evaluated according to specific gene sections 
				if ((this.gene[4] == 0) && (this.gene[5] == 0))
				{
					cScores = score(cooperators, 0); //stay still
				}
				else if ((this.gene[4] == 0) && (this.gene[5] == 1))
				{
					cScores = score(cooperators, 1); //follow
				}
				else if ((this.gene[4] == 1) && (this.gene[5] == 0))
				{
					cScores = score(cooperators, -1); //flee
				}
				else if((this.gene[4] == 1) && (this.gene[5] == 1))
				{
					int rand = (int) ((Math.random() * 3) -1); //randomly chose between the other option
					cScores = score(cooperators, rand);
				}

				//Cooperator locations are evaluated according to specific gene sections 
				if ((this.gene[6] == 0) && (this.gene[7] == 0))
				{
					dScores = score(defectors, 0); //Stay still
				}
				else if ((this.gene[6] == 0) && (this.gene[7] == 1))
				{
					dScores = score(defectors, 1); //Follow
				}

				else if ((this.gene[6] == 1) && (this.gene[7] == 0))
				{
					dScores = score(defectors, -1); //Flee
				}

				else if((this.gene[6] == 1) && (this.gene[7] == 1))
				{
					int rand = (int) ((Math.random() * 3) -1); //Random
					dScores = score(defectors, rand);
				}
				
				//Combine the cooperator and defector scores
				for (int i = 0; i < 3; i++){
					for(int j = 0; j < 3; j++){
						scores[i][j] = cScores[i][j] + dScores[i][j];
					}
				}
			}
			//if there is a defector but no cooperator
			else if ((defectorNear == true) && (cooperatorNear == false))
			{
				if ((this.gene[2] == 0) && (this.gene[3] == 0))
				{
					scores = score(prisoners, 0); //Stay still
				}
				else if ((this.gene[2] == 0) && (this.gene[3] == 1))
				{
					scores = score(prisoners, 1); //Follow
				}
				else if ((this.gene[2] == 1) && (this.gene[3] == 0))
				{
					scores = score(prisoners, -1); //Flee
				}
				else if ((this.gene[2] == 1) && (this.gene[3] == 1))
				{
					int rand = (int) ((Math.random() * 3) -1); //Random
					scores = score(prisoners, rand);
				}
			}
			//if there is a cooperator but no defector
			else if ((defectorNear == false) && (cooperatorNear == true))
			{
				if ((this.gene[0] == 0) && (this.gene[1] == 0))
				{
					scores = score(prisoners, 0); //Stay still
				}
				else if ((this.gene[0] == 0) && (this.gene[1] == 1))
				{
					scores = score(prisoners, 1); //Follow
				}
				else if ((this.gene[0] == 1) && (this.gene[1] == 0))
				{
					scores = score(prisoners, -1); //Flee
				}
				else if ((this.gene[0] == 1) && (this.gene[1] == 1))
				{
					int rand = (int) ((Math.random() * 3) -1); //Random
					scores = score(prisoners, rand);
				}
			}
		}
		//Find the highest scoring cell from the score array
		int moveTo[] = calculateBestMove(scores);
		
		//Move to the highest scoring cell
		moveToBest(moveTo);
}

	public void randomWalk()
	{
		boolean moved = false;//avoids choosing invalid locations
		while(!moved)
		{
			int randLoc[] = locations[(int) (Math.random() * 8)];

			int[] moveTo = new int[2];
			moveTo[0] = modulo(x+randLoc[0], world.worldSizeX); //wrapping
			moveTo[1] = modulo(y+randLoc[1], world.worldSizeY);

			//check for valid location
			if(world.getAt(moveTo[0],moveTo[1])==null){
				
				Prisoner p = world.getAt(x,y);
				world.setAt(x,y,null);
				world.setAt(moveTo[0],moveTo[1],p);

				x=moveTo[0];
				y=moveTo[1];
				moved = true;
			}
		}
	}

	private void moveToBest(int[] loc)
	{
		int[] moveTo = new int[2];
		moveTo[0] = modulo(x+loc[0], world.worldSizeX);
		moveTo[1] = modulo(y+loc[1], world.worldSizeY);

		if(world.getAt(moveTo[0], moveTo[1]) == null){
			Prisoner p = world.getAt(x,y);
			world.setAt(x,y,null);
			world.setAt(moveTo[0],moveTo[1], p);

			x = moveTo[0];
			y = moveTo[1];
		}
	}

	private int modulo(int index, int max){
		if(index < 0) return index + max;
		else if(index >= max) return index % max;
		else return index;
	}

	private int[] calculateBestMove(int[][] scores)
	{
		int highVal = scores[0][0];
		int highLoc[] = {0,0};
		
		//Find the highest score
		for (int i = 0; i < 3; i++){
			for(int j = 0; j < 3; j++){
				
				if(highVal > scores[i][j]){
					
					highVal = scores[i][j];
					highLoc[0] = i;
					highLoc[1] = j;
				}
				else if(highVal == scores[i][j]){
					//Coin toss for tie breakers to avoid direction bias
					if((int)(Math.random()*2) == 0){
						highVal = scores[i][j];
						highLoc[0] = i;
						highLoc[1] = j;
					}
				}
			}
		}
		//Determine where of 8 movement locations the highest scoring cell is located 
		int index = 0;
		
		if(highLoc[0] == 0) {
			index = highLoc[1];
		}
		else if(highLoc[0] == 1 && highLoc[0] == 0) {
			index = 3;
		}
		else if(highLoc[0] == 1 && highLoc[0] == 2) {
			index = 4;
		}
		else {
			index = highLoc[1] + 5;
		}
		
		return this.locations[index];
	}

	public String getStrategy()
	{
		return strategy;
	}

	public void setStrategy(String strategy){
		this.strategy = strategy;
	}

	public int getFitnessScore(){
		return fitnessScore;
	}

	public int getNearbySize(){
		return nearby.size();
	}

	public void clearNearby(){
		nearby.clear();
	}

	public void setFitnessScore(int score){
		this.fitnessScore = score;
	}

	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public void setX(int x){
		this.x = x;
	}
	public void setY(int y){
		this.y = y;
	}
	public void setGene(int[] gene){
		this.gene = gene;
	}
	public int[] getGene(){
		return gene;
	}
}

