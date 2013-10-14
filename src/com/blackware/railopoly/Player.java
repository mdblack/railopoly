package com.blackware.railopoly;

public class Player 
{
	String name;
	boolean[] rails;
	int home, source, destination, location, money;
	boolean ingame=true;

	int townpath[];
	int townstatus[];
	int towndist[];
	int railpath[];
	int playernumber;
	int train=0;
	boolean computer;
	int playertype;
	int lastrail=-1;
//	AuctionBox auctionbox;
	boolean autobid;
	Railopoly railopoly;
	boolean lastmove;
	
	public Player(Railopoly railopoly, String nm, int pn, boolean cp, int type)
	{
		int i;
		name=nm;
		playernumber=pn;
		computer=cp;
		playertype=type;
		
		rails=new boolean[28];
		for (i=0; i<28; i++)
			rails[i]=false;
		
		source=-1;
		destination=-1;
		money=railopoly.startingmoney;
		this.railopoly=railopoly;
	}
	
	public void shoulddeclare()
	{
		shortestpath(false,home);
		if ((towndist[location]<=10)&&(money>=railopoly.winningmoney))
			railopoly.declared[playernumber]=true;
		if ((towndist[location]<=20)&&(money>=railopoly.winningmoney+20000))
			railopoly.declared[playernumber]=true;
		
		if (railopoly.declared[playernumber])
		{
			railopoly.message(name+" declares");
		}
	}
	
	public void shouldpursue()
	{
		int i;
		
		for (i=0; i<railopoly.number_of_players; i++)
		{
			if ((railopoly.declared[i])&&(i!=playernumber))
			{
				if (money>50000)
				{
					shortestpath(false,railopoly.player[i].location);
				}
			}
		}
	}
	
	public int shouldbuy()
	{
		int i,j,k;
		
		//players will not buy railroads unless they have at least $5000 extra cash
		//player type 0: wants most access.  will buy railroad that gives most access
		//player type 1: wants monopolies.
		//player type 2: buys the best he can afford
		//player type 3: will buy anything
		//if no railroads available, will buy engine upgrade (if have $10000 extra cash)
		
		j=-1;
		k=0;
		for (i=0; i<28; i++)
		{
			if (railopoly.railroad[i].player==-1)
			{
				if (j==-1)
				{
					j=i;
					if (playertype==0)
						k=paccess(i)-paccess();
					else if (playertype==1)
						k=pmonopoly(i)-pmonopoly();
					else if ((playertype==2)&&(money-railopoly.railroad[i].cost>=5000))
						k=railopoly.railroad[i].cost;
					else if (playertype==3)
						k=0;
					else
						j=-1;
				}
				else
				{
					if (playertype==0)
					{
						if (paccess(i)-paccess()>k)
						{
							j=i;
							k=paccess(i)-paccess();
						}
					}
					else if (playertype==1)
					{
						if (pmonopoly(i)-pmonopoly()>k)
						{
							j=i;
							k=pmonopoly(i)-pmonopoly();
						}
					}
					else if (playertype==2)
					{
						if ((money-railopoly.railroad[i].cost>=5000)&&(railopoly.railroad[i].cost>k))
						{
							j=i;
							k=railopoly.railroad[i].cost;
						}
					}
					else
					{
						k=railopoly.getRandom(5);
						if ((money-railopoly.railroad[i].cost>=0)&&(k==1))
							j=i;
					}
				}
			}
		}
		if (j!=-1)
		{
			if (money-railopoly.railroad[j].cost<5000)
				j=-1;
		}
		if (j==-1)
		{
			if ((money>50000)&&(train<2))
				j=30;
			else if ((money>14000)&&(train<1))
				j=29;
		}
		return(j);
	}
	
