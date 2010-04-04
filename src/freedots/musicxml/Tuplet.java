package freedots.musicxml;
import freedots.music.TupletElement;
import freedots.math.Fraction;
import freedots.math.PowerOfTwo;

public class Tuplet extends freedots.music.Tuplet {
		
    private Tuplet parent=null; //if parent is null, the tuplet is the main tuplet
    private Fraction actualType=null;
    private Fraction normalType=null;
    
    public Tuplet(){
    }
    
    public void setActualType(Fraction actualType){
	this.actualType=actualType;
    }
    
    public void setNormalType(Fraction normalType){
	this.normalType=normalType;
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
    
    public Tuplet getParent(){
	return parent;
    }
    
    public Fraction getActualType(){
	if (actualType==null){
	    return getActual();
	    }
	return actualType;
    }
	
    public Fraction getNormalType(){
	if (normalType==null){
	    return getNormal();
	}
	
	return normalType;
    }
    
    private Fraction getNormal(){
	if (getParent()==null && getFirst() instanceof Note){
	    Note note=(Note)getFirst();
	    int num=note.getTimeModification().getNormalNotes()*note.getTimeModification().getNormalType().numerator();
	    int den=note.getTimeModification().getNormalType().denominator();
	    return new Fraction(num,den).simplify();
	} 
	return null;
    }
    
    private Fraction getActual(){
	if (getParent()==null && getFirst() instanceof Note){
	    Note note=(Note)getFirst();
	    int num=note.getTimeModification().getActualNotes()*note.getTimeModification().getNormalType().numerator();
	    int den=note.getTimeModification().getNormalType().denominator();
	    return new Fraction(num,den).simplify();
	} 
	return null;
    }


    public boolean completed(){
	Fraction expectedFrac=this.getNormalType();
	Fraction sumFrac=new Fraction(0,1);
	for (TupletElement tE: this){
	    Fraction currentFrac=new Fraction(0,1);
	    if (tE instanceof Tuplet){
		Tuplet tuplet=(Tuplet)tE;
		currentFrac=tuplet.getNormalType();
	    }
	    else{
		Note note=(Note)tE;
		int den=((PowerOfTwo)note.getAugmentedFraction()).denominator();
		int num=((PowerOfTwo)note.getAugmentedFraction()).numerator();
		currentFrac=new Fraction(num,den);
	    }
	    sumFrac=sumFrac.add(currentFrac);
	}
	System.out.println("sumFrac"+sumFrac+"expectedFrac"+expectedFrac);
	return sumFrac.equals(expectedFrac);
    }
    
    
}

