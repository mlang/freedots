package org.delysid.freedots.musicxml;

import java.io.InputStream;

public final class Library {
  public Score loadScore(String resourceName) {
    InputStream inputStream = getClass().getResourceAsStream(resourceName);
    String extension = null;

    int dot = resourceName.lastIndexOf('.');
    if (dot != -1) extension = resourceName.substring(dot + 1);

    Score score = null;
    try {
      score = new Score(inputStream, extension);
    } catch (Exception e) {
      e.printStackTrace();
      score = null;
    }
    if (score == null) System.exit(1);
    return score;
  }
}
