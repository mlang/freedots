/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.gui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.Action;
import javax.swing.JButton;
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
import org.delysid.freedots.Transcriber;

import org.delysid.StandardMidiFileWriter;

public final class GraphicalUserInterface
  extends JFrame
  implements javax.swing.event.CaretListener,
             org.delysid.freedots.playback.PlaybackObserver {
  
  protected Score score = null;
  protected Transcriber transcriber = null;
  protected StatusBar statusBar=null;

  public void setScore(Score score) {
    this.score = score;
    try {
      transcriber.setScore(score);
      textArea.append(transcriber.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected JTextArea textArea;
  Object lastObject = null;
  boolean autoPlay = false;
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
    int pos = transcriber.getIndexOfObject(object);
    if (pos >= 0) {
      textArea.setCaretPosition(pos);
    }
  }

  public GraphicalUserInterface(Transcriber transcriber) {
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
    fileMenu.setMnemonic(KeyEvent.VK_F);

    Action openAction = new FileOpenAction(this);
    JMenuItem openItem = new JMenuItem(openAction);
    openItem.setMnemonic(KeyEvent.VK_O);
    openItem.getAccessibleContext().setAccessibleDescription(
      "Open a MusicXML score file.");
    fileMenu.add(openItem);

    JMenuItem playItem = new JMenuItem("Play score", KeyEvent.VK_P);
    playItem.getAccessibleContext().setAccessibleDescription(
      "Play the complete score.");
    playItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  if (score != null)
      try {
	      midiPlayer.setSequence(new MIDISequence(score, metaEventRelay));
	      midiPlayer.start();
	    } catch (javax.sound.midi.InvalidMidiDataException exception) {
	      exception.printStackTrace();
	    }
	}
      });
    fileMenu.add(playItem);

    JMenuItem saveMidiItem = new JMenuItem("Save as MIDI", KeyEvent.VK_M);
    saveMidiItem.getAccessibleContext().setAccessibleDescription(
      "Save as Standard MIDI file.");
    saveMidiItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if (score != null) {
	  JFileChooser fileChooser = new JFileChooser();
	  if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
	    File file = fileChooser.getSelectedFile();
	    try {
	      FileOutputStream fileOutputStream = new FileOutputStream(file);
	      try {
		StandardMidiFileWriter mfw = new StandardMidiFileWriter();
		mfw.write(new MIDISequence(score), 1, fileOutputStream);
	      } catch (Exception exception) {
		exception.printStackTrace();
	      } finally {
		fileOutputStream.close();
	      }
	    } catch (java.io.IOException exception) {
	      exception.printStackTrace();
	    }
	  }
	}
      }
    });
    fileMenu.add(saveMidiItem);

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

    menuBar.add(libraryMenu);

    setJMenuBar(menuBar);

    // Create the toolbar.
    JToolBar toolBar = new JToolBar();
    // setFloatable(false) to make the toolbar non movable
    JButton openButton = new JButton(openAction);
    toolBar.add(openButton);

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
    contentPane.setPreferredSize(new Dimension(400, 100));
    contentPane.add(toolBar, BorderLayout.NORTH);
    contentPane.add(scrollPane, BorderLayout.CENTER);
    statusBar = new StatusBar();
    contentPane.add(statusBar, java.awt.BorderLayout.SOUTH);
    setContentPane(contentPane);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
	quit();
      }
    });

    pack();
    setVisible(true);
  }

  public void setTranscriber(Transcriber transcriber) {
    if (transcriber != null) {
      this.transcriber = transcriber;
      this.score = transcriber.getScore();
      textArea.append(transcriber.toString());
    }
  }
  public void quit() {
    if (midiPlayer != null)
      midiPlayer.close();
    System.exit(0);
  }
}
