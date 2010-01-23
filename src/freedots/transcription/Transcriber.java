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
package freedots.transcription;

import java.util.ArrayList;
import java.util.List;

import freedots.Options;

import freedots.braille.Atom;
import freedots.braille.BrailleList;
import freedots.braille.BrailleSequence;
import freedots.braille.NewLine;
import freedots.braille.Space;
import freedots.braille.Text;
import freedots.braille.UpperNumber;
import freedots.musicxml.Direction;
import freedots.musicxml.Score;

/**
 * Transcribes a {@link Score} to braille music code.
 */
public final class Transcriber {
  private Score score;

  /** The score which was used to generate the current transcription.
   * @return the score object
   */
  public Score getScore() { return score; }

  /** Transcribe the given score to braille.
   * @param score is the abstract representation of the music to transcribe
   */
  public void setScore(final Score score) {
    this.score = score;
    clear();
    transcribe();
  }

  private Options options;
  Options getOptions() { return options; }

  private BrailleList strings;
  private int characterCount;
  private int lineCount;
  private int pageNumber;

  boolean isLastLine() {
    return lineCount == (options.getPageHeight() - 1);
  }
  int getCurrentColumn() { return characterCount; }
  int getRemainingColumns() {
    String text = toString();
    final int index = text.lastIndexOf(LINE_SEPARATOR);
    if (index == -1) return options.getPageWidth() - text.length();
    else return options.getPageWidth() - (text.length() - index);
  }

  /** Find the braille sign at a given character index.
   */
  public Atom getSignAtIndex(final int index) {
    return strings.getSignAtIndex(index);
  }

  /** Find Object responsible for character at specified index.
   *
   * @param characterIndex indicates the position relative to {@link #toString}
   *
   * @return the object responsible for the character at index, or {@code null}
   *         if none was found / specified.
   */
  public Object getScoreObjectAtIndex(final int characterIndex) {
    return strings.getScoreObjectAtIndex(characterIndex);
  }
  /** Find the starting index of the character sequence for Object.
   *
   * This is the reverse of getObjectAtIndex()
   *
   * @param object is a score object to search in the transcribed text
   *
   * @return the index of the first character that was generated due to object
   */
  public int getIndexOfScoreObject(final Object object) {
    return strings.getIndexOfScoreObject(object);
  }
  public char charAt(final int index) { return strings.charAt(index); }
  public Object getScoreObject() { return score; }

  public static final String LINE_SEPARATOR =
    System.getProperty("line.separator");

  private Strategy strategy;

  /** Construct a new transcriber object.
   * @param options is used to pass in command-line or GUI options
   */
  public Transcriber(final Options options) {
    this.options = options;
    clear();
  }
  private void clear() {
    strings = new BrailleList();
    characterCount = 0;
    lineCount = 0;
    pageNumber = 1;

    alreadyPrintedDirections = new ArrayList<Direction>();
  }

  private void transcribe() {
    String workNumber = score.getWorkNumber();
    String workTitle = score.getWorkTitle();
    String movementTitle = score.getMovementTitle();
    String composer = score.getComposer();
    String lyricist = score.getLyricist();
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
    if (isNonEmpty(composer) && isNonEmpty(lyricist)) {
      printCenteredLine("Music by "+composer);
      printCenteredLine("Lyrics by "+lyricist);
      headerAvailable = true;
    } else if (isNonEmpty(composer)) {
      printCenteredLine(composer);
      headerAvailable = true;
    }
    if (headerAvailable) newLine();

    switch (options.getMethod()) {
    case SectionBySection:
      strategy = new SectionBySection();
      break;
    case BarOverBar:
      strategy = new BarOverBar();
      break;
    default:
      throw new AssertionError(options.getMethod());
    }
    strategy.transcribe(this);
  }

  private static boolean isNonEmpty(final String string) {
    return string != null && string.length() > 0;
  }

  void printString(final String text) {
    printString(new Text(text));
  }
  void printString(final BrailleSequence braille) {
    strings.add(braille);
    characterCount += braille.length();
  }
  void printLine(final String text) {
    printString(text);
    newLine();
  }
  void printCenteredLine(final String text) {
    int skip = (getRemainingColumns() - text.length()) / 2;
    if (skip > 0) {
      StringBuilder skipString = new StringBuilder();
      for (int i = 0; i < skip; i++) skipString.append(" ");
      printString(skipString.toString());
    }
    printString(text);
    newLine();
  }
  void spaceOrNewLine() {
    if (getRemainingColumns() > 1) printString(new Space());
    else newLine();
  }
  void newLine() {
    strings.add(new NewLine());
    characterCount = 0;
    lineCount += 1;
    if (lineCount == options.getPageHeight()) {
      BrailleSequence pageIndicator = new UpperNumber(pageNumber++);
      indentTo(options.getPageWidth() - pageIndicator.length());
      strings.add(pageIndicator);
      strings.add(new NewLine());
      characterCount = 0;
      lineCount = 0;
    }
  }
  void indentTo(final int column) {
    int difference = column - characterCount;
    while (difference > 0) {
      strings.add(new Space());
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

  List<Direction> alreadyPrintedDirections;
}
