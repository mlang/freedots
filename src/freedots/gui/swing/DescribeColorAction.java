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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** Pops up a dialog to describe the different colors used for the various
 *  signs.
 */
public final class DescribeColorAction extends javax.swing.AbstractAction {
  private final Main gui;
  private ColorLegend colorLegend;
 
  /** Flag to avoid firing up several message dialogs.
   */
  private boolean dialogShowing = false;

  public DescribeColorAction(final Main gui) {
    super("Describe colors...");
    this.gui = gui;
    //putValue(SHORT_DESCRIPTION, "Describe the sign at caret position");
    //putValue(ACCELERATOR_KEY,
            // KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
  }
  public void actionPerformed(ActionEvent event) {
    final String title = "Description of the colors used";
    if (colorLegend == null) colorLegend = new ColorLegend(null, title, false);
    if (!dialogShowing) {
      dialogShowing = true;
      colorLegend.setVisible(true);   
      dialogShowing = false;
    }
  }
}

class ColorLegend extends JDialog {

  public ColorLegend(JFrame parent, String title, boolean modal) {
    super(parent, title, modal);
    this.setSize(550, 270);
    this.setLocationRelativeTo(null);
    this.setResizable(true);
    this.initComponent();
  }

  private void initComponent() {
    JPanel panAccidental = new JPanel();
    panAccidental.setBackground(Color.orange);
    panAccidental.setPreferredSize(new Dimension(100, 50));
    panAccidental.setBorder(BorderFactory.createTitledBorder("Accidental"));

    JPanel panFinger = new JPanel();
    panFinger.setBackground(Color.yellow);
    panFinger.setPreferredSize(new Dimension(100, 50));
    panFinger.setBorder(BorderFactory.createTitledBorder("Fingering"));

    JPanel panAccents = new JPanel();
    panAccents.setBackground(new Color(0, 250, 154));
    panAccents.setPreferredSize(new Dimension(100, 50));
    panAccents.setBorder(BorderFactory.createTitledBorder("Accents"));

    JPanel panDot = new JPanel();
    panDot.setBackground(Color.cyan);
    panDot.setPreferredSize(new Dimension(100, 50));
    panDot.setBorder(BorderFactory.createTitledBorder("Dot"));

    JPanel panBarSigns = new JPanel();
    panBarSigns.setBackground(Color.red);
    panBarSigns.setPreferredSize(new Dimension(100, 50));
    panBarSigns.setBorder(BorderFactory.createTitledBorder("Bar Signs"));

    JPanel panOctave = new JPanel();
    panOctave.setBackground(Color.pink);
    panOctave.setPreferredSize(new Dimension(100, 50));
    panOctave.setBorder(BorderFactory.createTitledBorder("Octave sign"));

    JPanel panPitch = new JPanel();
    panPitch.setBackground(Color.blue);
    panPitch.setPreferredSize(new Dimension(100, 50));
    panPitch.setBorder(BorderFactory.createTitledBorder("Pitch and value"));

    JPanel panRest = new JPanel();
    panRest.setBackground(Color.cyan);
    panRest.setPreferredSize(new Dimension(100, 50));
    panRest.setBorder(BorderFactory.createTitledBorder("Rest signs"));

    JPanel panSlur = new JPanel();
    panSlur.setBackground(Color.LIGHT_GRAY);
    panSlur.setPreferredSize(new Dimension(100, 50));
    panSlur.setBorder(BorderFactory.createTitledBorder("Slur and tie"));

    JPanel panText = new JPanel();
    panText.setBackground(new Color(160, 82, 45));
    panText.setPreferredSize(new Dimension(100, 50));
    panText.setBorder(BorderFactory.createTitledBorder("Text"));

    JPanel content = new JPanel();
    content.setBackground(Color.white);
    content.add(panAccidental);
    content.add(panFinger);
    content.add(panAccents);
    content.add(panDot);
    content.add(panBarSigns);
    content.add(panOctave);
    content.add(panPitch);
    content.add(panRest);
    content.add(panSlur);
    content.add(panText);

    this.getContentPane().add(content, BorderLayout.CENTER);
  }
}
