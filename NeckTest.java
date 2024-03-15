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

public class NeckTest extends Application
{
	
	//-------Tester
	private Image testImage =  AniSquare.loadImage("visuals/TitleBKGsanshead.png",2);
	private Image smokeImage2 = AniSquare.loadImage("visuals/Naybur2.png",2);
	
	
	//-----------------Tester
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
			srnTitleBKG.getGraphicsContext2D().drawImage(testImage,0.0,0.0);
			
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
	
	///ANISQUARE STUFF
	{AniSquare.setScale(scale);}
	private AniSquare aniPlayer = new AniSquare(srnTitleBKG.getGraphicsContext2D(),"visuals\\naybur2.png",AniSquare.loadImage("visuals/Naybur2.png",2),15.0,15.0,"n,e,s,w,N,E,S,W");{aniPlayer.setAnimationLock(false);aniPlayer.setFrameReset(false); aniPlayer.setFrameRate(0);aniPlayer.changeAnimation("N");aniPlayer.setPlayDirection(AniSquare.going.PLAY);}
	
	/*
	private int size = 720;
	private Stage myStage;
	private StackPane root = new StackPane();
	private Scene rootScene = new Scene(root,size,size);
	private Canvas canvas = new Canvas (size,size);
	
	{root.getChildren().add(canvas); 
	canvas.getGraphicsContext2D().setFill(Color.BLACK);
	canvas.getGraphicsContext2D().fillRect(0.0,0.0,800.0,800.0);
	canvas.getGraphicsContext2D().drawImage(AniSquare.loadImage("visuals/TitleBKG.png",2),0.0,0.0);
	}
	*/
	
		@Override
	public void start (Stage stage)
	{
		myStage = stage;
		myStage.setScene(rootScene);
		myStage.setTitle("Neck");
		myStage.show();
		myStage.setOnCloseRequest(e -> timer.cancel());
		FileInputStream imgIn;
		Image Nugget = AniSquare.loadImage("visuals/Naybur2.png",3);
		aniPlayer.mainGear();
		aniPlayer.mainGear();
		aniPlayer.mainGear();
		aniPlayer.renderGear();
		/*
		try
		{
		imgIn =	new FileInputStream("visuals/naybur2.png");
		smokeImage = new Image(imgIn);
		imgIn.close();
		
		}
		catch (IOException e)
		{
			
		}
		finally
		{
			//if (imgIn != null){}
		}
		*/
		
		//srnTitleBKG.getGraphicsContext2D().drawImage(Nugget,0.0,0.0);
		fart();
		//myStage.setOnCloseRequest(e -> timer.cancel());
		//rootScene.setOnKeyPressed(new keyListen());
		//rootScene.setOnKeyReleased(new keyListen());
		//resetGame();
		//loopTimer();
		
	}
	/*
	public static Image loadImage (String from, int tmpScale)
	{
		Image rawIMG = new Image(from);

		
		return new Image(from,rawIMG.getWidth()*tmpScale,rawIMG.getHeight()*tmpScale,true,false);
	}
	
	public static Image loadImage2 (String from, int tmpScale)
	{
		FileInputStream incoming;
		Image toSend ;
		try
		{
		incoming = new FileInputStream(from);
		Image rawIMG = new Image(incoming);
		
		
		toSend = new Image(incoming,rawIMG.getWidth()*tmpScale,rawIMG.getHeight()*tmpScale,true,false);
	
		return toSend;
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
		finally
		{
			
		}
		
		
		System.out.println("IO FAILED!");
		return loadImage (from,tmpScale);
	}
	
	*/
	public void fart()
	{
		//AniSquare test = new AniSquare(srnGameOther.getGraphicsContext2D(),"visuals/naybur2.png",smokeImage2,15.0,15.0,"n,e,s,w,N,E,S,W");
	}
	
	public static void main (String[] args)
	{
		launch(args);
	}
}