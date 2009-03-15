/* -*- c-basic-offset: 2; -*- */
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
 * This software is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.delysid.StandardMidiFileWriter;
import org.delysid.freedots.gui.GraphicalUserInterface;
import org.delysid.freedots.musicxml.MIDISequence;
import org.delysid.freedots.musicxml.Score;
import org.delysid.freedots.playback.MIDIPlayer;

import org.delysid.freedots.transcription.Transcriber;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class Main {
  public static void main(String[] args) {
    Options options = new Options(args);
    Transcriber transcriber = new Transcriber(options);
    Score score = null;
    if (options.getLocation() != null) {
      try {
        score = new Score(options.getLocation());
      } catch (XPathExpressionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ParserConfigurationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (SAXParseException e) {
        System.exit(1);
      } catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        System.exit(1);
      }
      if (score != null) transcriber.setScore(score);
    }
    if (options.getWindowSystem()) {
      try {
        GraphicalUserInterface gui = null;
        Class guiClass = Class.forName(options.getUI().getClassName());
        if (GraphicalUserInterface.class.isAssignableFrom(guiClass)) {
          Constructor constructor = guiClass.getConstructor(new Class []{Transcriber.class});
          try {
            gui = (GraphicalUserInterface)constructor.newInstance(new Object[]{transcriber});
          } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause();
          }
          gui.run();
        }
      } catch (HeadlessException e) {
        options.setWindowSystem(false);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
    if (!options.getWindowSystem()) {
      if (transcriber != null) {
        if (options.getExportMidiFile() != null) {
          File file = new File(options.getExportMidiFile());
          try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
              StandardMidiFileWriter smfw = new StandardMidiFileWriter();
              smfw.write(new MIDISequence(score), 1, fileOutputStream);
            } catch (Exception exception) {
              exception.printStackTrace();
            } finally {
              fileOutputStream.close();
            }
          } catch (IOException exception) {
            exception.printStackTrace();
          }
        } else {
          System.out.println(transcriber.toString());
          if (options.getPlayScore() && score != null) {
            try {
              MIDIPlayer player = new MIDIPlayer();
              player.setSequence(new MIDISequence(score));
              player.start();
              try {
                while (player.isRunning()) Thread.sleep(250);
              } catch (InterruptedException ie) {}
              player.close();
            } catch (MidiUnavailableException mue) {
              System.err.println("MIDI playback not available.");
            } catch (InvalidMidiDataException imde) {
              imde.printStackTrace();
            }
          }
        }
      } else {
        System.err.println("No window system available and no filename specified.");
        printUsage();
        System.exit(1);
      }
    }
  }
  static void printUsage() {
    System.out.println("Usage: java -jar freedots.jar " +
                       "[-w PAGEWIDTH] [-nw] [-p] [FILENAME|URL]");
    System.out.println("Options:");
    System.out.println("\t-nw:\t\tNo Window System");
    System.out.println("\t-p:\t\tPlay complete score");
  }
}
