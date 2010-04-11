package freedots.musicxml;
import freedots.music.TupletElement;
import freedots.math.Fraction;
import freedots.math.PowerOfTwo;

public class Tuplet extends freedots.music.Tuplet {
		
    
    
  
    public Tuplet(){
    }
    
  
    
    public Fraction getNextMoment(){
    	TupletElement tE=this.getLast();
    	while (!(tE instanceof Note))
    		tE=((Tuplet)tE).getLast();
    	return ((Note)tE).getMoment().add(((Note)tE).getDuration());
    }
    
    public Fraction getMoment(){
    	TupletElement tE=this.getFirst();
    	while (!(tE instanceof Note))
    		tE=((Tuplet)tE).getFirst();
    	return ((Note)tE).getMoment();
    }
	
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
    
    


    public boolean completed(){
	
	Fraction expectedFrac=this.getActualType().simplify();
	Fraction sumFrac=new Fraction(0,1);
	for (TupletElement tE: this){
		Fraction currentFrac=new Fraction(0,1);
		if (tE instanceof Tuplet){
			Tuplet tuplet=(Tuplet)tE;
			currentFrac=tuplet.getNormalType();
	    }
	    else{
	    	Note note=(Note)tE;
	    	PowerOfTwo pot=new PowerOfTwo(note.getAugmentedFraction().getPower());
	    	int den=pot.denominator();
	    	int num=pot.numerator();
	    	currentFrac=new Fraction(num,den);
	    }
		sumFrac=sumFrac.add(currentFrac);	
	}
	return sumFrac.equals(expectedFrac);
    }
    
    
}

