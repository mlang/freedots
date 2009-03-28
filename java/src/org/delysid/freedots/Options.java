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
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots;

public final class Options {
  private static Options instance = null;

  int pageWidth = 32;
  int pageHeight = 20;
  public MultiStaffMeasures multiStaffMeasures = MultiStaffMeasures.VISUAL;

  String location = null;
  boolean windowSystem = true;
  boolean playScore = false;
  String exportMidiFile = null;
  UI ui = UI.Swing;

  boolean showFingering = true;

  public Options(String[] args) {
    for (int index = 0; index < args.length; index++) {
      String option = args[index];
      if (option.equals("-w") || option.equals("--width")) {
        if (index < args.length-1) {
          pageWidth = Integer.parseInt(args[++index]);
        }
      } else if (option.equals("-h") || option.equals("--height")) {
        if (index < args.length-1) {
          pageHeight = Integer.parseInt(args[++index]);
        }
      } else if ("-msm".equals(option)) {
        if (index < args.length-1) {
          String arg = args[++index];
          if ("visual".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.VISUAL;
          else if ("2".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.TWO;
          else if ("3".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.THREE;
          else if ("4".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.FOUR;
          else if ("5".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.FIVE;
          else if ("6".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.SIX;
          else if ("7".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.SEVEN;
          else if ("8".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.EIGHT;
          else if ("9".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.NINE;
          else if ("10".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.TEN;
          else if ("11".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.ELEVEN;
          else if ("12".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.TWELVE;
          else
            throw new IllegalArgumentException("'-msm "+arg+"'");
        }
      } else if ("-nw".equals(option)) {
        windowSystem = false;
      } else if ("-p".equals(option) || "--play".equals(option)) {
        playScore = true;
      } else if ("-emf".equals(option) || "--export-midi-file".equals(option)) {
        if (index < args.length-1) {
          exportMidiFile = args[++index];
        }
      } else if ("-swt".equals(option)) {
        ui = UI.SWT;
      } else if ("-nofg".equals(option)) {
        showFingering = false;
      } else {
        if (index == args.length-1) {
          location = args[index];
        } else throw new IllegalArgumentException();
      }
    }
    instance = this;
  }
  public static Options getInstance() { return instance; }

  public String getLocation() {
    return location;
  }
  public int getPageHeight() { return pageHeight; }
  public int getPageWidth() { return pageWidth; }
  public boolean getWindowSystem() { return windowSystem; }
  public void setWindowSystem(boolean windowSystem) {
    this.windowSystem = windowSystem;
  }
  public boolean getPlayScore() { return playScore; }

  public String getExportMidiFile() { return exportMidiFile; }
  public UI getUI() { return ui; }

  public boolean getShowFingering() { return showFingering; }

  public enum MultiStaffMeasures {
    VISUAL, /* break at new system */
    TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELVE;
  }
  public enum UI {
    Swing("swing.Main"), SWT("swt.Main");

    String className;
    UI(String name) { className = "org.delysid.freedots.gui." + name; }
    public String getClassName() { return className; }
  }
}
