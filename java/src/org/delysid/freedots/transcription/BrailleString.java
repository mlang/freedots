package org.delysid.freedots.transcription;

class BrailleString {
  Object model = null;
  String string;
  BrailleString(String string) {
    this.string = string;
  }
  BrailleString(String string, Object model) {
    this(string);
    this.model = model;
  }
  Object getModel() { return model; }
  @Override
  public String toString() { return string; }
  public int length() { return toString().length(); }
}
