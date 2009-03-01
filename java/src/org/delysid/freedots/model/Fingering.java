package org.delysid.freedots.model;

import java.util.ArrayList;
import java.util.List;

import org.delysid.freedots.Braille;

public class Fingering {
  private List<Integer> fingers = new ArrayList<Integer>(1);

  public List<Integer> getFingers() { return fingers; }
  public void setFingers(List<Integer> fingers) { this.fingers = fingers; }

  public String toBrailleString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < fingers.size(); i++) {
      stringBuilder.append(Braille.finger(fingers.get(i)));
      if (i < fingers.size() - 1) {
        stringBuilder.append(Braille.slur.toString());
      }
    }

    return stringBuilder.toString();
  }
}
