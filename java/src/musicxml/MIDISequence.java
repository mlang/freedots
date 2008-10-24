/* -*- c-basic-offset: 2; -*- */
package musicxml;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

public class MIDISequence extends Sequence {
  public MIDISequence (MusicXML score) throws InvalidMidiDataException {
    super(Sequence.PPQ, score.getDivisions());
    for (Part part:score.parts()) {
      Track track = createTrack();
      int currentTick = 0;
      String trackName = new String(part.getName());
      MetaMessage metamessage = new MetaMessage();
      metamessage.setMessage(0x03, trackName.getBytes(), trackName.length());
      track.add(new MidiEvent(metamessage, currentTick));
      for (Measure measure:part.measures()) {
      }
    }
  }
}
