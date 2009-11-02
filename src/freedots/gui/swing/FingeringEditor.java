/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
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
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package freedots.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import javax.swing.JButton;
import javax.swing.InputVerifier;

import freedots.model.Fingering;
import freedots.musicxml.Note;

public class FingeringEditor extends JDialog implements ActionListener {
  private Note note;
  private Main main;

  private JTextField text;
  private JButton applyButton, cancelButton;

  public FingeringEditor(Main parent, Note note) {
    super(parent, "Fingering", true);
    this.note = note;
    this.main = parent;
    Fingering fingering = note.getFingering();
    String fingerText = "";
    if (fingering != null && fingering.getFingers().size() > 0) {
      for (int i = 0; i < fingering.getFingers().size(); i++) {
        fingerText += fingering.getFingers().get(i).toString();
        if (i < fingering.getFingers().size()-1) fingerText += "-";
      }
    }
    JPanel fingerPanel = new JPanel(new BorderLayout());
    JLabel label = new JLabel("Fingering: ");
    label.setDisplayedMnemonic(KeyEvent.VK_F);
    text = new JTextField(fingerText);
    InputVerifier verifier = new InputVerifier() {
      public boolean verify(JComponent input) {
        final JTextComponent source = (JTextComponent) input;
        String text = source.getText();
        if (text.length() == 0 || text.matches("[1-5-]+"))
          return true;
        else
          return false;
      }
    };
    text.setInputVerifier(verifier);

    label.setLabelFor(text);
    fingerPanel.add(label, BorderLayout.WEST);
    fingerPanel.add(text, BorderLayout.CENTER);

    applyButton = new JButton("Apply");
    applyButton.addActionListener(this);

    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);

    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(cancelButton);
    buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPane.add(applyButton);

    Container contentPane = getContentPane();
    contentPane.add(fingerPanel, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.PAGE_END);

    getRootPane().setDefaultButton(applyButton);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    pack();
    setLocationRelativeTo(parent);
  }

  public void actionPerformed(ActionEvent e) {
    if(applyButton == e.getSource()) {
      String fingerText = text.getText();
      Fingering fingering = new Fingering();
      if (fingerText != null && !fingerText.isEmpty()) {
        for (String finger: fingerText.split("-", -1)) {
          if (!finger.isEmpty())
            fingering.getFingers().add(Integer.valueOf(finger));
        }
      }
      note.setFingering(fingering);
      setVisible(false);
      main.triggerTranscription();
    } else if(cancelButton == e.getSource()) {
      setVisible(false);
    }
  }
}
