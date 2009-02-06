/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.playback;

import java.io.Closeable;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

/**
 * Class <code>MIDIPlayer</code> is a composite that coordinates
 * <link>javax.sound.midi.Sequencer</link> and
 * <link>javax.sound.midi.Synthesizer</link> to provide MIDI playback.
 *
 * Optionally it can use <link>MetaEventRelay</link> to resolve object
 * references embedded in <link>org.delysid.freedots.musicxml.MIDISequence</link>
 * and cause <link>PlaybackObserver</link> clients to be notified when a
 * certain object is played.
 */
public final class MIDIPlayer implements Closeable {
  private Synthesizer synthesizer;
  private Sequencer sequencer;

  public MIDIPlayer(MetaEventRelay metaEventRelay)
  throws MidiUnavailableException, InvalidMidiDataException,
         MetaEventListeningUnavailableException {
    this();
    if (!sequencer.addMetaEventListener(metaEventRelay)) {
      throw new MetaEventListeningUnavailableException();
    }
  }
  public MIDIPlayer() throws MidiUnavailableException,
			     InvalidMidiDataException {
    sequencer = MidiSystem.getSequencer();
    synthesizer = MidiSystem.getSynthesizer();
    sequencer.open();
    synthesizer.open();
    sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
    sequencer.setTempoInBPM(60);
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
    if (isRunning()) stop();
    sequencer.close();
    synthesizer.close();
  }
}
