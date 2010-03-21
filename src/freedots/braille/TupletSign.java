package freedots.braille;

public class TupletSign extends Sign{
	
	public TupletSign(){
		
		super(braille(23));
	}
	
	public String getDescription(){
		return "Indicates a tuplet";
	}
	

}