	public void sell()
	{
		int i,j,k;
		
		j=-1;
		k=0;
		for (i=0; i<28; i++)
		{
			if (railopoly.railroad[i].player==playernumber)
			{
				if (j==-1)
				{
					j=i;
					if (playertype==0)
						k=paccess(i)-paccess();
					else if (playertype==1)
						k=pmonopoly(i)-pmonopoly();
					else
						k=railopoly.railroad[i].cost;
				}
				else
				{
					if (playertype==0)
						if (k>paccess(i)-paccess())
						{
							j=i;
							k=paccess(i)-paccess();
						}
					else if (playertype==1)
						if (k>pmonopoly(i)-pmonopoly())
						{
							j=i;
							k=pmonopoly(i)-pmonopoly();
						}
					else if (playertype==2)
						if (k>railopoly.railroad[i].cost)
						{
							j=i;
							k=railopoly.railroad[i].cost;
						}
					else
					{
						k=railopoly.getRandom(3);
						if (k==1)
							j=i;
					}
				}
			}
		}
		if (money>=0)
		{
//			railopoly.board.statuscanvas.hide();
//			railopoly.board.statuscanvas.show();
			railopoly.phaseLock.postPhaseLockResume();
//			if (railopoly.togame2)
//				railopoly.game2();
//			else
//				railopoly.game3();
		}
		else if (j==-1)	
		{
			//out of money
			ingame=false;
			railopoly.message(railopoly.player[playernumber].name+" loses");
//			railopoly.board.message[railopoly.board.message_number]=railopoly.player[playernumber].name+" loses";
//			railopoly.board.messagecolor[railopoly.board.message_number]=playernumber;
//			railopoly.board.message_number++;
//			railopoly.board.railcanvas.paint(railopoly.board.railcanvas.getGraphics());
//			railopoly.board.statuscanvas.hide();
//			railopoly.board.statuscanvas.show();
			railopoly.phaseLock.postPhaseLockResume();
//			railopoly.game3();
		}
		else
		{
			k=0;
			for (i=0; i<railopoly.number_of_players; i++)
				if (railopoly.player[i].ingame)
					k++;
			if (k<3)
			{
				railopoly.railroad[j].player=-1;
				money+=railopoly.railroad[j].cost/2;
				railopoly.message(name+" sells the "+railopoly.railroad[j].name);
//				railopoly.board.messagecolor[railopoly.board.message_number]=playernumber;
//				railopoly.board.message_number++;
//				railopoly.board.railcanvas.paint(railopoly.board.railcanvas.getGraphics());
				sell();
			}
			else
				railopoly.startAuction(j);
//				auctionbox=new AuctionBox(railopoly.railroad[j].cost/2,playernumber,j,true);
		}
	}
	
	public boolean bid(int rail, int cost)
	{
		if (money<cost)
			return(false);
		if (money<cost+10000+railopoly.getRandom(1000)-500)
			return(false);
		if ((money<cost+40000+railopoly.getRandom(2000)-1000)&&(playertype==0)&&(paccess(rail)-paccess()<3000))
			return(false);
		if ((money<cost+40000+railopoly.getRandom(2000)-1000)&&(playertype==1)&&(pmonopoly(rail)-pmonopoly()<3000))
			return(false);
		if ((money<cost+80000+railopoly.getRandom(3000)-1500)&&(cost>railopoly.railroad[rail].cost*4))
			return(false);
		return(true);
	}
	
	public int percentaccess()
	{
		int i,j,k,l;
		
		k=0;
		l=0;
		for (i=0; i<railopoly.number_of_towns; i++)
		{
			if (railopoly.town[i].city)
			{
				l++;
				for (j=0; j<28; j++)
				{
					if (railopoly.town[i].rails[j])
						if (railopoly.railroad[j].player==playernumber)
						{
							k++;
							break;
						}
				}
			}
		}
		i=(int)(100*k)/l;
		return(i);
	}
	
	public int percentaccess(int prospective_railroad)
	{
		int i,j,k,l;
		
		k=0;
		l=0;
		for (i=0; i<railopoly.number_of_towns; i++)
		{
			if (railopoly.town[i].city)
			{
				l++;
				for (j=0; j<28; j++)
				{
					if (railopoly.town[i].rails[j])
						if ((railopoly.railroad[j].player==playernumber)||(j==prospective_railroad))
						{
							k++;
							break;
						}
				}
			}
		}
		i=(int)(100*k)/l;
		return(i);
	}
	
