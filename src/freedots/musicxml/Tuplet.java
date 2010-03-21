package freedots.musicxml;

public class Tuplet extends freedots.music.Tuplet<Note> {
		
	Tuplet(final Note initialNote) { super(initialNote); }
	
	@Override
	public final boolean add(final Note note) {
	    if (super.add(note)) {
	      note.addTuplet(this);
	      return true;
	    }
	    return false;
	}
}

