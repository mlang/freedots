package freedots.musicxml;

import freedots.music.TupletElement;

public class TupletGroup extends java.util.LinkedList<Tuplet>{

	public final boolean addTuplet(final Tuplet tuplet){
		if (super.add(tuplet)) {
			tuplet.setTupletGroup(this);
			return true;
		}
		return false;
	}
	
	public String getVoiceName(){
		if (this.size()==0)
			return null;
		TupletElement tE=this.getFirst();
		while (!(tE instanceof Note))
			tE=((Tuplet)tE).getFirst();
		return ((Note)tE).getVoiceName();
	}
		
	
}
