/* -*- c-basic-offset: 2; -*- */
package musicxml;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class MIDISequence extends javax.sound.midi.Sequence {
  public MIDISequence (MusicXML score) throws InvalidMidiDataException {
    super(PPQ, score.getDivisions());
    for (Part part:score.parts()) {
      Track track = createTrack();
      int currentTick = 0;
      int channel = 0;
      int velocity = 64;

      String trackName = new String(part.getName());
      MetaMessage metaMessage = new MetaMessage();
      metaMessage.setMessage(0x03, trackName.getBytes(), trackName.length());
      track.add(new MidiEvent(metaMessage, currentTick));

      for (Measure measure:part.measures()) {
	for (Musicdata musicdata:measure.musicdata()) {
	  if ("note".equals(musicdata.getNodeName())) {
	    Note note = (Note)musicdata;
	    Pitch pitch = note.getPitch();
	    try {
	      int duration = note.getDuration();
	      if (pitch != null) {
		int midiPitch = pitch.getMIDIPitch();
		ShortMessage msg = new ShortMessage();
		msg.setMessage(ShortMessage.NOTE_ON,
			       channel, midiPitch, velocity);
		track.add(new MidiEvent(msg, currentTick));
		msg = new ShortMessage();
		msg.setMessage(ShortMessage.NOTE_OFF,
			       channel, midiPitch, 0);
		track.add(new MidiEvent(msg, currentTick+duration));
	      }
	      currentTick += duration;
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }
	}
      }
    }
  }
}
