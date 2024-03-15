package anisquare;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.awt.Point;
import java.util.HashMap;

import javafx.scene.canvas.GraphicsContext;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.Rectangle;

import java.io.*;


public class  AniSquare implements AnimatedThing
{
	/*
	Public methods:
		changeAnimation (String to) = Changes the animation from the outside 
		checkPulse() = returns true if it is allowed to remain, if it is false then it needs to be removed
		kill() = sets alive to false, marking it for destruction;
		mainGear() = To be called in a timer function;
		massGear(List<AniSquare> from) = Sets off all mainGear in the list;
		pootRange(String name, Point iRange) = Sets a range; 
		reapDead(List<AniSquare>) = Static checks the list for elements that are "dead", dead elements are to be removed
		renderGear() = Renders the anisquare to the canvas
		renderGearMass(ArrayList<AniSquare>) = Renders anisquares in bulk
		setAnimationLock (boolean to) = if false then the animation can be switched from the outside
		setCanStay(boolean) = if false will "die" upon reaching final frame
		setFrameIndex(int) = goes to a particular frame;
		setFrameRate(int) = Sets the frame rate;
		setFrameReset(boolean) = toggles whether or not the frame index will reset upon animation change
		setGraphicsContext(GraphicsContext) = Sets GraphicsContext;
		setPlayDirection(going) = sets the how it plays;
			going.PLAY: Loops Normally
			going.STOP: Default, doesn't move;
			going.REVERSE: Loops backwards;
			going.OSCILLATE: Reverses at the last frame and plays normally when it reaches the first frame
		setScale(to int) = Static, sets the scale for all instances; 
		setHighBounds (Point2D.Double)/setLowBounds(Point2D.Double) = Sets the bounds.
		
		
	Protected methods:
	switchAnimation(String key) = switches animation range to the string.
	
	*/
	
	public static enum going {PLAY,PLAY_ONCE,STOP,REVERSE,REVERSE_ONCE,OSCILLATE}
	
	private boolean osciPlay = true; //If it is playing normally
	private boolean canStay = true; //If false then it dies instead of looping, does not work if set to OSCILLATE
	private static int imgScale = 1;
	private int imgScaleLast = imgScale;//Used to automatically update images;
	
	//private ArrayList<Image> meFrames = new ArrayList<Image>();
	//private HashMap <String,ArrayList<Image>> meAnimations = new HashMap<>();//Book of animations
	//private HashMap <String,String[]> meAnimationsSoft = new HashMap<>();
	//private String meAnimationsIndex = "default";
	
	private going playDirection = going.STOP;
	
	private Point meFramesRate = new Point(10,10);//When x counts down to 0 it is set to y and meFramesIndex 
	protected boolean alive = true;// If this is false then it needs to be removed;
	
	
	public Double Xf = 0.0;
	public Double Yf = 0.0;
	private Point2D.Double boundsHigh = new Point2D.Double(9001.0f,9001.0f); //Highest limit it can go;
	private Point2D.Double boundsLow = new Point2D.Double(-9001.0f,-9001.0f);//Lowest limit it can go;
	
	//Render Things
	private GraphicsContext canvasCore;
	private boolean renderMe = true; //if it should be rendered
	
	//One Image Things
	private Image strip;//One continuous image exported by graphics gale
	private Image stripCore;//The image that isn't stretched out by the scale
	private String stripLocus;
	private Double frameWidth = -9001.0;//width of each slice;
	private Double totalWidth = 0.0;//width of the entire strip;
	private Double frameHeight = 0.0; //Height of each height
	private Double totalHeight = 0.0;
	private int frameMax = 0; //Maximum frames
	private int frameMaxY = 0;
	private Point frameRange = new Point(0,0);//x is the max and y is the min for frameIndex;
	private HashMap<String,Point> Ranges = new HashMap<String,Point>();//Set of ranges
	private HashMap<String,Integer> RangesY = new HashMap<>();//Set of YIndexes;
	private String currentKey = "";
	private String rangeCurrent = "";
	private int meFramesIndex = 0;//current frame;
	private int meFramesIndexY = 0;
	
	private boolean eatEvent = false;
	private boolean animationLock = true;//If the animation can be changed from the outside
	private boolean frameReset = true;//If the frame goes back to zero upon resetting;
	
