import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Application;
import javafx.application.Platform;

import javafx.scene.layout.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import java.util.HashMap;
import java.util.ArrayList;
import javafx.scene.image.Image;

import java.util.Timer;
import java.util.TimerTask;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.nio.file.*;
import java.io.*;
import java.nio.file.attribute.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.awt.Point;
import java.util.Collections;

import anisquare.*;

public class Neck extends Application
{
	/*
	switchScene (screens) = changes the scene;
	/////////Game fns
	sparkEnd()//Play end animation;
	sparkNewRound()//Retract neck for new round; 46,226
	
	*/
	//Images
	private Image imgPlayer = AniSquare.loadImage("visuals/Naybur2.png",2);
	private Image imgBKG = AniSquare.loadImage("visuals/TitleBKGsanshead.png",2);
	
	
	//-----------------
	private int size = 720;
	private Stage myStage;
	private StackPane root = new StackPane();
	private Scene rootScene = new Scene(root,size,size);
	
	
	public enum screens {TITLE,GAME,TRANSITION,HIGH,WEIRD};
	private StackPane srnTitle = new StackPane();
		private Canvas srnTitleAnimated = new Canvas(size,size);
		private Canvas srnTitleBKG = new Canvas(size,size);
		{
			srnTitle.getChildren().addAll(srnTitleBKG,srnTitleAnimated);
			srnTitleBKG.getGraphicsContext2D().drawImage(imgBKG,0.0,0.0);
			
			root.getChildren().add(srnTitle);
			//srnTitleBKG.getGraphicsContext2D().setFill(Color.BLACK);
			//srnTitleBKG.getGraphicsContext2D().fillRect(50,50,50,50);
		}
	private StackPane srnGame = new StackPane();
		private Canvas srnGameBKG = new Canvas(size,size);{srnGameBKG.getGraphicsContext2D().setFill(Color.BLACK);srnGameBKG.getGraphicsContext2D().fillRect(0.0,0.0,size,size);}
		private Canvas srnGameNeck = new Canvas(size,size);//Holds neck parts;
		private Canvas srnGameOther = new Canvas(size,size);//Holds head and food;
		private Canvas srnGamePoof = new Canvas (size,size);//Holds clouds
		{srnGame.getChildren().addAll(srnGameBKG,srnGameNeck,srnGameOther,srnGamePoof);}
	private StackPane srnTransition = new StackPane();
	private StackPane srnHigh = new StackPane();
	private HashMap<screens,StackPane> screenBook = new HashMap<>();
	{
		screenBook.put(screens.TITLE,srnTitle);
		screenBook.put(screens.GAME,srnGame);
		screenBook.put(screens.TRANSITION,srnTransition);
		screenBook.put(screens.HIGH,srnHigh);
		
	}
	private screens screenCurrent = screens.TITLE;
	private screens screenTo = screens.GAME;//Which scene to go to when a transition is done playing
	
	private Timer timer = new Timer();
	

	private ArrayList<String> validKeys = new ArrayList<>();
	{
		String[] validKeysRaw = {"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","!","?",".",","};
		for (int a = 0; a < validKeysRaw.length; a ++)
		{
			validKeys.add(validKeysRaw[a]);
		}
	}

	private boolean shiftDown = false;//If the shift key is held down
	
	///----------------GAME VARS----------------------
	//!For rendering purposes, every locus is multiplied by squareSize and scale
	
	private boolean unpaused = true;//Paused or not;
	private String going = "n";//Which direction the head is going;
	private boolean goingChange = false;//If direction had been changed for that "turn"
	private ArrayList<String> pastGoings = new ArrayList<>();//Where the head was;
	private int score = 0;//When they get a thing
	
	private String highName = "Nobody";
	private int highScore = 51;
	private Integer scale = 2;
	private int speedBase = 5;//Speed it starts at
	private int speedMin = 0; //Lowest number this can go to;
	private Point speedTick = new Point(speedBase,speedBase);//When this ticks to 0 the head moves;
	private Integer squareSize = 15;//Main size of the square
	private final Point maxXY = new Point(23,23);//When this is exceeded warp to 0. 
	private Point startXY = new Point(13,23);//Starting location;
	
	
	
