package org.delysid.freedots.gui;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class QuitAction extends AbstractAction {
  GraphicalUserInterface gui;
  public QuitAction(GraphicalUserInterface gui) {
    super("Quit");
    this.gui = gui;
  }
  public void actionPerformed(ActionEvent event) {
    System.out.println("Quit invoked...");
    gui.quit();
  }
}
