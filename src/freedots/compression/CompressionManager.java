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

import java.util.*;
import freedots.braille.*;
import freedots.braille.BrailleChord.IntervalSign;
import freedots.braille.BrailleChord.ChordStep;
import java.util.LinkedList;

public final class CompressionManager{
  
  public void applyDoubling(BrailleList seq){

    OccurrenceCounter<SlurSign> slurOcc = new OccurrenceCounter<SlurSign>(); 
    OccurrenceCounter<IntervalSign> intervalOcc = new OccurrenceCounter<IntervalSign>(); 
    
    applyDoublingToSlurSignBis(seq, slurOcc);
    
    // FIXME : doesn't behave as expected.
    //applyDoublingToIntervalSignBis(seq, intervalOcc);
  }
  
  
  /* TODO : remove dead code if it remains unused
   *

  private void applyDoublingToSlurSign(BrailleList seq, OccurrenceCounter<SlurSign> occ){
    
    //current position in the linkedList (BrailleList seq)
    int fp=0; //first position of the slur counted 
    int bbn=0; //indicates if we've seen a BrailleNote on this level
    for (int i=0;i<seq.size();i++){
      System.out.println(" n: "+i);
      BrailleSequence se=seq.get(i);
      System.out.println(se.toString());
      
      if (se instanceof BrailleNote){
        System.out.println("On lit une BrailleNote");
        BrailleList bl= (BrailleList) se;
        if (bl.getLast() instanceof SlurSign){
          System.out.println("La brailleNote a une slur");
          if (occ.count==0){
            fp=i;
            System.out.println("Première slur  fp = "+fp);
          }
          bbn=1;
          occ.count++;
          System.out.println(" slur  n° = "+occ.count);
        }
        else{
          System.out.println("La braille Note n'a pas de slur");
          if (occ.count>3){
            System.out.println("Mais celle yen avait plus de 3 avant");
            BrailleList bs = (BrailleList)seq.get(fp);
            SlurSign s =(SlurSign) bs.getLast();
            s.mask=BrailleMask.DOUBLED;
         
            System.out.println("On double n :"+fp);
            for(int j=fp+1;j<i;j++){
              bs = (BrailleList) seq.get(j); 
              if ( bs.getLast() instanceof SlurSign){
                s=(SlurSign) bs.getLast();
                s.mask=BrailleMask.HIDDEN;
               
                System.out.println("On cache la slur de la BrailleNote n :"+j);
              }	    			
            }	
          }	
          occ.count=0;
          System.out.println("On remet à zéro car brailleNote sans slur");
        }
      }
      if (!(se instanceof BrailleNote) || (i==seq.size()-1)){
        System.out.println("On ne lit pas une BrailleNote");
        if (occ.count>3){
          System.out.println("Mais plus de 3 BrailleNote avec slur avant");
          BrailleList bs=(BrailleList)seq.get(fp);
          SlurSign s=(SlurSign) bs.getLast();
          s.mask=BrailleMask.DOUBLED;
         
          System.out.println("On double n :"+fp);
          for(int j=fp+1;j<i;j++){
            if(seq.get(j) instanceof BrailleList){
                bs = (BrailleList) seq.get(j); 
                if ( bs.getLast() instanceof SlurSign){
                    s=(SlurSign) bs.getLast();
                    s.mask=BrailleMask.HIDDEN;
             
                    System.out.println("On cache la slur de la BrailleNote n :"+j);
                }	    			
            }
          }
          occ.count=0;
          System.out.println("On remet à zéro car ya plus de BrailleNote");
        }	
        else{
          if((bbn==0) && (se instanceof BrailleList)){
            System.out.println("Pas de BrailleNote vu à ce niveau, appel sur le fils.");
            applyDoublingToSlurSign((BrailleList)se, occ);
          }
        }
      }
      if (i==seq.size()-1)
         System.out.println("Fin on remonte");
    }
  }

   *
   */
  
