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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import freedots.model.Fingering;
import freedots.musicxml.Note;

@SuppressWarnings("serial")
public final class EditFingeringAction extends AbstractAction {
  private Main gui;
  private FingeringEditor fingeringEditor = null;
  private boolean dialogShowing = false;

  public EditFingeringAction(final Main gui) {
    super("Fingering...");
    this.gui = gui;
    putValue(SHORT_DESCRIPTION, "Edit fingering for this note");
    putValue(ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
  }
  public void actionPerformed(ActionEvent event) {
    Object scoreObject = gui.getCurrentScoreObject();
    if (scoreObject != null && scoreObject instanceof Note) {
      if (fingeringEditor == null) fingeringEditor = new FingeringEditor(gui);
      if (!dialogShowing) {
        fingeringEditor.setNote((Note)scoreObject);
        dialogShowing = true;
        fingeringEditor.show();
        dialogShowing = false;
      }
    }
  }

  class FingeringEditor extends JDialog implements ActionListener {
    private Note note;
    private Main main;

    private JTextField text;
    private JButton okButton, cancelButton;

    public FingeringEditor(Main parent) {
      super(parent, "Fingering", true);
      this.main = parent;

      JPanel fingerPanel = new JPanel(new BorderLayout());
      JLabel label = new JLabel("Fingering: ");
      label.setDisplayedMnemonic(KeyEvent.VK_F);
      text = new JTextField();
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

      okButton = new JButton
        (UIManager.getString("OptionPane.okButtonText",getLocale()));
      okButton.addActionListener(this);

      cancelButton = new JButton
        (UIManager.getString("OptionPane.cancelButtonText",getLocale()));
      cancelButton.addActionListener(this);

      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
      buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
      buttonPane.add(Box.createHorizontalGlue());
      buttonPane.add(cancelButton);
      buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
      buttonPane.add(okButton);

      Container contentPane = getContentPane();
      contentPane.add(fingerPanel, BorderLayout.CENTER);
      contentPane.add(buttonPane, BorderLayout.PAGE_END);

      getRootPane().setDefaultButton(okButton);

      pack();
      setLocationRelativeTo(parent);
    }

    public void setNote(Note note) {
      this.note = note;

      Fingering fingering = note.getFingering();
      String fingerText = "";
      if (fingering != null && fingering.getFingers().size() > 0) {
        for (int i = 0; i < fingering.getFingers().size(); i++) {
          fingerText += fingering.getFingers().get(i).toString();
          if (i < fingering.getFingers().size()-1) fingerText += "-";
        }
      }
      text.setText(fingerText);
      text.requestFocus();
    }

    public void actionPerformed(ActionEvent e) {
      if (okButton == e.getSource()) {
        String fingerText = text.getText();
        Fingering fingering = new Fingering();
        if (fingerText != null && !fingerText.isEmpty()) {
          for (String finger: fingerText.split("-", -1)) {
            if (!finger.isEmpty()) {
              Integer number = Integer.valueOf(finger);
              if (number.intValue() > 5) {
                JOptionPane.showMessageDialog
                  (this, "Number "+number+" is not avalid fingering indicator",
                   "Illegal fingering", JOptionPane.ERROR_MESSAGE);
                text.requestFocus();
                return;
              }
              fingering.getFingers().add(number);
            }
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
}
