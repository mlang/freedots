/*

<This Java Class is part of the jMusic API version 1.5, March 2004.>

Copyright (C) 2000 Andrew Sorensen & Andrew Brown

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or any
later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

Enhanced by the Derryn McMaster

*/ 

package jm.util;

import jm.midi.MidiSynth;
import jm.music.data.*;
import jm.JMC;

/**
* This loads and plays the Score object in it's own thread
*/
class PlayThread extends Thread{
	private Score s;
	private MidiSynth ms;
	
	public PlayThread (Score segment){
		s = segment;
		ms = new MidiSynth();
	}
	
	public void run (){
		try {
			ms.play(s);
		}
		catch (Exception e) {
			System.err.println("MIDI Playback Error:" + e);
			return;
		}
	}
        
        /*
        * Halts the current playback.
        */
        public void stopPlayThread() {
            ms.stop();
        }
	
}
