package org.delysid.freedots.gui.swt;

import java.io.IOException;
import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.xml.sax.SAXException;

import org.delysid.musicxml.MIDISequence;
import org.delysid.musicxml.MusicXML;
import org.delysid.freedots.MIDIPlayer;
import org.delysid.freedots.gui.Messages;

/**
 */
public class MainFrame {
  public static void main(String[] args) {
    Display display = new Display();
    MainFrame frame = new MainFrame();
    Shell shell = frame.open(display);
    while (!shell.isDisposed())
      if (!display.readAndDispatch())
        display.sleep();
    display.dispose();
  }
  Shell shell;
  ToolBar toolBar;
  StyledText text;
  Vector<StyleRange> cachedStyles = new Vector<StyleRange>();
  Color RED = null;
  Color BLUE = null;
  Color GREEN = null;
  Font font = null;

  ToolItem boldButton, italicButton, underlineButton, strikeoutButton;

  MusicXML score;
  MIDIPlayer player;

  public MainFrame() {
    try {
      player = new MIDIPlayer();
    } catch (MidiUnavailableException e) {
      e.printStackTrace();
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }
  /*
   * Clear all style data for the selected text.
   */
  void clear() {
    Point sel = text.getSelectionRange();
    if (sel.y != 0) {
      StyleRange style;
      style = new StyleRange(sel.x, sel.y, null, null, SWT.NORMAL);
      text.setStyleRange(style);
    }
    text.setSelectionRange(sel.x + sel.y, 0);
  }

  Menu createEditMenu() {
    Menu bar = shell.getMenuBar();
    Menu menu = new Menu(bar);

    MenuItem item = new MenuItem(menu, SWT.PUSH);
    item.setText("Cut"); //$NON-NLS-1$
    item.setAccelerator(SWT.MOD1 + 'X');
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        text.cut();
      }
    });
    item = new MenuItem(menu, SWT.PUSH);
    item.setText("Copy"); //$NON-NLS-1$
    item.setAccelerator(SWT.MOD1 + 'C');
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        text.copy();
      }
    });
    item = new MenuItem(menu, SWT.PUSH);
    item.setText("Paste"); //$NON-NLS-1$
    item.setAccelerator(SWT.MOD1 + 'V');
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        text.paste();
      }
    });
    new MenuItem(menu, SWT.SEPARATOR);
    return menu;
  }

  Menu createFileMenu() {
    Menu bar = shell.getMenuBar();
    Menu menu = new Menu(bar);

    MenuItem item = new MenuItem(menu, SWT.PUSH);
    item.setText("Open...");
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
        fileDialog.setText("Open MusicXML score");
        String selectedItem = fileDialog.open();
        if (selectedItem != null) {
          MusicXML newScore = null;
          try {
            newScore = new MusicXML(selectedItem);
          } catch (XPathExpressionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          } catch (ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          } catch (SAXException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
          if (newScore != null) {
            score = newScore;
            try {
              player.setSequence(new MIDISequence(score));
            } catch (InvalidMidiDataException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
        }
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.getString("play")); //$NON-NLS-1$
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        player.start();
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.getString("exit")); //$NON-NLS-1$
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        shell.close();
      }
    });

    return menu;
  }

  void createMenuBar() {
    Menu bar = new Menu(shell, SWT.BAR);
    shell.setMenuBar(bar);

    MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
    fileItem.setText(Messages.getString("file")); //$NON-NLS-1$
    fileItem.setMenu(createFileMenu());

    MenuItem editItem = new MenuItem(bar, SWT.CASCADE);
    editItem.setText(Messages.getString("edit")); //$NON-NLS-1$
    editItem.setMenu(createEditMenu());
  }

  void createStyledText() {
    initializeColors();
    text = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
        | SWT.H_SCROLL);
    GridData spec = new GridData();
    spec.horizontalAlignment = GridData.FILL;
    spec.grabExcessHorizontalSpace = true;
    spec.verticalAlignment = GridData.FILL;
    spec.grabExcessVerticalSpace = true;
    text.setLayoutData(spec);
    text.addExtendedModifyListener(new ExtendedModifyListener() {
      public void modifyText(ExtendedModifyEvent e) {
        handleExtendedModify(e);
      }
    });
    text.setText("Test text "+java.lang.Character.toString((char)(0X2800+7)));
  }

  void createToolBar() {
    toolBar = new ToolBar(shell, SWT.NONE);
    SelectionAdapter listener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        setStyle(event.widget);
      }
    };
    boldButton = new ToolItem(toolBar, SWT.CHECK);
    boldButton.setToolTipText("Bold"); //$NON-NLS-1$
    boldButton.addSelectionListener(listener);
    italicButton = new ToolItem(toolBar, SWT.CHECK);
    italicButton.setToolTipText("Italic"); //$NON-NLS-1$
    italicButton.addSelectionListener(listener);
    underlineButton = new ToolItem(toolBar, SWT.CHECK);
    underlineButton.setToolTipText("Underline"); //$NON-NLS-1$
    underlineButton.addSelectionListener(listener);
    strikeoutButton = new ToolItem(toolBar, SWT.CHECK);
    strikeoutButton.setToolTipText("Strikeout"); //$NON-NLS-1$
    strikeoutButton.addSelectionListener(listener);

    ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);
    item = new ToolItem(toolBar, SWT.PUSH);
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        fgColor(RED);
      }
    });
    item = new ToolItem(toolBar, SWT.PUSH);
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        fgColor(GREEN);
      }
    });
    item = new ToolItem(toolBar, SWT.PUSH);
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        fgColor(BLUE);
      }
    });
    item = new ToolItem(toolBar, SWT.SEPARATOR);
    item = new ToolItem(toolBar, SWT.PUSH);
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        clear();
      }
    });
  }

  /*
   * Set the foreground color for the selected text.
   */
  void fgColor(Color fg) {
    Point sel = text.getSelectionRange();
    if ((sel == null) || (sel.y == 0))
      return;
    StyleRange style, range;
    for (int i = sel.x; i < sel.x + sel.y; i++) {
      range = text.getStyleRangeAtOffset(i);
      if (range != null) {
        style = (StyleRange) range.clone();
        style.start = i;
        style.length = 1;
        style.foreground = fg;
      } else
        style = new StyleRange(i, 1, fg, null, SWT.NORMAL);
      text.setStyleRange(style);
    }
    text.setSelectionRange(sel.x + sel.y, 0);
  }

  void handleExtendedModify(ExtendedModifyEvent event) {
    if (event.length == 0)
      return;
    StyleRange style;
    if (event.length == 1
        || text.getTextRange(event.start, event.length).equals(
            text.getLineDelimiter())) {
      // Have the new text take on the style of the text to its right (during
      // typing) if no style information is active.
      int caretOffset = text.getCaretOffset();
      style = null;
      if (caretOffset < text.getCharCount())
        style = text.getStyleRangeAtOffset(caretOffset);
      if (style != null) {
        style = (StyleRange) style.clone();
        style.start = event.start;
        style.length = event.length;
      } else
        style = new StyleRange(event.start, event.length, null, null,
            SWT.NORMAL);
      if (boldButton.getSelection())
        style.fontStyle |= SWT.BOLD;
      if (italicButton.getSelection())
        style.fontStyle |= SWT.ITALIC;
      style.underline = underlineButton.getSelection();
      style.strikeout = strikeoutButton.getSelection();
      if (!style.isUnstyled())
        text.setStyleRange(style);
    } else
      // paste occurring, have text take on the styles it had when it was
      // cut/copied
      for (int i = 0; i < cachedStyles.size(); i++) {
        style = cachedStyles.elementAt(i);
        StyleRange newStyle = (StyleRange) style.clone();
        newStyle.start = style.start + event.start;
        text.setStyleRange(newStyle);
      }
  }

  void initializeColors() {
    Display display = Display.getDefault();
    RED = new Color(display, new RGB(255, 0, 0));
    BLUE = new Color(display, new RGB(0, 0, 255));
    GREEN = new Color(display, new RGB(0, 255, 0));
  }

  public Shell open(Display display) {
    shell = new Shell(display);
    shell.setText("FreeDots"); //$NON-NLS-1$
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    shell.setLayout(layout);
    shell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        if (font != null)
          font.dispose();
        RED.dispose();
        GREEN.dispose();
        BLUE.dispose();
      }
    });
    createMenuBar();
    createToolBar();
    createStyledText();
    shell.setSize(500, 300);
    shell.open();
    return shell;
  }

  /*
   * Set a style
   */
  void setStyle(Widget widget) {
    Point sel = text.getSelectionRange();
    if ((sel == null) || (sel.y == 0))
      return;
    StyleRange style;
    for (int i = sel.x; i < sel.x + sel.y; i++) {
      StyleRange range = text.getStyleRangeAtOffset(i);
      if (range != null) {
        style = (StyleRange) range.clone();
        style.start = i;
        style.length = 1;
      } else
        style = new StyleRange(i, 1, null, null, SWT.NORMAL);
      if (widget == boldButton)
        style.fontStyle ^= SWT.BOLD;
      else if (widget == italicButton)
        style.fontStyle ^= SWT.ITALIC;
      else if (widget == underlineButton)
        style.underline = !style.underline;
      else if (widget == strikeoutButton)
        style.strikeout = !style.strikeout;
      text.setStyleRange(style);
    }
    text.setSelectionRange(sel.x + sel.y, 0);
  }
}
