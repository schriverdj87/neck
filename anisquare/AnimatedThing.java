package anisquare;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import java.awt.Point;

public interface AnimatedThing //For dynamic objects
{
	/*--------Methods---------
	LAYER 1
	dispenseHashDirection() Creates a blank hash map of boolean values for compas directions
	rectToDots(Rectangle2D) or (AniSquare from) Hash map of compass directions to Points
	invertNSEW(String) -> String : Takes a compass direction ("n, ne ,e") and flips it
	
	LAYER 2
	directionBlock(HashMap<String,Boolean> blockee, HashMap<String,Boolean> block) -> HashMap<String,Boolean> : Returns a hash map of unobstructed directions
	dottyHits (AniSquare dots, AniSquare from) or (HashMap<String, Point2D> dots, Rectangle2D rect) -> ArrayList<String> : Checks if the dots are inside the square
	
	LAYER 3
	sideTest (AniSquare observer, AniSquare block) -> String : Boils a hit test down to a NSEW direction
	
	LAYER 4
	obstruct(AniSquare obstructee, AniSquare obstructor) -> String: Obstructs on all sides, returns sideTest;
	obstruct(AniSquare obstructee, AniSquare obstructor, String side) -> String: Obstructs only one side, returns sideTest;
	obstruct(AniSquare obstructee,ArrayList<AniSquare> obstructor) -> HashMap<String,Boolean>: Bulk for first version
	obstruct(AniSquare obstructee,ArrayList<AniSquare> obstructor, String side) -> HashMap<String,Boolean>: Bulk for second version
	*/
	
	public static final double diagMulti = 0.7071;
	
	public void mainGear ();//Main updated gear
	
	//Layer 4
	public static String obstruct(AniSquare obstructee, AniSquare obstructor)
	{
		String toSend = sideTest(obstructee,obstructor);
		Rectangle2D obstructeeR = obstructee.getRect();
		Rectangle2D obstructorR = obstructor.getRect();
		
		//Maybe remove the 0.001s eventually?
		
		if (toSend == "n")
		{
			obstructee.Yf = (double)(obstructorR.getY() + obstructorR.getHeight());
		}
		
		if (toSend == "s")
		{
			obstructee.Yf = (double)(obstructorR.getY() - obstructeeR.getHeight());
		}
		
		if (toSend == "e")
		{
			obstructee.Xf = (double)(obstructorR.getX() - obstructeeR.getWidth());
		}
		
		if (toSend == "w")
		{
			obstructee.Xf = (double)(obstructorR.getX() + obstructorR.getWidth());
		}
		
		return toSend;
	}
	
	
	public static String obstruct(AniSquare obstructee, AniSquare obstructor, String side)
	{
		String toSend = sideTest(obstructee,obstructor);
		Rectangle2D obstructeeR = obstructee.getRect();
		Rectangle2D obstructorR = obstructor.getRect();
		
		if (toSend.equals(side) == false)
		{
			toSend = "";
		}
		
		//Maybe remove the 0.001s eventually?
		
		if (toSend == "n")
		{
			obstructee.Yf = (double)(obstructorR.getY() + obstructorR.getHeight());
		}
		
		if (toSend == "s")
		{
			obstructee.Yf = (double)(obstructorR.getY() - obstructeeR.getHeight());
		}
		
		if (toSend == "e")
		{
			obstructee.Xf = (double)(obstructorR.getX() - obstructeeR.getWidth());
		}
		
		if (toSend == "w")
		{
			obstructee.Xf = (double)(obstructorR.getX() + obstructorR.getWidth());
		}
		
		return toSend;
	}
	

	
	//Layer 3
	public static String sideTest (AniSquare observer, AniSquare block)
	{//Boils a hit test down to a NSEW direction
		String toSend = "";
		boolean inverted = false;
		
		ArrayList<String> hits = dottyHits(observer,block);
		if (hits.size() == 0)
		{
			inverted = true;
			hits = dottyHits(block,observer);
		}
		
		//One of the sides hit
		if (hits.indexOf("n") != -1)
		{
			toSend = "n";
		}
		else if (hits.indexOf("s") != -1)
		{
			toSend = "s";
		}
		
		else if (hits.indexOf("e") != -1)
		{
			toSend = "e";
		}
		else if (hits.indexOf("w") != -1)
		{
			toSend = "w";
		}
		
		//One of the corners hit
		if (toSend == "" && hits.size() != 0)
		{
			String daCorner = hits.get(0);
			String daOpposite = invertNSEW(daCorner);
			HashMap<String, Point2D> dots1;
			HashMap<String, Point2D> dots2;
			
			if (inverted)
			{
				dots1 = rectToDots(block);
				dots2 = rectToDots(observer);
			}
			else
			{
				dots1 = rectToDots(observer);
				dots2 = rectToDots(block);
			}
			
			Point2D corner1 = dots1.get(daCorner);
			Point2D corner2 = dots2.get(daOpposite);
			
			double Xdiff = Math.abs((double)corner1.getX() - (double)corner2.getX());
			double Ydiff = Math.abs((double)corner1.getY() - (double)corner2.getY());
			
			if (Xdiff > Ydiff)
			{
				toSend = toSend + daCorner.charAt(0);
			}
			else
			{
				toSend = toSend + daCorner.charAt(1);
			}
			
		}
		
		
		if (inverted == true)
		{
			return (invertNSEW(toSend));
		}
		return toSend;
	}
	
	
	//Layer 2
	
