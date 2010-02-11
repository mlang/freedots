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

public class SignMask implements BrailleSequenceMask {
                          
    public SignMask(Sign sequence, int mask, BrailleListMask parent){
        this.setMask(mask);
        this.setSequence(sequence);
        this.setParent(parent);

    }    
   
    public SignMask(Sign sequence, int mask){
        this(sequence, mask, null)

    }    
    
    public SignMask(Sign sequence) {
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
    
    public void setSequence(Sign sequence){
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
                return data + data; //FIXME : the real simile-symbol, please.
                break;
            default :
                return data;
                break;
		}
    }
    public String maskedData(){
        return maskResult(this.mask, this.getSequence().data);
    }
     
    public final StringBuilder appendTo(StringBuilder sb) {
        return sb.append(this.maskedData());
    }
}
