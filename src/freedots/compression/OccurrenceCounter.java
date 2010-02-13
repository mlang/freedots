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

import freedots.braille.BrailleSequence;
import java.util.LinkedList;


public class OccurrenceCounter<T extends BrailleSequence> {
    public int count;
    public LinkedList<T> list;

    public OccurrenceCounter(){
	this.list = new LinkedList<T>();
	this.count = 0;
    }

    public OccurrenceCounter(T element){
	this.list = new LinkedList<T>();
	this.list.add(element);
	this.count = 1;
    }  
      
    public void addElement(T element){
	this.list.add(element);
	this.count++;
    } 
    
    public void empty(){
	this.list = new LinkedList<T>();
	this.count = 0;
    }
} 
