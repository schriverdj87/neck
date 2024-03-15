import javafx.scene.media.AudioClip;
import java.util.*;

public class SndBox
{
	/*
	playThis(String) plays the indexed sound
	getSoundKeys() gets keys
	
	*/
	
	
	private HashMap<String,AudioClip> myLib = new HashMap<>();
	private static double masterVolume = 1.0;
	private double myVolume = 1.0;
	
	public SndBox ()
	{
		AudioClip testSnd = new AudioClip(this.getClass().getResource("BLITTERI.wav").toExternalForm());
		testSnd.play();
	}
	
	public SndBox (ArrayList<String> daList)
	{
		for (String a : daList)
		{
			try
			{
				AudioClip pootMe = new AudioClip(this.getClass().getResource(a).toExternalForm());
				int preIndex = a.lastIndexOf("/") > -1 ? a.lastIndexOf("/") + 1:0;
				
				
				if (a.indexOf(".") != -1)
				{
					myLib.put(a.substring(preIndex,a.indexOf(".")),pootMe);
					//System.out.println(a.substring(preIndex,a.indexOf(".")));
				}
			}
			catch (NullPointerException e)
			{
				System.err.println(e);
			}
		}
	}
	
	public void playThis (String toPlay)
	{
		if (myLib.get(toPlay) != null)
		{
			
			myLib.get(toPlay).play(masterVolume);
		}
		else
		{
			System.out.println(toPlay + " is not in the library!");
		}
	}
	//Getters
	public ArrayList<String> getSoundKeys()
	{
		ArrayList<String> toSend = new ArrayList<>();
		
		for (Object a : myLib.keySet().toArray())
		{
			toSend.add((String) a);
		}
		
		return toSend;
	}
	
	//Setters
	public static final void setMasterVolume (double to)
	{
		masterVolume = Math.max(0.0,to);
		
	}
	
	public void setMyVolume (double to)
	{
		myVolume = Math.max(to,0);
		
		for (String a : getSoundKeys())
		{
			myLib.get(a).setVolume(myVolume * masterVolume);
		}
	}
	
	public static void main (String[] args)
	{
		//ArrayList<String> poop = new ArrayList<>();
		//poop.add("BLITTERI.wav");
		//new SndBox(poop);
	}
}