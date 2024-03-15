package anisquare;

public interface AniVent //<A extends AniSquare, B extends AniSquare>
{
	//public void say (A sender, B target, String ... message);//Sender is the one that dispatches the event, target is the other thing
	public void say (AniSquare sender, AniSquare target, String ... message);
}