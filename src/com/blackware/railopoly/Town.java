package com.blackware.railopoly;

public class Town 
{
	int number;
	boolean city;
	int x,y;
	String name="";
	boolean rails[];
	
	public Town(int id, int xcoor, int ycoor)
	{
		int i;
		
		number=id;
		city=false;
		x=xcoor;
		y=ycoor;
		
		rails=new boolean[28];
		for (i=0; i<28; i++)
			rails[i]=false;
	}

	public Town(int id, int xcoor, int ycoor, String thename)
	{
		int i;
		
		number=id;
		city=true;
		x=xcoor;
		y=ycoor;
		name+=thename;
		
		rails=new boolean[28];
		for (i=0; i<28; i++)
			rails[i]=false;
	}
}
