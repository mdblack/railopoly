package com.blackware.railopoly;

import java.util.ArrayList;
import java.util.Queue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Railopoly extends Activity 
{
	final int number_of_towns=531;
	final int number_of_railroads=28;
	final int max_players=7;
	final int precision=20;
	final int pprecision=20;
	final int animation=150;
	
	public Railroad[] railroad;
	public Town[] town;
	public int[][] pay;
	public int[] railcolor,playercolor;
	Player[] player;
	private int current_player;
	private java.util.Random generator;
	private RailBoard railboard;
	private Railopoly railopoly=this;
	
	private TextView statusbar;
	private int selected=-1;
	private boolean won=false;
	boolean allrailsbought=false;
	private boolean bonus;
	private boolean mustdeclare=true;
	private int fulltrajectory[];
	private int fullindex;
	private int trajectory[];
	private int trajrails[];
	private int moves=0;
	private int movecost=0;
	private int trajplay[];
	private int trnum=0;
	private int currentrail[];
	private int crnum=0;
	private boolean dostage8=false;
	private int exempt_rail[];
	private int exempt_rail_cost[];
	private boolean exemption_used[];
	boolean declared[];
	private boolean wongame[];
	boolean togame2=true;
	private boolean playercomputer[];
	private int playerai[];
	private String playername[];
	private int restart=0;
	private boolean turndone=false;
	private boolean initial=false;
	private int a,b,xx,yy;
	private int status;
	private int die1,die2,die3;
	private int railroad_selected=-1;
	public OptionsBox optionsbox;
	public Lock phaseLock,auctionLock;
	
	private Button button1;
	
	int startingmoney=20000;
	int number_of_players=7;
	int ownrailcost=0;
	int bankrailcost=1000;
	int railcost1=5000;
	private boolean showcash=true;
	int railcost2=10000;
	private boolean suggestmove=true;
	private int cashshowmoney=150000;
	int winningmoney=200000;
	private int stolenmoney=50000;
	private boolean waitaftercomputer=false;
	private boolean overbidding=false;
	private Handler handler;
	private int[] raillist;
	private boolean can_buy_express,can_buy_superchief;
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        new Database(this);
        phaseLock=new Lock();
        auctionLock=new Lock();

        getoptions();
		SetupPlayers();
		
		generator=new java.util.Random(System.currentTimeMillis());
		current_player=-1;
		
		exempt_rail=new int[number_of_players];
		exempt_rail_cost=new int[number_of_players];
		exemption_used=new boolean[number_of_players];
		declared=new boolean[number_of_players];
		wongame=new boolean[number_of_players];
		status=0;
		
		//determine player home cities
		for (int i=0; i<number_of_players; i++)
		{
			int j=getregion(getRandom(6)+1,getRandom(6)+1,getRandom(6)+1);
			player[i].home=getcity(j,getRandom(6)+1,getRandom(6)+1,getRandom(6)+1);
			player[i].location=player[i].home;
			player[i].destination=player[i].home;
			player[i].source=-1;
			exempt_rail[i]=-2;
			exemption_used[i]=false;
			declared[i]=false;
			wongame[i]=false;
		}
		
		railboard=new RailBoard(this);
		railboard.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		railboard.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,getWindowManager().getDefaultDisplay().getHeight()-180));
		layout.addView(railboard);
		
		setupButtons(layout);
		
		setContentView(layout);
        handler=new Handler();
        
		current_player=getRandom(number_of_players);
			
		railboard.scrollTo(0,0);
		railboard.invalidate();
		buttonwait("Start Game");
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater=getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case R.id.Exit:
     		System.exit(0);
    		return true;
    	case R.id.About:
    		doAboutBox();
    		return true;
    	case R.id.Help:
    		doHelpBox();
    		return true;
    	case R.id.Options:
    		doOptionsBox();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    private void setupButtons(LinearLayout layout)
    {
    	statusbar=new TextView(this);
    	statusbar.setText("");
    	layout.addView(statusbar);
    	
		LinearLayout blayout = new LinearLayout(this);
		blayout.setOrientation(LinearLayout.HORIZONTAL);
		blayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		ButtonListener bl=new ButtonListener();
		
		button1=new Button(this);
		button1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		button1.setText("Continue");
		button1.setOnClickListener(bl);
		blayout.addView(button1);
		
		layout.addView(blayout);
    }
    
    public void onBackPressed()
    {
    	if (status==5 || status==11)
    	{
			trajectory=new int[19];
			trajrails=new int[19];
			trajplay=new int[number_of_players+10];
			moves=0;
			movecost=0;
			trajectory[0]=player[current_player].location;
			trajrails[0]=-1;
			trnum=0;
			railboard.postInvalidate();
			buttonwait("Take Suggested Move");    		
    	}
    }
        
    private class ButtonListener implements View.OnClickListener
    {
		public void onClick(View v) 
		{
			if (((Button)v).getText().equals("Exit"))
				System.exit(0);
			else if (((Button)v).getText().equals("Start Game"))
			{
				restart=1;
				new Thread(new Runnable(){public void run(){
					Looper.prepare();
					game();
				}}).start();
			}
			else if (((Button)v).getText().equals("Continue"))
			{
				if (!turndone)
					new Thread(new Runnable(){public void run(){
						Looper.prepare();
//						game();
						phaseLock.postPhaseLockResume();
					}}).start();
				else
				{
					new Thread(new Runnable(){public void run(){
						Looper.prepare();
						humanturn(0);
						turndone=false;					
					}}).start();
				}
			}
			else if (((Button)v).getText().equals("Finished"))
			{
				if (status==5)
					status=6;
				else
					status=12;
				new Thread(new Runnable(){public void run(){
					Looper.prepare();
					humanturn(status);
				}}).start();
			}
			else if (((Button)v).getText().equals("Undo"))
			{
				trajectory=new int[19];
				trajrails=new int[19];
				trajplay=new int[number_of_players+10];
				moves=0;
				movecost=0;
				trajectory[0]=player[current_player].location;
				trajrails[0]=-1;
				trnum=0;
				railboard.postInvalidate();
				buttonwait("Take Suggested Move");
			}
			else if (((Button)v).getText().equals("Take Suggested Move"))
			{	
				int i,m;
				
				if (status==5)
				{
					m=a+b;
					status=6;
				}
				else
				{
					m=b;
					status=12;
				}
				
				trajectory[0]=player[current_player].location;
				trajrails[0]=-1;
				for (i=1; i<=m; i++)
				{
					trajectory[i]=player[current_player].townpath[trajectory[i-1]];
					trajrails[i]=player[current_player].railpath[trajectory[i-1]];
					if (trajectory[i]==player[current_player].destination)
					{
						i++;
						break;
					}
				}
				moves=i-1;
				new Thread(new Runnable(){public void run(){
					Looper.prepare();
					humanturn(status);
				}}).start();
			}
		}
    }
    
    private class RailBoard extends View implements View.OnTouchListener
    {
    	final int HEIGHT=800,WIDTH=1400;
    	float SCALE=(float)0.8;
    	float downX,downY,downX2,downY2;
    	private Bitmap railmap;
    	private Bitmap[] playerpiece;
    	private boolean zoommode=false;
    	
    	public RailBoard(Context context)
    	{
    		super(context);
	        railmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.railmap),1400,750,false);
	        playerpiece=new Bitmap[max_players*2];
	        playerpiece[0]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece1r),32,32,false);
	        playerpiece[1]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece2r),32,32,false);
	        playerpiece[2]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece3r),32,32,false);
	        playerpiece[3]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece4r),32,32,false);
	        playerpiece[4]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece5r),32,32,false);
	        playerpiece[5]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece6r),32,32,false);
	        playerpiece[6]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece7r),32,32,false);
	        playerpiece[7]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece1l),32,32,false);
	        playerpiece[8]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece2l),32,32,false);
	        playerpiece[9]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece3l),32,32,false);
	        playerpiece[10]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece4l),32,32,false);
	        playerpiece[11]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece5l),32,32,false);
	        playerpiece[12]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece6l),32,32,false);
	        playerpiece[13]=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piece7l),32,32,false);
    		
    		setOnTouchListener(this);
    		
    	}
    	protected void onDraw(Canvas canvas)
    	{
    		super.onDraw(canvas);
    		canvas.scale(SCALE, SCALE);
//    		canvas.translate(((float)canvas.getWidth()-WIDTH)/2,((float)canvas.getHeight()-HEIGHT)/2);
    		
 			int i,j;
			
 			Paint paint=new Paint();
 			paint.setColor(Color.BLACK);
 			
 			canvas.drawBitmap(railmap, 0, 0, paint);

    		for (i=0; i<number_of_towns; i++)
			{
				if (!town[i].city)
					canvas.drawCircle(town[i].x-2, town[i].y-2, (float)1.5, paint);
				else
					canvas.drawRect(town[i].x-3, town[i].y-3, town[i].x+3, town[i].y+3, paint);
			}
			for (i=0; i<number_of_railroads; i++)
			{
				for (j=0; j<number_of_towns; j++)
				{
					
					paint.setColor(railcolor[i]);
					if (railroad[i].nextcity1[j]!=-1)
						canvas.drawLine(town[j].x,town[j].y,town[railroad[i].nextcity1[j]].x,town[railroad[i].nextcity1[j]].y,paint);
					if (railroad[i].nextcity2[j]!=-1)
						canvas.drawLine(town[j].x,town[j].y,town[railroad[i].nextcity2[j]].x,town[railroad[i].nextcity2[j]].y,paint);
					if (railroad[i].nextcity3[j]!=-1)
						canvas.drawLine(town[j].x,town[j].y,town[railroad[i].nextcity3[j]].x,town[railroad[i].nextcity3[j]].y,paint);
					if (railroad[i].nextcity4[j]!=-1)
						canvas.drawLine(town[j].x,town[j].y,town[railroad[i].nextcity4[j]].x,town[railroad[i].nextcity4[j]].y,paint);
				}
			}
			paint.setColor(Color.BLACK);
			paint.setTextSize(12);
			for (i=0; i<number_of_towns; i++)
					canvas.drawText(town[i].name,town[i].x,town[i].y,paint);
			
			for (i=0; i<28; i++)
			{
				if ((railroad[i].player!=-1)||(railroad_selected==i))
				{
					for (j=0; j<number_of_towns; j++)
					{
						if (railroad_selected==i)
							paint.setColor(Color.WHITE);
						else
							paint.setColor(playercolor[railroad[i].player]);
						if (railroad[i].nextcity1[j]!=-1)
						{
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity1[j]].x+railroad[i].railoffset,town[railroad[i].nextcity1[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x-1+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity1[j]].x-1+railroad[i].railoffset,town[railroad[i].nextcity1[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+1+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity1[j]].x+1+railroad[i].railoffset,town[railroad[i].nextcity1[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y-1+railroad[i].railoffset,town[railroad[i].nextcity1[j]].x+railroad[i].railoffset,town[railroad[i].nextcity1[j]].y-1+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y+1+railroad[i].railoffset,town[railroad[i].nextcity1[j]].x+railroad[i].railoffset,town[railroad[i].nextcity1[j]].y+1+railroad[i].railoffset,paint);
						}
						if (railroad[i].nextcity2[j]!=-1)
						{
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity2[j]].x+railroad[i].railoffset,town[railroad[i].nextcity2[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x-1+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity2[j]].x-1+railroad[i].railoffset,town[railroad[i].nextcity2[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+1+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity2[j]].x+1+railroad[i].railoffset,town[railroad[i].nextcity2[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y-1+railroad[i].railoffset,town[railroad[i].nextcity2[j]].x+railroad[i].railoffset,town[railroad[i].nextcity2[j]].y-1+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y+1+railroad[i].railoffset,town[railroad[i].nextcity2[j]].x+railroad[i].railoffset,town[railroad[i].nextcity2[j]].y+1+railroad[i].railoffset,paint);
						}
						if (railroad[i].nextcity3[j]!=-1)
						{
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity3[j]].x+railroad[i].railoffset,town[railroad[i].nextcity3[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x-1+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity3[j]].x-1+railroad[i].railoffset,town[railroad[i].nextcity3[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+1+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity3[j]].x+1+railroad[i].railoffset,town[railroad[i].nextcity3[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y-1+railroad[i].railoffset,town[railroad[i].nextcity3[j]].x+railroad[i].railoffset,town[railroad[i].nextcity3[j]].y-1+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y+1+railroad[i].railoffset,town[railroad[i].nextcity3[j]].x+railroad[i].railoffset,town[railroad[i].nextcity3[j]].y+1+railroad[i].railoffset,paint);
						}
						if (railroad[i].nextcity4[j]!=-1)
						{
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity4[j]].x+railroad[i].railoffset,town[railroad[i].nextcity4[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x-1+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity4[j]].x-1+railroad[i].railoffset,town[railroad[i].nextcity4[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+1+railroad[i].railoffset,town[j].y+railroad[i].railoffset,town[railroad[i].nextcity4[j]].x+1+railroad[i].railoffset,town[railroad[i].nextcity4[j]].y+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y-1+railroad[i].railoffset,town[railroad[i].nextcity4[j]].x+railroad[i].railoffset,town[railroad[i].nextcity4[j]].y-1+railroad[i].railoffset,paint);
							canvas.drawLine(town[j].x+railroad[i].railoffset,town[j].y+1+railroad[i].railoffset,town[railroad[i].nextcity4[j]].x+railroad[i].railoffset,town[railroad[i].nextcity4[j]].y+1+railroad[i].railoffset,paint);
						}
					}
				}
			}
			//display end markers
			int[] sp=new int[number_of_towns];
			for (i=0; i<number_of_towns; i++)
				sp[i]=0;
			for (i=0; i<number_of_players; i++)
			{
				if (player[i].ingame)
				{
					int aa=town[player[i].destination].x+sp[player[i].destination];
					int bb=town[player[i].destination].y;
					paint.setColor(playercolor[i]);
					canvas.drawCircle(aa-5,bb-5,5,paint);
					paint.setColor(Color.BLACK);
					sp[player[i].destination]+=3;
				}
			}
			//display pieces
			sp=new int[number_of_towns];
			for (i=0; i<number_of_towns; i++)
				sp[i]=0;
			for (i=0; i<number_of_players; i++)
			{
				if (player[i].ingame)
				{
					if (player[i].lastmove)
						canvas.drawBitmap(playerpiece[i], town[player[i].location].x-32/2, town[player[i].location].y-32/2, paint);
					else
						canvas.drawBitmap(playerpiece[i+7], town[player[i].location].x-32/2, town[player[i].location].y-32/2, paint);						
					sp[player[i].location]+=3;
				}
			}
			//display suggested move and trajectory
			if ((status==5)||(status==11))
			{
				try{
				//display suggested move in white
				if (suggestmove)
				{
					paint.setColor(Color.WHITE);
					i=player[current_player].location;
					if (declared[current_player])
					{
						while(i!=player[current_player].home && i>=0 && i<player[current_player].townpath.length)
						{
							canvas.drawLine(town[i].x,town[i].y,town[player[current_player].townpath[i]].x,town[player[current_player].townpath[i]].y,paint);
							i=player[current_player].townpath[i];
						}
					}
					else
					{
						while(i!=player[current_player].destination && i>=0 && i<player[current_player].townpath.length)
						{
							canvas.drawLine(town[i].x,town[i].y,town[player[current_player].townpath[i]].x,town[player[current_player].townpath[i]].y,paint);
							i=player[current_player].townpath[i];
						}
					}
				}
				}catch(ArrayIndexOutOfBoundsException e){}
				//display trajectory in black
				paint.setColor(Color.BLACK);
				for (i=0; i<moves; i++)
				{
					canvas.drawLine(town[trajectory[i]].x,town[trajectory[i]].y,town[trajectory[i+1]].x,town[trajectory[i+1]].y,paint);
					canvas.drawLine(town[trajectory[i]].x-1,town[trajectory[i]].y,town[trajectory[i+1]].x-1,town[trajectory[i+1]].y,paint);
					canvas.drawLine(town[trajectory[i]].x+1,town[trajectory[i]].y,town[trajectory[i+1]].x+1,town[trajectory[i+1]].y,paint);
					canvas.drawLine(town[trajectory[i]].x,town[trajectory[i]].y-1,town[trajectory[i+1]].x,town[trajectory[i+1]].y-1,paint);
					canvas.drawLine(town[trajectory[i]].x,town[trajectory[i]].y+1,town[trajectory[i+1]].x,town[trajectory[i+1]].y+1,paint);
				}
			}

    	}
		public boolean onTouch(View v, MotionEvent event) 
		{
			float x=event.getX();
			float y=event.getY();
			float dx=downX-x;
			float dy=downY-y;
			
			System.out.println(getScrollY()+" "+dy);
			if (HEIGHT*SCALE<=v.getHeight())
				dy=0;
			else
			{
				if (getScrollY()+dy<0)
					dy=-getScrollY();
				if (getScrollY()+dy>HEIGHT*SCALE-v.getHeight())
					dy=HEIGHT*SCALE-v.getHeight()-getScrollY();
			}
			if (WIDTH*SCALE<=v.getWidth())
				dx=0;
			else
			{
				if (getScrollX()+dx<0)
					dx=-getScrollX();
				if (getScrollX()+dx>WIDTH*SCALE-v.getWidth())
					dx=WIDTH*SCALE-v.getWidth()-getScrollX();
			}
			if (status==5||status==11)
			{
				if (event.getActionMasked()==MotionEvent.ACTION_MOVE)
				{
					xx=(int)((event.getX()+getScrollX())/SCALE);
					yy=(int)((event.getY()+getScrollY())/SCALE);
					humanturn(status);
				}
			}
			else
			{
				switch(event.getActionMasked())
				{
					case MotionEvent.ACTION_POINTER_DOWN:
						downX=event.getX(0);
						downY=event.getY(0);
						downX2=event.getX(1);
						downY2=event.getY(1);
						zoommode=true;
						break;
					case MotionEvent.ACTION_POINTER_UP:
						zoommode=false;
						break;
						
					case MotionEvent.ACTION_DOWN:
						int xxx=(int)((x+getScrollX())/SCALE);
						int yyy=(int)((y+getScrollY())/SCALE);
						setStatusBar("");
						for (int i=0; i<number_of_players; i++)
						{
							if (Math.abs(town[player[i].location].x-xxx)<pprecision && Math.abs(town[player[i].location].y-yyy)<pprecision)
							{
								String s=player[i].name+": ";
								
								if (showcash||player[i].money>=cashshowmoney||(!playercomputer[i]))
								{
									s+=" $"+player[i].money;
									if (player[i].source!=-1)
										s+=" (Anticipated $"+(player[i].money+payoff(player[i].source,player[i].destination))+")";
								}
								else if (player[i].source!=-1)
								{
									s+=" (Anticipated Earnings $"+(player[i].money+payoff(player[i].source,player[i].destination))+")";
								}
								s+=", Access: "+player[i].percentaccess()+"%, Monopolies: "+player[i].percentmonopoly()+"%";
								if (wongame[i])
									s+=" WON!!!";
								else if(declared[i])
									s+="  DECLARED.  Going to "+town[player[i].home].name;
								else if(player[i].source!=-1)
									s+=" going from "+town[player[i].source].name+" to "+town[player[i].destination].name;
								else
									s+=" Stopped at "+town[player[i].destination].name;
								if (player[i].source!=-1)
								{
									if (declared[i])
										player[i].shortestpath(false,player[i].home);
									else
										player[i].shortestpath(false,player[i].destination);
									int j=player[i].towndist[player[i].location];
									s+=" "+j+" steps until arrival";
								}
								
								setStatusBar(s);
							}
						}
						
						downX=x;
						downY=y;
						
						break;
					case MotionEvent.ACTION_MOVE:
						if (!zoommode)
						{
							scrollBy((int)(dx),(int)(dy));
							downX=x;
							downY=y;
						}
						else
						{
							float dist1=(downX-downX2)*(downX-downX2)+(downY-downY2)*(downY-downY2);
							float dist2=(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))+(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1));
							if (dist2<dist1)
								SCALE=SCALE-(float)0.01;
							else if (dist2>dist1)
								SCALE=SCALE+(float)0.01;
							if (SCALE>2)
								SCALE=2;
							if (SCALE<0.1)
								SCALE=(float)0.1;
							railboard.invalidate();
							
							downX2=event.getX(1);
							downY2=event.getY(1);
							downX=event.getX(0);
							downY=event.getY(0);
						}
						break;
					case MotionEvent.ACTION_UP:
						if (!zoommode)
							scrollBy((int)(dx),(int)(dy));
						zoommode=false;
						break;
				}
			}
			return true;
		}
		public void positionAt(int x, int y) 
		{
			//if x,y is not within the middle 80% of the canvas, recenter
			if (WIDTH*SCALE>getWidth() && x*SCALE<getScrollX()+0.5*getWidth()-0.4*getWidth())
			{
				float dx=x*SCALE-getScrollX()-(float)(0.5*getWidth());
//				if (getScrollX()+dx<0)
//					dx=-getScrollX();
				final float ddx=dx;
				handler.post(new Runnable(){
					public void run()
					{
						for (int i=0; i<-ddx; i++)
						{
							if (getScrollX()<=0)
								break;
							scrollBy(-1,0);
						}
						
					}
				});
			}
			else if (WIDTH*SCALE>getWidth() && x*SCALE>getScrollX()+0.5*getWidth()+0.4*getWidth())
			{
				float dx=x*SCALE-getScrollX()-(float)(0.5*getWidth());
//				if (getScrollX()+dx>WIDTH*SCALE-getWidth())
//					dx=WIDTH*SCALE-getWidth()-getScrollX();
				final float ddx=dx;
				handler.post(new Runnable(){
					public void run()
					{
						for (int i=0; i<ddx; i++)
						{
							if(getScrollX()>=WIDTH*SCALE-getWidth())
								break;
							scrollBy(1,0);
						}
					}
				});
			}
			if (HEIGHT*SCALE>getHeight() && y*SCALE<getScrollY()+0.5*getHeight()-0.4*getHeight())
			{
				float dy=y*SCALE-getScrollY()-(float)(0.5*getHeight());
//				if (getScrollY()+dy<0)
//					dy=-getScrollY();
				final float ddy=dy;
				handler.post(new Runnable(){
					public void run()
					{
						for (int i=0; i<-ddy; i++)
						{
							if (getScrollY()<=0)
								break;
							scrollBy(0,-1);
						}
					}
				});
			}
			else if (HEIGHT*SCALE>getHeight() && y*SCALE>getScrollY()+0.5*getHeight()+0.4*getHeight())
			{
				float dy=y*SCALE-getScrollY()-(float)(0.5*getHeight());
//				if (getScrollY()+dy>HEIGHT*SCALE-getHeight())
//					dy=HEIGHT*SCALE-getHeight()-getScrollY();
				final float ddy=dy;
				handler.post(new Runnable(){
					public void run()
					{
						for (int i=0; i<ddy; i++)
						{
							if (getScrollY()>=HEIGHT*SCALE-getHeight())
								break;
							scrollBy(0,1);
						}
					}
				});
			}
		}    	
    }
    
    public void message(final String m)
    {
		handler.post(new Runnable(){
			public void run()
			{
		    	Toast.makeText(railopoly, m, Toast.LENGTH_LONG).show();
			}
		});
    }
    
    public void fastmessage(final String m)
    {
		handler.post(new Runnable(){
			public void run()
			{
		    	Toast.makeText(railopoly, m, Toast.LENGTH_SHORT).show();
			}
		});
    }
    
    public void doAboutBox()
    {
		handler.post(new Runnable(){
			public void run()
			{
    	AlertDialog.Builder adb=new AlertDialog.Builder(railopoly);
    	adb.setIcon(R.drawable.rricon2);
    	adb.setTitle("About Railopoly");
    	adb.setMessage("This game was written by Michael Black in 2003, and was adapted for the Android in June, 2011.\n\nIt is inspired by the Avalon Hill board game 'Rail Baron'.");
    	adb.setNeutralButton("Okay", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
			}});
    	adb.show();
			}
		});    	
    }
    
    public void doOptionsBox()
    {
    	if (restart!=0)
    	{
    		handler.post(new Runnable(){
    			public void run()
    			{
    		    	AlertDialog.Builder adb=new AlertDialog.Builder(railopoly);
    		    	adb.setTitle("Options");
    		    	adb.setMessage("Please select your options before starting a game");
    		    	adb.setPositiveButton("Done", new OnClickListener(){
    					public void onClick(DialogInterface dialog, int which) {
    					}});
    		    	adb.show();
    			}
    		});
    		return;
    	}
		handler.post(new Runnable(){
			public void run()
			{
    	AlertDialog.Builder adb=new AlertDialog.Builder(railopoly);
    	adb.setTitle("Options");
    	optionsbox=new OptionsBox(adb);
    	adb.setPositiveButton("Done", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				optionsbox.setOptions();
			}});
    	adb.show();
			}
		});    	    	
    }
 
    private class OptionsBox
    {
    	private EditText[] playerName;
    	private Spinner[] playerType;
    	private Spinner numberOfPlayers;
    	private EditText winmoney,secretmoney,stolemoney,startmoney,cost1,cost2,cost3,cost4;
    	private CheckBox pause,jump,overbid,cashshow;
    	
    	public OptionsBox(AlertDialog.Builder adb)
    	{
    		LinearLayout l=new LinearLayout(railopoly);
    		l.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
    		l.setOrientation(LinearLayout.VERTICAL);

   			numberOfPlayers=new Spinner(railopoly);
			numberOfPlayers.setAdapter(new ArrayAdapter<String>(railopoly,android.R.layout.simple_spinner_item,new String[]{"1","2","3","4","5","6","7"}));
			numberOfPlayers.setSelection(6);
			LinearLayout l2=new LinearLayout(railopoly);
			l2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			l2.setOrientation(LinearLayout.HORIZONTAL);
			TextView t=new TextView(railopoly);
			t.setText("Number of players: ");
			l2.addView(t);
			l2.addView(numberOfPlayers);
			l.addView(l2);    			
    		
    		playerName=new EditText[7];
    		playerType=new Spinner[7];
    		for (int i=0; i<7; i++)
    		{
    			playerName[i]=new EditText(railopoly);
    			playerName[i].setWidth(150);
    			playerName[i].setText(new String[]{"Blue","Green","Red","Yellow","Purple","White","Black"}[i]);
//    			playerName[i].setTextSize(10);
    			playerType[i]=new Spinner(railopoly);
    			playerType[i].setAdapter(new ArrayAdapter<String>(railopoly,android.R.layout.simple_spinner_item,new String[]{"Human","AI: best access","AI: most monopolies","AI: highest price"}));
    			playerType[i].setSelection(new int[]{0,1,1,2,2,3,3}[i]);
    			l2=new LinearLayout(railopoly);
    			l2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
    			l2.setOrientation(LinearLayout.HORIZONTAL);
    			l2.addView(playerName[i]);
    			l2.addView(playerType[i]);
    			l.addView(l2);    			
    		}

   			winmoney=new EditText(railopoly);
   			winmoney.setText(""+winningmoney);
   			l2=new LinearLayout(railopoly);
			l2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			l2.setOrientation(LinearLayout.HORIZONTAL);
			t=new TextView(railopoly);
			t.setText("Money needed to win: ");
			l2.addView(t);
			l2.addView(winmoney);
			l.addView(l2);

			secretmoney=new EditText(railopoly);
   			secretmoney.setText(""+cashshowmoney);
   			l2=new LinearLayout(railopoly);
			l2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			l2.setOrientation(LinearLayout.HORIZONTAL);
			t=new TextView(railopoly);
			t.setText("Maximum secret money: ");
			l2.addView(t);
			l2.addView(secretmoney);
			l.addView(l2);
			
   			startmoney=new EditText(railopoly);
   			startmoney.setText(""+startingmoney);
   			l2=new LinearLayout(railopoly);
			l2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			l2.setOrientation(LinearLayout.HORIZONTAL);
			t=new TextView(railopoly);
			t.setText("Starting money: ");
			l2.addView(t);
			l2.addView(startmoney);
			l.addView(l2);
			
   			stolemoney=new EditText(railopoly);
   			stolemoney.setText(""+stolenmoney);
   			l2=new LinearLayout(railopoly);
			l2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			l2.setOrientation(LinearLayout.HORIZONTAL);
			t=new TextView(railopoly);
			t.setText("Money stolen from declared player: ");
			l2.addView(t);
			l2.addView(stolemoney);
			l.addView(l2);
			
   			cost1=new EditText(railopoly);
   			cost1.setText(""+ownrailcost);
   			l2=new LinearLayout(railopoly);
			l2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			l2.setOrientation(LinearLayout.HORIZONTAL);
			t=new TextView(railopoly);
			t.setText("Cost to use your own rails: ");
			l2.addView(t);
			l2.addView(cost1);
			l.addView(l2);
			
   			cost2=new EditText(railopoly);
   			cost2.setText(""+bankrailcost);
   			l2=new LinearLayout(railopoly);
			l2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			l2.setOrientation(LinearLayout.HORIZONTAL);
			t=new TextView(railopoly);
			t.setText("Cost to use unowned rails: ");
			l2.addView(t);
			l2.addView(cost2);
			l.addView(l2);
			
  			cost3=new EditText(railopoly);
   			cost3.setText(""+railcost1);
   			l2=new LinearLayout(railopoly);
			l2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			l2.setOrientation(LinearLayout.HORIZONTAL);
			t=new TextView(railopoly);
			t.setText("Cost to use opponents' rails: ");
			l2.addView(t);
			l2.addView(cost3);
			l.addView(l2);
    		
  			cost4=new EditText(railopoly);
   			cost4.setText(""+railcost2);
   			l2=new LinearLayout(railopoly);
			l2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			l2.setOrientation(LinearLayout.HORIZONTAL);
			t=new TextView(railopoly);
			t.setText("Cost after all rails are bought: ");
			l2.addView(t);
			l2.addView(cost4);
			l.addView(l2);
    		
			pause=new CheckBox(railopoly);
			pause.setText("Pause after each turn");
			pause.setChecked(false);
			l.addView(pause);
			overbid=new CheckBox(railopoly);
			overbid.setText("Allow overbidding");
			overbid.setChecked(false);
			l.addView(overbid);
			jump=new CheckBox(railopoly);
			jump.setText("Must declare and return home to win");
			jump.setChecked(true);
			l.addView(jump);
			cashshow=new CheckBox(railopoly);
			cashshow.setText("Reveal opponents' cash");
			cashshow.setChecked(false);
			l.addView(cashshow);
			
			ScrollView s=new ScrollView(railopoly);
    		s.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
    		s.addView(l);
    		adb.setView(s);
    	}
    	
    	public void setOptions()
    	{
    		number_of_players=numberOfPlayers.getSelectedItemPosition()+1;
    		playername=new String[max_players];
    		playercomputer=new boolean[max_players];
    		playerai=new int[max_players];
    		for (int i=0; i<max_players; i++)
    		{
    			playername[i]=playerName[i].getText().toString();
    			player[i].name=playername[i];
    			playercomputer[i]=playerType[i].getSelectedItemPosition()!=0;
    			player[i].computer=playercomputer[i];
    			if (playerType[i].getSelectedItemPosition()==1)
    				playerai[i]=0;
    			else if (playerType[i].getSelectedItemPosition()==2)
    				playerai[i]=1;
    			else if (playerType[i].getSelectedItemPosition()==3)
    				playerai[i]=2;
    			player[i].playertype=playerai[i];
    		}
    		int i;

    		showcash=cashshow.isChecked();
			waitaftercomputer=pause.isChecked();
			overbidding=overbid.isChecked();
			mustdeclare=jump.isChecked();

			try
    		{
    		i=Integer.parseInt(winmoney.getText().toString());
			if ((i>=0)&&(i<1000000000))
				winningmoney=i;
    		i=Integer.parseInt(secretmoney.getText().toString());
			if ((i>=0)&&(i<1000000000))
				cashshowmoney=i;
    		i=Integer.parseInt(startmoney.getText().toString());
			if ((i>=0)&&(i<1000000000))
				startingmoney=i;
    		i=Integer.parseInt(stolemoney.getText().toString());
			if ((i>=0)&&(i<1000000000))
				stolenmoney=i;
    		i=Integer.parseInt(cost1.getText().toString());
			if ((i>=0)&&(i<1000000000))
				ownrailcost=i;
    		i=Integer.parseInt(cost2.getText().toString());
			if ((i>=0)&&(i<1000000000))
				bankrailcost=i;
    		i=Integer.parseInt(cost3.getText().toString());
			if ((i>=0)&&(i<1000000000))
				railcost1=i;
    		i=Integer.parseInt(cost4.getText().toString());
			if ((i>=0)&&(i<1000000000))
				railcost2=i;
    		}
			catch(NumberFormatException e)
			{
				message("One of your entries is not a valid number.  Run options again.");
			}
			
    		railboard.postInvalidate();
    	}
    }
    
 
    public void doHelpBox()
    {
		handler.post(new Runnable(){
			public void run()
			{
    	AlertDialog.Builder adb=new AlertDialog.Builder(railopoly);
    	adb.setTitle("Railopoly Help");
    	//
    	String message="Objective:  The purpose of this game is to build your own railroad empire by buying railroads and getting opponents to pay you to ride on them.  The first player to earn $200000 and return to its home city wins.\n\nGame Play:  Each player has a train that makes deliveries between cities in exchange for cash.  Players take turns moving to their destination, which is randomly determined after making a delivery.  Once a delivery is made, the player is paid some money based on the distance traveled, and is given the option of buying a railroad.\n\nStrategy:  While it costs nothing to travel on your own railroads, you must pay opponents to travel on their track.  A good railroad combination must consequently have access to as many cities as possible, while maintaining several monopoly cities that only your own rails connect to.  In general, higher priced rails give better access and more monopolies, but ultimately the best rail to buy is the one that helps your network grow the most.\n\nWinning: To win the game the normal way, you must earn $200000, declare, and return to the city you started in.  After you declare, but before you reach your home city, another player may 'jump' you by traveling to your location.  Once jumped, you must pay the jumper $50000, and you become undeclared and must proceed to make your delivery as normal.  You can also win by default if every other player loses.\n\nBankrupcy:  If you cannot afford to pay rail fees, or if your money goes negative, you will have to sell rails.  You can either sell them directly back to the bank for half-price, or you can put them up for auction.  If you are out of money and rails, you must withdraw from the game.\n\nGood luck and thanks for playing!";
    	adb.setMessage(message);
    	adb.setNeutralButton("Okay", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
			}});
    	adb.show();
			}
		});    	
    }
 
    public void doEndSelection(final String message)
    {
		handler.post(new Runnable(){
			public void run()
			{
    	AlertDialog.Builder adb=new AlertDialog.Builder(railopoly);
    	adb.setTitle(message);
    	adb.setNeutralButton("Okay", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				railopoly.buttonwait("Exit");
			}});
    	adb.show();
			}
		});
    }
    
    public void getDiceRoll(final String message, int dice)
    {
		die1=getRandom(6)+1;
		die2=getRandom(6)+1;
		die3=getRandom(6)+1;
//		message("Dice rolled: "+die1+", "+die2+", ["+die3+"]");
		humanturn(status);
    }
    
    public void doRailBuy()
    {
		handler.post(new Runnable(){
			public void run()
			{
				AlertDialog.Builder adb=new AlertDialog.Builder(railopoly);
				adb.setTitle("You have $"+player[current_player].money);
				
				selected=-1;
				int n=0;
				can_buy_superchief=false;
				can_buy_express=false;
				if (player[current_player].train<=1 && player[current_player].money>=40000)
				{
					can_buy_superchief=true;
					n++;
				}
				if (player[current_player].train==0 && player[current_player].money>=4000)
				{
					can_buy_express=true;
					n++;
				}
					
				//get all unowned rails
				int j=0;
				for (int i=0; i<28; i++)
					if (railroad[i].player==-1 && railroad[i].cost<=player[current_player].money)
						j++;
				raillist=new int[j];
				j=0;
				for (int i=0; i<28; i++)
					if (railroad[i].player==-1 && railroad[i].cost<=player[current_player].money)
					{
						raillist[j]=i;
						j++;
					}
				//organize by cost
				for (int i=0; i<raillist.length; i++)
				{
					int k=i;
					for (j=i; j<raillist.length; j++)
						if (railroad[raillist[j]].cost>railroad[raillist[k]].cost)
							k=j;
					int m=raillist[k];
					for (j=k; j>i; j--)
						raillist[j]=raillist[j-1];
					raillist[i]=m;
				}
				String[] railchoice=new String[raillist.length+n];
				for (int i=0; i<raillist.length; i++)
				{
					String l="";
					l+=railroad[raillist[i]].name+"  ($"+railroad[raillist[i]].cost+")";
					railchoice[i+n]=l;
				}
				if (can_buy_superchief)
				{
					railchoice[0]="Upgrade to Superchief ($40000)";
					if (can_buy_express)
						railchoice[1]="Upgrade to Express ($4000)";
				}
				else if (can_buy_express)
					railchoice[0]="Upgrade to Express ($4000)";
				
				final CharSequence[] items=railchoice;
				adb.setSingleChoiceItems(items, -1, new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						selected=which;
						int rselected=selected-((can_buy_express? 1:0) + (can_buy_superchief? 1:0));
						if (rselected>=0 && rselected<raillist.length)
						{
							railroad_selected=raillist[rselected];
						
							String l=railroad[railroad_selected].name;
							l+=" will give you +";
							l+=player[current_player].percentaccess(railroad_selected)-player[current_player].percentaccess();
							l+="% access, +";
							l+=player[current_player].percentmonopoly(railroad_selected)-player[current_player].percentmonopoly();
							l+="% monopoly)";
							message(l);

						railboard.invalidate();
						}
					}});
		    	adb.setPositiveButton("Buy", new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						if (can_buy_superchief && selected==0)
						{
							player[current_player].money-=40000;
							player[current_player].train=2;
							message(player[current_player].name+" upgrades to Superchief");
						}
						else if (can_buy_express && selected==(can_buy_superchief? 1:0))
						{
							player[current_player].money-=4000;
							player[current_player].train=1;
							message(player[current_player].name+" upgrades to Express");
						}
						else if (selected>=(can_buy_express? 1:0) + (can_buy_superchief? 1:0))
						{
							selected-=(can_buy_express? 1:0) + (can_buy_superchief? 1:0);
							player[current_player].money-=railroad[raillist[selected]].cost;
							railroad[raillist[selected]].player=current_player;
							message(player[current_player].name+" buys the "+railroad[raillist[selected]].name);
							
							int i;
							for (i=0; i<28; i++)
								if (railroad[i].player==-1)
									break;
							if (i==28)
							{
								if (!allrailsbought)
								{
									message("Rail use fees are now $"+railcost2);
								}
								allrailsbought=true;
							}

						}
						railroad_selected=-1;
						railboard.invalidate();
					}});
		    	adb.setNegativeButton("Don't buy", new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
					}});
		    	AlertDialog alert=adb.create();
		    	alert.show();
			}
		});
    }
    
    public void getRegionSelection(final String message)
    {
		handler.post(new Runnable(){
			public void run()
			{
    	AlertDialog.Builder adb=new AlertDialog.Builder(railopoly);
    	adb.setTitle(message);
    	a=0;
    	final CharSequence[] items={"Northeast","North Central","Southeast","South Central","Plains","Southwest","Northwest"};
    	adb.setSingleChoiceItems(items, 0, new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				a=which;
			}
    	});
    	adb.setNeutralButton("Select Region", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				humanturn(status);
			}});
    	adb.show();
			}
		});
    }
    
    public void setStatusBar(final String message)
    {
    	handler.post(new Runnable(){
    		public void run()
    		{
    			statusbar.setText(message);
    		}
    	});
    }
    
    public void getDeclareSelection()
    {
		handler.post(new Runnable(){
			public void run()
			{
    	AlertDialog.Builder adb=new AlertDialog.Builder(railopoly);
    	String message="You have $"+player[current_player].money+" and must get to "+town[player[current_player].home].name;
    	adb.setTitle(message);
    	adb.setPositiveButton("Declare", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				message(player[current_player].name+" declares");
				declared[current_player]=true;
				if (status!=1)
					humanturn(status);
				else
					humanturn(0);				
			}});
    	adb.setNegativeButton("Not now", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				//don't declare
				if (status!=1)
					humanturn(9);
				else
					humanturn(status);
			}});
    	adb.show();
		}
	});
    }
    
    public void buttonwait(final String m)
    {
    	handler.post(new Runnable(){
			public void run() 
			{
			   	button1.setText(m);
		    	button1.setVisibility(Button.VISIBLE);			
			}});
    }
      
    public void buttondisable()
    {
       	handler.post(new Runnable(){
			public void run() 
			{
		    	button1.setVisibility(Button.INVISIBLE);
			}});
    }
     
	public int getRandom(int max)
	{
		return((int)(generator.nextDouble()*max));
	}
	
	private int payoff(int a, int b)
	{
		if ((a<0)||(b<0))
			return(0);
		
		if (pay[a][b]==-1)
			return(pay[b][a]);
		return(pay[a][b]);
	}
	
	private int getcurrentregion(int city)
	{
		if (city<=8)
			return(0);
		else if (city<=31)
			return(1);
		else if (city<=94)
			return(2);
		else if (city<=177)
			return(3);
		else if (city<=267)
			return(4);
		else if (city<=388)
			return(5);
		else
			return(6);
	}
	
	private int getregion(int die1, int die2, int die3)
	{
		if (die3%2==1)
		{
			if (die1+die2==2)
				return(4);
			if (die1+die2==3)
				return(2);
			if (die1+die2==4)
				return(2);
			if (die1+die2==5)
				return(2);
			if (die1+die2==6)
				return(1);
			if (die1+die2==7)
				return(1);
			if (die1+die2==8)
				return(0);
			if (die1+die2==9)
				return(0);
			if (die1+die2==10)
				return(0);
			if (die1+die2==11)
				return(0);
			if (die1+die2==12)
				return(0);
		}
		else
		{
			if (die1+die2==2)
				return(5);
			if (die1+die2==3)
				return(3);
			if (die1+die2==4)
				return(3);
			if (die1+die2==5)
				return(3);
			if (die1+die2==6)
				return(5);
			if (die1+die2==7)
				return(5);
			if (die1+die2==8)
				return(4);
			if (die1+die2==9)
				return(6);
			if (die1+die2==10)
				return(6);
			if (die1+die2==11)
				return(4);
			if (die1+die2==12)
				return(6);
		}
		return(0);
	}
	
	private int getcity(int region, int die1, int die2, int die3)
	{
		if (region==0)
		{
			if (die3%2==1)
			{
				if (die1+die2==2)
					return(4);
				if (die1+die2==3)
					return(4);
				if (die1+die2==4)
					return(4);
				if (die1+die2==5)
					return(2);
				if (die1+die2==6)
					return(1);
				if (die1+die2==7)
					return(3);
				if (die1+die2==8)
					return(1);
				if (die1+die2==9)
					return(0);
				if (die1+die2==10)
					return(4);
				if (die1+die2==11)
					return(4);
				if (die1+die2==12)
					return(4);
			}
			else
			{
				if (die1+die2==2)
					return(4);
				if (die1+die2==3)
					return(8);
				if (die1+die2==4)
					return(5);
				if (die1+die2==5)
					return(5);
				if (die1+die2==6)
					return(6);
				if (die1+die2==7)
					return(8);
				if (die1+die2==8)
					return(6);
				if (die1+die2==9)
					return(7);
				if (die1+die2==10)
					return(7);
				if (die1+die2==11)
					return(7);
				if (die1+die2==12)
					return(4);
			}
		}
		else if (region==1)
		{
			if (die3%2==1)
			{
				if (die1+die2==2)
					return(24);
				if (die1+die2==3)
					return(24);
				if (die1+die2==4)
					return(24);
				if (die1+die2==5)
					return(24);
				if (die1+die2==6)
					return(25);
				if (die1+die2==7)
					return(25);
				if (die1+die2==8)
					return(29);
				if (die1+die2==9)
					return(31);
				if (die1+die2==10)
					return(31);
				if (die1+die2==11)
					return(28);
				if (die1+die2==12)
					return(31);
			}
			else
			{
				if (die1+die2==2)
					return(27);
				if (die1+die2==3)
					return(28);
				if (die1+die2==4)
					return(27);
				if (die1+die2==5)
					return(27);
				if (die1+die2==6)
					return(26);
				if (die1+die2==7)
					return(28);
				if (die1+die2==8)
					return(28);
				if (die1+die2==9)
					return(30);
				if (die1+die2==10)
					return(30);
				if (die1+die2==11)
					return(30);
				if (die1+die2==12)
					return(28);
			}
		}
		else if (region==2)
		{
			if (die3%2==1)
			{
				if (die1+die2==2)
					return(92);
				if (die1+die2==3)
					return(92);
				if (die1+die2==4)
					return(94);
				if (die1+die2==5)
					return(91);
				if (die1+die2==6)
					return(91);
				if (die1+die2==7)
					return(91);
				if (die1+die2==8)
					return(84);
				if (die1+die2==9)
					return(93);
				if (die1+die2==10)
					return(90);
				if (die1+die2==11)
					return(93);
				if (die1+die2==12)
					return(90);
			}
			else
			{
				if (die1+die2==2)
					return(85);
				if (die1+die2==3)
					return(85);
				if (die1+die2==4)
					return(85);
				if (die1+die2==5)
					return(86);
				if (die1+die2==6)
					return(89);
				if (die1+die2==7)
					return(87);
				if (die1+die2==8)
					return(89);
				if (die1+die2==9)
					return(88);
				if (die1+die2==10)
					return(88);
				if (die1+die2==11)
					return(90);
				if (die1+die2==12)
					return(85);
			}
		}
		else if (region==3)
		{
			if (die3%2==1)
			{
				if (die1+die2==2)
					return(171);
				if (die1+die2==3)
					return(171);
				if (die1+die2==4)
					return(171);
				if (die1+die2==5)
					return(172);
				if (die1+die2==6)
					return(170);
				if (die1+die2==7)
					return(169);
				if (die1+die2==8)
					return(167);
				if (die1+die2==9)
					return(168);
				if (die1+die2==10)
					return(168);
				if (die1+die2==11)
					return(167);
				if (die1+die2==12)
					return(171);
			}
			else
			{
				if (die1+die2==2)
					return(173);
				if (die1+die2==3)
					return(173);
				if (die1+die2==4)
					return(175);
				if (die1+die2==5)
					return(170);
				if (die1+die2==6)
					return(175);
				if (die1+die2==7)
					return(177);
				if (die1+die2==8)
					return(174);
				if (die1+die2==9)
					return(174);
				if (die1+die2==10)
					return(176);
				if (die1+die2==11)
					return(176);
				if (die1+die2==12)
					return(176);
			}
		}
		else if (region==4)
		{
			if (die3%2==1)
			{
				if (die1+die2==2)
					return(263);
				if (die1+die2==3)
					return(263);
				if (die1+die2==4)
					return(265);
				if (die1+die2==5)
					return(265);
				if (die1+die2==6)
					return(265);
				if (die1+die2==7)
					return(263);
				if (die1+die2==8)
					return(263);
				if (die1+die2==9)
					return(263);
				if (die1+die2==10)
					return(267);
				if (die1+die2==11)
					return(267);
				if (die1+die2==12)
					return(264);
			}
			else
			{
				if (die1+die2==2)
					return(264);
				if (die1+die2==3)
					return(260);
				if (die1+die2==4)
					return(260);
				if (die1+die2==5)
					return(260);
				if (die1+die2==6)
					return(260);
				if (die1+die2==7)
					return(264);
				if (die1+die2==8)
					return(262);
				if (die1+die2==9)
					return(266);
				if (die1+die2==10)
					return(266);
				if (die1+die2==11)
					return(261);
				if (die1+die2==12)
					return(261);
			}
		}
		else if (region==5)
		{
			if (die3%2==1)
			{
				if (die1+die2==2)
					return(382);
				if (die1+die2==3)
					return(382);
				if (die1+die2==4)
					return(385);
				if (die1+die2==5)
					return(382);
				if (die1+die2==6)
					return(384);
				if (die1+die2==7)
					return(380);
				if (die1+die2==8)
					return(388);
				if (die1+die2==9)
					return(386);
				if (die1+die2==10)
					return(387);
				if (die1+die2==11)
					return(386);
				if (die1+die2==12)
					return(386);
			}
			else
			{
				if (die1+die2==2)
					return(381);
				if (die1+die2==3)
					return(383);
				if (die1+die2==4)
					return(383);
				if (die1+die2==5)
					return(383);
				if (die1+die2==6)
					return(381);
				if (die1+die2==7)
					return(381);
				if (die1+die2==8)
					return(381);
				if (die1+die2==9)
					return(383);
				if (die1+die2==10)
					return(383);
				if (die1+die2==11)
					return(383);
				if (die1+die2==12)
					return(383);
			}
		}
		else
		{
			if (die3%2==1)
			{
				if (die1+die2==2)
					return(449);
				if (die1+die2==3)
					return(449);
				if (die1+die2==4)
					return(447);
				if (die1+die2==5)
					return(447);
				if (die1+die2==6)
					return(447);
				if (die1+die2==7)
					return(447);
				if (die1+die2==8)
					return(455);
				if (die1+die2==9)
					return(454);
				if (die1+die2==10)
					return(452);
				if (die1+die2==11)
					return(452);
				if (die1+die2==12)
					return(449);
			}
			else
			{
				if (die1+die2==2)
					return(449);
				if (die1+die2==3)
					return(453);
				if (die1+die2==4)
					return(453);
				if (die1+die2==5)
					return(453);
				if (die1+die2==6)
					return(448);
				if (die1+die2==7)
					return(448);
				if (die1+die2==8)
					return(448);
				if (die1+die2==9)
					return(451);
				if (die1+die2==10)
					return(450);
				if (die1+die2==11)
					return(450);
				if (die1+die2==12)
					return(448);
			}
		}
		return(0);
	}
	
	private void SetupPlayers()
	{
		int i;
		
		player=new Player[max_players];
		for (i=0; i<max_players; i++)
			player[i]=new Player(this,playername[i],i,playercomputer[i],playerai[i]);
	}

	private void getoptions()
	{
		playername=new String[max_players];
		playername[0]="Blue";
		playername[1]="Green";
		playername[2]="Red";
		playername[3]="Yellow";
		playername[4]="Purple";
		playername[5]="White";
		playername[6]="Black";
		playercomputer=new boolean[max_players];
		playercomputer[0]=false;
		playercomputer[1]=true;
		playercomputer[2]=true;
		playercomputer[3]=true;
		playercomputer[4]=true;
		playercomputer[5]=true;
		playercomputer[6]=true;
		playerai=new int[max_players];
		playerai[0]=0;
		playerai[1]=0;
		playerai[2]=0;
		playerai[3]=1;
		playerai[4]=1;
		playerai[5]=2;
		playerai[6]=2;
	}

	public void game()
	{
		while(true)
		{
			int d1,d2,d3,d,i;
			
			//check if only one player left
			d=0;
			d1=0;
			for (i=0; i<number_of_players; i++)
			{
				if (player[i].ingame)
				{
					d++;
					d1=i;
				}
			}
			if (d==1)
			{
				won=true;
				wongame[d1]=true;
				doEndSelection(player[d1].name+" wins by default!");
				return;
			}
			if (d==0)
			{
				won=true;
				doEndSelection(player[d1].name+" wins by default!");
				return;
			}
					
			if(!won)
			{
				buttondisable();
				
				//move to the next player
				current_player++;
				if (current_player>=number_of_players)
				{
					current_player=0;
				}
				while(!player[current_player].ingame)
				{
					current_player++;
					if (current_player==number_of_players)
					{
						current_player=0;
					}
				}
							
				currentrail=new int[number_of_players+10];
				crnum=0;
				
				if (player[current_player].computer)
				{
					initial=false;
					//if at a city, roll for next destination
					if (player[current_player].source==-1)
					{
						newcity();
						if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
						{
							won=true;
							wongame[current_player]=true;
							doEndSelection(player[d1].name+" wins!");
							return;
						}
					}
				
					//roll dice
					d1=getRandom(6)+1;
					d2=getRandom(6)+1;
					d3=getRandom(6)+1;
					bonus=((d1+d2==12)||((d1==d2)&&(player[current_player].train==1))||(player[current_player].train==2));
					d=d1+d2;
					b=d3;
					String m=player[current_player].name+" moves "+d+" spaces";
					if (bonus)
						m+=" + "+d3+" bonus";
//					message(m);
				
					move(d);
					
					if ((declared[current_player])&&(player[current_player].money<winningmoney))
					{
						message(player[current_player].name+" is no longer declared");
						declared[current_player]=false;
					}
					
					if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
					{
						won=true;
						wongame[current_player]=true;
						doEndSelection(player[d1].name+" wins!");
						return;
					}
					if (!mustdeclare && player[current_player].money>=winningmoney)
					{
						won=true;
						wongame[current_player]=true;
						doEndSelection(player[d1].name+" wins!");
						return;
					}
						
					//if at arrival
					if ((player[current_player].location==player[current_player].destination)&&(player[current_player].ingame))
					{
						arrive();
					}
					
					if (player[current_player].money<0)
					{
//						togame2=true;
						player[current_player].sell();
						phaseLock.lockWait();
					}
					//if at arrival
					if ((player[current_player].location==player[current_player].destination)&&(player[current_player].ingame))
					{
						if (bonus)
						{
							newcity();
							if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
							{
								won=true;
								wongame[current_player]=true;
								doEndSelection(player[current_player].name+" wins!");
								return;
							}
						}
					}
										
					//if bonus, move again
					if ((bonus)&&(player[current_player].ingame))
					{
						move(b);
						if ((declared[current_player])&&(player[current_player].money<winningmoney))
						{
							declared[current_player]=false;
							message(player[current_player].name+" is no longer declared");
						}
						
						if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
						{
							won=true;
							wongame[current_player]=true;
							doEndSelection(player[current_player].name+" wins!");
							return;
						}
						
						if ((player[current_player].location==player[current_player].destination)&&(player[current_player].ingame))
							arrive();
						//if player is out of cash, player loses
						if (player[current_player].money<0)
						{
//							togame2=false;
							player[current_player].sell();
							phaseLock.lockWait();
						}
					}
					if (!waitaftercomputer)
					{
						railboard.postInvalidate();
					}
					else
					{
						buttonwait("Continue");
						phaseLock.lockWait();
					}
					
				}
				else
				{
					if ((!waitaftercomputer)&&(!initial))
					{
						turndone=true;
						buttonwait("Continue");
						phaseLock.lockWait();
					}
					else
					{
						initial=false;
						humanturn(0);
						phaseLock.lockWait();
					}
				}
			}
		}
	}
	
