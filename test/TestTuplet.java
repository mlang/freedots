/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */

import freedots.Braille;
import freedots.Options;
import freedots.musicxml.Score;
import freedots.transcription.Transcriber;

public class TestTuplet extends junit.framework.TestCase {
  private static final String NL = System.getProperty("line.separator");
  
  public void testTupletWithoutTupletElement()
    throws javax.xml.parsers.ParserConfigurationException,
           java.io.IOException,
           org.xml.sax.SAXException,
           javax.xml.xpath.XPathExpressionException {
    Options options = new Options(new String[] { "-w", "40" });
    Score score = new Score("test/TupletsWithoutTupletElement.xml");
    Transcriber transcriber = new Transcriber(options);
    transcriber.setScore(score);
    
    final String keyAndTimeSignature = "⠼⠙⠲";
    final String expectedResult =
      score.getParts().get(0).getName() + NL
      + keyAndTimeSignature + NL
      + "  "
      + "⠨⠜⠐⠻⠳⠆⠪⠺⠹⠣⠅" + NL
      + "  "
      + "⠸⠜⠘⠊⠚⠆⠙⠑⠋⠘⠮⠾⠽⠵⠆⠯⠿⠷⠆⠮⠾⠽⠣⠅"
      + NL
      + NL;
    assertEquals("TupletsWithoutTupletElement.xml", transcriber.toString(), expectedResult);
  }
  
  
  
  public void testAmbiguousNestedTuplet()
    throws javax.xml.parsers.ParserConfigurationException,
           java.io.IOException,
           org.xml.sax.SAXException,
           javax.xml.xpath.XPathExpressionException {
    final String mxmlFile = "23d-Tuplets-Nested.xml";
    Options options = new Options(new String[] { "-w", "40" });
    Score score = new Score("test/" + mxmlFile);
    Transcriber transcriber = new Transcriber(options);
    transcriber.setScore(score);

    final String keyAndTimeSignature = "⠼⠃⠲";
    final String expectedResult = 
      keyAndTimeSignature + NL
      + "  "
      + "⠆⠸⠢⠄⠐⠚⠚⠚⠚⠚⠚⠚⠚⠚⠣⠅" + NL
      + NL;
    assertEquals(mxmlFile, transcriber.toString(), expectedResult);
  }
  
  public void testNestedTuplet()
    throws javax.xml.parsers.ParserConfigurationException,
           java.io.IOException,
           org.xml.sax.SAXException,
           javax.xml.xpath.XPathExpressionException {
    final String mxmlFile = "NestedTupletA.xml";
    Options options = new Options(new String[] { "-w", "40" });
    Score score = new Score("test/" + mxmlFile);
    Transcriber transcriber = new Transcriber(options);
    transcriber.setScore(score);
    
    final String keyAndTimeSignature = "⠼⠙⠲";
    final String expectedResult = keyAndTimeSignature + NL
      + "  "
      + "⠨⠹⠆⠋⠉⠜3⠄⠛⠉⠓⠆⠛⠉⠉⠸⠒⠄⠯⠉⠜3⠄⠜3⠄⠿⠉⠯⠉⠑⠹⠣⠅" + NL
      + NL;
    assertEquals(mxmlFile, transcriber.toString(), expectedResult);
  }
}