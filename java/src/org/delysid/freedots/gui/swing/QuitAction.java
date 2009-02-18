package org.delysid.freedots.gui.swing;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public final class QuitAction extends AbstractAction {
  Main gui;
  QuitAction(Main gui) {
    super("Quit");
    this.gui = gui;
  }
  public void actionPerformed(ActionEvent event) {
    gui.quit();
  }
}
