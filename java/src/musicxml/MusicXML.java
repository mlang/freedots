package musicxml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.w3c.dom.*;


public class MusicXML {
  private Document document;

  public MusicXML(
    String filename
  ) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    documentBuilder.setEntityResolver(new MusicXMLEntityResolver());

    File file = new File(filename);
    document = documentBuilder.parse(file);
    document.getDocumentElement().normalize();
  }

  public String getScoreType () {
    return document.getDocumentElement().getNodeName();
  }

  public static void main(String argv[]) {
    try {
      if (argv.length < 1 || argv.length > 1) {
        System.err.println("Usage: java MusicXML filename.xml");
        System.exit(1);
      }
      MusicXML score = new MusicXML(argv[0]);
      System.out.println("Root element " + score.getScoreType());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}


