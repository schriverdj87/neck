package jacknives;

import java.awt.Point;
import java.awt.geom.Point2D;

public class PointJacknife
{
	public static final double getAngle (Point2D.Double pt1, Point2D.Double pt2)
	{
		Point2D.Double combined = new Point2D.Double (pt1.x - pt2.x, pt1.y - pt2.y);
		Double rawAngle = Math.atan(combined.x/combined.y));
		
		if (combined.x > 0)
		{
			rawAngle = rawAngle + Math.PI;
		}
		if (pt1.x == pt2.x)
		{
			if (pt1.y < pt2.y)
			{
				rawAngle = (Math.PI * 2)/4;
			}
			else 
			{
				rawAngle = ((Math.PI * 2)/4) * 3;
			}
			
			
		}
		
		return rawAngle;
	}
	
	public static final Point2D.Double chase (Point2D.Double pt1, Point2D.Double pt2, double speed)
	{
		Double daAngle = getAngle(pt1,pt2);
		return new Point2D.Double (Math.cos(daAngle) * speed,Math.sin(daAngle));
	}
}