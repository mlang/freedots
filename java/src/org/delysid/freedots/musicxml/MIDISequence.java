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

public class MIDISequence extends javax.sound.midi.Sequence {
  public MIDISequence (Score score) throws InvalidMidiDataException {
    super(PPQ, calculatePPQ(score.getDivisions()));
    for (Part part:score.getParts()) {
      Track track = createTrack();
      int channel = 0;
      int velocity = 64;

      String trackName = new String(part.getName());
      MetaMessage metaMessage = new MetaMessage();
      metaMessage.setMessage(0x03, trackName.getBytes(), trackName.length());
      track.add(new MidiEvent(metaMessage, 0));

      MusicList events = part.getMusicList();

      int round = 1;
      int repeatStartIndex = -1;

      Fraction offset = new Fraction(0, 1);
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);
        if (event instanceof Note)
          addNote(track, (Note)event, channel, velocity, offset);
	else if (event instanceof Chord)
          for (Note note:(Chord)event)
            addNote(track, note, channel, velocity, offset);
        else if (event instanceof StartBar) {
          if (repeatStartIndex == -1) repeatStartIndex = i;
        } else if (event instanceof EndBar) {
          EndBar endbar = (EndBar)event;
          if (endbar.getRepeat()) {
            if (round == 1) {
              offset = endbar.getOffset();
              i = repeatStartIndex;
              round += 1;
            }
          }
        }
      }
    }
  }
  private void addNote(Track track, Note note, int channel, int velocity, Fraction add) {
    if (!note.isGrace()) {
      Pitch pitch = note.getPitch();
      try {
        int offset = note.getOffset().add(add).toInteger(resolution);
        int duration = note.getDuration().toInteger(resolution);
        if (pitch != null) {
          int midiPitch = pitch.getMIDIPitch();
          ShortMessage msg = new ShortMessage();
          msg.setMessage(ShortMessage.NOTE_ON, channel, midiPitch, velocity);
          track.add(new MidiEvent(msg, offset));
          msg = new ShortMessage();
          msg.setMessage(ShortMessage.NOTE_OFF, channel, midiPitch, 0);
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
}