/*	public void game()
	{
		int d1,d2,d3,d,i;
		
		//check if only one player left
		d=0;
		d1=0;
		for (i=0; i<number_of_players; i++)
		{
			if (player[i].ingame)
			{
				d++;
				d1=i;
			}
		}
		if (d==1)
		{
			won=true;
			wongame[d1]=true;
			doEndSelection(player[d1].name+" wins by default!");
		}
		if (d==0)
		{
			won=true;
			doEndSelection(player[d1].name+" wins by default!");
		}
				
		if(!won)
		{
			buttondisable();
			
			//move to the next player
			current_player++;
			if (current_player>=number_of_players)
			{
				current_player=0;
			}
			while(!player[current_player].ingame)
			{
				current_player++;
				if (current_player==number_of_players)
				{
					current_player=0;
				}
			}
						
			currentrail=new int[number_of_players+10];
			crnum=0;
			
			if (player[current_player].computer)
			{
				initial=false;
				//if at a city, roll for next destination
				if (player[current_player].source==-1)
				{
					newcity();
					if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
					{
						won=true;
						wongame[current_player]=true;
						doEndSelection(player[d1].name+" wins!");
						return;
					}
				}
			
				//roll dice
				d1=getRandom(6)+1;
				d2=getRandom(6)+1;
				d3=getRandom(6)+1;
				bonus=((d1+d2==12)||((d1==d2)&&(player[current_player].train==1))||(player[current_player].train==2));
				d=d1+d2;
				b=d3;
				String m=player[current_player].name+" moves "+d+" spaces";
				if (bonus)
					m+=" + "+d3+" bonus";
//				message(m);
			
				move(d);
				
				if ((declared[current_player])&&(player[current_player].money<winningmoney))
				{
					message(player[current_player].name+" is no longer declared");
					declared[current_player]=false;
				}
				
				if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
				{
					won=true;
					wongame[current_player]=true;
					doEndSelection(player[d1].name+" wins!");
					return;
				}
				if (!mustdeclare && player[current_player].money>=winningmoney)
				{
					won=true;
					wongame[current_player]=true;
					doEndSelection(player[d1].name+" wins!");
					return;
				}
					
				//if at arrival
				if ((player[current_player].location==player[current_player].destination)&&(player[current_player].ingame))
				{
					arrive();
				}
				
				if (player[current_player].money<0)
				{
					togame2=true;
					player[current_player].sell();
				}
				else
					game2();
			}
			else
			{
				if ((!waitaftercomputer)&&(!initial))
				{
					turndone=true;
					buttonwait("Continue");
				}
				else
				{
					initial=false;
					humanturn(0);
				}
			}
		}
	}*/
	
