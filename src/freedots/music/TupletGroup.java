package freedots.music;

import freedots.musicxml.Note;

public class TupletGroup extends java.util.LinkedList<Tuplet>{

	public final boolean addTuplet(final Tuplet tuplet){
		if (super.add(tuplet)) {
			tuplet.setTupletGroup(this);
			return true;
		}
		return false;
	}
	
	public int getType(){
		return getFirst().getType();
	}
	
	public boolean isFirstOfTupletGroup(Tuplet tuplet){
		try {
			return getFirst() == tuplet;
		} catch (java.util.NoSuchElementException e) {
			return false;
		}
	}
	
	public boolean isLastOfTupletGroup(Tuplet tuplet){
		try {
			return getLast() == tuplet;
		} catch (java.util.NoSuchElementException e) {
			return false;
		}
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
