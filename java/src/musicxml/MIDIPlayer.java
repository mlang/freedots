/* -*- c-basic-offset: 2; -*- */
package musicxml;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;

import java.io.File;
import java.io.FileOutputStream;

public class MIDIPlayer {
  private Synthesizer synthesizer;
  private Sequencer sequencer;

  public MIDIPlayer(
    MusicXML score
  ) throws MidiUnavailableException, InvalidMidiDataException {
    sequencer = MidiSystem.getSequencer();
    synthesizer = MidiSystem.getSynthesizer();
    sequencer.open();
    synthesizer.open();
    sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
    sequencer.setSequence(new MIDISequence(score));
    //sequencer.setTempoInBPM(120);
  }

  public void play() {
    sequencer.start();
    while (true) {
      try {
	Thread.sleep(100);
      } catch (Exception e) { }
      if (!sequencer.isRunning()) {
	break;
      }
    }
    sequencer.stop();

    sequencer.close();
    synthesizer.close();
  }
}
