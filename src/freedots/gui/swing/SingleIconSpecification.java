/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2009 Mario Lang  All Rights Reserved.
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
 * This file is maintained by Simon Kainz <simon@familiekainz.at>.
 */
package freedots.gui.swing;

public final class SingleIconSpecification {

  private boolean stem = false;
  private String noteHeadImage = null;
  private String flagsImage = null;
  private int stemOffsetX = 0;
  private int stemOffsetY = 0;
  private int offsetX = 0;
  private int offsetY = 0;

  public SingleIconSpecification(final String noteHeadImage,
                                 final String flagsImage,
                                 final boolean hasStem,
                                 final int offsetX, final int offsetY,
                                 final int stemOffsetX, final int stemOffsetY)
  {
    this.noteHeadImage = noteHeadImage;
    this.flagsImage = flagsImage;
    this.stem = hasStem;
    
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.stemOffsetX = stemOffsetX;
    this.stemOffsetY = stemOffsetY;
  }
  
  public SingleIconSpecification(final String noteHeadImage,
                                 final String flagsImage,
                                 final boolean hasStem)
  {
    this.noteHeadImage=noteHeadImage;
    this.flagsImage=flagsImage;
    this.stem=hasStem;
    
    
  }


  public String getFlagsImage() {
    return flagsImage;
  }


  public void setFlagsImage(String flagsImage) {
    this.flagsImage = flagsImage;
  }


  public String getNoteHeadImage() {
    return noteHeadImage;
  }


  public void setNoteHeadImage(String noteHeadImage) {
    this.noteHeadImage = noteHeadImage;
  }


  public boolean isStem() {
    return stem;
  }


  public void setStem(boolean stem) {
    this.stem = stem;
  }


  public int getStemOffsetX() {
    return stemOffsetX;
  }


  public void setStemOffsetX(int stemOffsetX) {
    this.stemOffsetX = stemOffsetX;
  }


  public int getStemOffsetY() {
    return stemOffsetY;
  }


  public void setStemOffsetY(int stemOffsetY) {
    this.stemOffsetY = stemOffsetY;
  }

  public int getOffsetX() {
    return offsetX;
  }

  public void setOffsetX(int offsetX) {
    this.offsetX = offsetX;
  }

  public int getOffsetY() {
    return offsetY;
  }

  public void setOffsetY(int offsetY) {
    this.offsetY = offsetY;
  }
  
  
}
