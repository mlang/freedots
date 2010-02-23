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
package freedots.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import freedots.musicxml.Score;

/**
 * An action for selecting and loading MusicXML documents.
 */
@SuppressWarnings("serial")
public final class FileOpenAction extends javax.swing.AbstractAction {
  private Main gui;
  /**
   * Construct a File->Open action.
   *
   * @param gui is the main application
   */
  public FileOpenAction(final Main gui) {
    super("Open");
    this.gui = gui;
    putValue(SHORT_DESCRIPTION, "Open an existing MusicXML file");
    putValue(MNEMONIC_KEY, KeyEvent.VK_O);
    putValue(ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
  }
  /** Launches a file open dialog and loads the selected file.
   * @see java.awt.event.ActionListener#actionPerformed
   */
  public void actionPerformed(final ActionEvent event) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(final File file) {
        return file.isDirectory() || file.getName().matches(".*\\.(mxl|xml)");
      }
      @Override
      public String getDescription() {
        return "*.mxl, *.xml";
      }
    });
    fileChooser.showOpenDialog(gui);
    try {
      gui.setStatusMessage("Loading "
                           + fileChooser.getSelectedFile().toString() + "...");
      Score newScore = new Score(fileChooser.getSelectedFile().toString());
      gui.setStatusMessage("Transcribing to braille...");
      gui.setScore(newScore);
      gui.setStatusMessage("Ready");
    } 
    catch (javax.xml.parsers.ParserConfigurationException exception) {
      exception.printStackTrace();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
