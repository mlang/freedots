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
package freedots.musicxml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import freedots.music.AccidentalContext;
import freedots.music.Articulation;
import freedots.music.EndBar;
import freedots.music.Event;
import freedots.music.Fraction;
import freedots.music.GlobalKeyChange;
import freedots.music.KeyChange;
import freedots.music.KeySignature;
import freedots.music.MusicList;
import freedots.music.Ornament;
import freedots.music.StartBar;
import freedots.playback.MetaEventRelay;

/** Converts MusicXML objects ({@link Score}, {@link Note}) to a representation
 *  suitable for consumption by the Java Sound API.
 * <p>
 * As a special extension, {@link MetaEventRelay} can be used to embed
 * references to the underlying objects in the MIDI sequence.
 * @see freedots.playback.PlaybackObserver
 */
public final class MIDISequence extends javax.sound.midi.Sequence {
  private boolean unroll = true;
  /** Some MIDI reading applications do prefer if all tempo changes are bundled
   *  in the first track of the file.
   */
  private Track tempoTrack;
  private MetaEventRelay metaEventRelay;
  private int velocity = 64;
  private Map<Integer, AccidentalContext> accidentalContexts;

  /** Converts a score to its unrolled MIDI representation.
   * @param score is the score to convert to MIDI messages
   * @throws InvalidMidiDataException if the conversion process generated
   *                                  invalid data.
   */
  public MIDISequence(final Score score) throws InvalidMidiDataException {
    this(score, true, null);
  }
  /** Converts a score to MIDI.
   * @param score          is the score to convert to MIDI representation
   * @param unroll         indicates if repeats and alternative endings should
   *                       be honoured
   * @param metaEventRelay factory for object reference meta message creation
   * @throws InvalidMidiDataException upon an unexpected conversion error
   */
  public MIDISequence(final Score score, final boolean unroll,
                      final MetaEventRelay metaEventRelay)
    throws InvalidMidiDataException {
    super(PPQ, calculatePPQ(score.getDivisions()));
    this.unroll = unroll;
    this.metaEventRelay = metaEventRelay;

    tempoTrack = createTrack();
    for (Part part: score.getParts()) createTrack(part);
  }
  /** Converts a single note to MIDI.
   * @param note is the note object to convert
   * @throws InvalidMidiDataException if the conversion unexpectedly failed
   */
  public MIDISequence(final Note note) throws InvalidMidiDataException {
    super(PPQ, calculatePPQ(note.getPart().getScore().getDivisions()));
    Track track = createTrack();

    initializeMidiPrograms(track, note.getPart());

    addToTrack(track, note, note.getOffset().negate());
  }

