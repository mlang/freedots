package org.delysid.freedots.transcription;

import org.delysid.freedots.model.MusicList;

class PartMeasureInAccord extends FullMeasureInAccord {
  MusicList head = new MusicList();
  MusicList tail = new MusicList();
  public PartMeasureInAccord() { super(); }
  public MusicList getHead() { return head; }
  public void setHead(MusicList head) { this.head = head; }
  public MusicList getTail() { return tail; }
  public void setTail(MusicList tail) { this.tail = tail; }
}

