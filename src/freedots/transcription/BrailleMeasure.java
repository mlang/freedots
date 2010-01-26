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
import java.util.Set;

import freedots.Braille;
import freedots.braille.BrailleChord;
import freedots.braille.BrailleDynamics;
import freedots.braille.BrailleList;
import freedots.braille.BrailleNote;
import freedots.braille.BrailleSequence;
import freedots.braille.BrailleWords;
import freedots.braille.Text;
import freedots.logging.Logger;
import freedots.music.AbstractPitch;
import freedots.music.Event;
import freedots.music.MusicList;
import freedots.music.TimeSignature;
import freedots.music.Voice;
import freedots.music.VoiceChord;
import freedots.musicxml.Direction;
import freedots.musicxml.Note;

class BrailleMeasure {
  private static final Logger LOG = Logger.getLogger(BrailleMeasure.class);

  private BrailleMeasure previous = null;
  private MusicList events = new MusicList();
  public MusicList getEvents() { return events; }
  private AbstractPitch finalPitch = null;
  private TimeSignature timeSignature = null;
  private int voiceDirection = -1; /* By default, top to bottom */
  private final Transcriber transcriber;
  BrailleMeasure(Transcriber transcriber) { this.transcriber = transcriber; }
  BrailleMeasure(final Transcriber transcriber, final BrailleMeasure previous) {
    this(transcriber);
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
      append(new Text(braille));
    }
    void append(BrailleSequence braille) {
      if (head.length() + braille.length() <= width && !hyphenated) {
        head.add(braille);
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
                LOG.warning(sb.toString());
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
        BrailleChord brailleChord = new BrailleChord((VoiceChord)element,
                                                     state.getLastPitch());
        AbstractPitch pitch = (AbstractPitch)brailleChord.getNotePitch();
        if (pitch != null) {
          state.setLastPitch(pitch);
        }
        state.append(brailleChord);
      } else if (element instanceof Direction) {
        Direction direction = (Direction)element;

        if (!transcriber.getAlreadyPrintedDirections().contains(direction)) {
          List<String> dynamics = direction.getDynamics();
          if (dynamics != null) {
            for (String dyn: dynamics) {
              state.append(new BrailleDynamics(dyn, direction));
              state.setLastPitch(null);
            }
          }

          String words = direction.getWords();
          if (words != null && !words.isEmpty()) {
            state.append(new BrailleWords(words, direction));
          }

          if (direction.isPedalPress())
            state.append(Braille.pedalPress.toString());
          else if (direction.isPedalRelease())
            state.append(Braille.pedalRelease.toString());
        }
      }
    }
  }
}
