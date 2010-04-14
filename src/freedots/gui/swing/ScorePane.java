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


import jm.JMC;
import jm.music.data.*;
import jm.util.*;

/** Displays the score in a text pane.
 */
public class ScorePane extends javax.swing.JPanel {
	private Score score;
	
	ScorePane() {
		super();
	    setFont(new Font("DejaVu Serif", Font.PLAIN, 14));
	    //setEditable(false);
	  
		Note n = new Note(60, 0.25);
        Phrase phr = new Phrase();
        phr.addNote(n);
        
        //View.notate(phr);
        this.score = new Score(new Part(phr));
        score = new Score(new Part(phr));
        setVisible(true);

	}
}

