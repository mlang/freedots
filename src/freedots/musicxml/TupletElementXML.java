package freedots.musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import freedots.musicxml.Note.Notations.Slur.Type;

public class TupletElementXML {
	private Element element;
	enum Type { START, STOP; }
	private final Type type;
	private final Integer number;
	
	public TupletElementXML(Element element){
		this.element=element;
		number=element.hasAttribute("number")?
				new Integer(element.getAttribute("number")): new Integer(1);
		type=	Enum.valueOf(Type.class,
				element.getAttribute("type").trim().toUpperCase());
	}
	
	public int tupletElementXMLNumber(){
		return number;
	}
	
	Type tupletElementXMLType(){
		return type;
	}
}	