	private ArrayList<Point> food = new ArrayList<Point>();//When this is empty the level is won;
	private ArrayList<Point> mines = new ArrayList<Point>();
	private Integer minesToPoot = 0;
	private final Integer minesMax = 30;
	private final Integer foodToPootBase = 5;//Initial food to poot;
	private final Integer foodToPootMax = 20;//Maximum food to poot;
	private Integer foodToPoot = foodToPootBase;
	private Point headLocus = new Point (startXY.x,startXY.y);//Where the head is;
	private ArrayList<Point> oldLoci = new ArrayList<>();//Where the head was. If there is ever a match to headLocus here then it's game over;
	private enum gamePhase {BEGIN,PLAYING,NEW_ROUND,ENDING};//BEGIN = When the game starts, PLAYING normal gameplay, NEW_ROUND neck retracting, ENDING game over
	private gamePhase currentPhase = gamePhase.BEGIN;
	private boolean warped = false; //If the player warped recently around the edge of the board (needed for correcting neck);
	
	
		//------Sound
	private ArrayList<String> sndList = new ArrayList<>();
	{
		SndBox.setMasterVolume (0.5);
		sndList.add("sound/step1.wav");
		sndList.add("sound/step2.wav");
		sndList.add("sound/01Headland.wav");
		sndList.add("sound/skullSpawn.wav");
		sndList.add("sound/starSpawn.wav");
		sndList.add("sound/neckHit.wav");
		sndList.add("sound/starGet0.wav");
		sndList.add("sound/starGet1.wav");
		sndList.add("sound/starGet2.wav");
		sndList.add("sound/starGet3.wav");
		sndList.add("sound/starGet4.wav");
		sndList.add("sound/neckEat.wav");
		sndList.add("sound/startGame.wav");
		sndList.add("sound/endGame.wav");
		sndList.add("sound/skullaugh.wav");
		
		
	}
	
	private String[] starSnds = {"starGet4","starGet3","starGet2","starGet1","starGet0"};
	private SndBox AUDIO = new SndBox(sndList);
	private boolean sndStep = false;
	private boolean unmuted = true;//Muted or not
	
	//-------Timerishes
	private Point endsStall = new Point (33,33);//Pause at the beginning and ends of the round;
	
	//----------AniSquares
	{AniSquare.setScale(scale);}
	private AniSquare aniPlayer = new AniSquare(srnGameOther.getGraphicsContext2D(),"visuals\\Naybur2.png",imgPlayer,15.0,15.0,"n,e,s,w,N,E,S,W");{aniPlayer.setAnimationLock(false);aniPlayer.setFrameReset(false); aniPlayer.setFrameRate(0);}
	private AniSquare aniNeck = new AniSquare(srnGameNeck.getGraphicsContext2D(),"visuals/theNeck.png",15.0,15.0,"ns,we,se,sw,nw,ne");{aniNeck.setAnimationLock(false);}
	private AniSquare aniSkull = new AniSquare(srnGameOther.getGraphicsContext2D(),"visuals/skull.png",15.0,15.0,"start,spin,N,W,S,E,kek");
	{
		aniSkull.setAnimationLock(false);
		aniSkull.setFrameRate(1);
		aniSkull.setPlayDirection(AniSquare.going.PLAY);
		aniSkull.changeAnimation("start");
	}
	private AniSquare aniSkullSpecial = new AniSquare(srnGameOther.getGraphicsContext2D(),"visuals/skull.png",15.0,15.0,"start,spin,N,W,S,E,kek");
	{
		aniSkullSpecial.setAnimationLock(false);
		aniSkullSpecial.setFrameRate(1);
		aniSkullSpecial.setPlayDirection(AniSquare.going.PLAY);
	}
	private AniSquare aniStar = new AniSquare(srnGameOther.getGraphicsContext2D(),"visuals/star.png");
	{
		aniStar.setPlayDirection(AniSquare.going.PLAY);
		aniStar.setFrameRate(1);
	}
	private AniSquare aniSkullCloud = new AniSquare(srnGamePoof.getGraphicsContext2D(),"visuals/skullCloud.png");
	ArrayList<Point> skullCloudPoints = new ArrayList<>();
	{
		aniSkullCloud.setFrameRate(1);
		aniSkullCloud.setPlayDirection(AniSquare.going.PLAY_ONCE);
	aniSkullCloud.setOnEnd((AniSquare sender, AniSquare target, String ... message)-> 
	{
		skullCloudPoints.clear();
		AUDIO.playThis("starSpawn");
		sender.setFrameIndex(0);
	},false
	);
	}
	
