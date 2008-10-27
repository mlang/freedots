/* -*- c-basic-offset: 2; -*- */
package gui;
// MIDI
import com.sun.media.sound.StandardMidiFileWriter;
import javax.sound.midi.MidiUnavailableException;

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
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

import musicxml.MusicXML;
import musicxml.Part;
import musicxml.Measure;
import musicxml.MIDIPlayer;
import musicxml.MIDISequence;

public class GraphicalUserInterface extends JFrame {
  protected MusicXML score = null;
  protected JTextArea textArea;
  protected String newline = "\n";

  public GraphicalUserInterface() {
    super("FreeDots");
    // Create the menubar.
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    JMenuItem openItem = new JMenuItem("Open", KeyEvent.VK_O);
    openItem.getAccessibleContext().setAccessibleDescription(
      "Open a MusicXML score file.");
    openItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  JFileChooser fileChooser = new JFileChooser();
	  fileChooser.setAcceptAllFileFilterUsed(false);
	  fileChooser.setFileFilter(new FileFilter() {
	      @Override
	      public boolean accept(File f) {
		return f.isDirectory() || f.getName().matches(".*\\.(mxl|xml)");
	      }
	      @Override
	      public String getDescription() {
		return "*.mxl, *.xml";
	      }
	    });
	  fileChooser.showOpenDialog(null);
	  try {
	    MusicXML newScore = new MusicXML(fileChooser.getSelectedFile().toString());
	    setScore(newScore);
	  } catch (javax.xml.parsers.ParserConfigurationException exception) {
	    exception.printStackTrace();
	  } catch (Exception exception) {
	    exception.printStackTrace();
	  }
	}
      });
    fileMenu.add(openItem);


    JMenuItem playItem = new JMenuItem("Play score", KeyEvent.VK_P);
    playItem.getAccessibleContext().setAccessibleDescription(
      "Play the complete score.");
    playItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  if (score != null) {
	    try {
	      MIDIPlayer midiPlayer = new MIDIPlayer(score);
	      midiPlayer.play();
	    } catch (MidiUnavailableException exception) {
	      exception.printStackTrace();
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
    JToolBar jtbMainToolbar = new JToolBar();
    // setFloatable(false) to make the toolbar non movable
    addButtons(jtbMainToolbar);
    // Create the text area
    textArea = new JTextArea(5, 30);
    JScrollPane jsPane = new JScrollPane(textArea);
    // Lay out the content pane.
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.setPreferredSize(new Dimension(400, 100));
    contentPane.add(jtbMainToolbar, BorderLayout.NORTH);
    contentPane.add(jsPane, BorderLayout.CENTER);
    setContentPane(contentPane);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	quit();
      }
    });
  }
  public void addButtons(JToolBar jtbToolBar) {
    JButton jbnToolbarButtons = null;
    // first button
    jbnToolbarButtons = new JButton(new ImageIcon("left.gif"));
    jbnToolbarButtons.setToolTipText("left");
    jbnToolbarButtons.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    displayInTextArea("This is Left Toolbar Button Reporting");
		}
	    });
	jtbToolBar.add(jbnToolbarButtons);
	// 2nd button
	jbnToolbarButtons = new JButton(new ImageIcon("right.gif"));
	jbnToolbarButtons.setToolTipText("right");
	jbnToolbarButtons.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    displayInTextArea("This is right Toolbar Button Reporting");
		}
	    });
	jtbToolBar.add(jbnToolbarButtons);
	jtbToolBar.addSeparator();
	// 3rd button
	jbnToolbarButtons = new JButton(new ImageIcon("open.gif"));
	jbnToolbarButtons.setToolTipText("open");
	jbnToolbarButtons.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    displayInTextArea("This is open Toolbar Button Reporting");
		}
	    });
	jtbToolBar.add(jbnToolbarButtons);
	// 4th button
	jbnToolbarButtons = new JButton(new ImageIcon("save.gif"));
	jbnToolbarButtons.setToolTipText("save");
	jbnToolbarButtons.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    displayInTextArea("This is save Toolbar Button Reporting");
		}
	    });
	jtbToolBar.add(jbnToolbarButtons);
	// We can add separators to group similar components
	jtbToolBar.addSeparator();
	// fourth button
	jbnToolbarButtons = new JButton("Text button");
	jbnToolbarButtons.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    displayInTextArea("Text button");
		}
	    });
	jtbToolBar.add(jbnToolbarButtons);
	// fifth component is NOT a button!
	JTextField jtfButton = new JTextField("Text field");
	jtfButton.setEditable(false);
	jtfButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    displayInTextArea("TextField component can also be placed");
		}
	    });
	jtbToolBar.add(jtfButton);
    }

  protected void displayInTextArea(String actionDescription) {
    textArea.append(actionDescription + newline);
  }

  public void setScore(MusicXML score) {
    this.score = score;
    textArea.append(Character.toString((char)(0X2800+0X07)));
  }
  public void quit() {
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
