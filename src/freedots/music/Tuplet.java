package freedots.music;

import freedots.musicxml.Note;

public class Tuplet<T extends VoiceElement> extends java.util.LinkedList<T>{
	
	public Tuplet(final T initialNote){
		super();
		add(initialNote);	
	}
	
}
