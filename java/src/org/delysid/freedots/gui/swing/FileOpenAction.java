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

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.delysid.freedots.musicxml.Score;

public final class FileOpenAction extends AbstractAction {
  private Main gui;
  public FileOpenAction(Main gui) {
    super("Open");
    this.gui = gui;
    putValue(SHORT_DESCRIPTION, "Open an existing MusicXML file");
    putValue(MNEMONIC_KEY, KeyEvent.VK_O);
    putValue(ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
  }
  public void actionPerformed(ActionEvent event) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
	return f.isDirectory() || f.getName().matches(".*\\.(mxl|xml)");
      }
      @Override
      public String getDescription() {
	return "*.mxl, *.xml";
      }
    });
    fileChooser.showOpenDialog(gui);
    try {
      // Update status bar display
      gui.statusBar.setText("Loading "+fileChooser.getSelectedFile().toString()+"...");
      gui.update(gui.getGraphics());
      
      Score newScore = new Score(fileChooser.getSelectedFile().toString());
      gui.setScore(newScore);
    
      gui.statusBar.setText("Ready.");
    } 
    catch (javax.xml.parsers.ParserConfigurationException exception) {
      exception.printStackTrace();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
