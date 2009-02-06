/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.playback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

/**
 * Class <code>MetaEventRelay</code> allows to encode object references
 * inside <link>javax.sound.midi.MetaMessage</link> instances.
 * This is used by <link>org.freedots.musicxml.MIDISequence</link> to
 * annotate MIDI sequences with references to the objects responsible
 * for individual MIDI messages.
 *
 * @author Mario Lang
 */
public class MetaEventRelay implements javax.sound.midi.MetaEventListener {
  private int lastId = 0;
  private Map<String, Object> objects = new HashMap<String, Object>();

  private List<PlaybackObserver>
  playbackObservers = new ArrayList<PlaybackObserver>(2);

  private static final int PROPRIETARY = 0X7F;

  /**
   * Create an instance of MetaEventRelay.
   *
   * @param target   initial target to deliver events to
   */
  public MetaEventRelay(PlaybackObserver target) {
    addPlaybackObserver(target);
  }

  /**
   * Add a callback object to the list of observers
   *
   * @param playbackObserver   the observer to add
   */
  public void addPlaybackObserver(PlaybackObserver playbackObserver) {
    playbackObservers.add(playbackObserver);
  }

  /**
   * Create a new MetaMessage which wraps a reference to the provided object
   *
   * @param object  the object reference to return when the MetaMessage
   *                fires inside a Sequencer
   */
  public MetaMessage createMetaMessage(Object object) {
    if (object != null) {
      int id = ++lastId;
      String key = Integer.toString(id);
      objects.put(key, object);

      MetaMessage metaMessage = new MetaMessage();
      try {
        metaMessage.setMessage(PROPRIETARY, key.getBytes(), key.length());
      } catch (InvalidMidiDataException e) {
        return null;
      }
      return metaMessage;
    } else return null;
  }

  /**
   * Implements the MetaEventListener interface
   *
   * @param meta   the MetaMessage to handle
   */
  public void meta(MetaMessage meta) {
    if (meta.getType() == PROPRIETARY) {
      String key = new String(meta.getData());
      if (objects.containsKey(key)) {
        Object object = objects.get(key);

        for (PlaybackObserver observer:playbackObservers)
          observer.objectPlaying(object);
      }
    }
  }
}