	private AniSquare aniStarCloud = new AniSquare(srnGamePoof.getGraphicsContext2D(),"visuals/starCloud.png");
	ArrayList<Point> starCloudPoints = new ArrayList<>();
	{
		aniStarCloud.setFrameRate(1);
		aniStarCloud.setPlayDirection(AniSquare.going.PLAY_ONCE);
	aniStarCloud.setOnEnd((AniSquare sender, AniSquare target, String ... message)-> 
	{
		starCloudPoints.clear();
		sender.setFrameIndex(0);
	},false
	);
	}
	
	private AniSquare aniPressup = new AniSquare(srnTitleAnimated.getGraphicsContext2D(),"visuals/Pressup.png",269.0,18.0,"all");
	{
		aniPressup.setFrameRate(0);
		aniPressup.Xf = 46.0;
		aniPressup.Yf = 260.0;
		aniPressup.setPlayDirection(AniSquare.going.PLAY);
	}
	
	private Image peeBreak = AniSquare.loadImage("visuals/peebreak.png",2);
	
	private AniSquare aniNeckstend = new AniSquare(srnTitleAnimated.getGraphicsContext2D(),"visuals/neckstend.png",75.0,195.0,"all");
	{
		aniNeckstend.setFrameRate(1);
		aniNeckstend.Xf = 142.0;
		aniNeckstend.setOnEnd((AniSquare sender, AniSquare target, String ... message)-> {switchScene(screens.GAME);},false);
	}
	
	//------Temp
	
	private Color colorPlayer = Color.DARKCYAN;
	private Color colorNeck = Color.CRIMSON;
	private Color colorFood = Color.LIME;
	private Color colorMine = Color.RED;
	private Point tempTicker = new Point (30,30);
	

	
	/////---------------------------------METHODS
	@Override
	public void start (Stage stage)
	{
		myStage = stage;
		myStage.setScene(rootScene);
		myStage.setTitle("Neck");
		myStage.show();
		myStage.setOnCloseRequest(e -> timer.cancel());
		myStage.getIcons().add(new Image("visuals/ico.png"));
		rootScene.setOnKeyPressed(new keyListen());
		rootScene.setOnKeyReleased(new keyListen());
		resetGame();
		loopTimer();
		
	}
	
	private void resetGame ()
	{
		unpaused = true;
		

		going = "n"; aniPlayer.changeAnimation("n");
		pastGoings.clear();
		mines.clear();
		oldLoci.clear();
		speedTick = new Point(speedBase,speedBase);
		currentPhase = gamePhase.BEGIN;
		headLocus = new Point (startXY.x,startXY.y);
		goingChange = false;
		minesToPoot = 0;
		food.clear();
		clearCanvi(srnGameNeck,srnGameOther);
		endsStall.x = endsStall.y;
		aniPlayer.setPlayDirection(AniSquare.going.STOP);
		aniPlayer.setFrameIndex(0);
		warped = false;
		aniPlayer.setFrameReset(false); aniPlayer.setFrameRate(0);
		aniSkull.setFrameIndex(0);
		aniSkullSpecial.setFrameIndex(0);
		aniSkull.changeAnimation("start");
		
		
		//highScoreGetter();
	}
	
	private void switchScene (screens to)
	{
		if (to == screenCurrent){return;}
		root.getChildren().clear();
		root.getChildren().add(screenBook.get(to));
		screenCurrent = to;
		tempTicker.x = tempTicker.y;//REMOVE EVENTUALLY
		
	}
	
