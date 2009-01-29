/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

import java.util.ArrayList;
import java.util.List;

import org.delysid.freedots.model.AbstractPitch;
import org.delysid.freedots.model.Accidental;
import org.delysid.freedots.model.Event;
import org.delysid.freedots.model.Staff;
import org.delysid.freedots.model.MusicList;
import org.delysid.freedots.model.StartBar;
import org.delysid.freedots.model.EndBar;
import org.delysid.freedots.model.Voice;
import org.delysid.freedots.model.VoiceChord;

import org.delysid.freedots.musicxml.Score;
import org.delysid.freedots.musicxml.Note;
import org.delysid.freedots.musicxml.Part;

public final class Transcriber {
  private Score score;

  public Score getScore() { return score; }

  Options options;

  private String textStore;
  private int characterCount;
  private int lineCount;
  private int pageNumber;

  private static String lineSeparator = System.getProperty("line.separator");

  public Transcriber(Score score, Options options) {
    this.score = score;
    this.options = options;
    clear();
    if (score != null)
      try { transcribe(); }
      catch (Exception e) { e.printStackTrace(); }
  }
  private void clear() {
    textStore = "";
    characterCount = 0;
    lineCount = 0;
    pageNumber = 1;
  }
  void transcribe() throws Exception {
    for (Part part:score.getParts()) {
      printLine(part.getName());
      printLine(part.getTimeSignature().toBraille());
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
            } else if (event instanceof EndBar) {
              EndBar rightBar = (EndBar)event;
              int charactersLeft = options.getPageWidth() - characterCount;
              if (charactersLeft <= 2) {
                newLine();
                charactersLeft = options.getPageWidth() - characterCount;
              }
                            
              boolean lastLine = (lineCount == (options.getPageHeight() - 1));
              String head = measure.head(charactersLeft, lastLine);
              String tail = measure.tail();
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
    }
  }
  private void printString(String text) {
    textStore += text;
    characterCount += text.length();
  }
  private void printLine(String text) {
    textStore += text;
    newLine();
  }
  private void newLine() {
    textStore += lineSeparator;
    characterCount = 0;
    lineCount += 1;
    if (lineCount == options.getPageHeight()) {
      indentTo(options.getPageWidth()-5);
      textStore += Integer.toString(pageNumber++) + lineSeparator;
      characterCount = 0;
      lineCount = 0;
    }
  }
  private void indentTo(int column) {
    int difference = column - characterCount;
    while (difference > 0) {
      textStore += " ";
      characterCount += 1;
      difference -= 1;
    }
  }
  public String toString() {
    return textStore;
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
  class BrailleMeasure {
    private BrailleMeasure previous = null;
    private MusicList events = new MusicList();
    private AbstractPitch finalPitch = null;
    BrailleMeasure() {}
    BrailleMeasure(BrailleMeasure previous) {
      this();
      this.previous = previous;
    }
    public void add(Event event) { events.add(event); }
    public AbstractPitch getFinalPitch() { return finalPitch; }

    String tail;
    public String tail() { return tail; }
    public String head(int width, boolean lastLine) {
      tail = "";
      String output = "";
      boolean hyphenated = false;

      AbstractPitch lastPitch = previous != null? previous.getFinalPitch(): null;
      List<Voice> voices = events.getVoices();
      int voiceCount = voices.size();

      for (int voiceIndex = 0; voiceIndex < voiceCount; voiceIndex++) {
        for (Event element:voices.get(voiceIndex)) {
          if (element instanceof Note) {
            String braille = "";
            Note note = (Note)element;
            Accidental accidental = note.getAccidental();
            if (accidental != null) {
              braille += accidental.toBraille().toString();
            }
            AbstractPitch pitch = (AbstractPitch)note.getPitch();
            if (pitch != null) {
              Braille octaveSign = pitch.getOctaveSign(lastPitch);
              if (octaveSign != null) { braille += octaveSign; }
              lastPitch = pitch;
            }
            braille += note.getAugmentedFraction().toBrailleString(pitch);
            if (braille.length() < width && !hyphenated) {
              output += braille;
              width -= braille.length();
            } else {
              hyphenated = true;
              tail += braille;
            }
          } else if (element instanceof VoiceChord) {
            String braille = "";
            VoiceChord chord = (VoiceChord)element;
            chord = chord.getSorted();
            Note firstNote = (Note)chord.get(0);
            Accidental accidental = firstNote.getAccidental();
            if (accidental != null) {
              braille += accidental.toBraille().toString();
            }
            AbstractPitch firstPitch = (AbstractPitch)firstNote.getPitch();
            Braille octaveSign = firstPitch.getOctaveSign(lastPitch);
            if (octaveSign != null) { braille += octaveSign; }
            lastPitch = firstPitch;
            braille += firstNote.getAugmentedFraction().toBrailleString(firstPitch);
            AbstractPitch previousPitch = firstPitch;

            for (int chordElementIndex = 1; chordElementIndex < chord.size(); chordElementIndex++) {
              Note currentNote = (Note)chord.get(chordElementIndex);
              accidental = currentNote.getAccidental();
              if (accidental != null) {
                braille += accidental.toBraille().toString();
              }
              AbstractPitch currentPitch = (AbstractPitch)currentNote.getPitch();
              int diatonicDifference = Math.abs(currentPitch.diatonicDifference(previousPitch));
              if (diatonicDifference == 0) {
                braille += currentPitch.getOctaveSign(null);
                diatonicDifference = 7;
              } else if (diatonicDifference > 7) {
                braille += currentPitch.getOctaveSign(null);
                while (diatonicDifference > 7) diatonicDifference -= 7;
              }
              braille += Braille.interval(diatonicDifference);
              previousPitch = currentPitch;
            }

            if (braille.length() < width && !hyphenated) {
              output += braille;
              width -= braille.length();
            } else {
              tail += braille;
              hyphenated = true;
            }
          }
        }

        if (voiceIndex < voiceCount-1) {
          String braille = Braille.fullMeasureInAccord.toString();
          if (braille.length() < width && !hyphenated) {
            output += braille;
            width -= braille.length();
          } else {
            tail += braille;
            hyphenated = true;
          }

          /* The octave mark must be shown for
           * the first note after an in-accord.
           ************************************/
          lastPitch = null;
        }
      }

      /* 5-12. The octave mark must be shown for the first note after an
       * in-accord and _at the beginning of the next measure_, whether or not
       * that measure contains an in-accord.
       ***********************************************************************/
      if (voiceCount == 1) finalPitch = lastPitch;

      return output;
    }
  }
}
