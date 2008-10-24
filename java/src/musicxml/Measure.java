/* -*- c-basic-offset: 2; -*- */
package musicxml;

import org.w3c.dom.Element;

public class Measure {
  private Part parentPart;
  private Element measure;

  public Measure(Element measure, Part part) {
    this.measure = measure;
    this.parentPart = part;
  }

  public String getNumber() {
    return measure.getAttribute("number");
  }
}
