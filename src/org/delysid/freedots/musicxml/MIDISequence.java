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
package org.delysid.freedots.musicxml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.delysid.freedots.model.AccidentalContext;
import org.delysid.freedots.model.Articulation;
import org.delysid.freedots.model.EndBar;
import org.delysid.freedots.model.Event;
import org.delysid.freedots.model.Fraction;
import org.delysid.freedots.model.GlobalKeyChange;
import org.delysid.freedots.model.KeyChange;
import org.delysid.freedots.model.KeySignature;
import org.delysid.freedots.model.MusicList;
import org.delysid.freedots.model.Ornament;
import org.delysid.freedots.model.StartBar;
import org.delysid.freedots.playback.MetaEventRelay;

public class MIDISequence extends javax.sound.midi.Sequence {
  private Map<Integer, AccidentalContext> accidentalContexts;
  /**
   * Create an instance of MIDISequence
   *
   * @param score   the score to convert
   */
  public MIDISequence (Score score) throws InvalidMidiDataException {
    this(score, null);
  }
  /**
   * Create an instance of MIDISequence
   *
   * @param score           the score to convert
   * @param metaEventRelay  factory for object reference meta message creation
   */
  public MIDISequence (
    Score score, MetaEventRelay metaEventRelay
  ) throws InvalidMidiDataException {
    super(PPQ, calculatePPQ(score.getDivisions()));
    Track tempoTrack = createTrack();
    for (Part part : score.getParts()) {
      Track track = createTrack();
      MetaMessage metaMessage;
      int velocity = 64;

      if (part.getName() != null) {
        String trackName = new String(part.getName());
        metaMessage = new MetaMessage();
        metaMessage.setMessage(0x03, trackName.getBytes(), trackName.length());
        track.add(new MidiEvent(metaMessage, 0));
      }

      initializeMidiPrograms(track, part);
      MusicList events = part.getMusicList();
      {
        int staffCount = events.getStaffCount();
        accidentalContexts = new HashMap<Integer, AccidentalContext>();
        for (int i = 0; i < staffCount; i++) {
          accidentalContexts.put(new Integer(i), new AccidentalContext(part.getKeySignature()));
        }
      }

      int round = 1;
      int repeatStartIndex = -1;

      Fraction offset = new Fraction(0, 1);
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);
        if (event instanceof Direction) {
          Direction direction = (Direction)event;
          if (direction.getSound() != null)
            event = direction.getSound();
        }          

        if (event instanceof GlobalKeyChange) {
          GlobalKeyChange globalKeyChange = (GlobalKeyChange)event;
          KeySignature keySignature = globalKeyChange.getKeySignature();
          for (int j = 0; j < accidentalContexts.size(); j++) {
            accidentalContexts.get(j).setKeySignature(keySignature);
          }
        } else if (event instanceof KeyChange) {
          KeyChange keyChange = (KeyChange)event;
          accidentalContexts.get(keyChange.getStaffNumber()).setKeySignature(keyChange.getKeySignature());
        } else if (event instanceof Note) {
          addToTrack(track, (Note)event, velocity, offset, metaEventRelay);
        } else if (event instanceof Chord) {
          int midiTick = event.getOffset().add(offset).toInteger(resolution);
          if (metaEventRelay != null) {
            metaMessage = metaEventRelay.createMetaMessage((Chord)event);
            if (metaMessage != null) {
              track.add(new MidiEvent(metaMessage, midiTick));
            }
          }
          for (Note note:(Chord)event)
            addToTrack(track, note, velocity, offset, null);
        } else if (event instanceof Sound) {
          Sound sound = (Sound)event;
          MetaMessage tempoMessage = sound.getTempoMessage();
          if (tempoMessage != null) {
            int midiTick = sound.getOffset().add(offset).toInteger(resolution);
            tempoTrack.add(new MidiEvent(tempoMessage, midiTick));
          }
          Integer newVelocity = sound.getMidiVelocity();
          if (newVelocity != null) {
            velocity = newVelocity;
          }
        } else if (event instanceof StartBar) {
          if (repeatStartIndex == -1) repeatStartIndex = i;

          for (int j = 0; j < accidentalContexts.size(); j++) {
            accidentalContexts.get(j).resetToKeySignature();
          }
          StartBar startBar = (StartBar)event;
          if (startBar.getRepeatForward()) {
            repeatStartIndex = i;
            round = 1;
          }
          if (startBar.getEndingStart() > 0 &&
              startBar.getEndingStart() != round) { /* skip to EndBar */
            for (int j = i + 1; j < events.size(); j++) {
              if (events.get(j) instanceof EndBar) {
                EndBar endBar = (EndBar)events.get(j);
                if (endBar.getEndingStop() == startBar.getEndingStart()) {
                  offset = offset.subtract(endBar.getOffset().subtract(startBar.getOffset()));
                  i = j + 1;
                  break;
                }
              }
            }
          }
        } else if (event instanceof EndBar) {
          EndBar endbar = (EndBar)event;
          if (endbar.getRepeat()) {
            if (round == 1) {
              StartBar repeatStart = (StartBar)events.get(repeatStartIndex);
              offset = offset.add(endbar.getOffset().subtract(repeatStart.getOffset()));
              i = repeatStartIndex;
              round += 1;
            }
          }
        }
      }
    }
  }
  /**
   * Create an instance of MIDISequence
   *
   * @param note   the note to convert
   */
  public MIDISequence (Note note) throws InvalidMidiDataException {
    super(PPQ, calculatePPQ(note.getPart().getScore().getDivisions()));
    Track track = createTrack();
    int velocity = 64;

    initializeMidiPrograms(track, note.getPart());

    Fraction offset = note.getOffset().negate();
    addToTrack(track, note, velocity, offset, null);
  }

  private void addToTrack(
    Track track,
    Note note, int velocity,
    Fraction add, MetaEventRelay metaEventRelay
  ) {
    if (!note.isGrace()) {
      Pitch pitch = note.getPitch();
      try {
        int offset = note.getOffset().add(add).toInteger(resolution);
        int duration = note.getDuration().toInteger(resolution);
        Set<Articulation> articulations = note.getArticulations();
        if (articulations != null) {
          if (articulations.contains(Articulation.staccatissimo)) {
            duration /= 4;
          } else if (articulations.contains(Articulation.staccato)) {
            duration /= 2;
          } else if (articulations.contains(Articulation.mezzoStaccato)) {
            duration -= duration / 4;
          }
        }
        boolean turn = false;
        Set<Ornament> ornaments = note.getOrnaments();
        if (ornaments != null) {
          if (ornaments.contains(Ornament.turn)) {
            turn = true;
          }
        }
        if (metaEventRelay != null) {
          MetaMessage metaMessage = metaEventRelay.createMetaMessage(note);
          if (metaMessage != null) {
            track.add(new MidiEvent(metaMessage, offset));
          }
        }
        if (pitch != null) {
          accidentalContexts.get(note.getStaffNumber()).accept(pitch, note.getAccidental());
          int midiPitch = pitch.getMIDIPitch();
          int midiChannel = note.getMidiChannel();
          if (turn) {
            Integer staffNumber = new Integer(note.getStaffNumber());
            AccidentalContext accidentalContext = accidentalContexts.get(staffNumber);
            int upperPitch = pitch.nextStep(accidentalContext).getMIDIPitch();
            int lowerPitch = pitch.previousStep(accidentalContext).getMIDIPitch();
            duration /= 4;
            addNoteToTrack(track, midiChannel, upperPitch, velocity, offset, duration);
            offset += duration;
            addNoteToTrack(track, midiChannel, midiPitch, velocity, offset, duration);
            offset += duration;
            addNoteToTrack(track, midiChannel, lowerPitch, velocity, offset, duration);
            offset += duration;
            addNoteToTrack(track, midiChannel, midiPitch, velocity, offset, duration);
          } else {
            addNoteToTrack(track, note.getMidiChannel(), midiPitch, velocity, offset, duration);
          }
        }
      } catch (MusicXMLParseException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  private void addNoteToTrack(
    Track track,
    int channel, int pitch, int velocity,
    int tick, int duration
  ) throws InvalidMidiDataException {
    ShortMessage msg = new ShortMessage();
    msg.setMessage(ShortMessage.NOTE_ON, channel, pitch, velocity);
    track.add(new MidiEvent(msg, tick));
    msg = new ShortMessage();
    msg.setMessage(ShortMessage.NOTE_OFF, channel, pitch, 0);
    track.add(new MidiEvent(msg, tick+duration));
  }
  private static int calculatePPQ(int ppq) {
    if (ppq < 5) return ppq * 80;
    else if (ppq < 10) return ppq * 40;
    else if (ppq < 20) return ppq * 20;
    else if (ppq < 40) return ppq * 10;
    else if (ppq < 100) return ppq * 4;
    return ppq;
  }

  private static void initializeMidiPrograms (
    Track track, Part part
  ) throws InvalidMidiDataException {
    MidiInstrument instrument = part.getMidiInstrument(null);
    if (instrument != null) {
      ShortMessage msg = new ShortMessage();
      msg.setMessage(ShortMessage.PROGRAM_CHANGE,
                     instrument.getMidiChannel(), instrument.getMidiProgram(), 0);
      track.add(new MidiEvent(msg, 0));
    }
  }
}
