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

    public static enum SubClassName{
        SLUR,
        CHORD,
        BRAILLENOTE
    }


}
