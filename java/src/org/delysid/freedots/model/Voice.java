/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class Voice extends MusicList {
  String name;

  Voice(String name) {
    super();
    this.name = name;
  }

  public int countEqualsAtBeginning(Voice other) {
    int elementCount = Math.min(this.size(), other.size());
    int index = 0;
    for (index = 0; index < elementCount; index++) {
      if (!this.get(index).equals(other.get(index))) break;
    }
    return index;
  }
  public int countEqualsAtEnd(Voice other) {
    int count = 0;

    // FIXME: Implement it
    return count;
  }
  public boolean restsOnly() {
    for (Event event:this)
      if (event instanceof StaffElement)
        if (!((StaffElement)event).isRest()) return false;
    return true;
  }
  public void swapPosition(Voice other) {
    String oldName = this.name;
    String newName = other.name;
    for (Event event:other) ((VoiceElement)event).setVoiceName(this.name);
    for (Event event:this) ((VoiceElement)event).setVoiceName(other.name);
    this.name = newName;
    other.name = oldName;
  }
}
