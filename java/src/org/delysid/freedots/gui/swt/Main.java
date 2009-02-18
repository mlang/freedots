package org.delysid.freedots.gui.swt;

import java.awt.HeadlessException;

import org.delysid.freedots.transcription.Transcriber;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

class Main {
  Transcriber transcriber;

  public Main(Transcriber transcriber) {
    this.transcriber = transcriber;
  }

  public void run() {
    try {
      Display display = new Display();
      MainFrame frame = new MainFrame();
      Shell shell = frame.open(display);
      while (!shell.isDisposed())
        if (!display.readAndDispatch()) display.sleep();
      display.dispose();
    } catch (SWTError e) {
      e.printStackTrace();
      throw new HeadlessException();
    }
  }
}