/*	public void game2()
	{
		//if at arrival
		if ((player[current_player].location==player[current_player].destination)&&(player[current_player].ingame))
		{
			if (bonus)
			{
				newcity();
				if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
				{
					won=true;
					wongame[current_player]=true;
					doEndSelection(player[current_player].name+" wins!");
					return;
				}
			}
		}
							
		//if bonus, move again
		if ((bonus)&&(player[current_player].ingame))
		{
			move(b);
			if ((declared[current_player])&&(player[current_player].money<winningmoney))
			{
				declared[current_player]=false;
				message(player[current_player].name+" is no longer declared");
			}
			
			if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
			{
				won=true;
				wongame[current_player]=true;
				doEndSelection(player[current_player].name+" wins!");
				return;
			}
			
			if ((player[current_player].location==player[current_player].destination)&&(player[current_player].ingame))
				arrive();
			//if player is out of cash, player loses
			if (player[current_player].money<0)
			{
				togame2=false;
				player[current_player].sell();
			}
		}
		game3();
	}
	
	public void game3()
	{
		if (!waitaftercomputer)
		{
			railboard.postInvalidate();
			game();
		}
		else
			buttonwait("Continue");
	}*/
	
	public void newcity()
	{
		int d;
		
		if (player[current_player].money>=winningmoney)
			player[current_player].shoulddeclare();
		d=getregion(getRandom(6)+1,getRandom(6)+1,getRandom(6)+1);
		//choose region if same as current
		if (d==getcurrentregion(player[current_player].location))
			d=chooseregion();
		d=getcity(d,getRandom(6)+1,getRandom(6)+1,getRandom(6)+1);
		player[current_player].source=player[current_player].destination;
		player[current_player].destination=d;
	}
	
	public int chooseregion()
	{
		int i;
		
		//choose a region as far as possible from the current region
		i=getcurrentregion(player[current_player].location);
		if (i==0)
			return(5);
		else if (i==1)
			return(5);
		else if (i==2)
			return(6);
		else if (i==3)
			return(6);
		else if (i==4)
			return(2);
		else if (i==5)
			return(0);
		else
			return(2);
	}
	
	//computer piece move d spaces
	public void move(int d)
	{
		int i,j;
		int railcurrent;
				
		//position board at player
		railboard.positionAt(town[player[current_player].location].x, town[player[current_player].location].y);
						
		//move the player
//		board.motion=true;
		for (i=0; i<d; i++)
		{
			try{
			//find next stop
			if (!declared[current_player])
				player[current_player].shortestpath(true,player[current_player].destination);
			else
				player[current_player].shortestpath(false,player[current_player].home);
			player[current_player].shouldpursue();
			//pay usage fee (unless at destination)
			if (player[current_player].railpath[player[current_player].location]!=-1)
			{
				for (j=0; j<crnum; j++)
					if (currentrail[j]==railroad[player[current_player].railpath[player[current_player].location]].player)
						break;
				if (j==crnum)
				{
					railcurrent=railroad[player[current_player].railpath[player[current_player].location]].player;
					currentrail[crnum]=railcurrent;
					crnum++;
					if (railcurrent==current_player)
					{
						player[current_player].money-=ownrailcost;
						if (exempt_rail[current_player]!=player[current_player].railpath[player[current_player].location])
							exempt_rail_cost[current_player]=ownrailcost;
						exemption_used[current_player]=false;
					}
					else if (railcurrent==-1)
					{
						player[current_player].money-=bankrailcost;
						if (exempt_rail[current_player]!=player[current_player].railpath[player[current_player].location])
							exempt_rail_cost[current_player]=bankrailcost;
						exemption_used[current_player]=false;
					}
					else if (!allrailsbought)
					{
						if ((exempt_rail[current_player]==player[current_player].railpath[player[current_player].location])&&(exempt_rail_cost[current_player]!=railcost1))
						{
							player[current_player].money-=exempt_rail_cost[current_player];
							player[railcurrent].money+=exempt_rail_cost[current_player];
							exemption_used[current_player]=true;
//							message(player[current_player].name+" pays $"+exempt_rail_cost[current_player]+" to "+player[railcurrent].name);
						}
						else
						{
							player[current_player].money-=railcost1;
							player[railcurrent].money+=railcost1;
							exemption_used[current_player]=false;
//							message(player[current_player].name+" pays $"+railcost1+" to "+player[railcurrent].name);
						}
						if (exempt_rail[current_player]!=player[current_player].railpath[player[current_player].location])
							exempt_rail_cost[current_player]=railcost1;
					}
					else
					{
						if ((exempt_rail[current_player]==player[current_player].railpath[player[current_player].location])&&(exempt_rail_cost[current_player]!=railcost2))
						{
							player[current_player].money-=exempt_rail_cost[current_player];
							player[railcurrent].money+=exempt_rail_cost[current_player];
							exemption_used[current_player]=true;
//							message(player[current_player].name+" pays $"+exempt_rail_cost[current_player]+" to "+player[railcurrent].name);
						}
						else
						{
							player[current_player].money-=railcost2;
							player[railcurrent].money+=railcost2;
							exemption_used[current_player]=false;
//							message(player[current_player].name+" pays $"+railcost2+" to "+player[railcurrent].name);
						}
						if (exempt_rail[current_player]!=player[current_player].railpath[player[current_player].location])
							exempt_rail_cost[current_player]=railcost2;
					}
				}
				if (!exemption_used[current_player])
					exempt_rail[current_player]=player[current_player].railpath[player[current_player].location];
			}
			//move
			if (town[player[current_player].townpath[player[current_player].location]].x>town[player[current_player].location].x)
				player[current_player].lastmove=true;
			else
				player[current_player].lastmove=false;
				
			//change the location
			player[current_player].location=player[current_player].townpath[player[current_player].location];
			//any declared players that are jumped become undeclared
			for (j=0; j<number_of_players; j++)
				if ((j!=current_player)&&(player[j].location==player[current_player].location)&&(declared[j]))
				{
					declared[j]=false;
					message(player[j].name+" is robbed by "+player[current_player].name+" and is no longer declared");
					player[j].money-=stolenmoney;
					player[current_player].money+=stolenmoney;
						
				}
			//if player moved out of view, reposition board
			railboard.positionAt(town[player[current_player].location].x, town[player[current_player].location].y);
			}catch(NullPointerException e){}
//			railboard.invalidate();
			railboard.postInvalidate();
//			railboard.refreshDrawableState();
			try { Thread.sleep(animation); } catch(InterruptedException e){ }
		}
//		board.motion=false;
	}
	
	public void arrive()
	{
		int i,j;
		
		player[current_player].money+=payoff(player[current_player].source,player[current_player].destination);
//		message(player[current_player].name+" collects $"+payoff(player[current_player].source,player[current_player].destination));
		player[current_player].source=-1;
		
		if (player[current_player].money>0)
		{
			i=player[current_player].shouldbuy();
			if (i!=-1)
			{
				if (i<=28)
				{
					player[current_player].money-=railroad[i].cost;
					railroad[i].player=current_player;
					message(player[current_player].name+" buys the "+railroad[i].name);
					//redraw the board
					railboard.postInvalidate();
					
					for (j=0; j<28; j++)
						if (railroad[j].player==-1)
							break;
					if (j==28)
					{
						if (!allrailsbought)
						{
							message("Rail use fees are now $"+railcost2);
						}
						allrailsbought=true;
					}
				}
				else if (i==29)
				{
					player[current_player].money-=4000;
					player[current_player].train=1;
//					message(player[current_player].name+" upgrades to Express");
				}
				else
				{
					player[current_player].money-=40000;
					player[current_player].train=2;
//					message(player[current_player].name+" upgrades to Superchief");
				}
			}
		}
	}
	
	public void humanturn(int stage)
	{
		int r,i;

		//stage 0:
		//player's turn is beginning
		//start rolling process
		if (stage==0)
		{
			//position board at player
			railboard.positionAt(town[player[current_player].location].x,town[player[current_player].location].y);
		
			if (player[current_player].source==-1)
			{
				if ((player[current_player].money>=winningmoney)&&(!declared[current_player]))
				{
					status=1;
					getDeclareSelection();
				}
				else if (declared[current_player])
				{
					//win by declaring on town that you're stopped in?
					if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
						humanturn(14);
					else
					{
						status=1;
						getDiceRoll("Roll to choose an alternative region",3);
					}
				}
				else
				{
					status=1;
					getDiceRoll("Roll to choose a region",3);
				}
			}
			else
			{
				status=4;
				getDiceRoll("Roll to move",3);
			}
		}
		//stage 1: select region, prompt to roll for city
		else if ((stage==1)||(stage==9))
		{
			r=getregion(die1,die2,die3);
			//detect if the region is the same as current
			if (getcurrentregion(player[current_player].location)==r)
			{
				if (status==1)
					status=2;
				else
					status=13;
				getRegionSelection("Choose a destination region");
			}
			else
			{
				String instruction1="Your region is ";
				if (r==0)
					instruction1+="Northeast";
				else if (r==1)
					instruction1+="North Central";
				else if (r==2)
					instruction1+="Southeast";
				else if (r==3)
					instruction1+="South Central";
				else if (r==4)
					instruction1+="Plains";
				else if (r==5)
					instruction1+="Southwest";
				else if (r==6)
					instruction1+="Northwest";
				instruction1+=". Roll for a city";
				a=r;
				if (status==1)
					status=3;
				else
					status=10;
				getDiceRoll(instruction1,3);
			}
		}
		//player won
		else if (stage==14)
		{
			if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
			{
				won=true;
				wongame[current_player]=true;
				status=0;
				doEndSelection(player[current_player].name+" wins!");
			}
			else if (!mustdeclare && player[current_player].money>=winningmoney)
			{
				won=true;
				wongame[current_player]=true;
				status=0;
				doEndSelection(player[current_player].name+" wins!");				
			}
		}
		//stage 2: geographic region known, now need city
		else if ((stage==2)||(stage==13))
		{
			if (status==2)
				status=3;
			else
				status=10;
			getDiceRoll("Roll for a city",3);
		}
		//stage 3: destination known.  now need movement roll.
		else if ((stage==3)||(stage==10))
		{
			r=getcity(a,die1,die2,die3);
			fulltrajectory=new int[500];
			fulltrajectory[0]=player[current_player].destination;
			fullindex=1;
			//check if you're already there
			if (player[current_player].destination==r)
			{
				moves=0;
				status=0;
				buttonwait("Continue");
			}
			//announce destination and prompt to roll to move
			else if (stage==3)
			{
				String instruction1;
				for (i=0; i<28; i++)
					if ((town[r].rails[i])&&((railroad[i].player==current_player)||(railroad[i].player==-1)))
						break;
				if (i==28)
					instruction1="Your destination is --"+town[r].name;
				else
					instruction1="Your destination is "+town[r].name;
				player[current_player].source=player[current_player].destination;
				player[current_player].destination=r;
				status=4;
				getDiceRoll(instruction1,3);
			}
			else
			{
				player[current_player].source=player[current_player].destination;
				player[current_player].destination=r;
				status=11;
				trajectory=new int[19];
				trajrails=new int[19];
				trnum=0;
				moves=0;
				movecost=0;
				trajectory[0]=player[current_player].location;
				trajrails[0]=-1;
				//suggest a move
				if (!declared[current_player])
					player[current_player].shortestpath(true,player[current_player].destination);
				else
					player[current_player].shortestpath(false,player[current_player].home);
				railboard.postInvalidate();
				xx=0;
				yy=0;
				buttonwait("Take Suggested Move");
				humanturn(11);
			}
		}
		//stage 4: let player figure out how to move
		else if (stage==4)
		{
			bonus=((die1+die2==12)||((die1==die2)&&(player[current_player].train==1))||(player[current_player].train==2));
			a=die1+die2;
			b=die3;
			
			String m=player[current_player].name+" moves "+a+" spaces";
			if (bonus)
				m+=" + "+b+" bonus";
//			message(m);
			if (!bonus)
				b=0;
			
			String instruction1;
			//if declared, dest is home
			int dest;
			if (declared[current_player])
				dest=player[current_player].home;
			else
				dest=player[current_player].destination;
			//announce destination (-- means can't reach on own rails)
			for (i=0; i<28; i++)
				if ((town[dest].rails[i])&&((railroad[i].player==current_player)||(railroad[i].player==-1)))
					break;
			if (i==28)
				instruction1="Your destination is --"+town[dest].name;
			else
				instruction1="Your destination is "+town[dest].name;
//			message(instruction1);
			String instruction2="Move "+a+" spaces";
			if (bonus)
				instruction2+=" plus a bonus of "+b;
			movecost=0;
//			message("You have "+(a+b)+" spaces left ($-"+movecost+")");
			
			setStatusBar(instruction1+". "+instruction2);

			//initialize structure to hold move
			trajectory=new int[19];
			trajrails=new int[19];
			trajplay=new int[number_of_players+10];
			moves=0;
			movecost=0;
			trajectory[0]=player[current_player].location;
			trajrails[0]=-1;
			trnum=0;
			
			//suggest a move
			if (declared[current_player])
				player[current_player].shortestpath(false,player[current_player].home);
			else
				player[current_player].shortestpath(true,player[current_player].destination);
			
			status=5;
			//position board at player
			//STRANGE?
			railboard.positionAt(town[player[current_player].location].x, town[player[current_player].location].y);
			railboard.postInvalidate();
			buttonwait("Take Suggested Move");
		}
		//user has selected a town along the route
		//fill in the route to the selected town
		//stage 5: start of move; stage 11: only bonus die left
		else if (stage==5 || stage==11)
		{
			//xx=x coordinate selected, yy=y coordinate selected (adjusted for offsets)
			if (stage==5)
				plottrajectory(xx,yy,a+b);
			else
				plottrajectory(xx,yy,b);
			
			String instruction1;
			int dest;
			if (declared[current_player])
				dest=player[current_player].home;
			else
				dest=player[current_player].destination;
			for (i=0; i<28; i++)
				if ((town[dest].rails[i])&&((railroad[i].player==current_player)||(railroad[i].player==-1)))
					break;
			if (i==28)
				instruction1="Your destination is --"+town[dest].name;
			else
				instruction1="Your destination is "+town[dest].name;
			
			if (stage==5)
			{
				instruction1+=", "+(a+b-moves)+" steps left ($-"+movecost+")";
			}
			else
			{
				instruction1+=", "+(b-moves)+" steps left ($-"+movecost+")";
			}
			setStatusBar(instruction1);
			
			if (stage==5)
				status=5;
			else
				status=11;
			
			railboard.postInvalidate();
			int totalmove;
			if (stage==5)
				totalmove=a+b;
			else
				totalmove=b;
			if ((moves==totalmove)||(trajectory[moves]==player[current_player].destination)
				||((trajectory[moves]==player[current_player].home)&&(declared[current_player])))
			{
				buttonwait("Finished");
			}
			else if (moves==0)
				buttonwait("Take Suggested Move");
			else
			{
				buttonwait("Undo");
			}
		}
		//trajectory selected.  ready to move.
		else if (stage==6 || stage==12)
		{
			setStatusBar("");
			movetrajectory();
			railboard.postInvalidate();
			//check if won
			if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
			{
				humanturn(14);
				return;
			}
			else if (!mustdeclare && player[current_player].money>=winningmoney)
			{
				humanturn(14);
				return;
			}
				
			//arrival
			if((player[current_player].location==player[current_player].destination)&&(player[current_player].ingame))
			{
				//buy railroads here
				player[current_player].money+=payoff(player[current_player].source,player[current_player].destination);
//				message(player[current_player].name+" collects $"+payoff(player[current_player].source,player[current_player].destination));
				player[current_player].source=-1;
				
				//if player is out of cash, player loses
				if (player[current_player].money<0)
				{
					buttondisable();
					doSell();
				}
				//player can buy a rail
				else if ((player[current_player].location==player[current_player].destination)&&(player[current_player].ingame))
				{
					buttondisable();
					doRailBuy();
				}

				if ((stage==6)&&(moves<=a)&&(bonus))
				{
					status=8;
					getDiceRoll("Bonus",1);
				}
				else
				{
					status=0;
					buttonwait("Continue");
				}
			}
			//hasn't arrived yet
			else
			{
				//if player is out of cash, player loses
				if (player[current_player].money<0)
				{
					buttondisable();
					doSell();
				}
				else
				{
					buttonwait("Continue");
					status=0;
				}
			}
		}
		//arrived at city, bonus roll remaining
		else if (stage==8)
		{
			if (!mustdeclare && player[current_player].money>=winningmoney)
			{
				humanturn(14);
			}
			else if ((player[current_player].money>=winningmoney)&&(!declared[current_player]))
			{
				getDeclareSelection();
			}
			else if (declared[current_player])
			{
				if ((declared[current_player])&&(player[current_player].location==player[current_player].home))
					humanturn(14);
				else
				{
					status=1;
					getDiceRoll("Roll to choose an alternative region",3);
				}
			}
			else
			{
				status=9;
				getDiceRoll("Roll to choose a region",3);
			}
		}
	}
	
	public void movetrajectory()
	{
		int i,j;
		
		//move along the trajectory
		if (moves!=0)
		{
			//position board at player
			railboard.positionAt(town[player[current_player].location].x, town[player[current_player].location].y);
			//move the player
//			board.motion=true;
			for (i=1; i<=moves; i++)
			{
				//pay usage fee (unless at destination)
				if (trajrails[i]!=-1)
				{
					for (j=0; j<crnum; j++)
						if (railroad[trajrails[i]].player==currentrail[j])
							break;
					if (j==crnum)
					{
						currentrail[crnum]=railroad[trajrails[i]].player;
						crnum++;
						if (currentrail[crnum-1]==current_player)
						{
							player[current_player].money-=ownrailcost;
							if (exempt_rail[current_player]!=trajrails[i])
								exempt_rail_cost[current_player]=ownrailcost;
							exemption_used[current_player]=false;
						}
						else if (currentrail[crnum-1]==-1)
						{
							player[current_player].money-=bankrailcost;
							if (exempt_rail[current_player]!=trajrails[i])
								exempt_rail_cost[current_player]=bankrailcost;
							exemption_used[current_player]=false;
						}
						else if (!allrailsbought)
						{
							if ((exempt_rail[current_player]==trajrails[i])&&(exempt_rail_cost[current_player]!=railcost1))
							{
								player[current_player].money-=exempt_rail_cost[current_player];
								if (currentrail[crnum-1]>=0)
									player[currentrail[crnum-1]].money+=exempt_rail_cost[current_player];
								exemption_used[current_player]=true;
//								message(player[current_player].name+" pays $"+exempt_rail_cost[current_player]+" to "+player[currentrail[crnum-1]].name);
							}
							else
							{
								player[current_player].money-=railcost1;
								if (currentrail[crnum-1]>=0)
									player[currentrail[crnum-1]].money+=railcost1;
								exemption_used[current_player]=false;
//								message(player[current_player].name+" pays $"+railcost1+" to "+player[currentrail[crnum-1]].name);
							}
							if (exempt_rail[current_player]!=trajrails[i])
								exempt_rail_cost[current_player]=railcost1;
						}
						else
						{
							if ((exempt_rail[current_player]==trajrails[i])&&(exempt_rail_cost[current_player]!=railcost2))
							{
								player[current_player].money-=exempt_rail_cost[current_player];
								if (currentrail[crnum-1]>=0)
									player[currentrail[crnum-1]].money+=exempt_rail_cost[current_player];
								exemption_used[current_player]=true;
//								message(player[current_player].name+" pays $"+exempt_rail_cost[current_player]+" to "+player[currentrail[crnum-1]].name);
							}
							else
							{
								player[current_player].money-=railcost2;
								if (currentrail[crnum-1]>=0)
									player[currentrail[crnum-1]].money+=railcost2;
								exemption_used[current_player]=false;
//								message(player[current_player].name+" pays $"+railcost2+" to "+player[currentrail[crnum-1]].name);
							}
							if (exempt_rail[current_player]!=trajrails[i])
								exempt_rail_cost[current_player]=railcost2;
						}
					}
					if (!exemption_used[current_player])
						exempt_rail[current_player]=trajrails[i];
				}
				//move
				if (town[trajectory[i]].x>town[player[current_player].location].x)
					player[current_player].lastmove=true;
				else
					player[current_player].lastmove=false;

				player[current_player].location=trajectory[i];
				player[current_player].lastrail=trajrails[i];
				//any declared players that are jumped become undeclared
				for (j=0; j<number_of_players; j++)
					if ((j!=current_player)&&(player[j].location==player[current_player].location)&&(declared[j]))
					{
						declared[j]=false;
						message(player[j].name+" is robbed by "+player[current_player].name+" and is no longer declared");
						player[j].money-=stolenmoney;
						player[current_player].money+=stolenmoney;
						
					}

				//if player moved out of view, reposition board
				railboard.positionAt(town[player[current_player].location].x, town[player[current_player].location].y);
				railboard.postInvalidate();
				try{ Thread.sleep(animation); } catch(InterruptedException e){ }
			}
//			board.motion=false;
		}
		//put trajectory into fulltrajectory
		for (i=1; i<=moves; i++)
		{
			fulltrajectory[fullindex]=trajectory[i];
			fullindex++;
		}
		//check if fulltrajectory traps the player
		for (i=0; i<28; i++)
		{
			if (railroad[i].nextcity1[player[current_player].location]!=-1)
			{
				for (j=0; j<fullindex; j++)
					if (railroad[i].nextcity1[player[current_player].location]==fulltrajectory[j])
						break;
				if(j==fullindex)
					break;
			}
			if (railroad[i].nextcity2[player[current_player].location]!=-1)
			{
				for (j=0; j<fullindex; j++)
					if (railroad[i].nextcity2[player[current_player].location]==fulltrajectory[j])
						break;
				if(j==fullindex)
					break;
			}
			if (railroad[i].nextcity3[player[current_player].location]!=-1)
			{
				for (j=0; j<fullindex; j++)
					if (railroad[i].nextcity3[player[current_player].location]==fulltrajectory[j])
						break;
				if(j==fullindex)
					break;
			}
			if (railroad[i].nextcity4[player[current_player].location]!=-1)
			{
				for (j=0; j<fullindex; j++)
					if (railroad[i].nextcity4[player[current_player].location]==fulltrajectory[j])
						break;
				if(j==fullindex)
					break;
			}
			if (railroad[i].nextcity5[player[current_player].location]!=-1)
			{
				for (j=0; j<fullindex; j++)
					if (railroad[i].nextcity5[player[current_player].location]==fulltrajectory[j])
						break;
				if(j==fullindex)
					break;
			}
		}
		if (i==28)
		{
			//trapped
			fullindex=0;
		}
		//end declared state if money is under 200000
		if ((declared[current_player])&&(player[current_player].money<winningmoney))
		{
			declared[current_player]=false;
			message(player[current_player].name+" is no longer declared");
		}
	}
	
	public void plottrajectory(int x, int y, int spaces)
	{
		try{
		//trajectory is set equal to spaces leading from source to x,y
		//use a breadth-first algorithm to get shortest distance
		//if tie between two paths, use cheapest railroad
		//if no route within a+b, moves=0
		//adjust moves and movecost accordingly
		
		ArrayList<Integer[]> queue,railq;
		int i,j,k,l,m,n,o,p;
		int source,dest;
		boolean stop,found,notthis;
		float kk,mm;
		int top=0;
		
		//first find town(x,y)
		for (i=0; i<number_of_towns; i++)
		{
			if ((Math.abs(town[i].x-x)<precision)&&(Math.abs(town[i].y-y)<precision))
				break;	
		}
		if (i==number_of_towns)
			return;
		dest=i;

		source=trajectory[moves];
		
		//initialize queue
		queue=new ArrayList<Integer[]>();
		railq=new ArrayList<Integer[]>();
		
		queue.add(new Integer[1]);
		queue.get(0)[0]=new Integer(source);
		railq.add(new Integer[1]);
		railq.get(0)[0]=new Integer(0);
		
		//search
		stop=false;
		found=false;
		while(!stop)
		{
			if (queue.get(top).length>spaces+1-moves)
				stop=true;
			else if (queue.get(top)[0].intValue()==dest)
			{
				stop=true;
				found=true;
			}
			else
			{
				for (k=0; k<28; k++)
				{
					if (railroad[k].nextcity1[queue.get(top)[0].intValue()]!=-1)
					{
						for (l=0; l<queue.get(top).length; l++)
							if (queue.get(top)[l].intValue()==railroad[k].nextcity1[queue.get(top)[0].intValue()])
								break;
						if (l==queue.get(top).length)
						{
							Integer[] bottom=new Integer[queue.get(top).length+1];
							for (j=0; j<queue.get(top).length; j++)
								bottom[j+1]=new Integer(queue.get(top)[j]);
							bottom[0]=railroad[k].nextcity1[queue.get(top)[0].intValue()];
							queue.add(bottom);
							bottom=new Integer[railq.get(top).length+1];
							for (j=0; j<railq.get(top).length; j++)
								bottom[j+1]=new Integer(railq.get(top)[j]);
							bottom[0]=new Integer(k);
							railq.add(bottom);
						}
					}
					if (railroad[k].nextcity2[queue.get(top)[0].intValue()]!=-1)
					{
						for (l=0; l<queue.get(top).length; l++)
							if (queue.get(top)[l].intValue()==railroad[k].nextcity2[queue.get(top)[0].intValue()])
								break;
						if (l==queue.get(top).length)
						{
							Integer[] bottom=new Integer[queue.get(top).length+1];
							for (j=0; j<queue.get(top).length; j++)
								bottom[j+1]=new Integer(queue.get(top)[j]);
							bottom[0]=railroad[k].nextcity2[queue.get(top)[0].intValue()];
							queue.add(bottom);
							bottom=new Integer[railq.get(top).length+1];
							for (j=0; j<railq.get(top).length; j++)
								bottom[j+1]=new Integer(railq.get(top)[j]);
							bottom[0]=new Integer(k);
							railq.add(bottom);
						}
					}
					if (railroad[k].nextcity3[queue.get(top)[0].intValue()]!=-1)
					{
						for (l=0; l<queue.get(top).length; l++)
							if (queue.get(top)[l].intValue()==railroad[k].nextcity3[queue.get(top)[0].intValue()])
								break;
						if (l==queue.get(top).length)
						{
							Integer[] bottom=new Integer[queue.get(top).length+1];
							for (j=0; j<queue.get(top).length; j++)
								bottom[j+1]=new Integer(queue.get(top)[j]);
							bottom[0]=railroad[k].nextcity3[queue.get(top)[0].intValue()];
							queue.add(bottom);
							bottom=new Integer[railq.get(top).length+1];
							for (j=0; j<railq.get(top).length; j++)
								bottom[j+1]=new Integer(railq.get(top)[j]);
							bottom[0]=new Integer(k);
							railq.add(bottom);
						}
					}
					if (railroad[k].nextcity4[queue.get(top)[0].intValue()]!=-1)
					{
						for (l=0; l<queue.get(top).length; l++)
							if (queue.get(top)[l].intValue()==railroad[k].nextcity4[queue.get(top)[0].intValue()])
								break;
						if (l==queue.get(top).length)
						{
							Integer[] bottom=new Integer[queue.get(top).length+1];
							for (j=0; j<queue.get(top).length; j++)
								bottom[j+1]=new Integer(queue.get(top)[j]);
							bottom[0]=railroad[k].nextcity4[queue.get(top)[0].intValue()];
							queue.add(bottom);
							bottom=new Integer[railq.get(top).length+1];
							for (j=0; j<railq.get(top).length; j++)
								bottom[j+1]=new Integer(railq.get(top)[j]);
							bottom[0]=new Integer(k);
							railq.add(bottom);
						}
					}
					if (railroad[k].nextcity5[queue.get(top)[0].intValue()]!=-1)
					{
						for (l=0; l<queue.get(top).length; l++)
							if (queue.get(top)[l].intValue()==railroad[k].nextcity5[queue.get(top)[0].intValue()])
								break;
						if (l==queue.get(top).length)
						{
							Integer[] bottom=new Integer[queue.get(top).length+1];
							for (j=0; j<queue.get(top).length; j++)
								bottom[j+1]=new Integer(queue.get(top)[j]);
							bottom[0]=railroad[k].nextcity5[queue.get(top)[0].intValue()];
							queue.add(bottom);
							bottom=new Integer[railq.get(top).length+1];
							for (j=0; j<railq.get(top).length; j++)
								bottom[j+1]=new Integer(railq.get(top)[j]);
							bottom[0]=new Integer(k);
							railq.add(bottom);
						}
					}
				}
				top++;
			}
		}
		//now find the trajectory that has the least cost
		if (found)
		{
			j=-1;
			kk=-1;
			p=-1;
			for (i=0; i<queue.size(); i++)
			{
				if ((queue.get(i)[0].intValue()==dest)&&(queue.get(i).length>1))
				{
					notthis=false;
					for (l=0; l<queue.get(i).length-1; l++)
					{
						for (m=0; m<moves+1; m++)
							if (queue.get(i)[l].intValue()==trajectory[m])
							{
								notthis=true;
							}
						for (m=0; m<fullindex; m++)
							if (queue.get(i)[l].intValue()==fulltrajectory[m])
							{
								notthis=true;
							}
					}
					if (!notthis)
					{
						mm=0;
						
						n=0;
						for (l=railq.get(i).length-1; l>=0; l--)
						{
							for (o=0; o<trnum+n; o++)
								if (trajplay[o]==railroad[railq.get(i)[l].intValue()].player)
									break;
						
							if (o==trnum+n)
							{
								if (railroad[railq.get(i)[l].intValue()].player==current_player)
								{
									mm+=ownrailcost;
								}
								else if (railroad[railq.get(i)[l].intValue()].player==-1)
								{
									mm+=bankrailcost;
								}
								else if ((railroad[railq.get(i)[l].intValue()].player==railroad[player[current_player].lastrail].player)&&(trnum+n==0))
								{
									if (!allrailsbought)
										mm+=railcost1;
									else
										mm+=railcost2;
								}
								else if (trnum+n>0)
								{
									if (railroad[railq.get(i)[l].intValue()].player==railroad[trajrails[trnum+n]].player)
									{
										if (!allrailsbought)
											mm+=railcost1;
										else
											mm+=railcost2;
									}
									else
									{
										if (!allrailsbought)
											mm+=railcost1+0.001;
										else
											mm+=railcost2+0.001;
									}
								}
								else
								{
									if (!allrailsbought)
										mm+=railcost1+0.001;
									else
										mm+=railcost2+0.001;
								}
								trajplay[trnum+n]=railroad[railq.get(i)[l].intValue()].player;
								n++;
							}
						}
						if (j==-1)
						{
							j=i;
							kk=mm;
							p=n;
						}
						else if (kk>mm)
						{
							j=i;
							kk=mm;
							p=n;
						}
					}
				}
			}
			if (j!=-1)
			{
				trnum+=p;
				movecost+=(int)kk;
				//copy trajectory from entry j
				for (i=0; i<queue.get(j).length-1; i++)
				{
					trajectory[moves+queue.get(j).length-i-1]=queue.get(j)[i].intValue();
					trajrails[moves+queue.get(j).length-i-1]=railq.get(j)[i].intValue();
				}
				trajectory[moves]=queue.get(j)[queue.get(j).length-1];
				moves+=queue.get(j).length-1;
			}
		}
		}catch(OutOfMemoryError e){}
	}
	
	boolean[] inauction;
	int winningbidder;
	int auctionCurrent;
	int auctionRail;
	int auctionAmount;

	private void doSell()
	{
		if (player[current_player].money>=0)
		{
			status=0;
			buttonwait("Continue");
		}
		else
		{
		handler.post(new Runnable(){
			public void run()
			{
				AlertDialog.Builder adb=new AlertDialog.Builder(railopoly);
				String msg=player[current_player].name+" you need $"+((-1)*player[current_player].money)+".  Choose a railroad to sell";
				adb.setTitle(msg);
				
				//get all rails
				int j=0;
				for (int i=0; i<28; i++)
					if (railroad[i].player==current_player)
						j++;
				raillist=new int[j];
				j=0;
				for (int i=0; i<28; i++)
					if (railroad[i].player==current_player)
					{
						raillist[j]=i;
						j++;
					}
				//organize by cost
				for (int i=0; i<raillist.length; i++)
				{
					int k=i;
					for (j=i; j<raillist.length; j++)
						if (railroad[raillist[j]].cost>railroad[raillist[k]].cost)
							k=j;
					int m=raillist[k];
					for (j=k; j>i; j--)
						raillist[j]=raillist[j-1];
					raillist[i]=m;
				}
				String[] railchoice=new String[raillist.length];
				for (int i=0; i<raillist.length; i++)
				{
					railchoice[i]=railroad[raillist[i]].name+"  ($"+railroad[raillist[i]].cost/2+")";
				}

				final CharSequence[] items=railchoice;
				adb.setSingleChoiceItems(items, 0, new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						selected=which;
						if (selected>=0)
							railroad_selected=raillist[selected];
						else
							railroad_selected=-1;
						railboard.invalidate();
					}});
				if (raillist.length>0)
				{
					selected=0;
			    	adb.setPositiveButton("Sell", new OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							railroad[raillist[selected]].player=-1;
							player[current_player].money+=railroad[raillist[selected]].cost/2;
							railroad_selected=-1;
							railboard.postInvalidate();
	
							if (player[current_player].money<0)
							{
								doSell();
							}
							else
								buttonwait("Continue");
						}});
			    	adb.setNeutralButton("Auction", new OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							startAuction(raillist[selected]);
						}});
				}
		    	adb.setNegativeButton("Resign", new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						player[current_player].ingame=false;
						message(player[current_player].name+" resigns");
						railboard.postInvalidate();
						status=0;
						buttonwait("Continue");
					}});
		    	AlertDialog alert=adb.create();
		    	alert.show();
			}
		});
		}
	}
	
	public void startAuction(int rail)
	{
//		message(""+railroad[rail].name+" is up for auction");
		railroad_selected=rail;
		railboard.postInvalidate();
		auctionAmount=railroad[rail].cost/2;

		inauction=new boolean[number_of_players];
		for (int i=0; i<number_of_players; i++)
		{
			if (player[i].ingame)
			{
				inauction[i]=true;
				player[i].autobid=false;
			}
		}
		inauction[railroad[rail].player]=false;
		winningbidder=-1;
		auctionCurrent=railroad[rail].player;
		auctionRail=rail;

		doAuctionRound();
	}

	private void doAuctionRound()
	{
		while(true)
		{
			int i,j;
			
			j=0;
			for (i=0; i<number_of_players; i++)
				if (inauction[i])
				{
					j++;
				}
			if ((j==1)&&(winningbidder!=-1))
			{
				//winning message
				player[railroad[auctionRail].player].money+=auctionAmount-500;
				railroad[auctionRail].player=winningbidder;
				player[winningbidder].money-=auctionAmount-500;
				message(player[winningbidder].name+" wins the "+railroad[auctionRail].name+" for $"+(auctionAmount-500));
				railroad_selected=-1;
				railboard.postInvalidate();
				
				if (player[current_player].computer)
					player[current_player].sell();
				else
					doSell();
				return;
			}
			else if (j==0)
			{
				//sell
				player[railroad[auctionRail].player].money+=railroad[auctionRail].cost/2;
				message(player[railroad[auctionRail].player].name+" sells the "+railroad[auctionRail].name);
				railroad[auctionRail].player=-1;
				railroad_selected=-1;
				railboard.postInvalidate();
				
				if (player[current_player].computer)
					player[current_player].sell();
				else
					doSell();
				return;
			}
			else
			{
				do
				{
					auctionCurrent+=1;
					if (auctionCurrent>=number_of_players)
						auctionCurrent=0;
				} while(!inauction[auctionCurrent]);
				
				
				if (player[auctionCurrent].computer)
				{
					if (player[auctionCurrent].bid(auctionRail,auctionAmount))
					{
						String msg=player[auctionCurrent].name+" bids "+auctionAmount;
						if (showcash)
							msg+=" (out of $"+player[auctionCurrent].money+")";
//						fastmessage(msg);
						auctionAmount+=500;
						winningbidder=auctionCurrent;
					}
					else
					{
						fastmessage(player[auctionCurrent].name+" withdraws");
						inauction[auctionCurrent]=false;
					}
					continue;
//					doAuctionRound();
				}
				else if (player[auctionCurrent].autobid)
				{
					if (player[auctionCurrent].money>=auctionAmount)
					{
						String msg=player[auctionCurrent].name+" bids "+auctionAmount;
						if (showcash)
							msg+=" (out of $"+player[auctionCurrent].money+")";
//						fastmessage(msg);
						auctionAmount+=500;
						winningbidder=auctionCurrent;
//						doAuctionRound();
						continue;
					}
					else if (overbidding)
					{
						player[auctionCurrent].autobid=false;
						railboard.postInvalidate();
						if ((player[auctionCurrent].money>=auctionAmount)||(overbidding))
							getBid(true,false,true);
						else
							getBid(false,false,true);
					}
					else
					{
						player[auctionCurrent].autobid=false;
						getBid(false,false,true);
					}
				}
				else
				{
					railboard.postInvalidate();
					if (player[auctionCurrent].money>=auctionAmount)
					{
						getBid(true,true,true);
					}
					else if (overbidding)
					{
						getBid(true,false,true);
					}
					else
						getBid(false,false,true);
				}
			}
			
		}
	}
	
