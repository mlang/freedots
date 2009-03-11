package org.delysid.freedots.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.delysid.freedots.musicxml.Score;

public final class PlayScoreAction extends AbstractAction {
  private Main gui;
  public PlayScoreAction(Main gui) {
    super("Play score");
    this.gui = gui;
    putValue(SHORT_DESCRIPTION, "Play the complete score");
    putValue(ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
  }
  public void actionPerformed(ActionEvent event) {
    gui.startPlayback();
  }
}
