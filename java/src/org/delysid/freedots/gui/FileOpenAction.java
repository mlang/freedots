package org.delysid.freedots.gui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import musicxml.MusicXML;

public class FileOpenAction extends AbstractAction {
  GraphicalUserInterface gui;
  public FileOpenAction(GraphicalUserInterface gui) {
    super("Open");
    this.gui = gui;
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
      MusicXML newScore = new MusicXML(fileChooser.getSelectedFile().toString());
      gui.setScore(newScore);
    } catch (javax.xml.parsers.ParserConfigurationException exception) {
      exception.printStackTrace();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
