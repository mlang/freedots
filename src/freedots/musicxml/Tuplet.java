package freedots.musicxml;
import freedots.music.TupletElement;

public class Tuplet extends freedots.music.Tuplet {
		
	private Tuplet parent=null; //if parent is null, the tuplet is the main tuplet
	
	public Tuplet() { }
	
	public final boolean addNote(final Note note) {
	    if (super.add(note)) {
	      note.addTuplet(this);
	      return true;
	    }
	    return false;
	}
	
	public final boolean addTuplet(Tuplet tuplet){	
		if (super.add(tuplet)){
			tuplet.setParent(this);  //A tuplet is only in one tuplet
			return true;
		}
		return false;
	}
	
	public void setParent(Tuplet tuplet){
		this.parent=tuplet;
	}
	
	public Tuplet getParent(){
		return parent;
	}
	
	public boolean completed(){
		return true;
	}
}

