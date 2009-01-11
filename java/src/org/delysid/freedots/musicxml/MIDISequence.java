/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.delysid.freedots.model.Event;

public class MIDISequence extends javax.sound.midi.Sequence {
  public MIDISequence (Score score) throws InvalidMidiDataException {
    super(PPQ, score.getDivisions());
    for (Part part:score.getParts()) {
      Track track = createTrack();
      int channel = 0;
      int velocity = 64;

      String trackName = new String(part.getName());
      MetaMessage metaMessage = new MetaMessage();
      metaMessage.setMessage(0x03, trackName.getBytes(), trackName.length());
      track.add(new MidiEvent(metaMessage, 0));

      for (Event event:part.getMusicList())
	if (event instanceof Note)
          addNote(track, (Note)event, channel, velocity);
	else if (event instanceof Chord)
          for (Note note:(Chord)event)
            addNote(track, note, channel, velocity);
    }
  }
  private void addNote(Track track, Note note, int channel, int velocity) {
    Pitch pitch = note.getPitch();
    try {
      int offset = note.getOffset().toInteger(resolution);
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
