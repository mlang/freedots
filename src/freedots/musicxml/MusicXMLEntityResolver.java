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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** A XML {@link org.xml.sax.EntityResolver} to load MusicXML DTDs from the
 * application JAR.
 * <p>
 * This class is used by {@link Score} to avoid the XML parser going out
 * via the network to fetch the MusicXML DTDs referenced in almost all score
 * files.
 */
class MusicXMLEntityResolver implements org.xml.sax.EntityResolver {
  private static final Map<String, String>
  PUBLIC_ID_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {
      {
        put("-//Recordare//DTD MusicXML 0.6b Partwise//EN", "1.0/partwise.dtd");
        put("-//Recordare//DTD MusicXML 0.7b Partwise//EN", "1.0/partwise.dtd");
        put("-//Recordare//DTD MusicXML 1.0 Partwise//EN", "1.0/partwise.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.0 Attributes//EN", "1.0/attributes.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.0 Barline//EN", "1.0/barline.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.0 Direction//EN", "1.0/direction.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.0 Common//EN", "1.0/common.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.0 Identity//EN", "1.0/identity.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.0 Link//EN", "1.0/link.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.0 Note//EN", "1.0/note.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.0 Score//EN", "1.0/score.dtd");
        put("-//Recordare//DTD MusicXML 1.1 Partwise//EN", "1.1/partwise.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.1 Attributes//EN", "1.1/attributes.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.1 Barline//EN", "1.1/barline.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.1 Common//EN", "1.1/common.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.1 Direction//EN", "1.1/direction.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.1 Identity//EN", "1.1/identity.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.1 Layout//EN", "1.1/layout.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.1 Link//EN", "1.1/link.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.1 Note//EN", "1.1/note.dtd");
        put("-//Recordare//ELEMENTS MusicXML 1.1 Score//EN", "1.1/score.dtd");
        put("-//Recordare//DTD MusicXML 2.0 Partwise//EN", "2.0/partwise.dtd");
        put("-//Recordare//ELEMENTS MusicXML 2.0 Common//EN", "2.0/common.mod");
        put("ISO 8879:1986//ENTITIES Added Latin 1//EN//XML", "2.0/isolat1.ent");
        put("ISO 8879:1986//ENTITIES Added Latin 2//EN//XML", "2.0/isolat2.ent");
        put("-//Recordare//ELEMENTS MusicXML 2.0 Layout//EN", "2.0/layout.mod");
        put("-//Recordare//ELEMENTS MusicXML 2.0 Identity//EN", "2.0/identity.mod");
        put("-//Recordare//ELEMENTS MusicXML 2.0 Attributes//EN", "2.0/attributes.mod");
        put("-//Recordare//ELEMENTS MusicXML 2.0 Link//EN", "2.0/link.mod");
        put("-//Recordare//ELEMENTS MusicXML 2.0 Note//EN", "2.0/note.mod");
        put("-//Recordare//ELEMENTS MusicXML 2.0 Barline//EN", "2.0/barline.mod");
        put("-//Recordare//ELEMENTS MusicXML 2.0 Direction//EN", "2.0/direction.mod");
        put("-//Recordare//ELEMENTS MusicXML 2.0 Score//EN", "2.0/score.mod");
        put("-//Recordare//DTD MusicXML 3.0 Partwise//EN", "3.0/partwise.dtd");
        put("-//Recordare//ELEMENTS MusicXML 3.0 Common//EN", "3.0/common.mod");
        put("-//Recordare//ELEMENTS MusicXML 3.0 Layout//EN", "3.0/layout.mod");
        put("-//Recordare//ELEMENTS MusicXML 3.0 Identity//EN", "3.0/identity.mod");
        put("-//Recordare//ELEMENTS MusicXML 3.0 Attributes//EN", "3.0/attributes.mod");
        put("-//Recordare//ELEMENTS MusicXML 3.0 Link//EN", "3.0/link.mod");
        put("-//Recordare//ELEMENTS MusicXML 3.0 Note//EN", "3.0/note.mod");
        put("-//Recordare//ELEMENTS MusicXML 3.0 Barline//EN", "3.0/barline.mod");
        put("-//Recordare//ELEMENTS MusicXML 3.0 Direction//EN", "3.0/direction.mod");
        put("-//Recordare//ELEMENTS MusicXML 3.0 Score//EN", "3.0/score.mod");
      }
    });

  public final InputSource resolveEntity(
    String publicID, String systemID
  ) throws SAXException, IOException {
    String fileName = PUBLIC_ID_MAP.get(publicID);
    if (fileName != null) {
      InputStream inputStream = getClass().getResourceAsStream(fileName);
      if (inputStream != null)
        return new InputSource(inputStream);
    }
    return null;
  }
}