	private class keyListen implements EventHandler<KeyEvent>
	{
		public void handle (KeyEvent e)
		{
			String daEvent = e.getEventType().toString();
			String daKey = e.getCode().toString();
			
			//if (daKey == "BACK_SPACE"){System.out.println("FART");}
			
			if (daEvent == "KEY_PRESSED")
			{
				if (daKey == "SHIFT"){shiftDown = true; }
							
							
				if (daKey == "M")
				{
					
					double volToPoot = unmuted ? 0.0:0.5;
					unmuted = !unmuted;
				
					SndBox.setMasterVolume(volToPoot);
				}
			
			}
			if (daEvent == "KEY_RELEASED")
			{
				if (daKey == "SHIFT"){shiftDown = false; }
			}
			

			if(screenCurrent == screens.TITLE && daKey == "UP" && daEvent == "KEY_RELEASED")
			{
				//Play press any key to start
				//Switch to Transition
				//Play AniSquare
				AUDIO.playThis("startGame");
				aniNeckstend.setPlayDirection(AniSquare.going.PLAY_ONCE);
			}
			
			if(screenCurrent == screens.GAME)
			{
				if (daEvent == "KEY_PRESSED")
				{
					if (unpaused == true && goingChange == false && oldLoci.size() > 0)
					{
						if (daKey == "UP" && going != "s")
						{
							going = "n";
							
							goingChange = true;
							
						}
						if (daKey == "DOWN" && going != "n")
						{
							going = "s";goingChange = true;
							
						}
						if (daKey == "LEFT" && going != "e")
						{
							going = "w";goingChange = true;
							
						}
						if (daKey == "RIGHT" && going != "w")
						{
							going = "e"; goingChange = true;
							
						}
					}
					if (daKey == "P")
					{
						unpaused = !unpaused;
						if (unpaused == false)
						{
							srnGamePoof.getGraphicsContext2D().drawImage(peeBreak,0.0,0.0);
						}
					}
					
					
				}
				else if (daEvent == "KEY_RELEASED")
				{
					//Unnecessairy
				}
				
				
			}
			
			if(screenCurrent == screens.HIGH)
			{
				if (daEvent == "KEY_PRESSED")
				{
				String keyToPut = shiftDown ? daKey:daKey.toLowerCase();
				
				if (daKey == "BACK_SPACE")
				{
					highName = highName.substring(0,highName.length()-1);
				}
				else
				{
					if (validKeys.indexOf(keyToPut) == -1){keyToPut = "";}
					
					highName = highName + keyToPut;
				}
				
				System.out.println(highName);
				}
			}
			
			
		}
	}

	private class motor extends TimerTask
	{
		public void run ()
		{
			/*
			Platform.runLater ( new Runnable(){
				
				@Override
				public void run()
				{
				trueRun();
				}
				
				});			
				*/
				Platform.runLater ( () -> {trueRun();});
		}
	}
	
	public void loopTimer()
	{
		try
		{
		timer.schedule(new motor(),33);
		}
		catch (IllegalStateException e)
		{
			//System.out.println("FRRRT!");
		}
	}
	
