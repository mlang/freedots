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
package org.delysid.freedots.gui.swt;

import java.awt.HeadlessException;

import org.delysid.freedots.transcription.Transcriber;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Main implements org.delysid.freedots.gui.GraphicalUserInterface {
  Transcriber transcriber;

  public Main(final Transcriber transcriber) {
    this.transcriber = transcriber;
  }

  public final void run() {
    try {
      Display display = new Display();
      MainFrame frame = new MainFrame(transcriber);
      Shell shell = frame.open(display);
      while (!shell.isDisposed())
        if (!display.readAndDispatch()) display.sleep();
      display.dispose();
    } catch (SWTError e) {
      e.printStackTrace();
      throw new HeadlessException();
    }
  }
}
