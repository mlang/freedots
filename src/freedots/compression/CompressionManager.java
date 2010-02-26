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
package freedots.compression;

import freedots.braille.*;

import java.util.LinkedList;

public final class CompressionManager{

	public void ApplyDoubling(BrailleSequence seq, LinkedList<SubClassName> classList){
	    LinkedList<OccurrenceCounter<?>> counterList = new LinkedList<OccurrenceCounter<?>>();
	    for(SubClassName scn : classList){
	        //FIXME : shall conserve the link between position in the list and the class represented.
	        counterList.add(generateCounterFromClass(scn));
	    }
	    /** TODO : must apply doubling to every class present in the classList.
	     */	
	}
	
	public void ApplyRepetitions(BrailleSequence seq, LinkedList<SubClassName> classList){
	    /** TODO : must apply repetition algorithm to every class present in the classList.
	     * FIXME : should tackle the classes repetition in a bottom-up order to improve speed.
	     */	
	}
	
	
	public static OccurrenceCounter<?> generateCounterFromClass(SubClassName className){
	    switch(className){
	        case SLUR :
	            return new OccurrenceCounter<SlurSign>();
	        case CHORD :
	            return new OccurrenceCounter<BrailleChord.IntervalSign>();
	        case BRAILLENOTE :
	            return new OccurrenceCounter<BrailleNote>();
	        default :
	            return new OccurrenceCounter<BrailleSequence>();
	    }
	}
    
    /**
     * attempt to implement doubling for slurs only
     * FIXME : to complete, and to generalize as much as possible
     * Besides, if we implement it this way, we won't be able to do 
     * all the algorithms in only one cover of the BrailleList.
     */
    public void applyDoublingToSlur(BrailleList seq){
        OccurrenceCounter<SlurSign> slurCounter = new OccurrenceCounter<SlurSign>();
        applyDoublingToSlurRec(seq, slurCounter);
    }
    
    private void applyDoublingToSlurRec(BrailleList seq, OccurrenceCounter<SlurSign> slurCounter){
        for(BrailleSequence subSeq : seq){
            if(subSeq instanceof BrailleNote){// then we look if it is a slurred note
                for(BrailleSequence subSubSeq : (BrailleList)subSeq){
                    if(subSubSeq instanceof SlurSign){// then we add the SlurSign to out SlurSign counter.
                        slurCounter.addElement((SlurSign)subSubSeq);
                        break;
                    }
                    else{// we stop the counter, we apply doubling to its elements and drop them to start a new count later.
                        doublingOnCounterElements(slurCounter);
                    }
                }
            }
            else if (subSeq instanceof BrailleList){ // then we have to call recursively the algorithm
                applyDoublingToSlurRec((BrailleList)subSeq, slurCounter);  
            }   
        }
    }
    
    
    
    public void doublingOnCounterElements(OccurrenceCounter<? extends BrailleSequence> counter){
        // TODO : disregarding the type of object you're dealing with, 
        // the object takes an OccurenceCounter and decide whether 
        // doubling should be applied on its content.
        // depending on size of the char chain for the type, and number of elements counted.
    }
    
    public static enum SubClassName{
        SLUR,
        CHORD,
        BRAILLENOTE
    }


}
