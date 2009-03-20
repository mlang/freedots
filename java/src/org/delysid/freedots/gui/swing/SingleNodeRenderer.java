package org.delysid.freedots.gui.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.delysid.freedots.musicxml.Note;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SingleNodeRenderer extends JPanel {

  /**
   * 
   */
  private static final long serialVersionUID = 8634349010555582625L;

  private Note currentNote=null;
  
  private int lineSpacing=8;
  private int lineLength=20;
  
  
  
  private static final char TRANSPARENT = '-';
  private static final char[]  charTable = new char[] {
  '#', // 0 Black^
  '$', // 1^
  '*', // 2^
  '0', // 3^
  'o', // 4^
  '+', // 5^
  '.', // 6^
  TRANSPARENT // 7^
  };
  
  private Map<String,BufferedImage> icons=new HashMap();
  private Map<String,SingleIconSpecification> noteDefs=new HashMap();
  
  
  private void readSingleIcon(String filename,String key, Map iconMap)
  {
    Document D=null;
    BufferedImage bImage=null;
    try {
      
      InputStream inputStream = getClass().getResourceAsStream(filename);
      
      if (inputStream==null) 
        {
        System.err.println("File "+filename+" not found");
        return;
        }
      D = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);  //"/home/skainz/workspace/trunk/G_CLEF.xml"));
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }  
    
    XPathFactory xPathFactory= XPathFactory.newInstance();
    
    XPath xPath = xPathFactory.newXPath();
    NodeList nodeList=null;
    
      String xPathExpression = "//icon/bitmap/row";
     
      try {
        nodeList = (NodeList) xPath.evaluate(xPathExpression,
                  D,
                  XPathConstants.NODESET);
      } catch (XPathExpressionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      int count = nodeList.getLength();
      
      
      bImage=new BufferedImage(nodeList.item(0).getTextContent().length(),count,BufferedImage.TYPE_INT_ARGB);
      
      
      
      
      
      for (int i=0;i<count;i++)
      {
        Node n=nodeList.item(i);
        String line=n.getTextContent();
        
          for   (int x=0;x<line.length();x++)
          {
            if (line.charAt(x)!=TRANSPARENT)
              {
              int col=toLevel(line.charAt(x));
              
              int ncol=((0xFF000000|(col<<16)|(col<<8))|col);
             
              
            bImage.setRGB(x, i, ncol);
          }
            
          }
          

      }
      
    
      iconMap.put(key, bImage);

  }
  
  private void readNoteIcons() 
  {
    
    
    readSingleIcon("G_CLEF.xml", "G_CLEF", icons);
    readSingleIcon("WHOLE_NOTE.xml", "WHOLE_NOTE", icons);
    readSingleIcon("NOTEHEAD_BLACK.xml", "NOTEHEAD_BLACK", icons);
    
    readSingleIcon("COMBINING_FLAG_1.xml", "COMBINING_FLAG_1", icons);
    readSingleIcon("COMBINING_FLAG_2.xml", "COMBINING_FLAG_2", icons);
    readSingleIcon("COMBINING_FLAG_3.xml", "COMBINING_FLAG_3", icons);
    readSingleIcon("COMBINING_FLAG_4.xml", "COMBINING_FLAG_4", icons);
    readSingleIcon("COMBINING_FLAG_5.xml", "COMBINING_FLAG_5", icons);
    
      
   
  }
  
  public SingleNodeRenderer()
  {
  
    readNoteIcons();
        
    noteDefs.put("1/1", new SingleIconSpecification("WHOLE_NOTE",null,false));
    noteDefs.put("1/2", new SingleIconSpecification("WHOLE_NOTE",null,true));
    noteDefs.put("1/4", new SingleIconSpecification("NOTEHEAD_BLACK",null,true));
    noteDefs.put("1/8", new SingleIconSpecification("NOTEHEAD_BLACK","COMBINING_FLAG_1",true));
    noteDefs.put("1/16", new SingleIconSpecification("NOTEHEAD_BLACK","COMBINING_FLAG_2",true));
    noteDefs.put("1/32", new SingleIconSpecification("NOTEHEAD_BLACK","COMBINING_FLAG_3",true));
    noteDefs.put("1/64", new SingleIconSpecification("NOTEHEAD_BLACK","COMBINING_FLAG_4",true));
    noteDefs.put("1/128", new SingleIconSpecification("NOTEHEAD_BLACK","COMBINING_FLAG_5",true));
  }
  
  public Dimension getPreferredSize()
  {
    return new Dimension(60,62);
  }
  
  public void setNote(Note note)
  {
    this.currentNote=note;
    this.updateUI();
  }
  
  
  private int toLevel (char c)
  {
      // Check the char^
      if (c == TRANSPARENT) {
          return 255;
      } else {
          for (int i = charTable.length - 1; i >= 0; i--) {
              if (charTable[i] == c) {
                  int level = 3 + (i * 36); // Range 3 .. 255 (not too bad)

                  return level;
              }
          }
      }

      // Unknown -> white^
      

      return 255;
  }
  
  public void drawClef(Graphics g)
  {
   
   Graphics2D g2=(Graphics2D)g;
   g2.drawImage(icons.get("G_CLEF"),null,3,0);
      
   
   
    
  }
  
  
  protected void drawLines(Graphics g)
  {
    for (int i=0;i<5;i++)
    {
      g.drawLine(1, 12+i*lineSpacing, 40+lineLength, 12+i*lineSpacing);
    }
  }
  
  protected void drawNote(Graphics g)
  {
    
    if (currentNote.getPitch()==null) return;
    Graphics2D g2=(Graphics2D)g;
    BufferedImage noteImage=null;
    BufferedImage noteHead=null;
    
    int num=currentNote.getAugmentedFraction().getNumerator();
    int den=currentNote.getAugmentedFraction().getDenominator();
    int notePosX;
    int notePosY;
    
    int stemDirection=0;
    boolean hasStem=false;
    
    
    notePosX=35;
    notePosY=12+5*lineSpacing-(currentNote.getPitch().getStep()*(lineSpacing/2))-4;
    
    
    
    SingleIconSpecification iconSpec=noteDefs.get(currentNote.getAugmentedFraction().getNumerator()+"/"+currentNote.getAugmentedFraction().getDenominator());
    
   // System.out.println(currentNote.getAugmentedFraction().getNumerator()+"/"+currentNote.getAugmentedFraction().getDenominator());
        

     // First draw note head
    
   
     g2.drawImage(icons.get(iconSpec.getNoteHeadImage()),null,notePosX,notePosY);
      
      
     // Eventually draw the stem
     if (iconSpec.isStem())
      {
        notePosX+=icons.get(iconSpec.getNoteHeadImage()).getWidth();
        g2.drawLine(notePosX, notePosY+3, notePosX, notePosY-27);
        notePosY-=27;
          
      }
     
      // draw the flags, if any
      g2.drawImage(icons.get(iconSpec.getFlagsImage()),null,notePosX,notePosY);
     
    
    
    
   
  }
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    if (currentNote!=null)
    {
      
      drawNote(g);
      drawClef(g);
      drawLines(g);
     // g.drawString(""+(currentNote.getPitch().getStep()),50,35);
      
    }
   
   
  }
}
