package org.delysid.freedots.transcription;

import java.util.ArrayList;

class BrailleList extends ArrayList<BrailleString> {
  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (BrailleString brailleString:this)
      stringBuilder.append(brailleString.toString());
    return stringBuilder.toString();
  }
  public int length() { return toString().length(); }
}
