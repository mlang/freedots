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
package org.delysid.freedots.transcription;

import java.util.ArrayList;
import java.util.List;
import org.delysid.freedots.Options;

import org.delysid.freedots.musicxml.Score;
import org.delysid.freedots.musicxml.Part;

/**
 * Transcribes a {@link Score} to braille music code.
 */
public final class Transcriber {
  private Score score;

  public Score getScore() { return score; }
  public void setScore(Score score) {
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
  public Object getObjectAtIndex(int characterIndex) {
    StringBuilder stringBuilder = new StringBuilder();
    for (BrailleString brailleString:strings) {
      if (stringBuilder.length() + brailleString.length() > characterIndex)
        return brailleString.getModel();
      stringBuilder.append(brailleString.toString());
    }
    return null;
  }
  public int getIndexOfObject(Object object) {
    StringBuilder stringBuilder = new StringBuilder();
    for (BrailleString brailleString:strings) {
      if (brailleString.getModel() == object)
        return stringBuilder.length();
      stringBuilder.append(brailleString.toString());
    }
    return -1;
  }

  private static String lineSeparator = System.getProperty("line.separator");

  private Strategy strategy = null;

  public Transcriber(Options options) {
    this.options = options;
    clear();
  }
  public Transcriber(Score score, Options options) {
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
    switch (options.getMethod()) {
      case SectionBySection: strategy = new SectionBySection();
                             break;
      case BarOverBar: strategy = new BarOverBar();
                             break;
    }
    strategy.transcribe(this);
  }

  public void printString(String text) {
    printString(new BrailleString(text));
  }
  public void printString(BrailleString text) {
    strings.add(text);
    characterCount += text.length();
  }
  public void printString(BrailleList text) {
    strings.addAll(text);
    characterCount += text.length();
  }
  public void printLine(String text) {
    strings.add(new BrailleString(text));
    newLine();
  }
  public void newLine() {
    strings.add(new BrailleString(lineSeparator));
    characterCount = 0;
    lineCount += 1;
    if (lineCount == options.getPageHeight()) {
      indentTo(options.getPageWidth()-5);
      strings.add(new BrailleString(Integer.toString(pageNumber++) + lineSeparator));
      characterCount = 0;
      lineCount = 0;
    }
  }
  public void indentTo(int column) {
    int difference = column - characterCount;
    while (difference > 0) {
      strings.add(new BrailleString(" "));
      characterCount += 1;
      difference -= 1;
    }
  }
  public String toString() {
    return strings.toString();
  }
}
