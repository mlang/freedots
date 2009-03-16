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
package org.delysid.freedots.gui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;

import org.delysid.freedots.musicxml.Library;
import org.delysid.freedots.musicxml.MIDISequence;
import org.delysid.freedots.musicxml.Note;
import org.delysid.freedots.musicxml.Score;
import org.delysid.freedots.playback.MIDIPlayer;
import org.delysid.freedots.playback.MetaEventRelay;
import org.delysid.freedots.playback.MetaEventListeningUnavailableException;
import org.delysid.freedots.transcription.Transcriber;

public final class Main
  extends JFrame
  implements javax.swing.event.CaretListener,
             org.delysid.freedots.gui.GraphicalUserInterface,
             org.delysid.freedots.playback.PlaybackObserver {
  
  protected Score score = null;
  public Score getScore() { return score; }

  protected Transcriber transcriber = null;
  protected StatusBar statusBar=null;

  public void setScore(Score score) {
    this.score = score;
    try {
      transcriber.setScore(score);
      textArea.setText(transcriber.toString());
      textArea.setCaretPosition(0);
      boolean scoreAvailable = score != null;
      fileSaveAsMidiAction.setEnabled(scoreAvailable);
      playScoreAction.setEnabled(scoreAvailable);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected JTextArea textArea;
  Object lastObject = null;

  boolean autoPlay = false;
  boolean caretFollowsPlayback = true;

  public void caretUpdate(CaretEvent caretEvent) {
    int index = caretEvent.getDot();
    Object object = null;
    if (transcriber != null) {
      object = transcriber.getObjectAtIndex(index);
    }
    if (object != lastObject) {
      if (object != null) {
        if (statusBar != null){
          statusBar.setMessage("At index "+index+" there is "+object.toString());
        }
        	
        if (autoPlay && object instanceof Note) {
          Note note = (Note)object;
          midiPlayer.stop();
          try {
            MIDISequence sequence = new MIDISequence(note);
            midiPlayer.setSequence(sequence);
            midiPlayer.start();
          } catch (javax.sound.midi.InvalidMidiDataException e) {
            e.printStackTrace();
          }
        }
      }
      lastObject = object;
    }
  }

  protected MIDIPlayer midiPlayer;
  protected MetaEventRelay metaEventRelay = new MetaEventRelay(this);
  public void objectPlaying(Object object) {
    if (caretFollowsPlayback) {
      int pos = transcriber.getIndexOfObject(object);
      if (pos >= 0) {
        boolean old = autoPlay;
        autoPlay = false;
        textArea.setCaretPosition(pos);
        autoPlay = old;
      }
    }
  }

  public boolean startPlayback() {
    if (score != null)
      try {
        midiPlayer.setSequence(new MIDISequence(score, metaEventRelay));
        midiPlayer.start();
        return true;
      } catch (javax.sound.midi.InvalidMidiDataException exception) {
        exception.printStackTrace();
      }

    return false;
  }
  public void stopPlayback() {
    if (midiPlayer != null) midiPlayer.stop();
  }

  private Action playScoreAction = new PlayScoreAction(this);
  private Action fileSaveAsMidiAction = new FileSaveAsMidiAction(this);

  public Main(Transcriber transcriber) {
    super("FreeDots");
    this.transcriber = transcriber;

    try {
      MIDIPlayer player = new MIDIPlayer(metaEventRelay);
      midiPlayer = player;
    } catch (MidiUnavailableException e) {
      String message = "MIDI playback unavailable";
      JOptionPane.showMessageDialog(this, message, "Alert",
                                    JOptionPane.ERROR_MESSAGE);
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      e.printStackTrace();
    } catch (MetaEventListeningUnavailableException e) {
      e.printStackTrace();
    }

    /* Load a font capable of displaying unicode braille */
    try {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      ge.registerFont(Font.createFont(Font.TRUETYPE_FONT,
				      getClass().getResourceAsStream("DejaVuSerif.ttf")));
    } catch (java.io.IOException ioe) {
      ioe.printStackTrace();
    } catch (FontFormatException ffe) {
      ffe.printStackTrace();
    }

    // Create the menubar.
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenu playbackMenu = new JMenu("Playback");
    
    fileMenu.setMnemonic(KeyEvent.VK_F);

    Action openAction = new FileOpenAction(this);
    fileMenu.add(openAction);

    playbackMenu.add(playScoreAction);
    playbackMenu.add(new StopPlaybackAction(this));

    JCheckBoxMenuItem autoPlayItem = new JCheckBoxMenuItem("Play on caret move");
    autoPlayItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        autoPlay = e.getStateChange() == ItemEvent.SELECTED;
      }
    });
    autoPlayItem.setSelected(autoPlay);
    playbackMenu.add(autoPlayItem);

    JCheckBoxMenuItem caretFollowsPlaybackItem = new JCheckBoxMenuItem("Caret follows playback");
    caretFollowsPlaybackItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        caretFollowsPlayback = e.getStateChange() == ItemEvent.SELECTED;
      }
    });
    caretFollowsPlaybackItem.setSelected(caretFollowsPlayback);
    playbackMenu.add(caretFollowsPlaybackItem);

    fileMenu.add(fileSaveAsMidiAction);

    fileMenu.addSeparator();

    JMenuItem quitItem = new JMenuItem(new QuitAction(this));
    quitItem.setMnemonic(KeyEvent.VK_Q);
    quitItem.getAccessibleContext().setAccessibleDescription(
      "Exit this application.");
    fileMenu.add(quitItem);

    menuBar.add(fileMenu);

    JMenu libraryMenu = new JMenu("Library");
    libraryMenu.setMnemonic(KeyEvent.VK_L);

    JMenu baroqueMenu = new JMenu("Baroque");
    baroqueMenu.setMnemonic(KeyEvent.VK_B);

    JMenu jsBachMenu = new JMenu("Johann Sebastian Bach");
    jsBachMenu.setMnemonic(KeyEvent.VK_B);

    JMenuItem bwv988Item = new JMenuItem("BWV 988 Aria", KeyEvent.VK_G);
    bwv988Item.getAccessibleContext().setAccessibleDescription(
      "Aria from goldberg variations");
    bwv988Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Library library = new Library();
        setScore(library.loadScore("bwv988-aria.xml"));
      }
    });
    jsBachMenu.add(bwv988Item);

    JMenuItem bwv1013Item = new JMenuItem("BWV 1013 1. Allemande", KeyEvent.VK_A);
    bwv1013Item.getAccessibleContext().setAccessibleDescription(
      "Partita in A minor for solo flute: first movement (Allemande)");
    bwv1013Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Library library = new Library();
        setScore(library.loadScore("bwv1013-1.xml"));
      }
    });
    jsBachMenu.add(bwv1013Item);

    baroqueMenu.add(jsBachMenu);

    libraryMenu.add(baroqueMenu);

    menuBar.add(playbackMenu);
    menuBar.add(libraryMenu);

    setJMenuBar(menuBar);

    // Create the text area
    textArea = new JTextArea(transcriber.getOptions().getPageHeight(),
                             transcriber.getOptions().getPageWidth());
    Font font = new Font("DejaVu Serif", Font.PLAIN, 14);
    textArea.setFont(font);
    setTranscriber(transcriber);

    textArea.addCaretListener(this);
    JScrollPane scrollPane = new JScrollPane(textArea);

    // Lay out the content pane.
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    //contentPane.setPreferredSize(new Dimension(400, 100));
    contentPane.add(scrollPane, BorderLayout.CENTER);
    statusBar = new StatusBar();
    contentPane.add(statusBar, BorderLayout.SOUTH);
    setContentPane(contentPane);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
	quit();
      }
    });

    pack();
  }
  public void run() {
    setVisible(true);
  }

  public void setTranscriber(Transcriber transcriber) {
    if (transcriber != null) {
      this.transcriber = transcriber;
      this.score = transcriber.getScore();
      textArea.setText(transcriber.toString());

      boolean scoreAvailable = score != null;
      fileSaveAsMidiAction.setEnabled(scoreAvailable);
      playScoreAction.setEnabled(scoreAvailable);
    }
  }
  public void quit() {
    if (midiPlayer != null) midiPlayer.close();
    System.exit(0);
  }
}