  private Track createTrack(Part part) throws InvalidMidiDataException {
    Track track = createTrack();

    MetaMessage metaMessage;
    velocity = 64;

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
        accidentalContexts.put(new Integer(i),
                               new AccidentalContext(part.getKeySignature()));
      }
    }

    int round = 1;
    int repeatStartIndex = -1;

    Fraction offset = Fraction.ZERO;
    for (int i = 0; i < events.size(); i++) {
      Event event = events.get(i);
      if (event instanceof Direction) {
        Direction direction = (Direction)event;
        if (direction.getSound() != null) event = direction.getSound();
        if (direction.isPedalPress()) {
          ShortMessage msg = new ShortMessage();
          msg.setMessage(ShortMessage.CONTROL_CHANGE,
                         0/*FIXME*/, 64, 127);
          track.add(new MidiEvent(msg, direction.getOffset().add(offset).toInteger(resolution)));
        } else if (direction.isPedalRelease()) {
          ShortMessage msg = new ShortMessage();
          msg.setMessage(ShortMessage.CONTROL_CHANGE,
                         0/*FIXME*/, 64, 0);
          track.add(new MidiEvent(msg, direction.getOffset().add(offset).toInteger(resolution)));
        }
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
        addToTrack(track, (Note)event, offset);
      } else if (event instanceof Chord) {
        MetaEventRelay temp = null;
        if (metaEventRelay != null) {
          int midiTick = event.getOffset().add(offset).toInteger(resolution);
          metaMessage = metaEventRelay.createMetaMessage((Chord)event);
          track.add(new MidiEvent(metaMessage, midiTick));
          temp = metaEventRelay;
          metaEventRelay = null; // Only notify once for a chord
        }
        for (Note note: (Chord)event) addToTrack(track, note, offset);
        if (temp != null) metaEventRelay = temp;
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
        if (unroll) {
          if (startBar.getRepeatForward()) {
            repeatStartIndex = i;
            round = 1;
          }
          if (startBar.getEndingStart() > 0
              && startBar.getEndingStart() != round) { /* skip to EndBar */
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
        }
      } else if (event instanceof EndBar) {
        EndBar endbar = (EndBar)event;
        if (unroll && endbar.getRepeat()) {
          if (round == 1) {
            StartBar repeatStart = (StartBar)events.get(repeatStartIndex);
            offset = offset.add(endbar.getOffset().subtract(repeatStart.getOffset()));
            i = repeatStartIndex;
            round += 1;
          }
        }
      }
    }

    return track;
  }
  private void addToTrack(Track track, Note note, Fraction add)
    throws InvalidMidiDataException {
    if (!note.isGrace()) {
      Pitch pitch = note.getPitch();
      int offset = note.getOffset().add(add).toInteger(resolution);
      int duration = note.getDuration().toInteger(resolution);
      Set<Articulation> articulations = note.getArticulations();
      if (articulations.contains(Articulation.staccatissimo)) {
        duration /= 4;
      } else if (articulations.contains(Articulation.staccato)) {
        duration /= 2;
      } else if (articulations.contains(Articulation.mezzoStaccato)) {
        duration -= duration / 4;
      }
      Set<Ornament> ornaments = note.getOrnaments();
      if (metaEventRelay != null) {
        MetaMessage metaMessage = metaEventRelay.createMetaMessage(note);
        if (metaMessage != null) {
          track.add(new MidiEvent(metaMessage, offset));
        }
      }
      if (pitch != null) {
        if (accidentalContexts != null) {
          accidentalContexts.get(note.getStaffNumber())
            .accept(pitch, note.getAccidental());
        }
        final int midiPitch = pitch.getMIDIPitch();
        final int midiChannel = note.getMidiChannel();
        if (accidentalContexts != null) {
          Integer staffNumber = new Integer(note.getStaffNumber());
          AccidentalContext accidentalContext =
            accidentalContexts.get(staffNumber);
          if (ornaments.contains(Ornament.turn)) {
            final int upperPitch = pitch.nextStep(accidentalContext).getMIDIPitch();
            final int lowerPitch = pitch.previousStep(accidentalContext).getMIDIPitch();
            duration /= 4;
            noteOnOff(track, midiChannel, upperPitch, velocity,
                      offset, duration);
            offset += duration;
            noteOnOff(track, midiChannel, midiPitch, velocity,
                      offset, duration);
            offset += duration;
            noteOnOff(track, midiChannel, lowerPitch, velocity,
                      offset, duration);
            offset += duration;
            noteOnOff(track, midiChannel, midiPitch, velocity,
                      offset, duration);
            return;
          } else if (ornaments.contains(Ornament.mordent)) {
            final int lowerPitch = pitch.previousStep(accidentalContext).getMIDIPitch();
            duration /= 8;
            noteOnOff(track, midiChannel, midiPitch, velocity,
                      offset, duration);
            offset += duration;
            noteOnOff(track, midiChannel, lowerPitch, velocity,
                      offset, duration);
            offset += duration;
            noteOnOff(track, midiChannel, midiPitch, velocity,
                      offset, duration*6);
            return;
          }
        }

        noteOnOff(track, note.getMidiChannel(), midiPitch, velocity,
                  offset, duration);
      }
    }
  }
  private static void noteOnOff(Track track,
                                int channel, int pitch, int velocity,
                                int tick, int duration)
    throws InvalidMidiDataException {
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

  private static void initializeMidiPrograms(Track track, Part part)
    throws InvalidMidiDataException {
    MidiInstrument instrument = part.getMidiInstrument(null);
    if (instrument != null) {
      ShortMessage msg = new ShortMessage();
      msg.setMessage(ShortMessage.PROGRAM_CHANGE,
                     instrument.getMidiChannel(), instrument.getMidiProgram(),
                     0);
      track.add(new MidiEvent(msg, 0));
    }
  }
}
