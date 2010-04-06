package freedots.braille;
import freedots.music.AugmentedPowerOfTwo;
import freedots.music.Tuplet;

public class TupletSign extends Sign{
	
	public TupletSign(Tuplet tuplet){
		super(getSign(tuplet));
	}
	
	public String getDescription(){
		return "Indicates a tuplet of";
	}
	
	private static String getSign(final Tuplet tuplet) {
		int type=tuplet.getType();
		final int[] tupletDots = { 23, 25, 0, 26, 235, 2356};  //what the sign for a 4-uplet?
		if (type==3 && tuplet.getParent()==null)
			return braille(23);
		return braille(456,tupletDots[type-1],3);		
	}

}
