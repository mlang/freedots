/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

import java.io.Closeable;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

public final class MIDIPlayer implements Closeable {
  private Synthesizer synthesizer;
  private Sequencer sequencer;
  private boolean isPlaying = false;

  public MIDIPlayer(MetaEventRelay metaEventRelay)
  throws MidiUnavailableException, InvalidMidiDataException {
    this();
    if (!sequencer.addMetaEventListener(metaEventRelay)) {
      System.out.println("Cant add metaeventlistener to sequencer");
    }
  }
  public MIDIPlayer() throws MidiUnavailableException,
			     InvalidMidiDataException {
    sequencer = MidiSystem.getSequencer();
    synthesizer = MidiSystem.getSynthesizer();
    sequencer.open();
    synthesizer.open();
    sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
    //sequencer.setTempoInBPM(120);
    sequencer.addMetaEventListener(new MetaEventListener() {
      public void meta(MetaMessage message) {
        if (message.getType() == 0X2F) isPlaying = false;
      }
    });
  }

  public void setSequence(Sequence sequence) throws InvalidMidiDataException {
    sequencer.setSequence(sequence);
  }
  public void start() {
    sequencer.start();
    isPlaying = true;
  }
  public boolean isRunning() { return sequencer.isRunning() && isPlaying; }
  public void stop() {
    sequencer.stop();
    isPlaying = false;
  }
  public void close() {
    if (isRunning()) stop();
    sequencer.close();
    synthesizer.close();
  }
}
