/* -*- c-basic-offset: 2; -*- */
package gui;
// MIDI
import com.sun.media.sound.StandardMidiFileWriter;
import javax.sound.midi.MidiUnavailableException;

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.Action;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.event.*;

import java.awt.GraphicsEnvironment;

import musicxml.MusicXML;
import musicxml.Part;
import musicxml.Measure;
import musicxml.MIDIPlayer;
import musicxml.MIDISequence;

public class GraphicalUserInterface extends JFrame {
  protected MusicXML score = null;
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
	  if (score != null) {
	    try {
	      midiPlayer.setSequence(new MIDISequence(score));
	      midiPlayer.start();
	    } catch (javax.sound.midi.InvalidMidiDataException exception) {
	      exception.printStackTrace();
	    }
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
      public void windowClosing(WindowEvent e) {
	quit();
      }
    });
  }
  protected void displayInTextArea(String actionDescription) {
    textArea.append(actionDescription + newline);
  }

  public void setScore(MusicXML score) {
    this.score = score;
    textArea.append("Hello "+Character.toString((char)(0X2800+0X07)));
  }
  public void quit() {
    if (midiPlayer != null) {
      midiPlayer.close();
    }
    System.exit(0);
  }
  public static void main(String[] args) {
    MusicXML score = null;
    try {
      score = new MusicXML(args[0]);
      GraphicalUserInterface gui = new GraphicalUserInterface(); // Extends Frame.
      gui.setScore(score);
      gui.pack();
      gui.setVisible(true);
    } catch (HeadlessException e) {
      System.err.println("No graphical environment available, exiting...");
      for (Part part:score.parts()) {
	System.out.println("Part name: " + part.getName());
	for (Measure measure:part.measures()) {
	  System.out.println("Measure number: " + measure.getNumber());
	} 
      }
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
