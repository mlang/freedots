/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */

import freedots.Braille;
import freedots.music.Fingering;

import freedots.braille.BrailleFingering;

public class TestBraille extends junit.framework.TestCase {
  public void testFingering() {
    Fingering fingering = new Fingering();

    fingering.getFingers().add(1);

    assertEquals("1st finger", new BrailleFingering(fingering).toString(), "⠁");
  }
}
