package org.delysid.freedots.musicxml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MusicXMLEntityResolver implements EntityResolver {
  private Map<String, String> map = new HashMap<String, String>() {
    {
      put("-//Recordare//DTD MusicXML 2.0 Partwise//EN", "partwise.dtd");
      put("-//Recordare//ELEMENTS MusicXML 2.0 Common//EN", "common.mod");
      put("ISO 8879:1986//ENTITIES Added Latin 1//EN//XML", "isolat1.ent");
      put("ISO 8879:1986//ENTITIES Added Latin 2//EN//XML", "isolat2.ent");
      put("-//Recordare//ELEMENTS MusicXML 2.0 Layout//EN", "layout.mod");
      put("-//Recordare//ELEMENTS MusicXML 2.0 Identity//EN", "identity.mod");
      put("-//Recordare//ELEMENTS MusicXML 2.0 Attributes//EN",
	  "attributes.mod");
      put("-//Recordare//ELEMENTS MusicXML 2.0 Link//EN", "link.mod");
      put("-//Recordare//ELEMENTS MusicXML 2.0 Note//EN", "note.mod");
      put("-//Recordare//ELEMENTS MusicXML 2.0 Barline//EN", "barline.mod");
      put("-//Recordare//ELEMENTS MusicXML 2.0 Direction//EN",
	  "direction.mod");
      put("-//Recordare//ELEMENTS MusicXML 2.0 Score//EN", "score.mod");
    }
  };

  // Implementation of org.xml.sax.EntityResolver

  /**
   * Describe <code>resolveEntity</code> method here.
   *
   * @param publicID a <code>String</code> value
   * @param systemID a <code>String</code> value
   * @return an <code>InputSource</code> value
   * @exception SAXException if an error occurs
   * @exception IOException if an error occurs
   */
  public final InputSource resolveEntity(
    String publicID, String systemID
  ) throws SAXException, IOException {
    String fileName = map.get(publicID);
    if (fileName != null) {
      InputStream inputStream = getClass().getResourceAsStream(fileName);
      if (inputStream != null)
        return new InputSource(inputStream);
    }
    return null;
  }
}
