package org.delysid.freedots;

public class Options {
  int pageWidth = 32;
  int pageHeight = 20;
  String location = null;
  boolean windowSystem = true;
  public Options(String[] args) {
    for (int i = 0; i<args.length; i++) {
      if (args[i].equals("-w") || args[i].equals("--width")) {
        if (i < args.length-1) {
          pageWidth = Integer.parseInt(args[++i]);
        }
      } else if ("-nw".equals(args[i])) {
        windowSystem = false;
      } else {
        if (i == args.length-1) {
          location = args[i];
        } else {
          throw new IllegalArgumentException();
        }
      }
    }
  }
  public String getLocation() {
    return location;
  }
  public int getPageHeight() {
    return pageHeight;
  }
  public int getPageWidth() {
    return pageWidth;
  }
  public boolean getWindowSystem() {
    return windowSystem;
  }
  public void setWindowSystem(boolean windowSystem) {
    this.windowSystem = windowSystem;
  }
}
