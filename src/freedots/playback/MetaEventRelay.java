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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

/** Encodes object references inside {@link javax.sound.midi.MetaMessage}
 *  instances.
 * This is used by {@link freedots.musicxml.MIDISequence} to
 * annotate MIDI sequences with references to the objects responsible
 * for individual MIDI messages.
 *
 * @author Mario Lang
 */
public final class MetaEventRelay
  implements javax.sound.midi.MetaEventListener {
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
  public MetaEventRelay(final PlaybackObserver target) {
    addPlaybackObserver(target);
  }

  /** Add a callback object to the list of observers.
   * @param playbackObserver is the observer to notify upon object playback
   */
  public void addPlaybackObserver(final PlaybackObserver playbackObserver) {
    playbackObservers.add(playbackObserver);
  }

  /**
   * Create a new MetaMessage which wraps a reference to the specified object.
   * @param object  the object to return when the MetaMessage fires
   * @return the MetaMessage
   */
  public MetaMessage createMetaMessage(Object object)
  throws InvalidMidiDataException {
    if (object != null) {
      final int id = ++lastId;
      final String key = Integer.toString(id);
      objects.put(key, object);

      MetaMessage metaMessage = new MetaMessage();
      metaMessage.setMessage(PROPRIETARY, key.getBytes(), key.length());

      return metaMessage;
    }
    throw new NullPointerException();
  }

  /**
   * Implements the MetaEventListener interface.
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