	private void trueRun()
	{
		if (screenCurrent == screens.TITLE)
		{
			renderCogTitle();
		}
		else if (screenCurrent == screens.TRANSITION)
		{
			
		}
		else if (screenCurrent == screens.HIGH)
		{
			
		}
		else if (screenCurrent == screens.GAME)
		{
			if (currentPhase == gamePhase.BEGIN)
			{
				if (tempTicker.x > 0){tempTicker.x--;}
				else {currentPhase = gamePhase.PLAYING;tempTicker.x = tempTicker.y;pootFood();}
			}
			else if (currentPhase == gamePhase.PLAYING && unpaused == true)
			{
				boolean neckHit = false;
				if (speedTick.x <= 0)
				{
				//PreCalculating
				Point oldLocus = new Point(headLocus.x,headLocus.y);
				oldLoci.add(oldLocus);
				
				
				//Moving
					goingChange = false;
					movePlayerCog();
					speedTick.x = speedTick.y;
				
				//Calculating
				
				
				for (Point p: oldLoci)
				{
					if (headLocus.x == p.x && headLocus.y == p.y){neckHit = true;AUDIO.playThis("neckHit");endsStall.x = endsStall.y;}
				}
				for (Point poo: mines)
				{
					if (headLocus.x == poo.x && headLocus.y == poo.y){neckHit = true;aniSkullSpecial.changeAnimation(going.toUpperCase());aniSkull.changeAnimation("kek");endsStall.x = 0;}
				}
				
				if (neckHit == true){currentPhase = gamePhase.ENDING; oldLoci.add(0,new Point(startXY.x,startXY.y + 1));pastGoings.add(0,"n");}//GAME OVER
					else
					{
						//AUDIO.playThis("01Headland");
						//if (sndStep){AUDIO.playThis("step1");}
						//else{AUDIO.playThis("step2");}
						//sndStep = !sndStep;
					}
				}
				else
				{
					speedTick.x--;
				}
				
				int foodHit = -1;
				
				for (Point pee: food)
				{
					if (headLocus.x == pee.x && headLocus.y == pee.y){foodHit = food.indexOf(pee);}
				}
				
				if (foodHit != -1){food.remove(foodHit);speedTick.y--;AUDIO.playThis(starSnds[food.size()]);}
				if (food.size() == 0){
					currentPhase = gamePhase.NEW_ROUND; 
					aniPlayer.setPlayDirection(AniSquare.going.PLAY);
					if (mines.size() > 0){aniSkull.changeAnimation("spin");}
					
					}//WON LEVEL
				
				//Sound
				
				
				//Rendering
				if (currentPhase == gamePhase.PLAYING)
				{
				aniPlayer.changeAnimation(going);
				}
				else if (neckHit == true)
				{
					aniPlayer.changeAnimation(going.toUpperCase());
					aniPlayer.setPlayDirection(AniSquare.going.PLAY_ONCE);
				}
				else
				{
					aniPlayer.changeAnimation(going);
				}
				renderCog(true);
				/*
				clearCanvi(srnGameOther);
				putRect(headLocus.x,headLocus.y,colorPlayer,srnGameOther);
				
				try
				{putRect(oldLoci.get(oldLoci.size()-1).x,oldLoci.get(oldLoci.size()-1).y,colorNeck,srnGameNeck);}catch (Exception e){}
				
				putRects(food,colorFood,srnGameOther);
				*/
				
			}
			else if (currentPhase == gamePhase.NEW_ROUND)
			{
				if (endsStall.x <= 0)
				{
					if (oldLoci.size() > 0)
					{
						headLocus = oldLoci.remove(oldLoci.size() - 1);
						//oldLoci.remove(oldLoci.size() - 1);
						//clearCanvi(srnGameOther);
						srnGameNeck.getGraphicsContext2D().clearRect(headLocus.x*scale*squareSize,headLocus.y*scale*squareSize,scale*squareSize,scale*squareSize);
						//putRect(headLocus.x,headLocus.y,colorPlayer,srnGameOther);
						if (pastGoings.size() > 0)
						{
							aniPlayer.changeAnimation(pastGoings.remove(pastGoings.size()-1));
						}
						
						if (oldLoci.size() == 0)
						{
							endsStall.x = endsStall.y;
							ArrayList<Point> leftover = pootFood();
							int pooty = 3;
							if (mines.size() < minesMax){AUDIO.playThis("skullSpawn");}
							while (pooty > 0)
							{
								if (mines.size() < minesMax)
								{
								Point mineLocusToPoot = leftover.remove(0);
									
								mines.add(mineLocusToPoot);
								
								skullCloudPoints.add(mineLocusToPoot);
								
								
								
								
								
								}
								pooty--;
							}
							//Check for overlaps
							int overlap = overlapCheck();
							while (overlap > -1)
							{
								food.remove(overlap);
								food.add(leftover.remove(0));
								overlap = overlapCheck();
							}
							
							for (Point frt: food)
							{
								starCloudPoints.add(new Point(frt.x-1,frt.y-1));
							}
							//pootMines(leftover);
							
						}
					}
					else
					{
						currentPhase = gamePhase.PLAYING;
						//minesToPoot = Math.min(minesToPoot + 1, minesMax);
						aniSkull.changeAnimation("start");
						aniPlayer.setPlayDirection(AniSquare.going.STOP);
						aniPlayer.setFrameIndex(0);
						speedTick = new Point(speedBase,speedBase);
						going = "n";
						endsStall.x = endsStall.y;
						
					}
				}
				else
				{
					endsStall.x--;
				}
				
				renderCog(false);
			}
			else if (currentPhase == gamePhase.ENDING)
			{
				if (endsStall.x <= 0)
				{
					if (oldLoci.size() > 0)
					{
					
						
						if (mines.indexOf(new Point(headLocus.x,headLocus.y)) != -1)//Getting eaten by a skull
						{
						Point baseLocus = oldLoci.remove(0);
						srnGameNeck.getGraphicsContext2D().clearRect(baseLocus.x*scale*squareSize,baseLocus.y*scale*squareSize,scale*squareSize,scale*squareSize);

						AUDIO.playThis("neckEat");
						if (oldLoci.size() == 0){endsStall.x = endsStall.y;aniSkullSpecial.changeAnimation("kek"); AUDIO.playThis("skullaugh");} 
						}
						else//Neck hit
						{
							headLocus = oldLoci.remove(oldLoci.size() - 1);
							srnGameNeck.getGraphicsContext2D().clearRect(headLocus.x*scale*squareSize,headLocus.y*scale*squareSize,scale*squareSize,scale*squareSize);
						//putRect(headLocus.x,headLocus.y,colorPlayer,srnGameOther);
						if (pastGoings.size() > 0)
						{
							aniPlayer.changeAnimation(pastGoings.remove(pastGoings.size()-1).toUpperCase());
						}
						if (oldLoci.size() == 0){endsStall.x = endsStall.y;}
						}
						
					}
					else
					{
						switchScene(screens.TITLE);
						screenTo = screens.TITLE;
						resetGame();
						aniNeckstend.setPlayDirection(AniSquare.going.REVERSE_ONCE);
						AUDIO.playThis("endGame");
						
					}
				}
				else
				{
					endsStall.x--;
				}
				
				renderCog(false);
			}
			
		}
		loopTimer();
	}
	
	
	///----------Methods exclusive to trueRun()
	

	
	private int overlapCheck()
	{
		for (int a = 0; a < food.size(); a++)
		{
			for (int b = 0; b < mines.size(); b++)
			{
				Point pntA = food.get(a);
				Point pntB = mines.get(b);
				if (pntA.x == pntB.x && pntA.y == pntB.y)
				{
					System.out.println("OVERLAP FOUND!");
					return a;
				}
			}
		}
		
		return -1;
	}
	
