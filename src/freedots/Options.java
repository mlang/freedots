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
package freedots;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Store and process command-line options (transcription parameters included).
 */
public final class Options {
  private static Options instance;

  private static final int DEFAULT_PAGE_WIDTH = 40;
  private static final int DEFAULT_PAGE_HEIGHT = 20;
  private static final int DEFAULT_SLUR_DOUBLING_THRESHOLD = 4;

  private int pageWidth = DEFAULT_PAGE_WIDTH;
  private int pageHeight = DEFAULT_PAGE_HEIGHT;
  private int measuresPerSection = 8;
  /** The number of measures to use per section for
   *  {@link freedots.transcription.SectionBySection} format.
   * @return 8 by default, otherwise the number specified at the command-line
   */
  public int getMeasuresPerSection() { return measuresPerSection; }

  private boolean newSystemEndsSection = true;
  /** When using {@link freedots.transcription.SectionBySection} format,
   *  true indicates section breaks should correspond to system breaks in the
   *  visual score.  This mode allows for easy communication between
   *  sighted and blind musicians.
   * @return true if system breaks should correlate with sections
   */
  public boolean getNewSystemEndsSection() { return newSystemEndsSection; }

  private String location;
  private boolean windowSystem = true;
  private boolean playScore = false;
  private File exportMidiFile = null;
  private UI ui = UI.Swing;

  private boolean showFingering = true;

  private int slurDoublingThreshold = DEFAULT_SLUR_DOUBLING_THRESHOLD;
  public int getSlurDoublingThreshold() { return slurDoublingThreshold; }

  private Method method = Method.SectionBySection;

  private File soundfont = null;
  /** Gets the soundfont requested for synthesis (if specified).
   * @return the path to the selected soundfont, or {@code null} if none was
   *         specified.
   */
  public File getSoundfont() { return soundfont; }

  /** Constructs a new instance from a list of command-line arguments.
   * @param args is the list of command-line arguments specified at startup
   * @throws FileNotFoundException if a required file was not found on the
   *                               filesystem.
   */
  public Options(final String[] args) throws FileNotFoundException {
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
          exportMidiFile = new File(args[++index]);
        }
      } else if ("-nofg".equals(option)) {
        showFingering = false;
      } else if ("-sdt".equals(option)) {
        if (index < args.length-1) {
          slurDoublingThreshold = Integer.valueOf(args[++index]);
          if (slurDoublingThreshold < 4) slurDoublingThreshold = 4;
        }
      } else if ("-bob".equals(option)) {
        method = Method.BarOverBar;
      } else if ("-sf".equals(option)) {
        if (index < args.length-1) {
          File file = new File(args[++index]);
          if (file.exists()) {
            soundfont = file;
          } else {
            throw new FileNotFoundException(file.toString());
          }
        }
      } else {
        if (index == args.length-1) {
          location = args[index];
        } else throw new IllegalArgumentException();
      }
    }
    instance = this;
  }
  public static Options getInstance() { return instance; }

  /** Gets the filename or URL specified on the command-line.
   * @return the string specified on the command-line, or {@code null} if
   *         absent.
   */
  public String getLocation() {
    return location;
  }
  /** Retrieves the requested number of lines per braille page.
   * @return {@code DEFAULT_PAGE_HEIGHT} is no value was specified
   * @see #DEFAULT_PAGE_HEIGHT
   */
  public int getPageHeight() { return pageHeight; }
  public void setPageHeight(int height) { pageHeight = height; }

  /** Number of columns of a braille page.
   * @return {@code DEFAULT_PAGE_WIDTH} is no value was specified
   * @see #DEFAULT_PAGE_WIDTH
   */
  public int getPageWidth() { return pageWidth; }
  public void setPageWidth(int width) { pageWidth = width; }

  boolean getWindowSystem() { return windowSystem; }
  void setWindowSystem(final boolean windowSystem) {
    this.windowSystem = windowSystem;
  }

  /** Indicates if playback was requested from the command-line.
   * @return true if "-p" was specified on the command-line.
   */
  public boolean getPlayScore() { return playScore; }

  /** Indicates if MIDI file export was requested from the command-line.
   * @return {@code null} if MIDI file export was not requested
   */
  public File getExportMidiFile() { return exportMidiFile; }

  UI getUI() { return ui; }

  /** Indicates if fingering information should be transcribed to braille.
   * @return true if fingering should be transcribed
   */
  public boolean getShowFingering() { return showFingering; }
  /** Enables or disable fingering transcription.
   * @param value indicates the new state of fingering transcription
   */
  public void setShowFingering(boolean value) { showFingering = value; }
  public Method getMethod() { return method; }
  public void setMethod(final Method method) { this.method = method; }

  /** Enumeration of available user interface implementations.
   * <p>
   * As of now, there is only one implementation ({@link freedots.gui.swing}).
   */
  enum UI {
    Swing("swing.Main");

    private String className;
    UI(final String name) { className = "freedots.gui." + name; }
    public String getClassName() { return className; }
  }
  /** Defines the available transcription formats, like section by section or
   * bar over bar.
   */
  public enum Method {
    SectionBySection, BarOverBar;
  }
}
