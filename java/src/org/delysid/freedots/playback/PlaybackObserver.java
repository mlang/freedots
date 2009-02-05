/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.playback;

public interface PlaybackObserver extends java.util.EventListener {
  public void objectPlaying(Object object);
}