	private void renderCogTitle()
	{
		clearCanvi(srnTitleAnimated);
		
		aniNeckstend.mainGear();
		aniNeckstend.renderGear();
		aniPressup.mainGear();
		aniPressup.renderGear();
	}
	
	private void renderCog(boolean includeNeck)
	{
		clearCanvi(srnGameOther,srnGamePoof);//,srnTitleAnimated
		
		if (screenCurrent == screens.TITLE)
		{
			
			
		}
		//putRect(headLocus.x,headLocus.y,colorPlayer,srnGameOther);
		
		
		//try{putRect(oldLoci.get(oldLoci.size()-1).x,oldLoci.get(oldLoci.size()-1).y,colorNeck,srnGameNeck);}catch (Exception e){}
		
		
		//putRects(food,colorFood,srnGameOther);
		
		//putRects(mines,colorMine,srnGameOther);
		
		//AniSquare
		if (currentPhase == gamePhase.PLAYING){aniPlayer.changeAnimation(going);}
		aniPlayer.mainGear();
		
		Integer playerX = headLocus.x;
		Integer playerY = headLocus.y;
		Integer neckX = -1;
		Integer neckY = -1;
		Integer oldNeckX = -1;
		Integer oldNeckY = -1;
		
		String neckBend = "ns";
		if (includeNeck )
		{
			if (oldLoci.size() >= 2)
			{
				neckX = oldLoci.get(oldLoci.size()-1).x;
				neckY = oldLoci.get(oldLoci.size()-1).y;
				oldNeckX = oldLoci.get(oldLoci.size()-2).x;
				oldNeckY = oldLoci.get(oldLoci.size()-2).y;
				boolean warpX = (Math.abs(neckX - oldNeckX) > 1);
				boolean warpY = (Math.abs(neckY - oldNeckY) > 1);
				
				if (warpX)
				{
					if (oldNeckX > neckX){oldNeckX = neckX - 1;}
					else {oldNeckX = neckX + 1;}
				}
				if (warpY)
				{
					if (oldNeckY < neckY){oldNeckY = neckY + 1;}
					else {oldNeckY = neckY - 1;}
				}
				
				//if (neckY == oldNeckY){neckBend = "we";}
				if (playerY < neckY)//To the north
				{
					if (playerX == oldNeckX){neckBend = "ns";}
					else if (oldNeckX < neckX){neckBend = "nw";}
					else{neckBend = "ne";}
					
				}
				else if (playerY > neckY)//To the south
				{
					if (playerX == oldNeckX){neckBend = "ns";}
					else if (oldNeckX < neckX){neckBend = "sw";}
					else{neckBend = "se";}
				}
				else if (playerX > neckX)//To the east
				{
					if (playerY == oldNeckY){neckBend = "we";}
					else if (oldNeckY < neckY){neckBend = "ne";}
					else{neckBend = "se";}
				}
				else if (playerX < neckX)//To the west
				{
					if (playerY == oldNeckY){neckBend = "we";}
					else if (oldNeckY < neckY){neckBend = "nw";}
					else{neckBend = "sw";}
				}
				
				/*
				if (warpX)
				{
				
					if (neckBend == "nw"){neckBend = "ne";}
					if (neckBend == "ne"){neckBend = "nw";}
					if (neckBend == "se"){neckBend = "sw";}
					if (neckBend == "sw"){neckBend = "se";}
					
				}
				if (warpY)
				{
					if (neckBend == "nw"){neckBend = "sw";}
					if (neckBend == "ne"){neckBend = "se";}
					if (neckBend == "se"){neckBend = "ne";}
					if (neckBend == "sw"){neckBend = "nw";}
					
				}
				*/
				
				
			
			
			
			}
			else if (oldLoci.size() == 1)
			{
				neckX = oldLoci.get(0).x;
				neckY = oldLoci.get(0).y;
				
				if (playerY < neckY){neckBend = "ns";}
				else if (playerX > neckX){neckBend = "sw";}
				else if (playerX < neckX){neckBend = "se";}
				
				
			}

			if (oldLoci.size() > 0)
			{
				neckX = oldLoci.get(oldLoci.size()-1).x;
				neckY = oldLoci.get(oldLoci.size()-1).y;
				
				
				if (warped)//The player went around the screen last time
				{
					
					if (neckBend == "nw")
					{
						if (playerY == neckY){neckBend = "ne";}
						else if (playerX == neckX) {neckBend = "sw";}
					}
					else if (neckBend == "ne")
					{
						if (playerY == neckY){neckBend = "nw";}
						else if (playerX == neckX) {neckBend = "se";}
					}
					else if (neckBend == "se")
					{
						if (playerY == neckY){neckBend = "sw";}
						else if (playerX == neckX) {neckBend = "ne";}
					}
					else if (neckBend == "sw")
					{
						if (playerY == neckY){neckBend = "se";}
						else if (playerX == neckX) {neckBend = "nw";}
					}
					
				}
				
				
				aniNeck.changeAnimation(neckBend);
				aniNeck.renderGear(neckX.doubleValue()*squareSize,neckY.doubleValue()*squareSize);
			}
		
			
		
		}
		
		if (mines.indexOf(new Point(playerX,playerY)) == -1)
		{
		aniPlayer.renderGear(playerX.doubleValue()*squareSize,playerY.doubleValue()*squareSize);
		}
		//Render Mines
		if (mines.size() > 0)
		{
			aniSkull.mainGear();
			aniSkullSpecial.mainGear();
			for (Point p: mines)
			{
				Integer mineX = p.x;
				Integer mineY = p.y;
				AniSquare pootSkull = aniSkull;
				
				if (p.x == playerX && p.y == playerY){pootSkull = aniSkullSpecial;}
				
				pootSkull.renderGear(mineX.doubleValue()*squareSize,mineY.doubleValue()*squareSize);
			}
			
		}


		
		if (skullCloudPoints.size() > 0)
		{
			aniSkullCloud.mainGear();
			
			for (Point poo: skullCloudPoints)
			{
				Integer redPoofX = poo.x - 1;
				Integer redPoofY = poo.y - 1;
				aniSkullCloud.renderGear(redPoofX.doubleValue()*squareSize,redPoofY.doubleValue()*squareSize);
			}
		}
		else
		{	
			if (food.size() > 0)
			{
				aniStar.mainGear();
			
				for (Point pee:food)
				{
					Integer starX = pee.x;
					Integer starY = pee.y;
					aniStar.renderGear(starX.doubleValue()*squareSize,starY.doubleValue()*squareSize);
				}
			}
			if (starCloudPoints.size() > 0)
			{
				aniStarCloud.mainGear();
			
				for (Point frt: starCloudPoints)
				{
					Integer whtPoofX = frt.x ;
					Integer whtPoofY = frt.y ;
					aniStarCloud.renderGear(whtPoofX.doubleValue()*squareSize,whtPoofY.doubleValue()*squareSize);
				}
			}
		}
		
	}
	