	public int percentmonopoly()
	{
		int i,j,k,l;
		
		k=0;
		l=0;
		for (i=0; i<railopoly.number_of_towns; i++)
		{
			if (railopoly.town[i].city)
			{
				l++;
				for (j=0; j<28; j++)
				{
					if (railopoly.town[i].rails[j])
						if (railopoly.railroad[j].player!=playernumber)
							break;
				}
				if (j==28)
					k++;
			}
		}
		i=(int)(100*k)/l;
		return(i);
	}
	
	public int percentmonopoly(int prospective_railroad)
	{
		int i,j,k,l;
		
		k=0;
		l=0;
		for (i=0; i<railopoly.number_of_towns; i++)
		{
			if (railopoly.town[i].city)
			{
				l++;
				for (j=0; j<28; j++)
				{
					if (railopoly.town[i].rails[j])
						if ((railopoly.railroad[j].player!=playernumber)&&(j!=prospective_railroad))
							break;
				}
				if (j==28)
					k++;
			}
		}
		i=(int)(100*k)/l;
		return(i);
	}

	public int paccess()
	{
		int i,j,k,l;
		
		k=0;
		l=0;
		for (i=0; i<railopoly.number_of_towns; i++)
		{
			if (railopoly.town[i].city)
			{
				l++;
				for (j=0; j<28; j++)
				{
					if (railopoly.town[i].rails[j])
						if (railopoly.railroad[j].player==playernumber)
						{
							k++;
							break;
						}
				}
			}
		}
		return((100000*k)/l);
	}
	
	public int paccess(int prospective_railroad)
	{
		int i,j,k,l;
		
		k=0;
		l=0;
		for (i=0; i<railopoly.number_of_towns; i++)
		{
			if (railopoly.town[i].city)
			{
				l++;
				for (j=0; j<28; j++)
				{
					if (railopoly.town[i].rails[j])
						if ((railopoly.railroad[j].player==playernumber)||(j==prospective_railroad))
						{
							k++;
							break;
						}
				}
			}
		}
		return((100000*k)/l);
	}
	
	public int pmonopoly()
	{
		int i,j,k,l;
		
		k=0;
		l=0;
		for (i=0; i<railopoly.number_of_towns; i++)
		{
			if (railopoly.town[i].city)
			{
				l++;
				for (j=0; j<28; j++)
				{
					if (railopoly.town[i].rails[j])
						if (railopoly.railroad[j].player!=playernumber)
							break;
				}
				if (j==28)
					k++;
			}
		}
		return((100000*k)/l);
	}
	
	public int pmonopoly(int prospective_railroad)
	{
		int i,j,k,l;
		
		k=0;
		l=0;
		for (i=0; i<railopoly.number_of_towns; i++)
		{
			if (railopoly.town[i].city)
			{
				l++;
				for (j=0; j<28; j++)
				{
					if (railopoly.town[i].rails[j])
						if ((railopoly.railroad[j].player!=playernumber)&&(j!=prospective_railroad))
							break;
				}
				if (j==28)
					k++;
			}
		}
		return((100000*k)/l);
	}

