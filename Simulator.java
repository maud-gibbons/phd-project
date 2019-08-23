package GameTheory;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class Simulator
{
	public static int numPrisoner = 100;
	public static int steps = 5000;
	public static int genLength = 5;
	public static int replaceRate = 25;
	public static int worldX = 45;
	public static int worldY = 45;
	public static int numSimulations = 100;


	public static void main(String[] args)
	{
		int d_wins = 0, c_wins = 0, draw = 0;
		
		for(int i = 0; i < numSimulations; i++)
		{
			ArrayList<Prisoner> population = new ArrayList<Prisoner>();

			World w = new World(worldX,worldY);

			int prisonerCount = 0;
			while (prisonerCount < numPrisoner)
			{
				int [] xy = initRandPos();
				int [] gene = initRandGene(); 

				Prisoner convict;
				if (w.getAt(xy[0],xy[1]) == null)		// Only put agent in empty spot
				{
					prisonerCount++;
					if(prisonerCount % 2 == 1)
						convict = new Prisoner(w, xy[0], xy[1], gene, "D");
					else
						convict = new Prisoner(w, xy[0], xy[1], gene, "C");
					
					w.setAt(xy[0],xy[1],convict);
					population.add(convict);
				}
			}
			 
			int step = 0;
			while(step<steps){

				w.simulateOneStep();
				if(step%genLength == (genLength - 1))
				{
					//Two different evolutionary algorithms are available
					
					//population = evolve(w,population); //tournament selection, crossover, and mutation
					population = killWeakestCopyFittest(w,population,replaceRate); //new algorithm
					
				}

				step++;
			}
					//--------------------------------------------------------------------
			int c = 0;//final cooperator count
			for(Prisoner p: population)
				if(p.getStrategy().equals("C"))
					c++;
				
			int d = population.size() - c; //final defector count

			if(c == population.size()){
				c_wins++;
				recordGenotype(population,"Cooperator");
				//System.out.println("Average Cooperator Gene: "+ averageGene(population));
			}
			else if( d == population.size() ){
				d_wins++;
				recordGenotype(population,"Defector");
				//System.out.println("Average Defector Gene: "+ averageGene(population));
			}
			else{
				draw++;
			}
		}

		System.out.println("Cooperator Wins "+(c_wins/(double)(c_wins+d_wins+draw)*numSimulations));
		System.out.println("Defector Wins "+(d_wins/(double)(c_wins+d_wins+draw)*numSimulations));
		System.out.println("Draws "+(draw/(double)(c_wins+d_wins+draw)*numSimulations));
	}

	private static int[] initRandGene()
	{
		//randomly generate each gene section
		int a = (int) (Math.random() * 2);
		int b = (int) (Math.random() * 2);
		int c = (int) (Math.random() * 2);
		int d = (int) (Math.random() * 2);
		int e = (int) (Math.random() * 2);
		int f = (int) (Math.random() * 2);
		int g = (int) (Math.random() * 2);
		int h = (int) (Math.random() * 2);

		return new int[] {a,b,c,d,e,f,g,h};
	}
	
	private static int[] placeNearby(World w, Prisoner p){
		int [] xy = new int[2];
		p.scanHorizon();

		if (p.getNearbySize() >= 8)//If space to move
		{
			xy = initRandPos();
		}
		else
		{
			boolean placed = false;
			while(!placed)
			{
				int placeBy[] = p.locations[(int) (Math.random() * 8)];

				int[] parentLoc = new int[2];
				parentLoc[0] = modulo(p.getX()+placeBy[0],w.worldSizeX);
				parentLoc[1] = modulo(p.getY()+placeBy[1],w.worldSizeY);

				//check for other prisoners
				if (w.getAt(parentLoc[0],parentLoc[1]) == null){
					xy = parentLoc;
					placed = true;
				}
			}
		}
		return xy;
	}
	
	private static ArrayList<Prisoner> evolve(World w, ArrayList<Prisoner> population){
		ArrayList<Prisoner> nextPopulation = new ArrayList<Prisoner>();
		ArrayList<Prisoner> tempPopulation = new ArrayList<Prisoner>();

		//Tournament Selection
		int n = 0;
		while (n < population.size())
		{
			Collections.shuffle(population);

			//get random Prisoner from population
			Prisoner parentA = population.get(0);
			Prisoner parentB = population.get(1);

			tempPopulation.addAll(tournamentSelection(w, parentA, parentB));
			n++;
		}

		//Crossover
		n = 0;
		double percent = 0.7;

		while (n < (population.size() * percent)/2)
		{
			Collections.shuffle(tempPopulation);
			Prisoner parentA = tempPopulation.get(0);
			Prisoner parentB = tempPopulation.get(1);

			tempPopulation.remove(parentA);
			tempPopulation.remove(parentB);

			ArrayList<Prisoner> crossoverPrisoners = crossover(w,parentA, parentB);
			nextPopulation.addAll(crossoverPrisoners);

			n++;
		}

		//Mutation
		Collections.shuffle(tempPopulation);
		Prisoner parent = tempPopulation.get(0);
		tempPopulation.remove(parent);

		Prisoner mutatedPrisoner = mutation(w,parent);
		nextPopulation.add(mutatedPrisoner);

		//Copy over the remainder unaltered
		nextPopulation.addAll(tempPopulation);

		//Reset Locations
		w.initilise();
		
		//finding valid locations for each agent
		int count = 0;
		while(count < population.size()){
			int [] xy = initRandPos();

			//check for valid location
			if(w.getAt(xy[0], xy[1]) == null){
				nextPopulation.get(count).setX(xy[0]);
				nextPopulation.get(count).setY(xy[1]);
				
				count++;
			}
		}
		
		//updating the world object with each agent
		for(Prisoner p: nextPopulation)
			w.setAt(p.getX(),p.getY(), p);

		//updating each agent with the new world object
		for(Prisoner p : nextPopulation)
			p.setWorld(w);
		
		return nextPopulation;

	}
	private static ArrayList<Prisoner> tournamentSelection(World w, Prisoner parentA, Prisoner parentB)
	{
		ArrayList<Prisoner> children = new ArrayList<Prisoner>();
		Prisoner child;

		//compare the fitness if the Prisoners; the prisoner with the higher fitness is cloned
		if ((parentA.getFitnessScore()) > (parentB.getFitnessScore()))
		{
			child = new Prisoner(w, parentA.getX(), parentA.getY(), parentA.getGene(), parentA.getStrategy());
			children.add(child);
		}
		else if ((parentA.getFitnessScore()) <= (parentB.getFitnessScore()))
		{
			child = new Prisoner(w, parentB.getX(), parentB.getY(), parentB.getGene(), parentB.getStrategy());
			children.add(child);
		}

		return children;
	}

	private static ArrayList<Prisoner> crossover(World w, Prisoner parentA, Prisoner parentB)
	{
		ArrayList<Prisoner> children = new ArrayList<Prisoner>();
		Prisoner childA = new Prisoner(w,parentA.getX(),parentA.getY(), parentA.getStrategy());
		Prisoner childB = new Prisoner(w,parentB.getX(),parentB.getY(), parentB.getStrategy());

		int cp = (int) (Math.random() * 7); //generate random number to get crossover point on genecode

		for(int i=0; i<8; i++){
			if(i < cp){
				childA.gene[i] = parentA.gene[i];
				childB.gene[i] = parentB.gene[i];
			}
			else{
				childA.gene[i] = parentB.gene[i];
				childB.gene[i] = parentA.gene[i];
			}
		}
		children.add(childA);
		children.add(childB);

		return children;
	}

	private static Prisoner mutation(World w, Prisoner parent)
	{
		Prisoner child = new Prisoner(w,parent.getX(),parent.getY(), parent.getGene(), parent.getStrategy());

		int mp = (int) (Math.random() * 8);//mutation point
		//flip the nth gene
		if(child.gene[mp] == 1)
			child.gene[mp] = 0;
		else
			child.gene[mp] = 1;

		return child;
	}

	private static ArrayList<Prisoner> killWeakestCopyFittest(World w, ArrayList<Prisoner> population, int num)
	{
		int numPrisoner = population.size();
		int prisonerCount = 0;
		w.initilise(); //resets the world

		for(int i = 0; i < num; i++)
		{
			Collections.sort(population);

			//Removing the weakest from population list and freeing up the position in the environment
	 		population.remove(0);
	 	}
	 	prisonerCount = numPrisoner-num;

		while(prisonerCount<numPrisoner)
		{
			Collections.sort(population);
	 		Prisoner fittest = population.get(population.size()-1);//Make a copy of the fittest
	 		Prisoner child = new Prisoner(w, 0, 0, fittest.getGene(), fittest.getStrategy());

	 		//Reducing the fitness of original and copy so neither are chosen on the next iteration
		 	child.setFitnessScore(fittest.getFitnessScore()/2);
		 	fittest.setFitnessScore(fittest.getFitnessScore()/2);

			int [] xy = placeNearby(w,fittest);

			//check for valid location
			if(w.getAt(xy[0],xy[1])==null){
				child.setX(xy[0]);
				child.setY(xy[1]);
				population.add(child);
				prisonerCount++;
			}

			for(Prisoner p: population)
 				w.setAt(p.getX(),p.getY(),p);

			for(Prisoner p : population)
 				p.setWorld(w);
		}
		return population;
	}
	
	private static double calMean(double[] values){
		double sum = 0.0;
		for(double i: values){
			sum += i;
		}
		return sum/(double)(values.length);
	}

	private static double calVariance(double[] values){
		double mean = calMean(values);
		double sum = 0.0;

		for(double x: values)
			sum += (mean - x)*(mean - x);

		return  sum /(double)(values.length);
	}
	private static double calStdDev(double[] values){
		return Math.sqrt(calVariance(values));
	}

	private static int[] initRandPos()
	{
		int x = (int) (Math.random() * (worldX-1));
		int y = (int) (Math.random() * (worldY-1));
		return new int[] {x,y};
	}

	public static String averageGene(ArrayList<Prisoner> population){

			int[] sumGene = {0,0,0,0,0,0,0,0};
			int[] avgGene = {0,0,0,0,0,0,0,0};
			int[] popGene;

			for( int i = 0; i < population.size(); i++){
				popGene = population.get(i).getGene();

				for( int j = 0; j < popGene.length; j++){
					sumGene[j] += popGene[j];
				}
			}

			for(int i = 0; i < sumGene.length; i++){
				avgGene[i] = Math.round((float)sumGene[i]/(float)population.size());
			}
			return "["+avgGene[0]+", "+avgGene[1]+", "+avgGene[2]+", "+avgGene[3]+", "+avgGene[4]+", "+avgGene[5]+", "+avgGene[6]+", "+avgGene[7]+"]";
	}
	
	private static int modulo(int index, int max){
		if(index < 0) return index + max;
		else if(index >= max) return index % max;
		else return index;
	}
	
  private static void recordGenotype(ArrayList<Prisoner> population, String filename){
	  
	  String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
	  String path = new File("src/GameTheory/out/"+date+"/"+filename+"_Gene.csv").getAbsolutePath();
	  try {
		  	File file = new File(path);
		  	file.getParentFile().mkdirs();
		  	
			FileWriter	fo = new FileWriter(file, true);
			PrintWriter out = new PrintWriter(fo);

			for(Prisoner p : population)
			{
				int[] gene = p.getGene();

				out.append(+gene[0]+",");
				out.append(+gene[1]+",");
				out.append(+gene[2]+",");
				out.append(+gene[3]+",");
				out.append(+gene[4]+",");
				out.append(+gene[5]+",");
				out.append(+gene[6]+",");
				out.append(""+gene[7]);

				out.append(System.getProperty("line.separator"));
				out.flush();
			}
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

