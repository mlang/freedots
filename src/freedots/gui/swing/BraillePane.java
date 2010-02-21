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
package freedots.gui.swing;

import java.awt.Font;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import freedots.braille.BrailleList;
import freedots.braille.BrailleSequence;
import freedots.braille.Sign;
import freedots.gui.SignColorMap;

/** Displays the braille output in a text area.
 * <p>
 * The caret is forced to visible even though instances of this
 * class are not editable by default.
 * <p>
 * Automatic line wrapping is also disabled since this interferes with
 * braille music transcription rules.
 */
final class BraillePane extends javax.swing.JTextPane {
  BraillePane() {
    super();

    setFont(new Font("DejaVu Serif", Font.PLAIN, 14));

    setEditable(false);
    setCaret(new ReadonlyVisibleCaret());
  }

  /** Returns {@code true} if a viewport should always force the width of this
   *  {@link javax.swing.Scrollable} to match the width of the viewport.
   * <p>
   * This is overridden to disable automatic line wrapping.
   */
  @Override public boolean getScrollableTracksViewportWidth() { return false; }

  /** Sets the text of this {@code BraillePane} to the specified content.
   * <p>
   * This creates a model of type {@code StyledDocument}
   * and initializes the model from the given list of braille signs.
   * @param text specifies the content for the new model.
   * @see #getText
   */
  public void setText(final BrailleList text) {
    StyledDocument document =
      (StyledDocument)getEditorKit().createDefaultDocument();

    insertBrailleList(text, document.getStyle(StyleContext.DEFAULT_STYLE),
                      document);
 
    setStyledDocument(document);
    setCaretPosition(0);
  }

  /** Inserts braille signs of the transcription in the document, with
   *  different colors corresponding to each braille sign.
   */
  private void insertBrailleList(BrailleList strings, Style defaut,
                                 StyledDocument document) {
    for (BrailleSequence seq: strings) {
      if (seq instanceof Sign) {
        Sign sign = (Sign)seq;
        Style style = document.addStyle(null, defaut);
        StyleConstants.setForeground(style, SignColorMap.DEFAULT.get(sign));

        try {
          document.insertString(document.getLength(), sign.toString(), style);
        } catch (BadLocationException e) { }
      } else insertBrailleList((BrailleList)seq, defaut, document);
    }
  }

  static class ReadonlyVisibleCaret extends javax.swing.text.DefaultCaret {
    ReadonlyVisibleCaret() { super(); }
    /** Called when the component containing the caret gains
     *  focus.
     * This is overridden to set the caret to visible
     * regardless of the components editable state.
     * @see java.awt.event.FocusListener#focusGained
     */
    @Override public void focusGained(java.awt.event.FocusEvent event) {
      if (getComponent().isEnabled()) {
        setVisible(true);
        setSelectionVisible(true);
      }
    }
  }
}

