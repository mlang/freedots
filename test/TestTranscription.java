/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */

import freedots.Braille;
import freedots.Options;
import freedots.musicxml.Score;
import freedots.transcription.Transcriber;

public class TestTranscription extends junit.framework.TestCase {
  public void testPartMeasureInAccord()
    throws javax.xml.parsers.ParserConfigurationException,
           java.io.IOException,
           org.xml.sax.SAXException,
           javax.xml.xpath.XPathExpressionException {
    Options options = new Options(new String[] { "-w", "40" });
    Score score = new Score("test/pmia-1.xml");
    Transcriber transcriber = new Transcriber(score, options);

    final String nl = Transcriber.lineSeparator;
    final String keyAndTimeSignature = "⠩⠼⠉⠲";
    final String expectedResult = "         " + score.getComposer() + nl
      + nl
      + score.getParts().get(0).getName() + nl
      + keyAndTimeSignature + nl
      + "  " + Braille.rightHandPart
      + "⠨⠳⠃⠳⠁⠉⠐⠖⠇⠊⠄⠾ ⠊⠐⠢⠷⠛⠇⠐⠢⠯⠕" + Braille.doubleBar + nl
      + "  " + Braille.leftHandPart
      + "⠸⠗⠄⠇⠣⠜⠧⠨⠅⠸⠞⠐⠂⠧⠐⠱ ⠸⠟⠄⠣⠜⠧⠨⠅⠸⠎⠐⠂⠧⠐⠱" + Braille.doubleBar + nl
      + nl;
    assertEquals("pmia-1.xml", transcriber.toString(), expectedResult);
  }
  public void testValueAmbiguity()
    throws javax.xml.parsers.ParserConfigurationException,
           java.io.IOException,
           org.xml.sax.SAXException,
           javax.xml.xpath.XPathExpressionException {
    Options options = new Options(new String[] { "-w", "40" });
    Score score = new Score("test/valueambiguity-1.xml");
    Transcriber transcriber = new Transcriber(score, options);

    final String nl = Transcriber.lineSeparator;
    final String keyAndTimeSignature = "⠩⠼⠉⠲";
    final String expectedResult = "         " + score.getComposer() + nl
      + nl
      + score.getParts().get(0).getName() + nl
      + keyAndTimeSignature + nl
      + "  "
      + "⠐⠎⠉⠗⠉⠿⠗⠉⠟⠉⠯⠐⠢⠋⠃" + Braille.valueDistinction + "⠕"
      + Braille.doubleBar + nl
      + nl;
    assertEquals("pmia-1.xml", transcriber.toString(), expectedResult);
  }
}
