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

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import freedots.musicxml.Note;

@SuppressWarnings("serial")
public final class EditFingeringAction extends AbstractAction {
  private Main gui;
  private FingeringEditor fingeringEditor = null;

  public EditFingeringAction(final Main gui) {
    super("Fingering...");
    this.gui = gui;
    putValue(SHORT_DESCRIPTION, "Edit fingering for this note");
    putValue(ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
  }
  public void actionPerformed(ActionEvent event) {
    Object scoreObject = gui.getCurrentScoreObject();
    if (scoreObject != null && scoreObject instanceof Note && fingeringEditor == null) {
      fingeringEditor = new FingeringEditor(gui, (Note)scoreObject);
      fingeringEditor.dispose();
      fingeringEditor = null;
    }
  }
}
