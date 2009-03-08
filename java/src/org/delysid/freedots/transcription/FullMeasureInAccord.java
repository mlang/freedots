package org.delysid.freedots.transcription;

import java.util.ArrayList;
import java.util.List;

import org.delysid.freedots.model.MusicList;

class FullMeasureInAccord {
  List<MusicList> parts = new ArrayList<MusicList>();
  FullMeasureInAccord() { super(); }
  void addPart(MusicList part) { parts.add(part); }
  List<MusicList> getParts() { return parts; }
  boolean isInAccord() { return parts.size() > 1; }
}
