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

public class MIDIPlayer {
  private Synthesizer synthesizer;
  private Sequencer sequencer;
  private Transmitter transmitter;
  private Receiver receiver;
  private Sequence sequence;

  public MIDIPlayer(
    MusicXML score
  ) throws MidiUnavailableException, InvalidMidiDataException {
    sequencer = MidiSystem.getSequencer();
    synthesizer = MidiSystem.getSynthesizer();
    transmitter = sequencer.getTransmitter();
    receiver = synthesizer.getReceiver();
    sequencer.open();
    synthesizer.open();
    transmitter.setReceiver(receiver);

    sequence = new MIDISequence(score);
    sequencer.setSequence(sequence);
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

  public static void main(String[] args) {
    try {
      MIDIPlayer player = new MIDIPlayer(new MusicXML(args[0]));
      player.play();
    } catch (MidiUnavailableException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(2);
    }
  }
}

