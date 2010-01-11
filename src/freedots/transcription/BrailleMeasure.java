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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import freedots.Braille;
import freedots.logging.Logger;
import freedots.Options;
import freedots.music.AbstractPitch;
import freedots.music.Accidental;
import freedots.music.Event;
import freedots.music.MusicList;
import freedots.music.RhythmicElement;
import freedots.music.Slur;
import freedots.music.TimeSignature;
import freedots.music.Voice;
import freedots.music.VoiceChord;
import freedots.musicxml.Note;

class BrailleMeasure {
  private static final Logger log = Logger.getLogger(BrailleMeasure.class);

  private BrailleMeasure previous = null;
  private MusicList events = new MusicList();
  public MusicList getEvents() { return events; }
  private AbstractPitch finalPitch = null;
  private TimeSignature timeSignature = null;
  private int voiceDirection = -1; /* By default, top to bottom */
  BrailleMeasure() {}
  BrailleMeasure(final BrailleMeasure previous) {
    this();
    this.previous = previous;
  }

  public void add(Event event) { events.add(event); }

  public void setTimeSignature(TimeSignature timeSignature) {
    this.timeSignature = timeSignature;
  }
  public void setVoiceDirection(int direction) {
    voiceDirection = direction;
  }
  public void unlinkPrevious() { previous = null; }

  private List<Object> brailleVoices = new ArrayList<Object>();

  /**
   * Find voice overlaps for part measure in-accord.
   */
  private boolean fullSimile = false;

  public void process() {
    if (previous != null) {
      if (previous.getEvents().equalsIgnoreOffset(this.events)) {
        fullSimile = true;
      }
    }

    if (!fullSimile) {
      brailleVoices = new ArrayList<Object>();

      List<Voice> voices = events.getVoices(voiceDirection);
      FullMeasureInAccord fmia = new FullMeasureInAccord();
      PartMeasureInAccord pmia = new PartMeasureInAccord();

      while (voices.size() > 0) {
        Voice voice = voices.get(0);
        boolean foundOverlap = false;
        int headLength = 0;

        for (int j = 1; j < voices.size(); j++) {
          int equalsAtBeginning = voice.countEqualsAtBeginning(voices.get(j));
          if (equalsAtBeginning > 0) {
            headLength = equalsAtBeginning;
            MusicList head = new MusicList();
            for (int k = 0; k < equalsAtBeginning; k++) {
              head.add(voice.get(k));
              voices.get(j).remove(0);
            }
            pmia.setHead(head);
            pmia.addPart(voice);
            pmia.addPart(voices.get(j));
            voices.remove(voices.get(j));
            foundOverlap = true;
          } else if (foundOverlap && equalsAtBeginning == headLength) {
            for (int k = 0; k < equalsAtBeginning; k++) {
              voices.get(j).remove(k);
            }
            pmia.addPart(voices.get(j));
            voices.remove(voices.get(j));
          }
        }
        
        if (foundOverlap) {
          for (int k = 0; k < headLength; k++) {
            voice.remove(0);
          }
        } else {
          fmia.addPart(voice);
        }

        voices.remove(voice);
      }
      if (fmia.getParts().size() > 0) brailleVoices.add(fmia);
      if (pmia.getParts().size() > 0) brailleVoices.add(pmia);
    }
  }
  public AbstractPitch getFinalPitch() { return finalPitch; }

  private BrailleList tail;
  public BrailleList tail() { return tail; }

  class State {
    private int width;
    private AbstractPitch lastPitch;
    private BrailleList head = new BrailleList();
    private BrailleList tail = new BrailleList();
    private boolean hyphenated = false;

    State(final int width, final AbstractPitch lastPitch) {
      this.width = width;
      this.lastPitch = lastPitch;
    }
    void append(String braille) {
      append(new BrailleString(braille));
    }
    void append(BrailleString braille) {
      if (braille.length() <= width && !hyphenated) {
        head.add(braille);
        width -= braille.length();
      } else {
        hyphenated = true;
        tail.add(braille);
      }
    }
    AbstractPitch getLastPitch() { return lastPitch; }
    void setLastPitch(AbstractPitch lastPitch) { this.lastPitch = lastPitch; }

