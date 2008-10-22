/* -*- c-basic-offset: 2; -*- */

package musicxml;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;

public class MusicXML {
  private Document document;

  public MusicXML(
    String filename
  ) throws ParserConfigurationException,
	   IOException, SAXException, XPathExpressionException {
    File file = new File(filename);
    InputStream inputStream = null;
    String extension = null;

    int dot = filename.lastIndexOf('.');
    if (dot != -1) {
      extension = filename.substring(dot + 1);
    }

    if (file.exists()) {
      inputStream = new FileInputStream(file);
    } else {
      URL url = new URL(filename);
      inputStream = url.openConnection().getInputStream();
    }
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    documentBuilder.setEntityResolver(new MusicXMLEntityResolver());

    if ("mxl".equals(extension)) {
      String zipEntryName = null;
      ZipInputStream zipInputStream = new ZipInputStream(inputStream);
      ZipEntry zipEntry = null;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
	if ("META-INF/container.xml".equals(zipEntry.getName())) {
	  Document container = documentBuilder.parse(getInputSourceFromZipInputStream(zipInputStream));
	  XPath xpath = XPathFactory.newInstance().newXPath();
	  zipEntryName = (String) xpath.evaluate("container/rootfiles/rootfile/@full-path",
						 container,
						 XPathConstants.STRING);
	} else if (zipEntry.getName().equals(zipEntryName)) {
	  document = documentBuilder.parse(getInputSourceFromZipInputStream(zipInputStream));
	}
	zipInputStream.closeEntry();
      }
    } else { /* Plain XML file */
      document = documentBuilder.parse(inputStream);
    }
    document.getDocumentElement().normalize();
  }

  private InputSource getInputSourceFromZipInputStream(
    ZipInputStream zipInputStream
  ) throws IOException {
    BufferedReader reader =
      new BufferedReader(new InputStreamReader(zipInputStream));
    StringBuilder stringBuilder = new StringBuilder();
    String string = null;
    while ((string = reader.readLine()) != null) {
      stringBuilder.append(string + "\n");
    }
    return new InputSource(new StringReader(stringBuilder.toString()));
  }

  public String getScoreType () {
    return document.getDocumentElement().getNodeName();
  }
}


