package org.delysid.freedots.gui.swing;


import java.awt.Dimension;

import javax.swing.JLabel;

public class StatusBar extends JLabel {

	  public StatusBar() {
	        super();
	        super.setPreferredSize(new Dimension(100, 16));
	        setMessage("Ready");
	    }
	  
	  public void setMessage(String message) {
	        setText(" "+message);        
	    }    
	
	
}