	//Been Touched
	private ArrayList<AniVent> touchyVents = new ArrayList<>();
		private ArrayList<AniSquare> touchys = new ArrayList<>(); 
		private ArrayList<Boolean> touchysEat = new ArrayList<>();
	
	private ArrayList<AniVent> noTouchyVents = new ArrayList<>();
		private ArrayList<AniSquare> noTouchys = new ArrayList<>();
		private ArrayList<Boolean> noTouchysEat = new ArrayList<>();
		
		
	//Death event
	private AniVent deathVent;
	
	//Bounds Vents
	private HashMap<String,AniVent> bVents = new HashMap<>();
	private String currentBound = "";
	
	//EndFrame 
	private AniVent endVent;
	private boolean eatEndVent = false;
	private String endVentString = "";
	
	public AniSquare (GraphicsContext iCore, String iStripLocus, Double iWidth, Double iHeight, String keys)
	{
		stripLocus = iStripLocus;
		canvasCore = iCore;
		frameWidth = iWidth;
		frameHeight = iHeight;
		strip = loadImg(stripLocus);
		String[] keez = keys.split(",");
		
		//if (keez.length != frameMaxY){System.out.println("Warning: Keys: " + keez.length + " frameMaxY" + frameMaxY);}
		
		for (int a = 0; a < keez.length; a++)
		{
			int augFrameMax = frameMax;
			
			while (frameIsEmpty(augFrameMax,a) == true && augFrameMax > 0){augFrameMax--;}
			
			
			pootRange(keez[a],new Point(0,augFrameMax),a);
		}
		
		switchAnimation(keez[0]);

	}
	public AniSquare (GraphicsContext iCore, String iStripLocus, Image iStrip,Double iWidth, Double iHeight, String keys)
	{
		stripLocus = iStripLocus;
		canvasCore = iCore;
		frameWidth = iWidth;
		frameHeight = iHeight;
		strip = iStrip;
		eatImage(iStrip);
		String[] keez = keys.split(",");
		
		//if (keez.length != frameMaxY){System.out.println("Warning: Keys: " + keez.length + " frameMaxY" + frameMaxY);}
		
		for (int a = 0; a < keez.length; a++)
		{
			int augFrameMax = frameMax;
			
			while (frameIsEmpty(augFrameMax,a) == true && augFrameMax > 0){augFrameMax--;}
			
			
			pootRange(keez[a],new Point(0,augFrameMax),a);
		}
		
		switchAnimation(keez[0]);

	}
	
	public boolean frameIsEmpty(int Xoff, int Yoff)
	{
		int daCheck = -1;
		PixelReader daRead = stripCore.getPixelReader();
		
		for (int _Y_ = 0; _Y_ < frameHeight; _Y_ ++)
		{
			for (int _X_ = 0; _X_ < frameWidth; _X_ ++)
			{
				int daColor = daRead.getArgb(Xoff * frameWidth.intValue() + _X_, Yoff * frameHeight.intValue() + _Y_);
				if (daCheck == -1){daCheck = daColor;}
				else if (daCheck != daColor){/*System.out.println(_X_ + "," + _Y_);*/return false;}
			}
		}
		
		return true;
		
	}
	
	public AniSquare (GraphicsContext iCore, String iStripLocus)
	{
		stripLocus = iStripLocus;
		canvasCore = iCore;
		strip = loadImg(stripLocus);
		frameWidth = frameHeight;
		frameRange = Ranges.get("all");
		
	}
	
	public AniSquare (GraphicsContext iCore, String iStripLocus, Double iWidth, int iFrameRate)
	{
		stripLocus = iStripLocus;
		canvasCore = iCore;
		frameWidth = iWidth;
		meFramesRate = new Point(0,iFrameRate);
		strip = loadImg(stripLocus);
		frameRange = Ranges.get("all");
		//Double scabWidth = strip.getWidth();
		//Ranges.put("all",new Point(0,(scabWidth.intValue()/imgScale)/iWidth.intValue()));
		
		
	}
	
	public AniSquare (GraphicsContext iCore)
	{
		canvasCore = iCore;
	}
	/*
	public AniSquare(GraphicsContext iCore, String[] iSoftFrames, int iFrameRate )//Setup without using a custom class`
	{
		canvasCore = iCore;
		//meFramesSoft = iSoftFrames;
		loadMeFrames(meAnimationsIndex, iSoftFrames);
		meFramesRate.x = meFramesRate.y = iFrameRate;
	}
	*/
	
