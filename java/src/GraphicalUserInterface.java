import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.*;
import musicxml.MusicXML;

public class GraphicalUserInterface extends JFrame {
  protected JTextArea textArea;
  protected String newline = "\n";

  public GraphicalUserInterface() {
    super("FreeDots");
    // Create the toolbar.
    JToolBar jtbMainToolbar = new JToolBar();
    // setFloatable(false) to make the toolbar non movable
    addButtons(jtbMainToolbar);
    // Create the text area
    textArea = new JTextArea(5, 30);
    JScrollPane jsPane = new JScrollPane(textArea);
    // Lay out the content pane.
    JPanel jplContentPane = new JPanel();
    jplContentPane.setLayout(new BorderLayout());
    jplContentPane.setPreferredSize(new Dimension(400, 100));
    jplContentPane.add(jtbMainToolbar, BorderLayout.NORTH);
    jplContentPane.add(jsPane, BorderLayout.CENTER);
    setContentPane(jplContentPane);
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
  public static void main(String[] args) {
    try {
      MusicXML score = new MusicXML(args[0]);
      GraphicalUserInterface jtfToolbar = new GraphicalUserInterface(); // Extends Frame.
      jtfToolbar.pack();
      jtfToolbar.addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	    System.exit(0);
	  }
	});
      jtfToolbar.setVisible(true);
    } catch (HeadlessException e) {
      System.err.println("No graphical environment available, exiting...");
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
