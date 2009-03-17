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
package org.delysid.freedots.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.delysid.StandardMidiFileWriter;
import org.delysid.freedots.musicxml.MIDISequence;
import org.delysid.freedots.musicxml.Score;

public final class FileSaveAsMidiAction extends AbstractAction {
  private Main gui;
  public FileSaveAsMidiAction(Main gui) {
    super("Save as MIDI");
    this.gui = gui;
    putValue(SHORT_DESCRIPTION, "Export to standard MIDI file");
    putValue(MNEMONIC_KEY, KeyEvent.VK_M);
  }
  public void actionPerformed(ActionEvent event) {
    Score score = gui.getScore();
    if (score != null) {
      JFileChooser fileChooser = new JFileChooser();
      if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
	File file = fileChooser.getSelectedFile();
	try {
	  FileOutputStream fileOutputStream = new FileOutputStream(file);
	  try {
	    StandardMidiFileWriter mfw = new StandardMidiFileWriter();
	    mfw.write(new MIDISequence(score), 1, fileOutputStream);
	  } catch (Exception exception) {
	    exception.printStackTrace();
	  } finally {
	    fileOutputStream.close();
	  }
	} catch (java.io.IOException exception) {
	  exception.printStackTrace();
	}
      }
    }
  }
}