	///----------Public Methods----------
	public final void testthing()
	{
		//System.out.println(meFrames.size());
	}
	
	public final void pootRange(String name, Point iRange, Integer YIndex)//☺
	{
		if (iRange.x < 0 /*|| Ranges.get("all").y < iRange.y*/)
		{
			System.out.println("iRange out of Range");
			return;
		}
		
		Ranges.put(name,iRange);
		RangesY.put(name,YIndex);
	}
	
	public final void pootRange (String name, Point iRange)
	{
		pootRange(name,iRange,0);
	}
	
	
	public final void setCanStay (boolean to)//☺
	{
		canStay = to;
	}
	
	public final void setGraphicsContext(GraphicsContext to)//☺
	{
		canvasCore = to;
	}

	
	public final void setFrameRate (int to)//☺
	{
		meFramesRate.y = Math.max(to,1);
		meFramesRate.x = meFramesRate.y;
	}
	
	public final void setPlayDirection (going to)//☺
	{
		playDirection = to;
	}
	
	public final void setFrameIndex(int too)//☺
	{
		int to = too;
		if (to < 0)
		{
			to = 0;
			System.out.println("Tried to set frame index too low @setFrameIndex " + to);
			//return;
		}
		else if (to > frameRange.y)//else if (to >= meAnimations.get(meAnimationsIndex).size())
		{
			to = frameRange.y;//meAnimations.get(meAnimationsIndex).size() - 1;
			System.out.println("Tried to set frame index too high @setFrameIndex " + to);
			//return;
		}
		meFramesIndex = to;
		
	}
	
	public final void setHighBounds (double X, double Y)//☺
	{
		boundsHigh.x = X;
		boundsHigh.y = Y;
	}
	
	public final void setLowBounds (double X, double Y)//☺
	{
		boundsLow.x = X;
		boundsLow.y = Y;
	}
	
	public Point2D.Double getHighBounds()
	{
		return boundsHigh;
	}
	
	public Point2D.Double getLowBounds()
	{
		return boundsLow;
	}
	
	public final boolean checkPulse()//☺
	{
		return alive;
	}
	
	public final void kill()//☺
	{
		alive = false;
	}
	
	public final void resurrect()//☺
	{
		alive = true;
	}
	
	public static int getScale()//☺
	{
		int toSend = imgScale;
		
		return toSend;
	}
	
	public Rectangle2D getRect()//☺
	{
		/*
		if (meFramesIndex >= meAnimations.get(meAnimationsIndex).size())
			{
				meFramesIndex = meAnimations.get(meAnimationsIndex).size() - 1;
			}
			*/
			
			
		return new Rectangle2D.Double(Xf.doubleValue(),Yf.doubleValue(),frameWidth,frameHeight);
		
		//return new Rectangle2D.Double(Xf,Yf,(double)(meAnimations.get(meAnimationsIndex).get(meFramesIndex).getHeight()/imgScale),(double)(meAnimations.get(meAnimationsIndex).get(meFramesIndex).getWidth()/imgScale));
	}
	
	///----------Event Setter------------------
	
	public final void setEatEvent(boolean fart)
	{
		eatEvent = fart;
	}
	
	
	
	public final void setOnTouch (AniSquare toWatch, AniVent onTouch)
	{
		touchyVents.add(onTouch);
		touchys.add(toWatch);
	}
	
	public final void setNoTouch (AniSquare toWatch, AniVent noTouch)
	{
		noTouchyVents.add(noTouch);
		noTouchys.add(toWatch);
	}
	
	
	
	public final void setOnDeath (AniVent onDeath)
	{
		deathVent = onDeath;
	}
	
	public final void setOnBound (String to, AniVent onBound)
	{
		bVents.put(to,onBound);
	}
	
	public final void setOnEnd (AniVent onEnd,boolean dead)//When it reaches the last frame
	{
		endVent = onEnd;
		eatEndVent = dead;
		
	}
	
	///----------Public Static Methods----------
	
	public static final ArrayList<Integer> getHits (AniSquare hitter,ArrayList<AniSquare> in)//☺
	{
		ArrayList<Integer> toSend = new ArrayList<Integer>();
		
		for (int a = 0; a < in.size(); a++)
		{
			if (AniSquare.binaryHitTest(hitter,in.get(a)))
			{
				int poot = a;
				toSend.add(poot);
			}
		}
		
		return toSend;
	}
	
