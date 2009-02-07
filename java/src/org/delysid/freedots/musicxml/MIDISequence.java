/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.delysid.freedots.model.EndBar;
import org.delysid.freedots.model.Event;
import org.delysid.freedots.model.Fraction;
import org.delysid.freedots.model.MusicList;
import org.delysid.freedots.model.StartBar;
import org.delysid.freedots.playback.MetaEventRelay;

public class MIDISequence extends javax.sound.midi.Sequence {
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
  public MIDISequence (Score score, MetaEventRelay metaEventRelay)
  throws InvalidMidiDataException {
    super(PPQ, calculatePPQ(score.getDivisions()));

    for (Part part:score.getParts()) {
      Track track = createTrack();
      int velocity = 64;

      String trackName = new String(part.getName());
      MetaMessage metaMessage = new MetaMessage();
      metaMessage.setMessage(0x03, trackName.getBytes(), trackName.length());
      track.add(new MidiEvent(metaMessage, 0));

      initializeMidiPrograms(track, part);

      MusicList events = part.getMusicList();

      int round = 1;
      int repeatStartIndex = -1;

      Fraction offset = new Fraction(0, 1);
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);
        if (event instanceof Note) {
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
        } else if (event instanceof StartBar) {
          if (repeatStartIndex == -1) repeatStartIndex = i;

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

  private void addToTrack(Track track,
                          Note note, int velocity,
                          Fraction add, MetaEventRelay metaEventRelay) {
    if (!note.isGrace()) {
      Pitch pitch = note.getPitch();
      try {
        int offset = note.getOffset().add(add).toInteger(resolution);
        int duration = note.getDuration().toInteger(resolution);
        if (metaEventRelay != null) {
          MetaMessage metaMessage = metaEventRelay.createMetaMessage(note);
          if (metaMessage != null) {
            track.add(new MidiEvent(metaMessage, offset));
          }
        }
        if (pitch != null) {
          int midiPitch = pitch.getMIDIPitch();
          ShortMessage msg = new ShortMessage();
          msg.setMessage(ShortMessage.NOTE_ON, note.getMidiChannel(), midiPitch, velocity);
          track.add(new MidiEvent(msg, offset));
          msg = new ShortMessage();
          msg.setMessage(ShortMessage.NOTE_OFF, note.getMidiChannel(), midiPitch, 0);
          track.add(new MidiEvent(msg, offset+duration));
        }
      } catch (MusicXMLParseException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
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
                     instrument.getMidiChannel(), instrument.getMidiProgram(), 0);
      track.add(new MidiEvent(msg, 0));
    }
  }
}
