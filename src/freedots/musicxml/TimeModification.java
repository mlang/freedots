package freedots.musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class TimeModification {
	
	private Element element;
	private int actualNotes, normalNotes;
	private Text normalType=null;
	
	public TimeModification(Element element){
		this.element=element;
		actualNotes=Integer.parseInt(Score.getTextNode(element, "actual-notes").getWholeText());
		normalNotes=Integer.parseInt(Score.getTextNode(element, "normal-notes").getWholeText());
		normalType=Score.getTextNode(element, "normal-type");
	}
	
	public int getActualNotes(){
		return actualNotes;
	}
	
	public int getNormalNotes(){
		return normalNotes;
	}

}
