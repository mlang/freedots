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
package freedots.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JOptionPane;

import freedots.Braille;
import freedots.musicxml.MIDISequence;
import freedots.musicxml.Score;
import freedots.playback.StandardMidiFileWriter;
import freedots.transcription.Transcriber;

/**
 * Action for saving the currently open MusicXML document.
 * The dialog allows for export to MIDI and Unicode as well as NABCC braille.
 */
@SuppressWarnings("serial")
public final class FileSaveAsAction extends AbstractAction {
  private Main gui;
  public FileSaveAsAction(final Main gui) {
    super("Save as...");
    this.gui = gui;
    putValue(SHORT_DESCRIPTION, "Export to braille or standard MIDI file");
    putValue(MNEMONIC_KEY, KeyEvent.VK_A);
  }
  public void actionPerformed(ActionEvent event) {
    Score score = gui.getScore();
    if (score != null) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setFileFilter(new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isDirectory() || f.getName().matches(".*\\.brl");
        }
        @Override
        public String getDescription() {
          return "Unicode braille (*.brl)";
        }
      });
      fileChooser.setFileFilter(new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isDirectory() || f.getName().matches(".*\\.brf");
        }
        @Override
        public String getDescription() {
          return "Legacy BRF (*.brf)";
        }
      });
      fileChooser.setFileFilter(new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isDirectory() || f.getName().matches(".*\\.mid");
        }
        @Override
        public String getDescription() {
          return "Standard MIDI file (*.mid)";
        }
      });
      fileChooser.setFileFilter(new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isDirectory() || f.getName().matches(".*\\.xml");
        }
        @Override
        public String getDescription() {
          return "MusicXML file (*.xml)";
        }
      });
      if (fileChooser.showSaveDialog(gui) == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        String ext = getExtension(file);
        if (ext != null && ext.equals("mid")) {
          exportToMidi(score, file);
        } else if (ext != null && ext.equals("brl")) {
          exportToUnicodeBraille(gui.getTranscriber(), file);
        } else if (ext != null && ext.equals("brf")) {
          exportToBRF(gui.getTranscriber(), file);
        } else if (ext != null && ext.equals("xml")) {
          exportToMusicXML(score, file);
        } else if (ext != null) {
          String message = "Unknown file extension '"+ext+"'";
          JOptionPane.showMessageDialog(gui, message, "Alert",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private static void exportToMusicXML(Score score, File file) {
    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream(file);
      score.save(fileOutputStream);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }
  private static void exportToMidi(Score score, File file) {
    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream(file);
      try {
        StandardMidiFileWriter mfw = new StandardMidiFileWriter();
        mfw.write(new MIDISequence(score), 1, fileOutputStream);
      } catch (Exception exception) {
        exception.printStackTrace();
      } finally {
        fileOutputStream.close();
      }
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  private static void
  exportToUnicodeBraille(Transcriber transcriber, File file) {
    Writer fileWriter = null;
    try {
      try {
        fileWriter = new FileWriter(file);
        fileWriter.write(transcriber.toString());
      } finally {
        fileWriter.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void
  exportToBRF(Transcriber transcriber, File file) {
    Writer fileWriter = null;
    try {
      try {
        fileWriter = new FileWriter(file);
        CharacterIterator
        iterator = new StringCharacterIterator(transcriber.toString());
        for(char c = iterator.first(); c != CharacterIterator.DONE;
            c = iterator.next()) {
          if (Braille.brfTable.containsKey(new Character(c))) {
            final Character mapped = Braille.brfTable.get(new Character(c));
            fileWriter = fileWriter.append(mapped);
          } else {
            fileWriter = fileWriter.append(c);
          }
        }
      } finally {
        fileWriter.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String getExtension(File file) {
    String ext = null;
    String fileName = file.getName();
    int index = fileName.lastIndexOf('.');

    if (index > 0 && index < fileName.length() - 1)
      ext = fileName.substring(index + 1).toLowerCase();

    return ext;
  }
}
