package org.delysid.freedots;

public class Options {
  int pageWidth = 32;
  int pageHeight = 20;
  String location = null;
  boolean windowSystem = true;
  boolean playScore = false;

  public Options(String[] args) {
    for (int index = 0; index < args.length; index++) {
      String option = args[index];
      if (option.equals("-w") || option.equals("--width")) {
        if (index < args.length-1) {
          pageWidth = Integer.parseInt(args[++index]);
        }
      } else if (option.equals("-h") || option.equals("--height")) {
        if (index < args.length-1) {
          pageHeight = Integer.parseInt(args[++index]);
        }
      } else if ("-nw".equals(option)) {
        windowSystem = false;
      } else if ("-p".equals(option) || "--play".equals(option)) {
        playScore = true;
      } else {
        if (index == args.length-1) {
          location = args[index];
        } else {
          throw new IllegalArgumentException();
        }
      }
    }
  }
  public String getLocation() {
    return location;
  }
  public int getPageHeight() { return pageHeight; }
  public int getPageWidth() { return pageWidth; }
  public boolean getWindowSystem() { return windowSystem; }
  public void setWindowSystem(boolean windowSystem) {
    this.windowSystem = windowSystem;
  }
  public boolean getPlayScore() { return playScore; }
}
