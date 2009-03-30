package org.delysid.freedots.gui.swing;

public class SingleIconSpecification {

  private boolean stem=false;
  private String noteHeadImage=null;
  private String flagsImage=null;
  private int stemOffsetX=0;
  private int stemOffsetY=0;
  
  
  public SingleIconSpecification(String noteHeadImage, String flagsImage, boolean hasStem)
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
  
  
}