/*	private void doAuctionRound()
	{
		int i,j;
		
		j=0;
		for (i=0; i<number_of_players; i++)
			if (inauction[i])
			{
				j++;
			}
		if ((j==1)&&(winningbidder!=-1))
		{
			//winning message
			player[railroad[auctionRail].player].money+=auctionAmount-500;
			railroad[auctionRail].player=winningbidder;
			player[winningbidder].money-=auctionAmount-500;
			message(player[winningbidder].name+" wins the "+railroad[auctionRail].name+" for $"+(auctionAmount-500));
			railroad_selected=-1;
			railboard.postInvalidate();
			
			if (player[current_player].computer)
				player[current_player].sell();
			else
				doSell();
		}
		else if (j==0)
		{
			//sell
			player[railroad[auctionRail].player].money+=railroad[auctionRail].cost/2;
			message(player[railroad[auctionRail].player].name+" sells the "+railroad[auctionRail].name);
			railroad[auctionRail].player=-1;
			railroad_selected=-1;
			railboard.postInvalidate();
			
			if (player[current_player].computer)
				player[current_player].sell();
			else
				doSell();
		}
		else
		{
			do
			{
				auctionCurrent+=1;
				if (auctionCurrent>=number_of_players)
					auctionCurrent=0;
			} while(!inauction[auctionCurrent]);
			
			
			if (player[auctionCurrent].computer)
			{
				if (player[auctionCurrent].bid(auctionRail,auctionAmount))
				{
					String msg=player[auctionCurrent].name+" bids "+auctionAmount;
					if (showcash)
						msg+=" (out of $"+player[auctionCurrent].money+")";
//					fastmessage(msg);
					auctionAmount+=500;
					winningbidder=auctionCurrent;
				}
				else
				{
					fastmessage(player[auctionCurrent].name+" withdraws");
					inauction[auctionCurrent]=false;
				}
				doAuctionRound();
			}
			else if (player[auctionCurrent].autobid)
			{
				if (player[auctionCurrent].money>=auctionAmount)
				{
					String msg=player[auctionCurrent].name+" bids "+auctionAmount;
					if (showcash)
						msg+=" (out of $"+player[auctionCurrent].money+")";
//					fastmessage(msg);
					auctionAmount+=500;
					winningbidder=auctionCurrent;
					doAuctionRound();
				}
				else if (overbidding)
				{
					player[auctionCurrent].autobid=false;
					railboard.postInvalidate();
					if ((player[auctionCurrent].money>=auctionAmount)||(overbidding))
						getBid(true,false,true);
					else
						getBid(false,false,true);
				}
				else
				{
					player[auctionCurrent].autobid=false;
					getBid(false,false,true);
				}
			}
			else
			{
				railboard.postInvalidate();
				if (player[auctionCurrent].money>=auctionAmount)
				{
					getBid(true,true,true);
				}
				else if (overbidding)
				{
					getBid(true,false,true);
				}
				else
					getBid(false,false,true);
			}
		}
	}
	*/
	
	private void getBid(final boolean bid, final boolean bidmax, final boolean pass)
	{
		handler.post(new Runnable(){
			public void run()
			{
    	AlertDialog.Builder adb=new AlertDialog.Builder(railopoly);
    	String message=railroad[auctionRail].name+" is up for auction by "+player[railroad[auctionRail].player].name+".  ";
		message+=player[auctionCurrent].name+":  Will you bid $"+auctionAmount+" or withdraw? (You have $"+player[auctionCurrent].money+")";
    	adb.setMessage(message);
    	if (bid)
    	{
    	adb.setPositiveButton("Bid", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				winningbidder=auctionCurrent;
//				fastmessage(player[auctionCurrent].name+" bids "+auctionAmount);
				auctionAmount+=500;
				auctionLock.postAuctionLockResume();
//				doAuctionRound();
			}});
    	}
    	if (bidmax)
    	{
    	adb.setNeutralButton("Bid to Max", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				winningbidder=auctionCurrent;
//				fastmessage(player[auctionCurrent].name+" bids "+auctionAmount);
				auctionAmount+=500;
				player[auctionCurrent].autobid=true;
				auctionLock.postAuctionLockResume();
//				doAuctionRound();
			}});
    	}
    	if (pass)
    	{
    	adb.setNegativeButton("Withdraw", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				fastmessage(player[auctionCurrent].name+" withdraws");
				inauction[auctionCurrent]=false;
				auctionLock.postAuctionLockResume();
//				doAuctionRound();
			}});
    	}
    	adb.show();
		}
		});		
		auctionLock.lockWait();
	}
    public void onConfigurationChanged(Configuration newConfig) 
    {
    	  super.onConfigurationChanged(newConfig);
    }
	public class Lock
	{
		public void lockWait()
		{
			synchronized(this)
			{
				try
				{
					this.wait();
				}
				catch(InterruptedException e)
				{
				}
			}
		}
		public void lockResume()
		{
			synchronized(this)
			{
				this.notify();
			}
		}
		public void postPhaseLockResume()
		{
			handler.post(new Runnable(){
				public void run() {
					phaseLock.lockResume();
				}});
		}
		public void postAuctionLockResume()
		{
			handler.post(new Runnable(){
				public void run() {
					auctionLock.lockResume();
				}});
		}
	}
}