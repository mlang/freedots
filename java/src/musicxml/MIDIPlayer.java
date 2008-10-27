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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;

public class MIDIPlayer implements Closeable {
  private Synthesizer synthesizer;
  private Sequencer sequencer;

  public MIDIPlayer() throws MidiUnavailableException,
			     InvalidMidiDataException {
    sequencer = MidiSystem.getSequencer();
    synthesizer = MidiSystem.getSynthesizer();
    sequencer.open();
    synthesizer.open();
    sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
    //sequencer.setTempoInBPM(120);
  }

  public void setSequence(Sequence sequence) throws InvalidMidiDataException {
    sequencer.setSequence(sequence);
  }
  public void start() {
    sequencer.start();
  }    
  public boolean isRunning() { return sequencer.isRunning(); }
  public void stop() {
    sequencer.stop();
  }
  public void close() {
    sequencer.close();
    synthesizer.close();
  }
}
