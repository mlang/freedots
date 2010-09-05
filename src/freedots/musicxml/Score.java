/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2010 Mario Lang  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details (a copy is included in the LICENSE.txt file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package freedots.musicxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Map;
import java.util.HashMap;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A MusicXML document in score-partwise format.
 */
public final class Score {
  private Document document;

  private static XPathFactory xPathFactory = XPathFactory.newInstance();
  private static DocumentBuilder documentBuilder;
  static {
    DocumentBuilderFactory
    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
      documentBuilder.setEntityResolver(new MusicXMLEntityResolver());
    } catch (ParserConfigurationException e) { e.printStackTrace(); }
  }

  /* --- Header fields --- */
  private Element workNumber, workTitle;
  private Element movementNumber, movementTitle;
  private Element composer, lyricist;
  private Element rights;
  private Element encoding;

  private List<Part> parts;

  public Score(
    final InputStream inputStream, final String extension
  ) throws ParserConfigurationException,
           IOException, SAXException, XPathExpressionException {
    parse(inputStream, extension);
  }

  /** Construct a score object from a URL.
   * @param filenameOrURL is either a local filename or a URL.
   */
  public Score(final String filenameOrURL)
    throws ParserConfigurationException,
           IOException, SAXException, XPathExpressionException
  {
    File file = new File(filenameOrURL);
    InputStream inputStream = null;
    String extension = null;

    int dot = filenameOrURL.lastIndexOf('.');
    if (dot != -1) extension = filenameOrURL.substring(dot + 1);

    if (file.exists()) { /* Local file */
      inputStream = new FileInputStream(file);
    } else {
      URL url = new URL(filenameOrURL);
      inputStream = url.openConnection().getInputStream();
    }

    parse(inputStream, extension);
  }

  private void parse(InputStream inputStream, String extension)
    throws ParserConfigurationException, IOException, SAXException,
           XPathExpressionException {
    Map<String,InputSource> Files = new HashMap<String,InputSource>();
    String zipEntryName = null;
    if ("mxl".equalsIgnoreCase(extension)) {
      ZipInputStream zipInputStream = new ZipInputStream(inputStream);
      ZipEntry zipEntry = null;

      while ((zipEntry = zipInputStream.getNextEntry()) != null) {

        InputSource currentInputSource=getInputSourceFromZipInputStream(zipInputStream);
        Files.put(zipEntry.getName(),currentInputSource);

        if ("META-INF/container.xml".equals(zipEntry.getName())) {
          Document container =
            documentBuilder.parse(currentInputSource);
          XPath xpath = xPathFactory.newXPath();
          zipEntryName =
            (String) xpath.evaluate("container/rootfiles/rootfile/@full-path",
                                    container, XPathConstants.STRING);
        } else if (zipEntry.getName().equals(zipEntryName))
          document = documentBuilder.parse(currentInputSource);
        zipInputStream.closeEntry();
      }
      if (document==null && !(zipEntryName==null)) {
        document = documentBuilder.parse(Files.get(zipEntryName));
      }
    } else {
      document = documentBuilder.parse(inputStream);
    }

    document.getDocumentElement().normalize();

    Element root = document.getDocumentElement();

    assert root.getTagName().equals("score-partwise");

    parts = new ArrayList<Part>();
    Element partList = null;

    for (Node node = root.getFirstChild(); node != null;
         node = node.getNextSibling()) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element scoreElement = (Element)node;

        if ("work".equals(scoreElement.getTagName())) {
          for (Node workNode = scoreElement.getFirstChild(); workNode != null;
               workNode = workNode.getNextSibling()) {
            if (workNode.getNodeType() == Node.ELEMENT_NODE) {
              Element workElement = (Element)workNode;
              if ("work-number".equals(workElement.getTagName())) {
                workNumber = workElement;
              } else if ("work-title".equals(workElement.getTagName())) {
                workTitle = workElement;
              }
            }
          }
        } else if ("movement-title".equals(scoreElement.getTagName())) {
          movementTitle = scoreElement;
        } else if ("movement-number".equals(scoreElement.getTagName())) {
          movementNumber = scoreElement;
        } else if ("identification".equals(scoreElement.getTagName())) {
          for (Node subNode = scoreElement.getFirstChild(); subNode != null;
               subNode = subNode.getNextSibling()) {
            if (subNode.getNodeType() == Node.ELEMENT_NODE) {
              Element identificationElement = (Element)subNode;
              if ("creator".equals(identificationElement.getTagName())) {
                Element creator = identificationElement;
                if (creator.getAttribute("type").equals("composer")) {
                  composer = creator;
                } else if (creator.getAttribute("type").equals("lyricist")) {
                  lyricist = creator;
                }
              } else if ("encoding".equals(identificationElement.getTagName())) { 
                encoding = identificationElement;
              }
            }
          }
        } else if ("part-list".equals(scoreElement.getTagName())) {
          partList = scoreElement;
        } else if ("part".equals(scoreElement.getTagName())) {
          Element part = scoreElement;
          String idValue = part.getAttribute("id");
          Element scorePart = null;
          for (Node partlistNode = partList.getFirstChild();
               partlistNode != null;
               partlistNode = partlistNode.getNextSibling()) {
            if (partlistNode.getNodeType() == Node.ELEMENT_NODE
             && "score-part".equals(partlistNode.getNodeName())) {
              Element sp = (Element)partlistNode;
              if (idValue.equals(sp.getAttribute("id"))) {
                scorePart = sp;
              }
            }
          }
          if (scorePart != null)
            parts.add(new Part(part, scorePart, this));
          else
            throw new RuntimeException("No <score-part> for part " + idValue);
        }
      }
    }
  }

  /** Demarshal this score object back to XML.
   * @param outputStream will be used to serialize the XML to.
   */ 
  public void save(OutputStream outputStream) {
    assert document != null;

    DocumentType docType = document.getDoctype();
    DOMSource domSource = new DOMSource(document);
    StreamResult resultStream = new StreamResult(outputStream);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    try {
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
                                    docType.getPublicId());
      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                                    docType.getSystemId());
      try {
        transformer.transform(domSource, resultStream);
      } catch (javax.xml.transform.TransformerException e) {
        e.printStackTrace();
      }
    } catch (javax.xml.transform.TransformerConfigurationException e) {
      e.printStackTrace();
    }
  }

  /** Gets the content of the work-number element.
   * @return the work-number of this work or {@code null} if it was not
   *         specified.
   */
  public String getWorkNumber() {
    return workNumber != null ? workNumber.getTextContent() : null;
  }
  /** Gets the content of the work-title element.
   * @return the title of this work or {@code null} if it was not specified.
   */
  public String getWorkTitle() {
    return workTitle != null ? workTitle.getTextContent() : null;
  }
  /** Get the content of the movement-number element.
   */
  public String getMovementNumber() {
    return movementNumber != null ? movementNumber.getTextContent() : null;
  }
  /** Get the content of the movement-title element.
   */
  public String getMovementTitle() {
    return movementTitle != null ? movementTitle.getTextContent() : null;
  }
  /** Get the composer (if set) of this score.
   */
  public String getComposer() {
    return composer != null ? composer.getTextContent() : null;
  }
  /** Get the lyricist (if set) of this score.
   */
  public String getLyricist() {
    return lyricist != null ? lyricist.getTextContent() : null;
  }

  private InputSource getInputSourceFromZipInputStream(
    ZipInputStream zipInputStream
  ) throws IOException {
    BufferedReader
    reader = new BufferedReader(new InputStreamReader(zipInputStream));
    StringBuilder stringBuilder = new StringBuilder();
    String string = null;
    while ((string = reader.readLine()) != null)
      stringBuilder.append(string + "\n");
    return new InputSource(new StringReader(stringBuilder.toString()));
  }

  /**
   * Calculate the least common multiple of all divisions elements in the score.
   */
  public int getDivisions() {
    XPath xPath = xPathFactory.newXPath();
    try {
      String xPathExpression = "//attributes/divisions/text()";
      NodeList nodeList = (NodeList) xPath.evaluate(xPathExpression,
                                                    document,
                                                    XPathConstants.NODESET);
      final int count = nodeList.getLength();
      BigInteger result = BigInteger.ONE;
      for (int index = 0; index < count; index++) {
        Node node = nodeList.item(index);
        BigInteger divisions = new BigInteger(new Integer(Math.round(Float.parseFloat(node.getNodeValue()))).toString());
        result = result.multiply(divisions).divide(result.gcd(divisions));
      }
      return result.intValue();
    } catch (XPathExpressionException e) {
      return 0;
    }
  }

  public List<Part> getParts() {
    return parts;
  }

  /** Indicates if the encoding supports a particular MusicXML element.
   * This is recommended for elements like beam, stem, and accidental,
   * where the absence of an element is ambiguous if you do not know if the
   * encoding supports that element.
   */
  public boolean encodingSupports(String elementName) {
    if (encoding != null) {
      for (Node node = encoding.getFirstChild(); node != null;
           node = node.getNextSibling()) {
        if (node.getNodeType() == Node.ELEMENT_NODE &&
            node.getNodeName().equals("supports")) {
          Element supports = (Element)node;
          if (supports.getAttribute("element").equals(elementName)) {
            return supports.getAttribute("type").equals(YES);
          }
        }
      }
    }
    return true;
  }

  /** Indicates if the encoding supports a particular MusicXML attribute
   * of a certain element with a given value.
   * This lets applications communicate, for example, that all system and/or
   * page breaks are contained in the MusicXML document.
   * @param element is the tagName of the {@link org.w3c.dom.Element}
   * @param attribute is the name of the attribute which is queried
   * @param value is true if support and false if absence is queried
   */
  public boolean encodingSupports(String element,
                                  String attribute, boolean value) {
    if (encoding != null) {
      for (Node node = encoding.getFirstChild(); node != null;
           node = node.getNextSibling()) {
        if (node.getNodeType() == Node.ELEMENT_NODE
            && node.getNodeName().equals("supports")) {
          Element supports = (Element)node;
         if (supports.getAttribute("element").equals(element)
              && supports.getAttribute("attribute").equals(attribute)
              && supports.getAttribute("value").equals(YES) == value) {
            return supports.getAttribute("type").equals(YES);
          }
        }
      }
    }
    return true;
  }

  static final String YES = "yes";

  /* --- W3C DOM convenience access utilities --- */

  static Text getTextNode(Element element, String childTagName) {
    NodeList nodeList = element.getElementsByTagName(childTagName);
    if (nodeList.getLength() >= 1) {
      nodeList = nodeList.item(nodeList.getLength()-1).getChildNodes();
      for (int index = 0; index < nodeList.getLength(); index++) {
        Node node = nodeList.item(index);
        if (node.getNodeType() == Node.TEXT_NODE) return (Text)node;
      }
    }
    return null;
  }
}
