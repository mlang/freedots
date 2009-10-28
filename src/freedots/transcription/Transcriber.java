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
package freedots.transcription;

import freedots.Braille;
import freedots.Options;

import freedots.musicxml.Score;

/**
 * Transcribes a {@link Score} to braille music code.
 */
public final class Transcriber {
  private Score score;

  public Score getScore() { return score; }
  public void setScore(final Score score) {
    this.score = score;
    clear();
    try {
      transcribe();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Options options;
  public Options getOptions() { return options; }

  private BrailleList strings;
  private int characterCount;
  private int lineCount;
  private int pageNumber;

  public boolean isLastLine() {
    return lineCount == (options.getPageHeight() - 1);
  }
  public int getCurrentColumn() { return characterCount; }
  public int getRemainingColumns() {
    return options.getPageWidth() - characterCount;
  }

  /* Find Object responsible for character at specified index
   *
   * This is used by the UI code.
   *
   * @returns the object responsible for the character at index, or null
   */
  public Object getObjectAtIndex(final int characterIndex) {
    StringBuilder stringBuilder = new StringBuilder();
    for (BrailleString brailleString:strings) {
      if (stringBuilder.length() + brailleString.length() > characterIndex)
        return brailleString.getModel();
      stringBuilder.append(brailleString.toString());
    }
    return null;
  }
  /*
   * Find the starting index of the character sequence for Object
   *
   * This is the reverse of getObjectAtIndex()
   */
  public int getIndexOfObject(final Object object) {
    StringBuilder stringBuilder = new StringBuilder();
    for (BrailleString brailleString : strings) {
      if (brailleString.getModel() == object)
        return stringBuilder.length();
      stringBuilder.append(brailleString.toString());
    }
    return -1;
  }

  private static String lineSeparator = System.getProperty("line.separator");

  private Strategy strategy;

  public Transcriber(final Options options) {
    this.options = options;
    clear();
  }
  public Transcriber(final Score score, final Options options) {
    this(options);
    setScore(score);
  }
  private void clear() {
    strings = new BrailleList();
    characterCount = 0;
    lineCount = 0;
    pageNumber = 1;
  }

  private void transcribe() throws Exception {
    String workNumber = score.getWorkNumber();
    String workTitle = score.getWorkTitle();
    String movementTitle = score.getMovementTitle();
    String composer = score.getComposer();
    boolean headerAvailable = false;

    if (isNonEmpty(workNumber)) {
      printCenteredLine(workNumber);
      headerAvailable = true;
    }
    if (isNonEmpty(workTitle)) {
      printCenteredLine(workTitle);
      headerAvailable = true;
    }
    if (isNonEmpty(movementTitle)) {
      printCenteredLine(movementTitle);
      headerAvailable = true;
    }
    if (isNonEmpty(composer)) {
      printCenteredLine(composer);
      headerAvailable = true;
    }
    if (headerAvailable) newLine();

    switch (options.getMethod()) {
    default:
    case SectionBySection:
      strategy = new SectionBySection();
      break;
    case BarOverBar:
      strategy = new BarOverBar();
      break;
    }
    strategy.transcribe(this);
  }

  private static boolean isNonEmpty(final String string) {
    return string != null && string.length() > 0;
  }

  void printString(final String text) {
    printString(new BrailleString(text));
  }
  void printString(final Braille braille) {
    printString(new BrailleString(braille));
  }
  void printString(final BrailleString text) {
    strings.add(text);
    characterCount += text.length();
  }
  void printString(final BrailleList text) {
    strings.addAll(text);
    characterCount += text.length();
  }
  void printLine(final String text) {
    strings.add(new BrailleString(text));
    newLine();
  }
  void printCenteredLine(final String text) {
    int skip = (getRemainingColumns() - text.length()) / 2;
    if (skip > 0) {
      StringBuilder skipString = new StringBuilder();
      for (int i = 0; i < skip; i++) skipString.append(" ");
      strings.add(new BrailleString(skipString.toString()));
    }
    strings.add(new BrailleString(text));
    newLine();
  }
  void newLine() {
    strings.add(new BrailleString(lineSeparator));
    characterCount = 0;
    lineCount += 1;
    if (lineCount == options.getPageHeight()) {
      String pageIndicator = Braille.numberSign.toString()
                           + Braille.upperNumber(pageNumber++);
      indentTo(options.getPageWidth() - pageIndicator.length());
      strings.add(new BrailleString(pageIndicator + lineSeparator));
      characterCount = 0;
      lineCount = 0;
    }
  }
  void indentTo(final int column) {
    int difference = column - characterCount;
    while (difference > 0) {
      strings.add(new BrailleString(" "));
      characterCount += 1;
      difference -= 1;
    }
  }
  /**
   * Convert transcription result to a plain string.
   *
   * @return result string of last transcription
   */
  @Override public String toString() {
    return strings.toString();
  }
}