	public static ArrayList<String> dottyHits (AniSquare dots, AniSquare from)
	{
		
		return dottyHits(rectToDots(dots),from.getRect());
	}
	
	public static ArrayList<String> dottyHits (HashMap<String, Point2D> dots, Rectangle2D rect)
	{
		ArrayList<String> toSend = new ArrayList<String>();
		
		if (rect.contains(dots.get("nw"))){toSend.add("nw");}
		if (rect.contains(dots.get("n"))){toSend.add("n");}
		if (rect.contains(dots.get("ne"))){toSend.add("ne");}
		if (rect.contains(dots.get("e"))){toSend.add("e");}
		if (rect.contains(dots.get("se"))){toSend.add("se");}
		if (rect.contains(dots.get("s"))){toSend.add("s");}
		if (rect.contains(dots.get("sw"))){toSend.add("sw");}
		if (rect.contains(dots.get("w"))){toSend.add("w");}
		
		return toSend;
	}
	
	
		public static HashMap<String,Boolean> obstruct(AniSquare obstructee,ArrayList<AniSquare> obstructor)
	{
		HashMap<String,Boolean> toSend = dispenseHashDirection ();
		
		if (obstructor.size() > 0 )
		{
		for (AniSquare a: obstructor)
		{
			String toPoot = obstruct(obstructee,a);
			
			if(toPoot != "")
			{
				toSend.put(toPoot,true);
			}
		}
		}
		
		return toSend;
	}
	
	public static HashMap<String,Boolean> obstruct(AniSquare obstructee,ArrayList<AniSquare> obstructor, String side)
	{
		HashMap<String,Boolean> toSend = dispenseHashDirection ();
		
		if (obstructor.size() > 0 )
		{
		for (AniSquare a: obstructor)
		{
			String toPoot = obstruct(obstructee,a,side);
			
			if(toPoot != "")
			{
				toSend.put(toPoot,true);
			}
		}
		}
		
		return toSend;
	}
	
	public static HashMap<String,Boolean> directionBlock (HashMap<String,Boolean> blockee, HashMap<String,Boolean> block)//counters out the direction of the next one
	{
		HashMap<String,Boolean> toSend = dispenseHashDirection();
		Object[] keys = toSend.keySet().toArray();
		
		for (int a = 0; a < keys.length; a++)
		{
			String daKey = (String) keys[a];
			toSend.put(daKey,blockee.get(daKey));
			
			if (block.get(daKey) == true)
			{
				toSend.put(daKey, false);
			}
			
			
		}
		
		
		return toSend;
	}
	
	//Layer 1 
	public static String invertNSEW (String from)
	{
		if (from == "nw"){return "se";}
		if (from == "n"){return "s";}
		if (from == "ne"){return "sw";}
		if (from == "e"){return "w";}
		if (from == "se"){return "nw";}
		if (from == "s"){return "n";}
		if (from == "sw"){return "ne";}
		if (from == "w"){return "e";}
		
		return "";
	}
	
	public static HashMap<String, Point2D> rectToDots (AniSquare from)
	{
		return rectToDots (from.getRect());
	}
	
	public static HashMap<String, Point2D> rectToDots (Rectangle2D from )
	{
		HashMap<String, Point2D> toSend = new HashMap<String, Point2D>();
		toSend.put("nw",new Point2D.Double((double)from.getX(),(double)from.getY()));
		toSend.put("n",new Point2D.Double((double)(from.getX() + from.getWidth()/2),(double)from.getY()));
		toSend.put("ne",new Point2D.Double((double)(from.getX() + from.getWidth()),(double)from.getY()));
		toSend.put("e",new Point2D.Double((double)(from.getX() + from.getWidth()),(double)(from.getY() + from.getHeight()/2)));
		toSend.put("se",new Point2D.Double((double)(from.getX() + from.getWidth()),(double)(from.getY() + from.getHeight())));
		toSend.put("s",new Point2D.Double((double)(from.getX() + from.getWidth()/2),(double)(from.getY() + from.getHeight())));
		toSend.put("sw",new Point2D.Double((double)(from.getX()),(double)(from.getY() + from.getHeight())));
		toSend.put("w",new Point2D.Double((double)(from.getX()),(double)(from.getY() + from.getHeight()/2)));
		toSend.put("c", new Point2D.Double((double)from.getCenterX(),(double)from.getCenterY()));
		
		
		return toSend;
		
	}
	
	public static HashMap<String,Boolean> dispenseHashDirection ()
	{
		HashMap<String,Boolean> toSend = new HashMap<String,Boolean>();
		toSend.put("n",false);
		toSend.put("s",false);
		toSend.put("e",false);
		toSend.put("w",false);
		toSend.put("c",false);
		
		return toSend;
	}
	
	public static HashMap<String, ArrayList<Point>> imgToHash(Image toScan)
	{
		HashMap<String, ArrayList<Point>> toSend = new HashMap<>();
		
		PixelReader scanCore = toScan.getPixelReader();
		
		for (int _y_ = 0; _y_ < toScan.getHeight(); _y_++)
		{
			for (int _x_ = 0; _x_ < toScan.getWidth(); _x_++)
			{
				String daColor = scanCore.getColor(_x_,_y_).toString();
				
				if (toSend.get(daColor) == null){toSend.put(daColor,new ArrayList<Point>());}
				toSend.get(daColor).add(new Point(_x_,_y_));
				
			}
		}
		
		
		return toSend;
	}
	
	public static HashMap<String, ArrayList<Point>> imgToHash(String toScan)
	{
		return imgToHash(new Image(toScan));
	}
	
}