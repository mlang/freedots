/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class Slur<T extends RhythmicElement> extends java.util.ArrayList<T> {
  public Slur(T initialNote) {
    super(3);
    add(initialNote);
  }

  public boolean lastNote(T note) {
    return indexOf(note) == size() - 1;
  }
}