	public static final void setScale (int to)//☺
	{
		imgScale = Math.max(to,1);
		
		
	}
	
	public static final void reapDead(List<AniSquare> from)//Removes those who are marked for deletion //☺
	{
		int dead = getDead(from);
		while (dead != -1)
		{
			//from.get(dead) = null; Can't assign a null value?
			from.remove(dead);
			
			dead = getDead(from);
		}
	}
	
	public static final int getDead(List<AniSquare> from)//Checks and returns the index of something that is dead //☺
	{
		for (int a = 0; a < from.size(); a++)
		{
			if (from.get(a).checkPulse() == false)
			{
				return a;
			}
		}
		return -1;
	}
	
	public static final void massGear(List<AniSquare> from) //☺
	{
		for (int a = 0; a < from.size(); a++)
		{
			from.get(a).mainGear();
		}
	}
	
	public void loadinateTo (String[] softy, ArrayList<Image> to)//DEPRECIATE!
	{
		
		for (int a = 0; a < softy.length; a++)
		{
			Image toPoot = loadImg(softy[a]);
			to.add(toPoot);
			//to.add(loadImg(softy[a]));
		}
	}
	
	public static final boolean binaryHitTest(AniSquare A, AniSquare B)//☺
	{
		/*
		Rectangle2D rawRectA = A.getRect();
		Rectangle2D rawRectB = B.getRect();
		
		Rectangle2D.Double rectA = new Rectangle2D.Double((double)rawRectA.getX(),(double)rawRectB.getY(),(double) rawRectA.getWidth());
		Rectangle2D.Double rectB = new Rectangle2D.Double();
		*/
		
		
		//return A.getRect().intersects(B.getRect());
		
		if (AnimatedThing.dottyHits(A,B).size() != 0)
		{
			//System.out.println(AnimatedThing.dottyHits(A,B));
			//System.out.println("Side hit: "  + AnimatedThing.sideTest(A,B));
			return true;
		}
		
		return (AnimatedThing.dottyHits(A,B).size() != 0);
		
	}
	
	public static final boolean _inRange ()
	{
		return false;
	}
	
	
	//-----------Not Public Methods----------
	
	//protected Image getFrame ()//DEPRECIATE
	//{//Returns the current frame.
	//	return meAnimations.get(meAnimationsIndex).get(meFramesIndex);
	//}
	
	
	protected void reloadMeFrames ()//DEPRECIATE
	{
		
		strip = loadImg(stripLocus);
		
		/*OLD
		Object[] daKeys = meAnimations.keySet().toArray();
		
		for (int a = 0; a < daKeys.length; a++)
		{
			loadMeFrames((String)daKeys[a], meAnimationsSoft.get((String)daKeys[a]));
		}
		meFramesIndex = 0;
		*/
	}
	/*
	protected void reloadMeFrames(String[] from)
	{
		meFramesSoft = from;
		meFramesIndex = 0;
		reloadMeFrames();
	}
	
	protected void reloadMeFrames()
	{
		meFrames.clear();
		
		for (String a: meFramesSoft)
		{
			meFrames.add(loadImg(a));
		}
	}
	*/
	
	public final void setAnimationLock (boolean to)
	{
		animationLock = to;
	}
	
	public final void setFrameReset (boolean to)
	{
		frameReset = false;
	}
	
	public final void changeAnimation (String key)
	{
		if (animationLock == false)
		{
			switchAnimation(key);
		}
	}
	
	
	protected void switchAnimation(String key)//☺
	{//Switches to a new key
	
		if (currentKey == key){return;}
		if (Ranges.get(key) == null){System.out.println("@switchAnimation " + key + " was not found in ranges!"); return;}
		if (rangeCurrent == key){return;}
		frameRange = Ranges.get(key);
		if (frameReset)
		{
			meFramesIndex = frameRange.x;
		}
		else
		{
			meFramesIndex = Math.min(meFramesIndex,frameRange.y);
		}
		meFramesIndexY = RangesY.get(key);
		currentKey = key;
		
		
	
	/* OLD
		meAnimationsIndex = key;
		
		if (meAnimationsIndex.equals(key) == false)
		{
		meFramesIndex = 0;
		
		}
	*/
	}
	/*
	protected void loadMeFrames(String key, String[] locuses)//DEPRECIATE
	{ 
		meAnimationsSoft.put(key,locuses);
		ArrayList<Image> toSend = new ArrayList<>();
		
		for (String a: locuses)
		{
			toSend.add(loadImg(a));
		}
		
		meAnimations.put(key,toSend);
	}
	*/
	
