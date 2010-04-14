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
* This Thread is run to enable a Score object to cycle-play independantly from
* the currently executing thread.  This is particularly important if a GUI 
* controller is being used to manipulate the Score object, as without using this 
* extra Thread the GUI would effectively be tied-up until the Score had finished 
* playing (which in the case of a loop means it would never be untied, rendering 
* the GUI useless).
*/
public class PlayCycle extends Thread{
	
	private Score s;
	private MidiSynth ms;
        private PlayThread pt;
 /**
  * Constructor for the Thread.  Sets the Score to be played.
  * @param Score The score to be played
  */
	public PlayCycle(Score segment){
		s = segment;
                //ms = new MidiSynth();
	}
	
 /**
  * Creates a PlayThread, waits for the length of time the score will play for, 
  * then repeats (loops) while Play.cycleIsPlaying() == true.
  */
	public void run(){
		System.out.println("Cycle-playing "+s.getTitle());
		while (Play.cycleIsPlaying()){
			//creates a defensive copy to avoid any protection issues
			Score defensiveCopy = s.copy();
                        pt = new PlayThread(defensiveCopy);
			new Thread(pt).start();
			//waits for a Score-length of time until starting to load the next segment
			Play.waitCycle(defensiveCopy);
		}
		System.out.println("Stopping");
		Play.stopCycle();//resets the cyclePlaying variable to false
	}
        
        public void stopPlayCycle() {
            pt.stopPlayThread();
        }
}


        

        