	private void movePlayerCog()
	{
		Point toGo = new Point (0,0);
		if (going == "n"){toGo.y = -1;}
		if (going == "s"){toGo.y = 1;}
		if (going == "e"){toGo.x = 1;}
		if (going == "w"){toGo.x = -1;}
		
		headLocus.x = headLocus.x + toGo.x; headLocus.y = headLocus.y + toGo.y;
		warped = false;
		if (headLocus.x < 0){headLocus.x = maxXY.x;warped = true;}
		if (headLocus.x > maxXY.x){headLocus.x = 0;warped = true;}
		if (headLocus.y < 0){headLocus.y = maxXY.y;warped = true;}
		if (headLocus.y > maxXY.y){headLocus.y = 0;warped = true;}
		pastGoings.add(going.replace("poop","pee"));
		
	}
	
	//private void putRects(Color color, Canvas to, Point ... pootme)
	
	private void putRect (Integer X, Integer Y, Color color, Canvas to)
	{
		GraphicsContext g2d = to.getGraphicsContext2D();
		Paint oldColor = g2d.getFill();
		g2d.setFill(color);
		g2d.fillRect(X*scale*squareSize,Y*scale*squareSize,scale*squareSize,scale*squareSize);
		
		g2d.setFill(oldColor);
		
	}
	