    BrailleList getHead() { return head; }
    BrailleList getTail() { return tail; }
  }
  public BrailleList head(int width, boolean lastLine) {
    State state = new State(width,
                            previous != null? previous.getFinalPitch(): null);

    if (fullSimile) {
      state.append(Braille.simileSign.toString());
    } else {
      for (int i = 0; i < brailleVoices.size(); i++) {
        if (brailleVoices.get(i) instanceof PartMeasureInAccord) {
          PartMeasureInAccord pmia = (PartMeasureInAccord)brailleVoices.get(i);
          if (i > 0) {
            String braille = Braille.fullMeasureInAccord.toString();
            state.append(braille);

            /* The octave mark must be shown for
             * the first note after an in-accord.
             ************************************/
            state.setLastPitch(null);
          }
          MusicList pmiaHead = pmia.getHead();
          if (pmiaHead.size() > 0) {
            printNoteList(pmiaHead, state, null);
            state.append(Braille.partMeasureInAccord.toString());
          }
          for (int p = 0; p < pmia.getParts().size(); p++) {
            printNoteList(pmia.getParts().get(p), state, null);
            if (p < pmia.getParts().size() - 1) {
              String braille = Braille.partMeasureInAccordDivision.toString();
              state.append(braille);

              /* The octave mark must be shown for
               * the first note after an in-accord.
               ************************************/
              state.setLastPitch(null);
            }
          }
          MusicList pmiaTail = pmia.getTail();
          if (pmiaTail.size() > 0) {
            state.append(Braille.partMeasureInAccord.toString());
            printNoteList(pmiaTail, state, null);
          }
        } else if (brailleVoices.get(i) instanceof FullMeasureInAccord) {
          FullMeasureInAccord fmia = (FullMeasureInAccord)brailleVoices.get(i);
          for (int p = 0; p < fmia.getParts().size(); p++) {
            Object splitPoint = null;
            ValueInterpreter valueInterpreter =
              new ValueInterpreter(fmia.getParts().get(p), timeSignature);
            Set<ValueInterpreter.Interpretation>
              interpretations = valueInterpreter.getInterpretations();
            if (interpretations.size() > 1) {
              splitPoint = valueInterpreter.getSplitPoint();
              if (splitPoint == null) {
                StringBuilder sb = new StringBuilder();

                sb.append("Unimplemented: "
                          + interpretations.size()
                          + " possible interpretations:\n");
                for (ValueInterpreter.Interpretation
                       interpretation:interpretations) {
                  sb.append((interpretation.isCorrect()?" * ":"   ")
                            + interpretation.toString() + "\n");
                }
                log.warning(sb.toString());
              }
            }
            printNoteList(fmia.getParts().get(p), state, splitPoint);
            if (p < fmia.getParts().size() - 1) {
              String braille = Braille.fullMeasureInAccord.toString();
              state.append(braille);

              /* The octave mark must be shown for
               * the first note after an in-accord.
               ************************************/
              state.setLastPitch(null);
            }
          }
        }
      }

      if (brailleVoices.size() == 0) {
        state.append(Braille.wholeRest.toString());
      }

      /* 5-12. The octave mark must be shown for the first note after an
       * in-accord and _at the beginning of the next measure_, whether or not
       * that measure contains an in-accord.
       ***********************************************************************/
      if (brailleVoices.size() == 1
          && !((FullMeasureInAccord)brailleVoices.get(0)).isInAccord())
        finalPitch = state.getLastPitch();
    }
    tail = state.getTail();
    return state.getHead();
  }
  void printNoteList(MusicList musicList, State state, Object splitPoint) {
    for (Event element:musicList) {
      if (element instanceof Note) {
        Note note = (Note)element;
        if (splitPoint != null && splitPoint == note) {
          state.append(Braille.valueDistinction.toString());
        }
        BrailleNote brailleNote = new BrailleNote(note, state.getLastPitch());
        AbstractPitch pitch = (AbstractPitch)note.getPitch();
        if (pitch != null) {
          state.setLastPitch(pitch);
        }
        state.append(brailleNote);
      } else if (element instanceof VoiceChord) {
        printChord((VoiceChord)element, state);
      }
    }
  }

  /** Transcribes a chord with the use of interval signs.
   */
  private void printChord(VoiceChord chord, State state) {
    StringBuilder braille = new StringBuilder();
    final boolean showFingering = Options.getInstance().getShowFingering();

    final Iterator<RhythmicElement> iterator = chord.getSorted().iterator();
    assert iterator.hasNext();
    final Note firstNote = (Note)iterator.next();
    Accidental accidental = firstNote.getAccidental();
    if (accidental != null) braille.append(Braille.valueOf(accidental));

    AbstractPitch firstPitch = (AbstractPitch)firstNote.getPitch();
    if (firstPitch == null)
      firstPitch = (AbstractPitch)firstNote.getUnpitched();
    Braille octaveSign = firstPitch.getOctaveSign(state.getLastPitch());
    if (octaveSign != null) braille.append(octaveSign);
    state.setLastPitch(firstPitch);
    braille.append(Braille.toString(firstNote.getAugmentedFraction(),
                                    firstPitch));
    if (showFingering)
      braille.append(Braille.toString(firstNote.getFingering()));

    if (firstNote.isTieStart()) {
      braille.append(Braille.tie);
    } else {
      boolean printSlur = false;
      for (Slur<Note> slur:firstNote.getSlurs()) {
        if (!slur.lastNote(firstNote)) {
          printSlur = true;
          break;
        }
      }
      if (printSlur) braille.append(Braille.slur);
    }

    state.append(new BrailleString(braille.toString(), firstNote));
    braille = new StringBuilder();

    assert iterator.hasNext();

    while (iterator.hasNext()) {
      final Note currentNote = (Note)iterator.next();
      accidental = currentNote.getAccidental();
      if (accidental != null) braille.append(Braille.valueOf(accidental));
      AbstractPitch currentPitch = (AbstractPitch)currentNote.getPitch();
      if (currentPitch == null)
        currentPitch = (AbstractPitch)currentNote.getUnpitched();

      int diatonicDifference =
        Math.abs(currentPitch.diatonicDifference(firstPitch));
      if (diatonicDifference == 0) {
        braille.append(currentPitch.getOctaveSign(null));
        diatonicDifference = 7;
      } else if (diatonicDifference > 7) {
        braille.append(currentPitch.getOctaveSign(null));
        while (diatonicDifference > 7) diatonicDifference -= 7;
      }
      braille.append(Braille.interval(diatonicDifference));

      if (showFingering)
        braille.append(Braille.toString(currentNote.getFingering()));

      if (currentNote.isTieStart()) {
        braille.append(Braille.tie);
      }

      state.append(new BrailleString(braille.toString(), currentNote));
      if (iterator.hasNext()) braille = new StringBuilder();
    }
  }
}
