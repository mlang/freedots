package org.delysid.freedots;

import java.io.IOException;

import java.awt.HeadlessException;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.delysid.freedots.gui.swt.MainFrame;
import org.delysid.freedots.gui.swing.GraphicalUserInterface;

import org.delysid.freedots.musicxml.MIDISequence;
import org.delysid.freedots.musicxml.Score;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class Main {
  public static void main(String[] args) {
    Options options = new Options(args);
    Transcriber transcriber = null;
    Score score = null;
    if (options.getLocation() != null) {
      try {
        score = new Score(options.getLocation());
      } catch (XPathExpressionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ParserConfigurationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (SAXParseException e) {
        System.exit(1);
      } catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        System.exit(1);
      }
      if (score != null) transcriber = new Transcriber(score, options);
    }
//     try {
//       Display display = new Display();
//       MainFrame frame = new MainFrame();
//       Shell shell = frame.open(display);
//       while (!shell.isDisposed())
//         if (!display.readAndDispatch()) display.sleep();
//       display.dispose();
//     } catch (SWTError e) {
    if (options.getWindowSystem()) {
      try {
        GraphicalUserInterface gui = new GraphicalUserInterface();
        if (transcriber != null) gui.setTranscriber(transcriber);
        gui.pack();
        gui.setVisible(true);
      } catch (HeadlessException e) {
        options.setWindowSystem(false);
      }
    }
    if (!options.getWindowSystem()) {
      if (transcriber != null) {
        System.out.println(transcriber.toString());
        if (options.getPlayScore() && score != null) {
          try {
            MIDIPlayer player = new MIDIPlayer();
            player.setSequence(new MIDISequence(score));
            player.start();
            try {
              while (player.isRunning())
                Thread.sleep(100);
            } catch (InterruptedException ie) {}
          } catch (MidiUnavailableException mue) {
            System.err.println("MIDI playback not available.");
          } catch (InvalidMidiDataException imde) {
            imde.printStackTrace();
          }
        }
      } else {
        System.err.println("No window system available and no filename specified.");
        printUsage();
        System.exit(1);
      }
    }
  }
  static void printUsage() {
    System.out.println("Usage: java -jar freedots.jar " +
                       "[-w PAGEWIDTH] [-nw] [-p] [FILENAME|URL]");
    System.out.println("Options:");
    System.out.println("\t-nw:\t\tNo Window System");
    System.out.println("\t-p:\t\tPlay complete score");
  }
}
