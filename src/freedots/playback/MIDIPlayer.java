/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2010 Mario Lang  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details (a copy is included in the LICENSE.txt file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package freedots.playback;

import java.io.Closeable;
import java.io.File;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

/**
 * Class {@code MIDIPlayer} is a composite that coordinates
 * {@link javax.sound.midi.Sequencer} and
 * {@link javax.sound.midi.Synthesizer} to provide MIDI playback.
 *
 * Optionally it can use {@link MetaEventRelay} to resolve object
 * references embedded in {@link freedots.musicxml.MIDISequence}
 * and cause {@link PlaybackObserver} clients to be notified when a
 * certain object is played.
 */
public final class MIDIPlayer implements Closeable {
  public static final int SLEEP_TIME = 250;

  private Synthesizer synthesizer = MidiSystem.getSynthesizer();
  private Sequencer sequencer = MidiSystem.getSequencer();

  public MIDIPlayer(final MetaEventRelay metaEventRelay)
    throws MidiUnavailableException, InvalidMidiDataException,
           MetaEventListeningUnavailableException {
    this();
    if (!sequencer.addMetaEventListener(metaEventRelay)) {
      throw new MetaEventListeningUnavailableException();
    }
  }
  public MIDIPlayer() throws MidiUnavailableException,
                             InvalidMidiDataException {
    sequencer.open();
    synthesizer.open();
    sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
    //sequencer.setTempoInBPM(60);
  }

  /** Load a Soundfont (extension .sf2 or .dls) for use with the Synthesizer.
   * @return true if the Soundfont was loaded successfully, false otherwise.
   */
  public boolean loadSoundbank(File file) {
    try {
      Soundbank soundbank = MidiSystem.getSoundbank(file);
      if (synthesizer.isSoundbankSupported(soundbank)) {
        return synthesizer.loadAllInstruments(soundbank);
      }
    } catch (javax.sound.midi.InvalidMidiDataException exception) {
      return false;
    } catch (java.io.IOException exception) {
      return false;
    }
    return false;
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
