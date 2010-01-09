/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */

import freedots.Braille;
import freedots.Options;
import freedots.musicxml.Score;
import freedots.transcription.Transcriber;

public class TestTranscription extends junit.framework.TestCase {
  private static final String NL = Transcriber.LINE_SEPARATOR;

  public void testPartMeasureInAccord()
    throws javax.xml.parsers.ParserConfigurationException,
           java.io.IOException,
           org.xml.sax.SAXException,
           javax.xml.xpath.XPathExpressionException {
    Options options = new Options(new String[] { "-w", "40" });
    Score score = new Score("test/pmia-1.xml");
    Transcriber transcriber = new Transcriber(options);
    transcriber.setScore(score);

    final String keyAndTimeSignature = "⠩⠼⠉⠲";
    final String expectedResult = "         " + score.getComposer() + NL
      + NL
      + score.getParts().get(0).getName() + NL
      + keyAndTimeSignature + NL
      + "  " + Braille.rightHandPart
      + "⠨⠳⠃⠳⠁⠉⠐⠖⠇⠊⠄⠾ ⠊⠐⠢⠷⠛⠇⠐⠢⠯⠕" + Braille.doubleBar + NL
      + "  " + Braille.leftHandPart
      + "⠸⠗⠄⠇⠣⠜⠧⠨⠅⠸⠞⠐⠂⠧⠐⠱ ⠸⠟⠄⠣⠜⠧⠨⠅⠸⠎⠐⠂⠧⠐⠱" + Braille.doubleBar + NL
      + NL;
    assertEquals("pmia-1.xml", transcriber.toString(), expectedResult);
  }
  public void testValueAmbiguity()
    throws javax.xml.parsers.ParserConfigurationException,
           java.io.IOException,
           org.xml.sax.SAXException,
           javax.xml.xpath.XPathExpressionException {
    final String mxmlFile = "valueambiguity-1.xml";
    Options options = new Options(new String[] { "-w", "40" });
    Score score = new Score("test/" + mxmlFile);
    Transcriber transcriber = new Transcriber(options);
    transcriber.setScore(score);

    final String keyAndTimeSignature = "⠩⠼⠉⠲";
    final String expectedResult = "         " + score.getComposer() + NL
      + NL
      + score.getParts().get(0).getName() + NL
      + keyAndTimeSignature + NL
      + "  "
      + "⠐⠎⠉⠗⠉⠿⠗⠉⠟⠉⠯⠐⠢⠋⠃" + Braille.valueDistinction + "⠕"
      + Braille.doubleBar + NL
      + NL;
    assertEquals(mxmlFile, transcriber.toString(), expectedResult);
  }

  public void testHarmonyNoChord()
    throws javax.xml.parsers.ParserConfigurationException,
           java.io.IOException,
           org.xml.sax.SAXException,
           javax.xml.xpath.XPathExpressionException {
    final String mxmlFile = "harmony-nc.xml";
    Options options = new Options(new String[] { "-w", "40" });
    Score score = new Score("test/" + mxmlFile);
    Transcriber transcriber = new Transcriber(options);
    transcriber.setScore(score);

    final String keyAndTimeSignature = "⠼⠑⠆";
    final String expectedResult = "                " + score.getMovementTitle() + NL
      + NL
      + keyAndTimeSignature + NL
      + "  "
      + Braille.musicPart + "⠨⠋⠗⠑⠑⠙⠕⠞⠎ ⠨⠋⠗⠑⠑⠙⠕⠞⠎ ⠨⠋⠗⠑⠑⠙⠕⠞⠎"
      + Braille.doubleBar + NL
      + "⠒⠜⠨⠉maj⠼⠛⠨⠁m⠼⠛⠨⠙m⠼⠛⠨⠑m⠼⠛⠨⠋maj⠼⠛ ⠨nc " + NL
      + "⠨⠋maj⠼⠛⠨⠑m⠼⠛⠨⠙m⠼⠛⠨⠑m⠼⠛⠨⠁ " + NL
      + NL;
    assertEquals(mxmlFile, transcriber.toString(), expectedResult);
  }
}