	private void putRects(ArrayList<Point> poots,Color color, Canvas to)
	{	
		try
		{
			for (Point p: poots)
			{
				putRect(p.x,p.y,color,to);
			}
		}
		catch (Exception e){}
	}
	
	private void clearCanvi (Canvas ... grind)
	{
		for (int a = 0; a < grind.length; a++)
		{
			grind[a].getGraphicsContext2D().clearRect(0,0,size,size);
		}
	}
	
	private ArrayList<Point> fullGrid(Boolean shuffle)//Returns a thrice shuffled grid
	{
		
		ArrayList<Point> toSend = fullGrid ();
		
		if (shuffle)
		{
			Collections.shuffle(toSend);
			Collections.shuffle(toSend);
			Collections.shuffle(toSend);
			
		}
		
		return toSend;
	}
	
	private ArrayList<Point> fullGrid ()//Gets every possible locus 
	{
		
		ArrayList<Point> toSend = new ArrayList<>();
		for (int _Y_ = 0; _Y_ < maxXY.y; _Y_++)
		{
			for (int _X_ = 0; _X_ < maxXY.x; _X_++)
			{
				
				if (_X_ != startXY.x && _Y_ != startXY.y)
				{
				toSend.add(new Point(_X_,_Y_));
				}
			}
		}
		
		return toSend;
	}
	
	private ArrayList<Point> pootFood()
	{
		ArrayList<Point> farter = fullGrid(true);
		int topoot = foodToPoot;
		
		while (topoot > 0)
		{
			food.add(farter.remove(0));
			topoot --;
		}
		
		return farter;
		
	}
	
	private void pootMines (ArrayList<Point> leftover)
	{
		int topoot = minesToPoot;
		while (topoot > 0 && leftover.size() > 0)
		{
			mines.add(leftover.remove(0));
			topoot--;
		}
	}
	
	private void sparkNewRound()// When all the food is gone launch this 
	{
		
	}
	
	private void sparkEnd() // When there is a match in oldLoci to headLocus
	{
		if (screenCurrent != screens.WEIRD)//KEEP GOING FOREVER!
		{
			
		}
	}
	
	
	public static void main (String[] args)
	{
		launch(args);
	}
	//-----------------------------------------JUNK------------------------------------
	private void highScoreGetter()
	{
		String tempThing = "";
		Path txtLocus = Paths.get("high.txt");
		
		if (Files.exists(txtLocus))
		{
			
		
		
		try
		{
			byte[] rawIn = Files.readAllBytes(txtLocus);
			tempThing = new String(rawIn);
		}
		catch (IOException e)
		{
			
		}
		
		int tempScore = -5;
		
		try
		{
			Integer.valueOf(tempThing.split(":")[1]) ;
		}
		catch (NumberFormatException e)
		{
			//They tampered with the high file! Make it weird!
		}
		if (tempScore > highScore)
		{
			highName = tempThing.split(":")[0];
			highScore = tempScore;
		}
		
		}
		highName = "Nobody";
		highScore = 51;
		score = 0;
	}
}