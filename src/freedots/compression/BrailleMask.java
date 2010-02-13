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

/**
 * An enum class for the mask type, 
 * 
 */
public enum BrailleMask {
    // don't display the BrailleSequence
	HIDDEN(0), 
	
	// display the BrailleSequence normally
	NORMAL(1), 
    
    // display the BrailleSequence twice 
    //(used for doubling on Sign objects)
	DOUBLED(2), 
	
	// display a simile-symbol instead of the BrailleSequence 
	//(used for repetitions on BrailleList objects)
	REPEATED(3),
	
	// possible use for special rules where the repetitions 
	// are replaced by a count of the occurence of the repeated object.
	// (whole-rest measures, etc ? )
    COUNTED(4);
    
    public int intValue;
    BrailleMask(int i){
        this.intValue = i;
    }
    
    
}
