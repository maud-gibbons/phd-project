package GameTheory;

abstract class Agent
{
	protected boolean moved;
	protected World world;
	protected int[] gene = new int[8];

	public Agent(World world, int[] gene)
	{
		this.world = world;
		moved = false;
		this.gene = gene;
	}

	public Agent(World world)
	{
		this.world = world;
		moved = false;
	}

	public boolean getMoved()
	{
		return moved;
	}

	public void setMoved(boolean moved)
	{
		this.moved = moved;
	}

	public int[] getGene()
	{
		return gene;
	}

	public void setGene(int[] gene)
	{
		this.gene = gene;
	}

	public void setWorld(World w){
		this.world = w;
	}
	public World getWorld(){
		return world;
	}

	public abstract void takeTurn();
}

