/* -*- c-basic-offset: 2; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2009 Mario Lang  All Rights Reserved.
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
 * This software is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots.musicxml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MusicXMLEntityResolver implements org.xml.sax.EntityResolver {
  private static Map<String, String> map = new HashMap<String, String>() {
    {
      put("-//Recordare//DTD MusicXML 1.0 Partwise//EN", "partwise.dtd");
      put("-//Recordare//DTD MusicXML 1.1 Partwise//EN", "partwise.dtd");
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
