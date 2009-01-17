package org.delysid.freedots;

public class Options {
  int pageWidth = 32;
  int pageHeight = 20;
  MultiStaffMeasures multiStaffMeasures = MultiStaffMeasures.VISUAL;

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
      } else if ("-msm".equals(option)) {
        if (index < args.length-1) {
          String arg = args[++index];
          if ("visual".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.VISUAL;
          else if ("2".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.TWO;
          else if ("3".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.THREE;
          else if ("4".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.FOUR;
          else if ("5".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.FIVE;
          else if ("6".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.SIX;
          else if ("7".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.SEVEN;
          else if ("8".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.EIGHT;
          else if ("9".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.NINE;
          else if ("10".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.TEN;
          else if ("11".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.ELEVEN;
          else if ("12".equals(arg))
            multiStaffMeasures = MultiStaffMeasures.TWELVE;
          else
            throw new IllegalArgumentException("'-msm "+arg+"'");
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

  public enum MultiStaffMeasures {
    VISUAL, /* break at new system */
    TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELVE;
  }
}
