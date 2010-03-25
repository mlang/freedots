package freedots.musicxml;
import freedots.music.TupletElement;

public class Tuplet extends freedots.music.Tuplet {
		
	public Tuplet(final TupletElement initialNote) { super(initialNote); }
	
	public final boolean addNote(final Note note) {
	    if (super.add(note)) {
	      note.addTuplet(this);
	      return true;
	    }
	    return false;
	}
	
	public final boolean addTuplet(Tuplet tuplet){	
		if (super.add(tuplet)){
			tuplet.add(this);
			return true;
		}
		return false;
	}
}

