package freedots.music;

import freedots.musicxml.Note;

public class Tuplet extends java.util.LinkedList<TupletElement> implements TupletElement {
	
	public Tuplet(){
		super();	
	}
	
	public boolean isFirstNoteOfTuplet(TupletElement note) {
	    try {
	      return getFirst() == note;
	    } catch (java.util.NoSuchElementException e) {
	      return false;
	    }
	  }
	
}