	public static Image loadImage (String from, int tmpScale)
	{
		Image rawIMG = new Image(from);

		
		return new Image(from,rawIMG.getWidth()*tmpScale,rawIMG.getHeight()*tmpScale,true,false);
	}
	/*
	public static Image loadImage2 (String from, int tmpScale)
	{
		FileInputStream incoming;
		Image toSend ;
		try
		{
		incoming = new FileInputStream(from);
		Image rawIMG = new Image(incoming);
		
		
		toSend = new Image(incoming,rawIMG.getWidth()*tmpScale,rawIMG.getHeight()*tmpScale,true,false);
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
		finally
		{
			incoming.close();
		}
		
		if (toSend != null){return toSend;}
		System.out.println("IO FAILED!");
		return loadImage (from,tmpScale);
	}
	*/
	
	private void eatImage (Image to)//Takes given image assuming it is properly scaled up and everything
	{
		stripCore = to;
		if (frameHeight == 0){frameHeight = to.getHeight()/imgScale;}
		totalWidth = stripCore.getWidth()/imgScale;
		totalHeight = stripCore.getHeight()/imgScale;
		
		if (frameWidth < 0){frameWidth = frameHeight;}
		
		if (totalWidth.intValue() % frameWidth.intValue() != 0)
		{
			System.out.println("WARNING: Total width to frameWidth mismatch");
		}
		
		frameMax = (totalWidth.intValue()/frameWidth.intValue()) - 1;
		frameMaxY = (totalHeight.intValue()/frameHeight.intValue()) - 1;
		Ranges.put("all",new Point(0,frameMax));
		
		meFramesIndex = frameRange.x;
		
	}
	
	private Image loadImg(String from)//☺
	{
		stripCore = new Image(from);
		if (frameHeight == 0) {frameHeight = stripCore.getHeight();}
		totalWidth = stripCore.getWidth();
		totalHeight = stripCore.getHeight();
		
		if (frameWidth < 0)
		{
			frameWidth = frameHeight;
		}
		
		if (totalWidth.intValue() % frameWidth.intValue() != 0)
		{
			System.out.println("WARNING: Total width to frameWidth mismatch");
		}
		frameMax = (totalWidth.intValue()/frameWidth.intValue()) - 1;
		frameMaxY = (totalHeight.intValue()/frameHeight.intValue()) - 1;
		//System.out.println(totalHeight);
		
		Ranges.put("all",new Point(0,frameMax));
		
		meFramesIndex = frameRange.x;
		return new Image (from,stripCore.getWidth()*imgScale,stripCore.getHeight()*imgScale,true,false);
	}
	
	///-----------Gear-----------
	
	public void mainGear()//Accessed from outside //☺
	{
		if (alive)
		{
		preGear();
		midGear();
		postGear();
		eventGear();
		}
		else
		{
			deadGear();
		}
	}
	
	protected void eventGear()//Checks event
	{
		//Hit Test
		
		for (int a = touchys.size() - 1; a > -1; a--)
		{
			if (1==1)
			{
				String pootSide = AnimatedThing.sideTest(this,touchys.get(a));
				
				if (pootSide != "")
				{
					touchyVents.get(a).say(this,touchys.get(a),pootSide);
					
					if (eatEvent){touchyVents.remove(a); touchys.remove(a);}
				}
			}
		}
		
		//Non Hit Test
		
		for (int a2 = noTouchys.size() - 1; a2 > -1; a2--)
		{
			if (1==1)
			{
				String pootSide2 = AnimatedThing.sideTest(this,noTouchys.get(a2));
				
				if (pootSide2 == "")
				{
					noTouchyVents.get(a2).say(this,noTouchys.get(a2),pootSide2);
					
					if (eatEvent){noTouchyVents.remove(a2); noTouchys.remove(a2);}
				}
			}
		}
		
		
		//Death Event
		
		if (deathVent != null)
		{
			if (alive == false)
			{
				deathVent.say(this,this,"dead");
				if (eatEvent){deathVent = null;}
			}
		}
		
		//boundsEvent
		
		if (bVents.get(currentBound) != null){bVents.get(currentBound).say(this,this,Xf.toString() + "," + Yf.toString());}
		
	}
	
