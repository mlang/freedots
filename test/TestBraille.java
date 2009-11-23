/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */

import freedots.Braille;
import freedots.music.Fingering;

public class TestBraille extends junit.framework.TestCase {
  public void testFingering() {
    Fingering fingering = new Fingering();

    assertEquals("empty", Braille.toString(fingering), "");

    fingering.getFingers().add(1);

    assertEquals("1st finger", Braille.toString(fingering), "⠁");
  }
}
