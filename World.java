package GameTheory;

public class World
{
	public int worldSizeX, worldSizeY;

	//The prisoners' world is an NxM grid
	Prisoner[][] grid;

	public World(int x, int y)
	{
		//Constructor creates the agent grid. Each cell initialised to null  
		
		worldSizeX = x;
		worldSizeY = y;

		grid = new Prisoner[worldSizeX][worldSizeY];
		
		initilise();
	}

	public void initilise(){
		
		//initialise each cell in the grid
		for (int i = 0; i < worldSizeX; i++)
		{
			for (int j = 0; j < worldSizeY; j++)
			{
				grid[i][j] = null;
			}
		}
	}

	public Prisoner getAt(int x, int y)
	{
		//check for valid coordinates
		if ((x>=0) && (x<worldSizeX) && (y>=0) &&(y<worldSizeY))
			return grid[x][y];
		
		return null;
	}

	public void setAt(int x, int y, Prisoner agent)
	{
		//check for valid coordinates
		if ((x>=0) && (x<worldSizeX) && (y>=0) &&(y<worldSizeY))
			grid[x][y] = agent;
	}

	public void display()
	{
		//display the grid, if call empty 0 is displayed, C for Cooperator and D for Defector
		for (int i = 0; i < worldSizeX; i++)
		{
			for (int j = 0; j < worldSizeY; j++)
			{
				if(j % worldSizeX == 0)
					System.out.print("\n");
				
				if(grid[i][j] == null)
					System.out.print("0");
				
				else
					System.out.print(grid[i][j].getStrategy());
					
			}
		}
		System.out.println();
	}

	public int countPrisoners(){
		// Used to verify the number of agents in the grid
		int count = 0;
		
		for (int i=0; i<worldSizeX; i++)
			{
				for (int j=0; j<worldSizeY; j++)
				{
					if(grid[i][j]!=null)
					{
						count++;
					}
				}
			}
		return count;
	}

	public void simulateOneStep()
	{
		//Calls the function for each agent on the grid to take one turn

		//reset the movement status of each agent to not moved
		for (int i=0; i<worldSizeX; i++)
		{
			for (int j=0; j<worldSizeY;j++)
			{
				if(grid[i][j]!=null)
					grid[i][j].setMoved(false);
			}
		}

		// Each prisoner takes one turn
		for (int i=0;i<worldSizeX; i++)
		{
			for (int j=0; j<worldSizeY; j++)
			{
			 	if ((grid[i][j]!=null))
			 	{
			 		//agents only move once per turn
					if (grid[i][j].getMoved() == false)
					{
						grid[i][j].setMoved(true);
				 		grid[i][j].takeTurn();
					}
			 	}
			}
		}
	}
}