	protected void gearBounds() //☺
	{//Makes sure that the element stays in a certain area.
		Rectangle2D daSquare = getRect();
		Point2D.Double augBounds = new Point2D.Double(boundsHigh.x - (double)daSquare.getWidth(),boundsHigh.y - (double)daSquare.getHeight());//This is bounds max minus the size of the rectangle
		
		currentBound = "";
		//Checking the bounds
		if (Xf > augBounds.x){currentBound = "e";}
		else if (Xf < boundsLow.x){currentBound = "w";}
		else if (Yf > augBounds.y){currentBound = "s";}
		else if (Yf < boundsLow.y){currentBound = "n";}
		
		//if (currentBound != ""){System.out.println("IS:" + currentBound);}
		
		
		Xf = Math.min(Xf, augBounds.x);
		Yf = Math.min(Yf, augBounds.y);
		
		Xf = Math.max(Xf, boundsLow.x);
		Yf = Math.max(Yf, boundsLow.y);
		
		
	}
	
	protected void preGear()//☺
	{
		if (imgScaleLast != imgScale)
		{
			strip = loadImg(stripLocus);
		}
		imgScaleLast = imgScale;
	}
	
	protected void midGear()//☺
	{
		playGear();
		
	}
	
	protected void postGear()//☺
	{
		
	}
	
	protected void deadGear()//Invoked when dead//☺
	{
		
	}
	
	protected void playGear()//Changes index of playhead//☺
	{
		
		if (alive == false || playDirection == going.STOP){return;}
		
		//Make sure meFramesIndex stays in range
		meFramesIndex = Math.min(frameRange.y,meFramesIndex);
		meFramesIndex = Math.max(frameRange.x,meFramesIndex);
		
		if (meFramesRate.x > 0)
		{
			meFramesRate.x = meFramesRate.x - 1;
		}
		else if (meFramesRate.x <= 0)
		{
			meFramesRate.x = meFramesRate.y;
			
			if (playDirection == going.PLAY)
			{
				meFramesIndex ++;
				
				if (meFramesIndex > frameRange.y && canStay == false)
				{
					alive = false;
					meFramesIndex = frameRange.y;
				}
				
				else if (meFramesIndex > frameRange.y)
				{
					//meFramesIndex = meFramesIndex + 1 > frameRange.y ? frameRange.x:meFramesIndex + 1;
					meFramesIndex = frameRange.x;
					
				}
				//System.out.println( meFramesIndex+ ":"+ frameRange.x + ","+ frameRange.y  );
				
			}
			if (playDirection == going.PLAY_ONCE)
			{
				if (meFramesIndex < frameRange.y)
				{
					meFramesIndex ++;
					if (meFramesIndex == frameRange.y && endVent != null)
					{
							endVent.say(this,this,endVentString);
							
							if(eatEndVent == true){endVent = null;}
					}
				}
				
				if (meFramesIndex > frameRange.y && canStay == false)
				{
					alive = false;
					meFramesIndex = frameRange.y;
				}
				
				else if (meFramesIndex > frameRange.y)
				{
					//meFramesIndex = meFramesIndex + 1 > frameRange.y ? frameRange.x:meFramesIndex + 1;
					
					meFramesIndex = frameRange.y;
					
				}
			}
			if (playDirection == going.REVERSE)
			{
				meFramesIndex --;
				
				if (meFramesIndex  > frameRange.y && canStay == false)
				{
					alive = false;
					meFramesIndex = frameRange.y;
				}
				
				else if (meFramesIndex < frameRange.x)
				{
					//meFramesIndex = meFramesIndex + 1 > frameRange.y ? frameRange.x:meFramesIndex + 1;
					meFramesIndex = frameRange.y;
					
				}
			}				
			if (playDirection == going.REVERSE_ONCE)
			{
				if (meFramesIndex > 0){meFramesIndex --;}
				
				if (meFramesIndex  > frameRange.y && canStay == false)
				{
					alive = false;
					meFramesIndex = frameRange.y;
				}
				
				else if (meFramesIndex < frameRange.x)
				{
					//meFramesIndex = meFramesIndex + 1 > frameRange.y ? frameRange.x:meFramesIndex + 1;
					meFramesIndex = 0;
					
				}
			}		
			if (playDirection == going.OSCILLATE)
			{
				if (osciPlay == true)
				{
					meFramesIndex = meFramesIndex + 1;
					if (meFramesIndex >= frameRange.y)
					{
						osciPlay = false;
					}
				}
				else
				{
					meFramesIndex = meFramesIndex - 1;
					if (meFramesIndex <= frameRange.x)
					{
						osciPlay = true;
					}
				}
			}
		}
		
		
		/*OLD
		0. Just return if dead
		1. Make sure meFramesIndex stays in range
		2. Check if meFramesRate.x == 0. Goto 3 if so, meFrameRate.x-- otherwise
		3. 
			A. going.PLAY = Index goes up, resets to min if too high unless canStay is false. Then it stays at the high;
			B. going.REVERSE = Opposite of PLAY;
			C. going.OSCILLATE = PLAY when osciPlay is true REVERSE when false, toggle osciPlay at extremes
			
		
		if (meFramesIndex >= meAnimations.get(meAnimationsIndex).size())
		{
			meFramesIndex = meAnimations.get(meAnimationsIndex).size() - 1;
		}
		else if (meFramesIndex < 0)
		{
			meFramesIndex = 0;
		}
		
		if (meFramesRate.x == 0)
		{
			if (playDirection == going.PLAY)
			{
				meFramesIndex++;
				if (meFramesIndex >= meAnimations.get(meAnimationsIndex).size())
				{
					meFramesIndex = 0;
				}
				alive = canStay;
				
				if (alive == false)
				{
					meFramesIndex = meAnimations.get(meAnimationsIndex).size() - 1;
				}

				
			}
			else if (playDirection == going.REVERSE)
			{
				meFramesIndex--;
				if (meFramesIndex < 0)
				{
					meFramesIndex = meAnimations.get(meAnimationsIndex).size() - 1;
				}
				alive = canStay;
				
				if (alive == false)
				{
					meFramesIndex = 0;
				}
				
			}
			else if (playDirection == going.OSCILLATE)
			{
				if (osciPlay == true)
				{
					meFramesIndex ++;
					if (meFramesIndex >= meAnimations.get(meAnimationsIndex).size() - 1)
					{
						osciPlay = false;
						meFramesIndex = meAnimations.get(meAnimationsIndex).size() - 1;
					}
				}
				else
				{
					meFramesIndex --;
					if (meFramesIndex <= 0)
					{
						osciPlay = true;
						meFramesIndex = 0;
					}
				}
			}
			
			meFramesRate.x = meFramesRate.y;
		}
		else
		{
			meFramesRate.x --;
		}
		*/
	}
	
