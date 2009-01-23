/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class Voice extends MusicList {
  String name;

  Voice(String name) {
    super();
    this.name = name;
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
