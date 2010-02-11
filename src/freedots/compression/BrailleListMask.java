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
import freedots.braille.BrailleSequence;

public class BrailleListMask extends java.util.LinkedList<BrailleSequenceMask>
                             implements BrailleSequenceMask {
    public BrailleListMask(BrailleList sequence, int mask, BrailleList parent){
        super();
        this.setMask(mask);
        this.setSequence(sequence);
        this.setParent(parent);
        
        for (BrailleSequence subSeq : sequence){
            if(BrailleList.isInstance(subSeq)){
                this.add(new BrailleSequenceMask(subSeq, mask, this);
            }
            else if(Sign.isInstance(subSeq)){
                this.add(new SignMask(subSeq, mask, this);
            }
            else{
             // FIXME : shouldn't be so subclass-dependant.
            }
        }
    }
    
    public BrailleListMask(BrailleList sequence, int mask){
        this(sequence, mask, null);
        
    }    
   
    public BrailleListMask(BrailleSequence sequence) {
        this(sequence, 1); //FIXME : we could use something more flexible instead of simple integers. 
    }                   
    
    public int getMask(){
        return this.mask;
    }
    
    public void setMask(int mask){
        this.mask = mask;
    }
    
    public BrailleSequence getSequence(){
        return this.sequence;
    }
    
    public void setSequence(BrailleSequence sequence){
        this.sequence = sequence;
    }
    
    public BrailleListMask getParent(){
        return this.parent;
    }
    
    public void setParent(BrailleListMask parent){
        this.parent = parent;
    }
    
    public String maskResult(int mask, String data){
        switch (mask){
            case 0 :
                return new String("");
                break;
            case 1 :
                return data;
                break;
            case 2 :
                return new String("[simile-symbol]"); //FIXME : the real simile-symbol, please.
                break;
            default :
                return data;
                break;
		}
	}
	
    public String maskedData(){
        StringBuilder sb = new StringBuilder("");
        for (BrailleSequenceMask subSeqMask : this){
           sb = subSeqMask.appendTo(sb);
        }
        return maskResult(this.mask, this.);
    }
     
    public final StringBuilder appendTo(StringBuilder sb) {
        return sb.append(this.maskedData());
    }
}
