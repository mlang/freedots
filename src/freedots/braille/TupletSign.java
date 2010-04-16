package freedots.braille;

import freedots.music.Tuplet;
import freedots.music.TupletGroup;

public class TupletSign extends Sign{
 
    private Tuplet tuplet;
    private TupletGroup tupletGroup=null;
   
    public TupletSign(Tuplet tuplet){
	super(getSign(tuplet));
	this.tuplet=tuplet;
    }
    
    public TupletSign(TupletGroup tupletGroup){
    	super(getSign(tupletGroup));
    	this.tupletGroup=tupletGroup;
	this.tuplet=tupletGroup.getFirst();
        }
    
    public String getDescription(){
	return "Indicates a tuplet of "+tuplet.getType();
    }
    
    private static String getSign(final Tuplet tuplet) {
	int type=tuplet.getType();
	final int[] tupletDots = { 23, 25, 0, 26, 235, 2356};  //what the sign for a 4-uplet?
	if (type==3 && tuplet.getParent()==null)
	    return braille(23);
	return braille(456,tupletDots[type-2],3);		
    }
    
    private static String getSign(final TupletGroup tupletGroup) {
    	int type=tupletGroup.getType();
    	final int[] tupletDots = { 23, 25, 0, 26, 235, 2356};  //what the sign for a 4-uplet?
    	if (type==3 && tupletGroup.getFirst().getParent()==null)
    	    return braille(23,23);
    	return braille(456,tupletDots[type-2],456)+braille(tupletDots[type-2],3);		
        }
    
}