	public void renderGear()//Remember to clear the canvas first!//☺
	{
		
		renderGear(0.0,0.0);
		/*
		if (canvasCore != null && renderMe == true)
		{
			
			
			Integer daPosition = meFramesIndex * (frameWidth.intValue()*imgScale);
			//Xposition of original, Yposition of original,sizeOrigin,sizeOrigin,Xnew,Ynew,sizeNew,sizeNew
			canvasCore.drawImage(strip,daPosition,0,frameWidth.intValue()*imgScale,frameHeight.intValue()*imgScale,Xf.intValue()*imgScale,Yf.intValue()*imgScale,frameWidth.intValue()*imgScale,frameHeight.intValue()*imgScale);

		}
		*/
	}
	
	public void renderGear(Double Xoff, Double Yoff)//Remember to clear the canvas first!//☺
	{
		if (canvasCore != null && renderMe == true)
		{
			
			
			Integer daPosition = meFramesIndex * (frameWidth.intValue()*imgScale);
			Integer daPositionY = meFramesIndexY * (frameHeight.intValue()*imgScale);
			//Xposition of original, Yposition of original,sizeOrigin,sizeOrigin,Xnew,Ynew,sizeNew,sizeNew
			canvasCore.drawImage(strip,daPosition,daPositionY,frameWidth.intValue()*imgScale,frameHeight.intValue()*imgScale,(Xf.intValue() + Xoff.intValue())*imgScale,(Yf.intValue() + Yoff.intValue())*imgScale,frameWidth.intValue()*imgScale,frameHeight.intValue()*imgScale);

		}
	}
	
	public static void renderGearMass(ArrayList<AniSquare> mrender)//☺
	{
		for (AniSquare a : mrender)
		{
			a.renderGear();
		}
	}	
	
	public static void renderGearMass(ArrayList<AniSquare> mrender,Double Xoff, Double Yoff)//☺
	{
		for (AniSquare a : mrender)
		{
			a.renderGear(Xoff,Yoff);
		}
	}
	
	
}