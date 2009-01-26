/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public interface RhythmicElement extends VoiceElement {
  public AugmentedFraction getAugmentedFraction();
  public AbstractPitch getPitch();
}
