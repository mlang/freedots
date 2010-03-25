package freedots.music;

import freedots.musicxml.Note;

public class Tuplet extends java.util.LinkedList<TupletElement> implements TupletElement {
	
	public Tuplet(final TupletElement initialElement){
		super();
		add(initialElement);	
	}
	
}
