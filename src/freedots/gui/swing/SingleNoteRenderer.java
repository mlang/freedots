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
 * This file is maintained by Simon Kainz <simon@familiekainz.at>.
 */
package freedots.gui.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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

import freedots.musicxml.Note;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Draw a single note using staff notation.
 */
final class SingleNoteRenderer extends JPanel {
  private static final long serialVersionUID = 8634349010555582625L;

  private Note currentNote = null;
  
  private int lineSpacing = 8;
  private int lineLength = 20;
  
  private int globalNotePos = 0;
  
  private static final char TRANSPARENT = '-';
  private static final char[] CHARTABLE = new char[] {
    '#', // 0 Black
    '$', // 1
    '*', // 2
    '0', // 3
    'o', // 4
    '+', // 5
    '.', // 6
    TRANSPARENT // 7
  };
  
  private Map<String, BufferedImage> icons =
    new HashMap<String, BufferedImage>();
  private Map<String, SingleIconSpecification> noteDefs =
    new HashMap<String, SingleIconSpecification>();
  
  private void readSingleIcon(String filename, String key, Map iconMap) {
    Document D = null;
    BufferedImage bImage = null;
    try {
      InputStream inputStream = getClass().getResourceAsStream(filename);

      if (inputStream == null) {
        System.err.println("File "+filename+" not found");
        return;
      }
      D = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
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
    
    XPathFactory xPathFactory = XPathFactory.newInstance();
    
    XPath xPath = xPathFactory.newXPath();
    NodeList nodeList = null;

    String xPathExpression = "//icon/bitmap/row";
    try {
      nodeList = (NodeList) xPath.evaluate(xPathExpression,
                                           D, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    int count = nodeList.getLength();

    bImage = new BufferedImage(nodeList.item(0).getTextContent().length(),
                               count, BufferedImage.TYPE_INT_ARGB);

    for (int i = 0; i<count; i++) {
      Node n = nodeList.item(i);
      String line = n.getTextContent();

      for (int x = 0; x<line.length(); x++) {
        if (line.charAt(x) != TRANSPARENT) {
          int col = toLevel(line.charAt(x));

          int ncol = (0xFF000000 | (col<<16) | (col<<8)) | col;

          bImage.setRGB(x, i, ncol);
        }
      }
    }

    iconMap.put(key, bImage);
  }
  
  private void readNoteIcons() 
  {
    
    
    readSingleIcon("G_CLEF.xml", "G_CLEF", icons);
    readSingleIcon("F_CLEF.xml", "F_CLEF", icons);

    readSingleIcon("KEY_FLAT_1.xml", "KEY_FLAT_1", icons);
    readSingleIcon("KEY_FLAT_2.xml", "KEY_FLAT_2", icons);
    readSingleIcon("KEY_FLAT_3.xml", "KEY_FLAT_3", icons);
    readSingleIcon("KEY_FLAT_4.xml", "KEY_FLAT_4", icons);
    readSingleIcon("KEY_FLAT_5.xml", "KEY_FLAT_5", icons);
    readSingleIcon("KEY_FLAT_6.xml", "KEY_FLAT_6", icons);
    readSingleIcon("KEY_FLAT_7.xml", "KEY_FLAT_7", icons);

    readSingleIcon("KEY_SHARP_1.xml", "KEY_SHARP_1", icons);
    readSingleIcon("KEY_SHARP_2.xml", "KEY_SHARP_2", icons);
    readSingleIcon("KEY_SHARP_3.xml", "KEY_SHARP_3", icons);
    readSingleIcon("KEY_SHARP_4.xml", "KEY_SHARP_4", icons);
    readSingleIcon("KEY_SHARP_5.xml", "KEY_SHARP_5", icons);
    readSingleIcon("KEY_SHARP_6.xml", "KEY_SHARP_6", icons);
    readSingleIcon("KEY_SHARP_7.xml", "KEY_SHARP_7", icons);

    readSingleIcon("WHOLE_NOTE.xml", "WHOLE_NOTE", icons);
    readSingleIcon("NOTEHEAD_BLACK.xml", "NOTEHEAD_BLACK", icons);
    
    readSingleIcon("COMBINING_FLAG_1.xml", "COMBINING_FLAG_1", icons);
    readSingleIcon("COMBINING_FLAG_2.xml", "COMBINING_FLAG_2", icons);
    readSingleIcon("COMBINING_FLAG_3.xml", "COMBINING_FLAG_3", icons);
    readSingleIcon("COMBINING_FLAG_4.xml", "COMBINING_FLAG_4", icons);
    readSingleIcon("COMBINING_FLAG_5.xml", "COMBINING_FLAG_5", icons);
    
      
   
  }
  
  public SingleNoteRenderer()
  {
    readNoteIcons();
        
    noteDefs.put("1/1",
                 new SingleIconSpecification("WHOLE_NOTE", null, false));
    noteDefs.put("1/2",
                 new SingleIconSpecification("WHOLE_NOTE", null, true));
    noteDefs.put("1/4",
                 new SingleIconSpecification("NOTEHEAD_BLACK", null, true));
    noteDefs.put("1/8",
                 new SingleIconSpecification("NOTEHEAD_BLACK",
                                             "COMBINING_FLAG_1", true));
    noteDefs.put("1/16",
                 new SingleIconSpecification("NOTEHEAD_BLACK",
                                             "COMBINING_FLAG_2", true));
    noteDefs.put("1/32",
                 new SingleIconSpecification("NOTEHEAD_BLACK",
                                             "COMBINING_FLAG_3", true));
    noteDefs.put("1/64",
                 new SingleIconSpecification("NOTEHEAD_BLACK",
                                             "COMBINING_FLAG_4", true));
    noteDefs.put("1/128",
                 new SingleIconSpecification("NOTEHEAD_BLACK",
                                             "COMBINING_FLAG_5", true));

    noteDefs.put("G_CLEF",
                 new SingleIconSpecification(null, null, false, 3, 2, 0, 0));
    noteDefs.put("F_CLEF",
                 new SingleIconSpecification(null, null, false, 3, 14, 0, 0));
  }

  public Dimension getPreferredSize() {
    return new Dimension(60, 62);
  }

  public void setNote(Note note) {
    this.currentNote = note;
    //this.updateUI();
    this.repaint();
  }

  private int toLevel(char c) {
    // Check the char^
    if (c == TRANSPARENT) {
      return 255;
    } else {
      for (int i = CHARTABLE.length - 1; i >= 0; i--) {
        if (CHARTABLE[i] == c) {
          int level = 3 + (i * 36); // Range 3 .. 255 (not too bad)

          return level;
        }
      }
    }

    // Unknown -> white^
    return 255;
  }
  
  public void drawClef(Graphics g) {
    BufferedImage currentClef = null;

    int clefYOffset = 0;
    int clefXOffset = 0;
    switch (currentNote.getClef().sign) {
    case G:
      currentClef = icons.get("G_CLEF");

      if (noteDefs.get("G_CLEF") != null) {
        clefXOffset = noteDefs.get("G_CLEF").getOffsetX();
        clefYOffset = noteDefs.get("G_CLEF").getOffsetY();
      }
      break;
      
    case F:
      currentClef = icons.get("F_CLEF");
      
      if (noteDefs.get("F_CLEF") != null) {
        clefXOffset = noteDefs.get("F_CLEF").getOffsetX();
        clefYOffset = noteDefs.get("F_CLEF").getOffsetY();
      }
      break;
    default: throw new AssertionError(currentNote.getClef().sign);
    }

    //BufferedImage currentClef=icons.get("G_CLEF");

    Graphics2D g2 = (Graphics2D)g;
    g2.drawImage(currentClef, null, clefXOffset, clefYOffset);

    globalNotePos = currentClef.getWidth()+4;
  }
  
  
  protected void drawLines(Graphics g) {
    for (int i = 0; i<5; i++) {
      g.drawLine(1, 14+i*lineSpacing, 40+lineLength, 14+i*lineSpacing);
    }
  }
  
  
  protected void drawKey(Graphics g) {
    int keyType = currentNote.getActiveKeySignature().getType();
    String iconNameBase = null;
    
    if (keyType==0) {
      // at least move the followign note by some pixels right, to make it look sexier.
      globalNotePos+=4;
      return;
    }

    if (keyType>0) {
      iconNameBase = "KEY_SHARP_"+keyType;
    }

    if (keyType<0) {
      iconNameBase = "KEY_FLAT_"+keyType;
    }
    
    Graphics2D g2 = (Graphics2D)g;
    g2.drawImage(icons.get(iconNameBase), null, 25+4, 0);
    globalNotePos += icons.get(iconNameBase).getWidth() + 8;
  }
  
  
  
  protected int getNotePosition() {
    int retval = 0;
    switch (currentNote.getClef().sign)
    {
    case G:
      retval = 14+5*lineSpacing-(currentNote.getPitch().getStep()*(lineSpacing/2))-4;
      
      break;
      
    case F:
      retval = 14+5*lineSpacing-(currentNote.getPitch().getStep()*(lineSpacing/2))-4-lineSpacing*2-lineSpacing/2;
      break;
      
    }
   return retval; 
  }
  
  protected void drawNote(Graphics g)
  {
    if (currentNote.getPitch() == null) return;
    Graphics2D g2 = (Graphics2D)g;
    BufferedImage noteImage = null;
    BufferedImage noteHead = null;

    int notePosX;
    int notePosY;

    int stemDirection = 0;
    boolean hasStem = false;

    notePosX = globalNotePos;
    notePosY = getNotePosition(); //14+5*lineSpacing-(currentNote.getPitch().getStep()*(lineSpacing/2))-4;
    
   // System.out.println("Modifier type:"+currentNote.getActiveKeySignature().getType());
   // System.out.println("Modifier count:"+currentNote.getActiveKeySignature().getModifierCount());
    
    SingleIconSpecification iconSpec =
      noteDefs.get(currentNote.getAugmentedFraction().numerator()
                   +"/"+currentNote.getAugmentedFraction().denominator());
    
    if (iconSpec==null) return;
    
        // First draw note head
    if (icons.get(iconSpec.getNoteHeadImage())==null) return;
    
    g2.drawImage(icons.get(iconSpec.getNoteHeadImage()), null,
                 notePosX, notePosY);
    // Eventually draw the stem
    if (iconSpec.isStem()) {
      notePosX += icons.get(iconSpec.getNoteHeadImage()).getWidth();
      g2.drawLine(notePosX, notePosY+3, notePosX, notePosY-27);
      notePosY-=27;
    }

    // draw the flags, if any
    g2.drawImage(icons.get(iconSpec.getFlagsImage()), null,
                 notePosX, notePosY);
    
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    if (currentNote!=null)
    {
      drawLines(g);
      drawClef(g);
     
      drawKey(g);
      drawNote(g);
      
     // g.drawString(""+(currentNote.getPitch().getStep()),50,35);
      
    }
   
   
  }
}
