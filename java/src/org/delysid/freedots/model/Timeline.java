/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class Timeline<E> extends java.util.TreeMap<Fraction, E> {
  Timeline(E initial) { super(); put(new Fraction(0, 1), initial); }
  public E get(Fraction offset) {
    return floorEntry(offset).getValue();
  }
}
