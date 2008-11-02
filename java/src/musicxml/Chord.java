package musicxml;

import java.util.ArrayList;
import java.util.List;

public class Chord {
  private List<Note> notes = new ArrayList<Note>();
  Chord() { }
  Chord(Note initialNote) {
    add(initialNote);
  }
  public void add(Note note) {
    notes.add(note);
  }
}
