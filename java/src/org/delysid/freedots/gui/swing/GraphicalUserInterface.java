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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.delysid.freedots.MIDIPlayer;
import org.delysid.freedots.Transcriber;
import org.delysid.freedots.musicxml.MIDISequence;
import org.delysid.freedots.musicxml.Score;

import org.delysid.StandardMidiFileWriter;

public final class GraphicalUserInterface extends JFrame {
  protected Score score = null;
  protected Transcriber transcriber = null;

  protected JTextArea textArea;
  protected String newline = "\n";
  protected MIDIPlayer midiPlayer;

  public GraphicalUserInterface() {
    super("FreeDots");

    try {
      MIDIPlayer player = new MIDIPlayer();
      midiPlayer = player;
    } catch (MidiUnavailableException e) {
      e.printStackTrace();
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      e.printStackTrace();
    }
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
	      midiPlayer.setSequence(new MIDISequence(score));
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
    setJMenuBar(menuBar);

    // Create the toolbar.
    JToolBar toolBar = new JToolBar();
    // setFloatable(false) to make the toolbar non movable
    JButton openButton = new JButton(openAction);
    toolBar.add(openButton);

    // Create the text area
    textArea = new JTextArea(5, 30);
    Font font = new Font("DejaVu Serif", Font.PLAIN, 14);
    textArea.setFont(font);
    JScrollPane scrollPane = new JScrollPane(textArea);

    // Lay out the content pane.
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.setPreferredSize(new Dimension(400, 100));
    contentPane.add(toolBar, BorderLayout.NORTH);
    contentPane.add(scrollPane, BorderLayout.CENTER);
    setContentPane(contentPane);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
	quit();
      }
    });
  }
  public void setScore(Score score) {
    this.score = score;
    textArea.append("Hello "+Character.toString((char)(0X2800+0X07)));
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
  public static void main(String[] args) {
    Score score = null;
    try {
      if (java.lang.reflect.Array.getLength(args) == 1)
        score = new Score(args[0]);
      GraphicalUserInterface gui = new GraphicalUserInterface(); // Extends Frame.
      if (score != null) gui.setScore(score);
      gui.pack();
      gui.setVisible(true);
    } catch (HeadlessException e) {
      System.err.println("No graphical environment available, exiting...");
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
