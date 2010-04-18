/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2010 Mario Lang  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details (a copy is included in the LICENSE.txt file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */

package freedots.gui.swing;

import java.awt.Font;

import javax.swing.JLabel;


import jm.JMC;
import jm.gui.cpn.Notate2;
import jm.music.data.*;
import jm.util.*;
import javax.swing.JPanel;

import freedots.music.Event;
import freedots.music.MusicList;
import freedots.musicxml.Part;
import freedots.musicxml.Pitch;
import freedots.transcription.Transcriber;

/** Displays the score in a text pane.
 */
public class ScorePane extends JPanel {
  private static final int DEFAULT_SCORE_PANE_WIDTH = 450;
  private static final int DEFAULT_SCORE_PANE_HEIGHT = 450;
	  
  private int scorePaneWidth = DEFAULT_SCORE_PANE_WIDTH;
  private int scorePaneHeight = DEFAULT_SCORE_PANE_HEIGHT;
	 
	
  private jm.music.data.Score score;
  public Notate2 not;
  public JPanel panel;
  
  public Phrase phr;
	
	ScorePane() {
		//super();
	    //setFont(new Font("DejaVu Serif", Font.PLAIN, 14));
	    //JLabel label = new JLabel ("Part for the screen display");
	    //add(label);
	    //setEditable(false);
		
		this.phr = new Phrase();
	    
	  /*
		Note n = new Note(60, 0.25);
		Phrase phr = new Phrase();
        phr.addNote(n);
        n = new Note(64, 0.5);
        phr.addNote(n);
        
        // Avec View et Notate
        View.notate(phr);
        
        */
        
        // Avec Notate2
	    //not = new Notate2(phr);
	    //panel = not.display();
	}
	
	void displayScore(Transcriber transcriber){
				
		freedots.musicxml.Score score = transcriber.getScore();
		Object scoreObject = null;
		int index;
		
		System.out.println("Score display");
		
		for(index = 100; index < 300; index ++){
					
			scoreObject = transcriber.getScoreObjectAtIndex(index);

			if(scoreObject != null)
				System.out.println("ScoreObject class : " + scoreObject.getClass().toString());
		
			if(scoreObject instanceof freedots.musicxml.Note){
				freedots.musicxml.Note note = (freedots.musicxml.Note)scoreObject;
				try{
					int pitch = note.getPitch().getMIDIPitch();
					float duration = note.getDuration().floatValue();
				
					System.out.println("Score display : Pitch et duration : " + pitch + " " + duration);
			
					Note n = new Note(pitch, duration);
					phr.addNote(n);
				}
				catch(Exception e){ System.out.println(e.getMessage());}
			}
			
		
			
		}
		
		System.out.println("Score display : on sort de la boucle");
		
		View.notate(phr);
	}
	
  public int getScorePaneWidth() { return scorePaneWidth; }  
  public int getScorePaneHeight() { return scorePaneHeight; }
}

