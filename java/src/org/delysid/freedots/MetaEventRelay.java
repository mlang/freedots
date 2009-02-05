package org.delysid.freedots;

import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;

public class MetaEventRelay implements MetaEventListener {
  private Map<String, Object> objects = new HashMap<String, Object>();
  private int lastId = 0;
  private PlaybackObserver playbackObserver;
  public MetaEventRelay(PlaybackObserver target) {
    playbackObserver = target;
  }
  public MetaMessage add(Object object) {
    int id = ++lastId;
    String key = Integer.toString(id);
    objects.put(key, object);
    MetaMessage metaMessage = new MetaMessage();
    try {
      metaMessage.setMessage(0x7F, key.getBytes(), key.length());
    } catch (InvalidMidiDataException e) {
      return null;
    }
    return metaMessage;
  }
  public void meta(MetaMessage meta) {
    if (meta.getType() == 0X7F) {
      String key = new String(meta.getData());
      if (objects.containsKey(key)) {
        Object object = objects.get(key);
        if (playbackObserver != null) playbackObserver.objectPlaying(object);
      }
    }
  }
}
