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

import java.util.LinkedList;

public final class CompressionManager{
  
  public void ApplyDoubling(BrailleList seq){
    /*    
LinkedList<OccurrenceCounter<?>> counterList = new LinkedList<OccurrenceCounter<?>>();
    for(SubClassName scn : classList){
      //FIXME : shall conserve the link between position in the list counterList and the class represented.
      //the order is the same in classList and counterList or use indexOf(Object o)
      counterList.add(generateCounterFromClass(scn));
    }
    /** TODO : must apply doubling to every class present in the classList.
     */
    OccurrenceCounter<SlurSign> occ = new OccurrenceCounter<SlurSign>(); //FIXME : difficult using of counterList
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
            bs = (BrailleList) seq.get(j); 
            if ( bs.getLast() instanceof SlurSign){
              s=(SlurSign) bs.getLast();
              s.mask=BrailleMask.HIDDEN;
             
              System.out.println("On cache la slur de la BrailleNote n :"+j);
            }	    			
            }
          occ.count=0;
          System.out.println("On remet à zéro car ya plus de BrailleNote");
        }	
        else{
          if((bbn==0) && (se instanceof BrailleList)){
            System.out.println("Pas de BrailleNote vu à ce niveau, appel sur le fils.");
            ApplyDoubling((BrailleList)se);
          }
        }
      }
      if (i==seq.size()-1)
         System.out.println("Fin on remonte");
    }
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