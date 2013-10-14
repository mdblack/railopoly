package com.blackware.railopoly;

public class Railroad 
{
	int number;
	String name;
	int player;
	int cost;
	int railoffset;
	String abbreviation;
	
	public int nextcity1[];
	public int nextcity2[];
	public int nextcity3[];
	public int nextcity4[];
	public int nextcity5[];
	
	public Railroad(int id, String thename, String abbrev, int cst, int number_of_towns)
	{
		int i;
		
		number=id;
		name=thename;
		abbreviation=abbrev;
		cost=cst;
		railoffset=0;
		
		nextcity1=new int[number_of_towns];
		nextcity2=new int[number_of_towns];
		nextcity3=new int[number_of_towns];
		nextcity4=new int[number_of_towns];
		nextcity5=new int[number_of_towns];
		for (i=0; i<number_of_towns; i++)
			nextcity1[i]=-1;
		for (i=0; i<number_of_towns; i++)
			nextcity2[i]=-1;
		for (i=0; i<number_of_towns; i++)
			nextcity3[i]=-1;
		for (i=0; i<number_of_towns; i++)
			nextcity4[i]=-1;
		for (i=0; i<number_of_towns; i++)
			nextcity5[i]=-1;
		
		player=-1;
	}
}