	public void shortestpath(boolean cheapest, int dest)
	{
		int i,weight,j,k;
		
		townpath=new int[railopoly.number_of_towns];
		townstatus=new int[railopoly.number_of_towns];
		towndist=new int[railopoly.number_of_towns];
		railpath=new int[railopoly.number_of_towns];
		for (i=0; i<railopoly.number_of_towns; i++)
		{
			townstatus[i]=0;
			townpath[i]=-1;
			towndist[i]=10000000;
			railpath[i]=-1;
		}
		towndist[dest]=0;
		townpath[dest]=dest;
		
		k=0;
		while (k!=-1)
		{
			j=10000000;
			k=-1;
			weight=1;
			for (i=0; i<railopoly.number_of_towns; i++)
			{
				//find the node with smallest distance from source of status 0
				if ((towndist[i]<j)&&(townstatus[i]==0))
				{
					j=towndist[i];
					k=i;
				}
			}
			if (k!=-1)
			{
				townstatus[k]=1;
				//update the distance on all adjacent nodes
				for (i=0; i<28; i++)
				{
					if (cheapest)
					{
						if (railopoly.railroad[i].player==playernumber)
							weight=railopoly.ownrailcost;
						else if (railopoly.railroad[i].player==-1)
							weight=railopoly.bankrailcost;
						else if (railopoly.allrailsbought)
							weight=railopoly.railcost2;
						else
							weight=railopoly.railcost1;
						weight++;
						if(railpath[k]!=-1)
						{
							if (railopoly.railroad[i].player!=railopoly.railroad[railpath[k]].player)
							{
								if((!rails[i])&&(!rails[railpath[k]]))
								{
								   if((railopoly.railroad[i].player>-1)&&(railopoly.railroad[railpath[k]].player>-1))
								   {
										if(railopoly.allrailsbought)
											weight+=3*railopoly.railcost2;
										else
											weight+=3*railopoly.railcost1;
								   }
								}
							}
						}
					}
					else
					{
						weight=1;
					}
					if (railopoly.railroad[i].nextcity1[k]!=-1)
					{
						if (townstatus[railopoly.railroad[i].nextcity1[k]]==0)
						{
							if (towndist[railopoly.railroad[i].nextcity1[k]]>towndist[k]+weight)
							{
								towndist[railopoly.railroad[i].nextcity1[k]]=towndist[k]+weight;
								townpath[railopoly.railroad[i].nextcity1[k]]=k;
								railpath[railopoly.railroad[i].nextcity1[k]]=i;
							}
						}
					}
					if (railopoly.railroad[i].nextcity2[k]!=-1)
					{
						if (townstatus[railopoly.railroad[i].nextcity2[k]]==0)
						{
							if (towndist[railopoly.railroad[i].nextcity2[k]]>towndist[k]+weight)
							{
								towndist[railopoly.railroad[i].nextcity2[k]]=towndist[k]+weight;
								townpath[railopoly.railroad[i].nextcity2[k]]=k;
								railpath[railopoly.railroad[i].nextcity2[k]]=i;
							}
						}
					}
					if (railopoly.railroad[i].nextcity3[k]!=-1)
					{
						if (townstatus[railopoly.railroad[i].nextcity3[k]]==0)
						{
							if (towndist[railopoly.railroad[i].nextcity3[k]]>towndist[k]+weight)
							{
								towndist[railopoly.railroad[i].nextcity3[k]]=towndist[k]+weight;
								townpath[railopoly.railroad[i].nextcity3[k]]=k;
								railpath[railopoly.railroad[i].nextcity3[k]]=i;
							}
						}
					}
					if (railopoly.railroad[i].nextcity4[k]!=-1)
					{
						if (townstatus[railopoly.railroad[i].nextcity4[k]]==0)
						{
							if (towndist[railopoly.railroad[i].nextcity4[k]]>towndist[k]+weight)
							{
								towndist[railopoly.railroad[i].nextcity4[k]]=towndist[k]+weight;
								townpath[railopoly.railroad[i].nextcity4[k]]=k;
								railpath[railopoly.railroad[i].nextcity4[k]]=i;
							}
						}
					}
					if (railopoly.railroad[i].nextcity5[k]!=-1)
					{
						if (townstatus[railopoly.railroad[i].nextcity5[k]]==0)
						{
							if (towndist[railopoly.railroad[i].nextcity5[k]]>towndist[k]+weight)
							{
								towndist[railopoly.railroad[i].nextcity5[k]]=towndist[k]+weight;
								townpath[railopoly.railroad[i].nextcity5[k]]=k;
								railpath[railopoly.railroad[i].nextcity5[k]]=i;
							}
						}
					}
				}
			}
		}
	}
}