  private void applyDoublingToSlurSignBis(BrailleList seq, OccurrenceCounter<SlurSign> occ){
    for (int i=0;i<seq.size();i++){
      System.out.println("(Parsing element n: "+i+")");
      BrailleSequence se=seq.get(i);
      System.out.println(se.toString()); 
      if (se instanceof BrailleNote){
        System.out.println("Reading a BrailleNote...");
        BrailleList bl= (BrailleList) se;
        if (bl.getLast() instanceof SlurSign){
          System.out.println("the note is slurred.");
          if (occ.count==0){
            System.out.println("First SlurSign occured on element n°"+i);
          }
          occ.addElement((SlurSign)bl.getLast());
          System.out.println("New SlurSign occured : slur n°"+occ.count);
        }
        else{
          System.out.println("...the note is not slurred."); 
          applyDoublingMaskOnSlurCounter(occ);
        }
      }
      if (!(se instanceof BrailleNote)){
        System.out.println("Not reading a BrailleNote...");	
        if(se instanceof BrailleList){
          System.out.println("...but another list.  Recursive call on its elements.");
          applyDoublingToSlurSignBis((BrailleList)se, occ);
        }
      }
      if (i==seq.size()-1){
         System.out.println("End of sequence, parsing back to parent.");
      }
    }
  }
  
  public void applyDoublingMaskOnSlurCounter(OccurrenceCounter<SlurSign> occ){
    int length = occ.length();
    if(length == 0) return;
    if(length>3){
        System.out.println("More than 3 occurences, doubling applied on counter content.");

        occ.getElement(0).setMask(BrailleMask.DOUBLED);   
        occ.getElement(length-1).setMask(BrailleMask.NORMAL);
        for(int i=1;i < (length-1);i++){
            occ.getElement(i).setMask(BrailleMask.HIDDEN);
            
        }
    }         
    else{
        System.out.println("3 or less occurences, doubling non applied to the counter content. ");
        for(int i=0;i < length;i++){
            occ.getElement(i).setMask(BrailleMask.NORMAL);
        }
    }
    System.out.println("Counter emptied, ready for next count.");
    System.out.println("****");
    occ.empty();
  }
  
  private void applyDoublingToIntervalSignBis(BrailleList seq, OccurrenceCounter<IntervalSign> occ){
    for (int i=0;i<seq.size();i++){
      System.out.println("(Parsing element n: "+i+")");
      BrailleSequence se=seq.get(i);
      System.out.println(se.toString()); 
      if (se instanceof BrailleChord){
        System.out.println("Reading a BrailleChord...");
        BrailleList bl= (BrailleList) se;
        for(BrailleSequence subSe : bl){
            if (subSe instanceof ChordStep){
                IntervalSign itvl = (IntervalSign)(((BrailleList)subSe).getLast());
                if (((occ.length()==0) || occ.getLast().getSteps() == itvl.getSteps())){
                     System.out.println("the chord has the same interval as the chord before.");
                     occ.addElement(itvl);
                     System.out.println("New IntervalSign occured : interval n°"+occ.count);
                }
                else {
                    System.out.println("...the chord has not the same interval as the chord before."); 
                    System.out.println("Applying doubling on the counter content.");
                    applyDoublingMaskOnCounter(occ);
                }
                break;
            }
            
            
        }
      }
      if (!(se instanceof BrailleChord)){
        System.out.println("Not reading a BrailleChord...");	
        if(se instanceof BrailleList){
          System.out.println("...but another list.  Recursive call on its elements.");
          applyDoublingToIntervalSignBis((BrailleList)se, occ);
        }
        else if(se instanceof BrailleNote){
          System.out.println("...but a BrailleNote. End of potential doubling sequence. Applying doubling on the counter content.");
          applyDoublingMaskOnCounter(occ);
        }
      }
      if (i==seq.size()-1){
         System.out.println("End of sequence, parsing back to parent.");
      }
    }
  }
  
  public void applyDoublingMaskOnCounter(OccurrenceCounter<? extends Doublable> occ){
    int length = occ.length();
    if(length == 0) return;
    if(length>3){
        System.out.println("More than 3 occurences, doubling applied on counter content.");

        occ.getElement(0).setMask(BrailleMask.DOUBLED);   
        occ.getElement(length-1).setMask(BrailleMask.NORMAL);
        for(int i=1;i < (length-1);i++){
            occ.getElement(i).setMask(BrailleMask.HIDDEN);
            
        }
    }         
    else{
        System.out.println("3 or less occurences, doubling non applied to the counter content. ");
        for(int i=0;i < length;i++){
            occ.getElement(i).setMask(BrailleMask.NORMAL);
        }
    }
    System.out.println("Counter emptied, ready for next count.");
    System.out.println("****");
    occ.empty();
  }
  
  
  
  
  public void applyRepetitions(BrailleSequence seq, LinkedList<SubClassName> classList){
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
