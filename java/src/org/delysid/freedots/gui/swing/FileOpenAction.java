package org.delysid.freedots.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.delysid.freedots.musicxml.Score;

public final class FileOpenAction extends AbstractAction {
  private Main gui;
  public FileOpenAction(Main gui) {
    super("Open");
    this.gui = gui;
    putValue(SHORT_DESCRIPTION, "Open an existing MusicXML file");
    putValue(ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
  }
  public void actionPerformed(ActionEvent event) {
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
    fileChooser.showOpenDialog(gui);
    try {
      Score newScore = new Score(fileChooser.getSelectedFile().toString());
      gui.setScore(newScore);
    } catch (javax.xml.parsers.ParserConfigurationException exception) {
      exception.printStackTrace();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
