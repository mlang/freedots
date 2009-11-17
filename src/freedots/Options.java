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

/**
 * Store and process command-line options (transcription parameters included).
 */
public final class Options {
  private static Options instance;

  private static final int DEFAULT_PAGE_WIDTH = 32;
  private static final int DEFAULT_PAGE_HEIGHT = 20;

  private int pageWidth = DEFAULT_PAGE_WIDTH;
  private int pageHeight = DEFAULT_PAGE_HEIGHT;
  private int measuresPerSection = 8;
  public int getMeasuresPerSection() { return measuresPerSection; }
  private boolean newSystemEndsSection = true;
  public boolean getNewSystemEndsSection() { return newSystemEndsSection; }

  private String location;
  private boolean windowSystem = true;
  private boolean playScore = false;
  private String exportMidiFile = null;
  private UI ui = UI.Swing;

  private boolean showFingering = true;
  private Method method = Method.SectionBySection;

  public Options(final String[] args) {
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
      } else if ("-mps".equals(option)) {
        if (index < args.length-1) {
          measuresPerSection = Integer.valueOf(args[++index]);
          newSystemEndsSection = false;
        }
      } else if ("-nw".equals(option)) {
        windowSystem = false;
      } else if ("-p".equals(option) || "--play".equals(option)) {
        playScore = true;
      } else if ("-emf".equals(option) || "--export-midi-file".equals(option)) {
        if (index < args.length-1) {
          exportMidiFile = args[++index];
        }
      } else if ("-nofg".equals(option)) {
        showFingering = false;
      } else if ("-bob".equals(option)) {
        method = Method.BarOverBar;
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
  public void setWindowSystem(final boolean windowSystem) {
    this.windowSystem = windowSystem;
  }
  public boolean getPlayScore() { return playScore; }

  public String getExportMidiFile() { return exportMidiFile; }
  public UI getUI() { return ui; }

  /** Indicates if fingering information should be transcribed to braille.
   * @return true if fingering should be transcribed
   */
  public boolean getShowFingering() { return showFingering; }
  /** Enable or disable fingering transcription
   * @param value indicates the new state of fingering transcription
   */
  public void setShowFingering(boolean value) { showFingering = value; }
  public Method getMethod() { return method; }
  public void setMethod(final Method method) { this.method = method; }

  enum UI {
    Swing("swing.Main");

    private String className;
    UI(final String name) { className = "freedots.gui." + name; }
    public String getClassName() { return className; }
  }
  /** This enum defines the available transcription strategies.
   */
  public enum Method {
    SectionBySection, BarOverBar;
  }
}
