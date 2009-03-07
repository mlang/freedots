/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.transcription;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.delysid.freedots.Braille;
import org.delysid.freedots.Options;

import org.delysid.freedots.model.AbstractPitch;
import org.delysid.freedots.model.Accidental;
import org.delysid.freedots.model.EndBar;
import org.delysid.freedots.model.Event;
import org.delysid.freedots.model.MusicList;
import org.delysid.freedots.model.Staff;
import org.delysid.freedots.model.StartBar;
import org.delysid.freedots.model.TimeSignature;
import org.delysid.freedots.model.Voice;
import org.delysid.freedots.model.VoiceChord;

import org.delysid.freedots.musicxml.Score;
import org.delysid.freedots.musicxml.Note;
import org.delysid.freedots.musicxml.Part;

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

  Options options;
  public Options getOptions() { return options; }

  private BrailleList strings;
  private int characterCount;
  private int lineCount;
  private int pageNumber;

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
  void transcribe() throws Exception {
    for (Part part:score.getParts()) {
      printLine(part.getName());
      printLine(part.getKeySignature().toBraille() +
                part.getTimeSignature().toBraille());
      for (Segment segment:getSegments(part)) {
        int staffCount = segment.getStaffCount();
        for (int staffIndex = 0; staffIndex < staffCount; staffIndex++) {
	  Staff staff = segment.getStaff(staffIndex);
	  BrailleMeasure measure = new BrailleMeasure();
          boolean displayClefChange = false;

          if (characterCount > 0) newLine();
          indentTo(2);

          if (staffCount == 1 && staff.containsChords()) {
            displayClefChange = true;
          } else if (staffCount == 2) {
            if (staffIndex == 0) {
              printString(Braille.rightHandPart.toString());
            } else if (staffIndex == 1) {
              printString(Braille.leftHandPart.toString());
            }
          }

          String lyric = staff.getLyricText();
          if (lyric.length() > 0) printLine(lyric);

          StartBar startBar = null;

          for (int staffElementIndex = 0; staffElementIndex < staff.size();
               staffElementIndex++) {
            Event event = staff.get(staffElementIndex);

            if (event instanceof StartBar) {
              startBar = (StartBar)event;
              measure.setTimeSignature(startBar.getTimeSignature());
            } else if (event instanceof EndBar) {
              EndBar rightBar = (EndBar)event;
              int charactersLeft = options.getPageWidth() - characterCount;
              if (charactersLeft <= 2) {
                newLine();
                charactersLeft = options.getPageWidth() - characterCount;
              }
                            
              boolean lastLine = (lineCount == (options.getPageHeight() - 1));
              measure.process();
              BrailleList head = measure.head(charactersLeft, lastLine);
              BrailleList tail = measure.tail();
              if (head.length() <= tail.length() / 10) {
                newLine();
                charactersLeft = options.getPageWidth() - characterCount;
                head = measure.head(charactersLeft, lastLine);
                tail = measure.tail();
              }
              if (startBar != null) {
                if (startBar.getRepeatForward()) {
                  String braille = Braille.postDottedDoubleBar.toString();
                  braille += Braille.unicodeBraille(Braille.dotsToBits(3));
                  printString(braille);
                }
                if (startBar.getEndingStart() > 0) {
                  String braille = Braille.numberSign.toString();
                  braille += Braille.lowerDigit(startBar.getEndingStart());
                  braille += Braille.unicodeBraille(Braille.dotsToBits(3));
                  printString(braille);
                }
              }
              printString(head);
              if (tail.length() > 0) {
                printString(Braille.hyphen.toString());
                newLine();
                printString(tail);
              }

              if (rightBar.getRepeat())
                printString(Braille.dottedDoubleBar.toString());
              else if (rightBar.getEndOfMusic())
                printString(Braille.doubleBar.toString());

              printString(" ");

              measure = new BrailleMeasure(measure);
            } else {
              measure.add(event);
            }
          }
        }
      }
      newLine();
    }
  }
  private void printString(String text) {
    printString(new BrailleString(text));
  }
  private void printString(BrailleString text) {
    strings.add(text);
    characterCount += text.length();
  }
  private void printString(BrailleList text) {
    strings.addAll(text);
    characterCount += text.length();
  }
  private void printLine(String text) {
    strings.add(new BrailleString(text));
    newLine();
  }
  private void newLine() {
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
  private void indentTo(int column) {
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
  class Segment extends MusicList {
    Segment() { super(); }
  }
  List<Segment> getSegments(Part part) throws Exception {
    List<Segment> segments = new ArrayList<Segment>();
    Segment currentSegment = new Segment();
    segments.add(currentSegment);
    MusicList musicList = part.getMusicList();
    int index = 0;
    int measureCount = 0;

    while (true) {
      while (index < musicList.size()) {
	Event event = musicList.get(index++);
	currentSegment.add(event);
	if (event instanceof EndBar) { measureCount++; break; }
      }

      if (index == musicList.size()) return segments;

      if (!(musicList.get(index) instanceof StartBar))
	throw new Exception();

      StartBar startBar = (StartBar)musicList.get(index);
      if ((startBar.getStaffCount() != currentSegment.getStaffCount()) ||
          (currentSegment.getStaffCount() > 1 && (
           (options.multiStaffMeasures == Options.MultiStaffMeasures.VISUAL &&
            startBar.getNewSystem()) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.TWO &&
            measureCount == 2) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.THREE &&
            measureCount == 3) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.FOUR &&
            measureCount == 4) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.FIVE &&
            measureCount == 5) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.SIX &&
            measureCount == 6) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.SEVEN &&
            measureCount == 7) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.EIGHT &&
            measureCount == 8) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.NINE &&
            measureCount == 9) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.TEN &&
            measureCount == 10) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.ELEVEN &&
            measureCount == 11) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.TWELVE &&
            measureCount == 12))) ||
          (currentSegment.getLyricText().length() >= options.getPageWidth() ||
           startBar.getNewSystem())) {
	currentSegment = new Segment();
	segments.add(currentSegment);
        measureCount = 0;
      }
    }
  }
}
