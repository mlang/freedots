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
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.logging.LogRecord;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;

import freedots.Options;
import freedots.logging.Logger;
import freedots.musicxml.Library;
import freedots.musicxml.MIDISequence;
import freedots.musicxml.Note;
import freedots.musicxml.Score;
import freedots.playback.MIDIPlayer;
import freedots.playback.MetaEventRelay;
import freedots.playback.MetaEventListeningUnavailableException;
import freedots.transcription.Transcriber;

/**
 * Main class for Swing based graphical user interface.
 */
public final class Main
  extends JFrame
  implements javax.swing.event.CaretListener,
             freedots.gui.GraphicalUserInterface,
             freedots.playback.PlaybackObserver
{
  private Score score;
  public Score getScore() { return score; }

  private Transcriber transcriber;
  public Transcriber getTranscriber() { return transcriber; }

  protected StatusBar statusBar = null;
  protected SingleNoteRenderer noteRenderer = null;

  public void setScore(Score score) {
    this.score = score;
    try {
      transcriber.setScore(score);
      textArea.setText(transcriber.toString());
      textArea.setCaretPosition(0);
      boolean scoreAvailable = score != null;
      fileSaveAsAction.setEnabled(scoreAvailable);
      
      //this.midiPlayer
      
      playScoreAction.setEnabled(scoreAvailable);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private JTextArea textArea, logArea;
  private Object lastObject = null;

  private boolean autoPlay = false;
  private boolean caretFollowsPlayback = true;

  public void caretUpdate(CaretEvent caretEvent) {
    int index = caretEvent.getDot();
    Object object = null;
    if (transcriber != null) {
      object = transcriber.getObjectAtIndex(index);
    }
    if (object != lastObject) {
      final boolean isPitchedNote = object != null && object instanceof Note
                                 && !((Note)object).isRest();
      editFingeringAction.setEnabled(isPitchedNote);
      if (object != null) {
        
        if (object instanceof Note)  noteRenderer.setNote((Note)object);
            
        if (statusBar != null){
          statusBar.setMessage(object.toString());
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

  private MIDIPlayer midiPlayer;
  private MetaEventRelay metaEventRelay = new MetaEventRelay(this);
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
  private Action fileSaveAsAction = new FileSaveAsAction(this);
  private Action editFingeringAction = new EditFingeringAction(this);

  public Main(final Transcriber transcriber) {
    super("FreeDots " + freedots.Main.VERSION);
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
      InputStream dejaVu = getClass().getResourceAsStream("DejaVuSerif.ttf");
      GraphicsEnvironment
      graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
      graphicsEnvironment.registerFont(Font.createFont(Font.TRUETYPE_FONT,
                                                       dejaVu));
    } catch (java.io.IOException ioe) {
      ioe.printStackTrace();
    } catch (FontFormatException ffe) {
      ffe.printStackTrace();
    }

    // Create the menubar.
    setJMenuBar(createMenuBar());

    // Create the text area
    textArea = new JTextArea(transcriber.getOptions().getPageHeight(),
                             transcriber.getOptions().getPageWidth());
    Font font = new Font("DejaVu Serif", Font.PLAIN, 14);
    textArea.setFont(font);
    setTranscriber(transcriber);

    textArea.setEditable(false);
    textArea.addCaretListener(this);
    JScrollPane scrollPane = new JScrollPane(textArea);

    logArea = new JTextArea(5, 60);
    logArea.setEditable(false);

    // Lay out the content pane.
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    //contentPane.setPreferredSize(new Dimension(400, 100));
    contentPane.add(scrollPane, BorderLayout.CENTER);

    scrollPane = new JScrollPane(logArea);
    contentPane.add(scrollPane, BorderLayout.SOUTH);

    noteRenderer = new SingleNoteRenderer();
    contentPane.add(noteRenderer, BorderLayout.AFTER_LAST_LINE);
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
      fileSaveAsAction.setEnabled(scoreAvailable);
      playScoreAction.setEnabled(scoreAvailable);
    }
  }
  public void quit() {
    if (midiPlayer != null) midiPlayer.close();
    System.exit(0);
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    
    fileMenu.add(new FileOpenAction(this));
    fileMenu.add(fileSaveAsAction);
    fileMenu.addSeparator();
    JMenuItem quitItem = new JMenuItem(new QuitAction(this));
    quitItem.setMnemonic(KeyEvent.VK_Q);
    quitItem.getAccessibleContext().setAccessibleDescription(
      "Exit this application.");
    fileMenu.add(quitItem);

    menuBar.add(fileMenu);

    JMenu editMenu = new JMenu("Edit");
    editMenu.setMnemonic(KeyEvent.VK_E);
    editMenu.add(editFingeringAction);

    menuBar.add(editMenu);

    JMenu transcriptionMenu = new JMenu("Transcription");
    transcriptionMenu.setMnemonic(KeyEvent.VK_T);

    JRadioButtonMenuItem sectionBySectionItem = new JRadioButtonMenuItem("Section by Section");
    JRadioButtonMenuItem barOverBarItem = new JRadioButtonMenuItem("Bar over Bar");
    ButtonGroup group = new ButtonGroup();
    group.add(sectionBySectionItem);
    group.add(barOverBarItem);
    switch (transcriber.getOptions().getMethod()) {
    case SectionBySection: sectionBySectionItem.setSelected(true); break;
    case BarOverBar: barOverBarItem.setSelected(true); break;
    }
    sectionBySectionItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          transcriber.getOptions().setMethod(Options.Method.SectionBySection);
          triggerTranscription();
        }
      });

    barOverBarItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          transcriber.getOptions().setMethod(Options.Method.BarOverBar);
          triggerTranscription();
        }
      });
    transcriptionMenu.add(sectionBySectionItem);
    transcriptionMenu.add(barOverBarItem);

    JCheckBoxMenuItem showFingeringItem = new JCheckBoxMenuItem("Show fingering");
    showFingeringItem.setSelected(transcriber.getOptions().getShowFingering());
    showFingeringItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        transcriber.getOptions().setShowFingering(e.getStateChange() == ItemEvent.SELECTED);
        triggerTranscription();
      }
    });
    transcriptionMenu.add(showFingeringItem);

    menuBar.add(transcriptionMenu);

    JMenu playbackMenu = new JMenu("Playback");
    playbackMenu.setMnemonic(KeyEvent.VK_P);

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
    menuBar.add(playbackMenu);

    menuBar.add(createLibraryMenu());

    return menuBar;
  }

  private JMenu createLibraryMenu() {
    JMenu libraryMenu = new JMenu("Library");
    libraryMenu.setMnemonic(KeyEvent.VK_L);

    JMenu baroqueMenu = new JMenu("Baroque");
    baroqueMenu.setMnemonic(KeyEvent.VK_B);

    JMenu jsBachMenu = new JMenu("Johann Sebastian Bach");
    jsBachMenu.setMnemonic(KeyEvent.VK_B);

    JMenu bwv988Menu = new JMenu("BWV 988: Aria con Variazioni");
    JMenuItem bwv988_ariaItem = new JMenuItem("Aria", KeyEvent.VK_A);
    bwv988_ariaItem.getAccessibleContext().setAccessibleDescription(
      "Aria from goldberg variations");
    bwv988_ariaItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Library library = new Library();
        setScore(library.loadScore("bwv988-aria.xml"));
      }
    });
    bwv988Menu.add(bwv988_ariaItem);

    JMenuItem bwv988_1Item = new JMenuItem("Variation 1", KeyEvent.VK_1);
    bwv988_1Item.getAccessibleContext().setAccessibleDescription(
      "Variation 1 from goldberg variations");
    bwv988_1Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Library library = new Library();
        setScore(library.loadScore("bwv988-1.xml"));
      }
    });
    bwv988Menu.add(bwv988_1Item);

    jsBachMenu.add(bwv988Menu);

    JMenu bwv1013Menu = new JMenu("BWV 1013: Partita in A minor for solo flute");

    JMenuItem bwv1013_1Item = new JMenuItem("1. Allemande", KeyEvent.VK_A);
    bwv1013_1Item.getAccessibleContext().setAccessibleDescription(
      "Partita in A minor for solo flute: first movement (Allemande)");
    bwv1013_1Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Library library = new Library();
        setScore(library.loadScore("bwv1013-1.xml"));
      }
    });
    bwv1013Menu.add(bwv1013_1Item);
    JMenuItem bwv1013_2Item = new JMenuItem("2. Corrente", KeyEvent.VK_C);
    bwv1013_2Item.getAccessibleContext().setAccessibleDescription(
      "Partita in A minor for solo flute: second movement (Corrente)");
    bwv1013_2Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Library library = new Library();
        setScore(library.loadScore("bwv1013-2.xml"));
      }
    });
    bwv1013Menu.add(bwv1013_2Item);
    JMenuItem bwv1013_3Item = new JMenuItem("3. Sarabande", KeyEvent.VK_S);
    bwv1013_3Item.getAccessibleContext().setAccessibleDescription(
      "Partita in A minor for solo flute: second movement (Sarabande)");
    bwv1013_3Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Library library = new Library();
        setScore(library.loadScore("bwv1013-3.xml"));
      }
    });
    bwv1013Menu.add(bwv1013_3Item);
    JMenuItem bwv1013_4Item = new JMenuItem("4. Bouree", KeyEvent.VK_B);
    bwv1013_4Item.getAccessibleContext().setAccessibleDescription(
      "Partita in A minor for solo flute: second movement (Bouree)");
    bwv1013_4Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Library library = new Library();
        setScore(library.loadScore("bwv1013-4.xml"));
      }
    });
    bwv1013Menu.add(bwv1013_4Item);

    jsBachMenu.add(bwv1013Menu);

    baroqueMenu.add(jsBachMenu);

    libraryMenu.add(baroqueMenu);

    JMenu classicalMenu = new JMenu("Classical");
    classicalMenu.setMnemonic(KeyEvent.VK_C);

    JMenu lvBeethovenMenu = new JMenu("Ludwig Van Beethoven");
    lvBeethovenMenu.setMnemonic(KeyEvent.VK_B);

    JMenu moonshineMenu = new JMenu("Sonata XIV Op.27 No.2");

    lvBeethovenMenu.add(moonshineMenu);

    JMenuItem moonshine_1Item = new JMenuItem("1. First movement", KeyEvent.VK_1);
    moonshine_1Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Library library = new Library();
        setScore(library.loadScore("lvb-moonlight-1.xml"));
      }
    });

    moonshineMenu.add(moonshine_1Item);

    classicalMenu.add(lvBeethovenMenu);

    libraryMenu.add(classicalMenu);

    return libraryMenu;
  }

  Object getCurrentScoreObject() {
    int position = textArea.getCaretPosition();
    return transcriber.getObjectAtIndex(position);
  }
  void triggerTranscription() {
    int position = textArea.getCaretPosition();
    Object object = getCurrentScoreObject();
    transcriber.setScore(score);
    textArea.setText(transcriber.toString());
    if (object != null) {
      int objectPosition = transcriber.getIndexOfObject(object);
      if (objectPosition != -1) position = objectPosition;
    }
    textArea.setCaretPosition(position);
  }

  public void notifyLog() {
    javax.swing.SwingUtilities.invokeLater
      (new Runnable() {
          public void run () {
            final BlockingQueue<LogRecord> queue = Logger.getQueue();
            while (queue.size() != 0) {
              LogRecord record = queue.poll();
              if (record != null) {
                StringBuilder sb = new StringBuilder(256);
                sb.append(record.getLevel().toString())
                  .append(" - ")
                  .append(record.getMessage())
                  .append("\n");
                logArea.append(sb.toString());
              }
            }
          }
        });
  }
}
