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
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package freedots;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ResourceBundle;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import freedots.gui.GraphicalUserInterface;
import freedots.logging.Logger;
import freedots.musicxml.MIDISequence;
import freedots.musicxml.Score;
import freedots.playback.MIDIPlayer;
import freedots.transcription.Transcriber;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This is the main program entry point.
 */
public final class Main {
  private static final Logger LOG = Logger.getLogger(Main.class);

  private static GraphicalUserInterface gui = null;

  private Main() { super(); }

  /**
   * Entry point for JAR execution.
   * @param args Arguments from the command-line
   */
  public static void main(final String[] args) {
    Options options = new Options(args);
    Transcriber transcriber = new Transcriber(options);
    if (options.getLocation() != null) {
      Score score = null;
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
    maybeStartGUI(options, transcriber);
    if (!options.getWindowSystem()) {
      if (transcriber.getScore() != null) {
        System.out.println(transcriber.toString());

        if (options.getExportMidiFile() != null) {
          File file = new File(options.getExportMidiFile());
          try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
              MidiSystem.write(new MIDISequence(transcriber.getScore()), 1,
                               fileOutputStream);
            } catch (javax.sound.midi.InvalidMidiDataException exception) {
              exception.printStackTrace();
            } finally {
              fileOutputStream.close();
            }
          } catch (IOException exception) {
            exception.printStackTrace();
          }
        }
        if (options.getPlayScore() && transcriber.getScore() != null) {
          try {
            MIDIPlayer player = new MIDIPlayer();
            if (options.getSoundfont() != null) {
              if (!player.loadSoundbank(new File(options.getSoundfont()))) {
                LOG.warning("Soundfont '"+options.getSoundfont()+"'"
                            + " could not be loaded");
              }
            }
            player.setSequence(new MIDISequence(transcriber.getScore()));
            player.start();
            try {
              while (player.isRunning()) Thread.sleep(MIDIPlayer.SLEEP_TIME);
            } catch (InterruptedException ie) { }
            player.close();
          } catch (MidiUnavailableException mue) {
            System.err.println("MIDI playback not available.");
          } catch (InvalidMidiDataException imde) {
            imde.printStackTrace();
          }
        }
      } else {
        System.err.println("No window system available and "
                           + "no filename specified.");
        printUsage();
        System.exit(0);
      }
    }
  }

  // Constants from build.xml
  public static final String VERSION;
  static {
    ResourceBundle compilationProperties =
      ResourceBundle.getBundle("compilation");
    VERSION = compilationProperties.getString("freedots.compile.version");
  }

  private static void maybeStartGUI(Options options, Transcriber transcriber) {
    if (options.getWindowSystem()) {
      try {
        Class<?> guiClass = Class.forName(options.getUI().getClassName());
        if (GraphicalUserInterface.class.isAssignableFrom(guiClass)) {
          Constructor constructor =
            guiClass.getConstructor(new Class[] {Transcriber.class});
          try {
            gui = (GraphicalUserInterface)
              constructor.newInstance(new Object[]{transcriber});
          } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause();
          }
          gui.run();
        }
      } catch (HeadlessException e) {
        System.err.println("Graphical display not available");
        options.setWindowSystem(false);
      } catch (ClassNotFoundException exception) {
        System.err.println("Requested GUI class "
                           + options.getUI().getClassName()
                           + "was not found in the classpath");
        options.setWindowSystem(false);
      } catch (InstantiationException exception) {
        System.err.println("Unable to instantiate GUI");
        options.setWindowSystem(false);
      } catch (IllegalAccessException exception) {
        exception.printStackTrace();
        options.setWindowSystem(false);
      } catch (NoSuchMethodException exception) {
        System.err.println("No constructor for requested GUI found");
        options.setWindowSystem(false);
      } catch (Throwable throwable) {
        throwable.printStackTrace();
        options.setWindowSystem(false);
      }
    }
  }

  public static GraphicalUserInterface getGui() { return gui; }

  private static void printUsage() {
    System.out.println("FreeDots " + VERSION);
    System.out.println("Usage: java -jar freedots.jar "
                       + "[OPTIONS...] [FILENAME|URL]");
    System.out.println();
    System.out.println("Options:");
    System.out.println("\t-nw\t\tNo Window System");
    System.out.println("\t-emf file.mid\tExport score to MIDI file");
    System.out.println("\t-p\t\tPlay complete score");
    System.out.println("\t-sf soundfont\tUse the given Soundfont for synthesis");
    System.out.println();
    System.out.println("\t-w WIDTH\tSet braille page width");
    System.out.println("\t-mps NUM\tSpecify number of measures per section");
    System.out.println("\t-bob\t\tBar-over-bar method");
  }
}
