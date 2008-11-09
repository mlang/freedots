import os

from pkg_resources import resource_string

import pygtk
pygtk.require('2.0')

import gtk

import musicxml
import braillemusic
import playback

__license__ = resource_string(__name__, "COPYING")

class MusicXMLFilter(gtk.FileFilter):
    def __init__(self):
        super(self.__class__, self).__init__()
        self.set_name("MusicXML files")
        self.add_pattern("*.xml")
        self.add_pattern("*.mxl")
        #self.add_pattern("*.xml.gz")

class Application(gtk.Window):
    def __init__(self, filename=None, width=32):
        super(self.__class__, self).__init__(gtk.WINDOW_TOPLEVEL)
        self.set_title('FreeDots')
        self.connect('delete-event', self.delete_event_cb)
        self.set_size_request(640, 480)

        vbox = gtk.VBox()
        self.add(vbox)

        self.create_ui()
        vbox.pack_start(self.ui.get_widget('/Menubar'), expand=False)
        vbox.pack_start(self.ui.get_widget('/Toolbar'), expand=False)

        spinbutton = gtk.SpinButton()
        spinbutton.set_range(8, 80)
        spinbutton.set_increments(2, 10)
        spinbutton.set_numeric(False)
        spinbutton.set_value(width)
        spinbutton.connect("value-changed", self.width_changed_cb)
        self.widthWidget = spinbutton
        vbox.pack_start(self.widthWidget, expand=False)
        sw = gtk.ScrolledWindow()
        sw.set_policy(gtk.POLICY_AUTOMATIC, gtk.POLICY_AUTOMATIC)
        vbox.pack_start(sw)

        textview = gtk.TextView()
        textview.set_editable(False)
        textview.connect("key-press-event", self.key_press_cb)
        self.buffer = textview.get_buffer()
        self.buffer.connect("mark-set", self.mark_set_cb)
        sw.add(textview)

        self.status_bar = gtk.Statusbar()
        vbox.pack_end(self.status_bar, expand=False)

        if filename:
            self.open(filename)

    def create_ui(self):
        self.globalActionGroup = gtk.ActionGroup('WindowActions')
        self.globalActionGroup.add_actions([
            ('FileMenu', None, '_File'),
            ('Open',     gtk.STOCK_OPEN, '_Open', '<control>O',
             'Open a file', self.file_open_cb),
            ('OpenRecent', None, '_Recently opened files...'),
            ('Quit',     gtk.STOCK_QUIT, '_Quit', '<control>Q',
             'Quit application', self.file_quit_cb),
            ('EditMenu', None, '_Edit'),
            ('HelpMenu', None, '_Help'),
            ('About',    None, '_About', None, 'About application',
             self.help_about_cb),
            ])
        self.openFileActionGroup = gtk.ActionGroup('FileActions')
        self.openFileActionGroup.add_actions([
            ('Save',     gtk.STOCK_SAVE, '_Save', '<control>S',
             'Save file', self.file_save_cb),
            ('Play',     None, '_Play', '<control>P', 'Play score', self.play_cb),
            ])
        self.openFileActionGroup.set_sensitive(False)
        self.editPitchedActionGroup = gtk.ActionGroup('EditPitchedNoteActions')
        self.editPitchedActionGroup.add_actions([
            ('OctaveUp', None, 'Octave _up', '<control>plus', 'Change note one octave up', self.octave_up_cb),
            ('OctaveDown', None, 'Octave _down', '<control>minus', 'Change note one octave down', self.octave_down_cb)
            ])
        self.editPitchedActionGroup.set_sensitive(False)

        self.ui = gtk.UIManager()
        self.ui.insert_action_group(self.globalActionGroup, 0)
        self.ui.insert_action_group(self.editPitchedActionGroup, 0)
        self.ui.insert_action_group(self.openFileActionGroup, 0)
        self.ui.add_ui_from_string("""
<ui>
  <menubar name='Menubar'>
    <menu action='FileMenu'>
      <menuitem action='Open'/>
      <menuitem action='OpenRecent'/>
      <menuitem action='Save'/>
      <menuitem action='Play'/>
      <separator/>
      <menuitem action='Quit'/>
    </menu>
    <menu action='EditMenu'>
      <menuitem action='OctaveUp'/>
      <menuitem action='OctaveDown'/>
    </menu>
    <menu action='HelpMenu'>
      <menuitem action='About'/>
    </menu>
  </menubar>
  <toolbar name='Toolbar'>
    <toolitem action='Open'/>
    <toolitem action='Play'/>
    <separator/>
    <toolitem action='Quit'/>
  </toolbar>
</ui>""")
        me = gtk.RecentChooserMenu()
        me.set_show_not_found(False)
        me.set_show_numbers(True)
        me.connect("item-activated", self.open_recent_cb)
        self.ui.get_widget('/Menubar/FileMenu/OpenRecent').set_submenu(me)

        self.add_accel_group(self.ui.get_accel_group())

    def run(self):
        self.show_all()
        self.show()
        gtk.main()

    def open(self, filename):
        self.score = musicxml.Score(filename)
        self.update_buffer()
        gtk.recent_manager_get_default().add_item(filename)
        self.openFileActionGroup.set_sensitive(True)

    def get_insert_offset(self):
        return self.buffer.get_iter_at_mark(self.buffer.get_insert()
                                            ).get_offset()
    def update_buffer(self):
        offset = self.get_insert_offset()
        output = braillemusic.Embosser(width=self.widthWidget.get_value_as_int())
        output.format(self.score)
        self.buffer.set_text(unicode(output))
        self.displayedObjects = output.objectMap
        self.buffer.place_cursor(self.buffer.get_iter_at_offset(offset))
        self.lastFocusedObject = None

    def width_changed_cb(self, action):
        self.update_buffer()

    def mark_set_cb(self, buffer, new_location, mark):
        object = self.displayedObjects[new_location.get_offset()]
        if object is not None:
            if object != self.lastFocusedObject:
                self.status_bar.pop(0)
                self.status_bar.push(0, `object`)
                playback.play(object)
                self.lastFocusedObject = object
                if hasattr(object, 'pitch') and object.pitch:
                    self.editPitchedActionGroup.set_sensitive(True)
                else:
                    self.editPitchedActionGroup.set_sensitive(False)
        else:
            self.editPitchedActionGroup.set_sensitive(False)
    def key_press_cb(self, text_view, event):
        buffer = text_view.get_buffer()
        iter = buffer.get_iter_at_mark(buffer.get_insert())
        object = self.displayedObjects[iter.get_offset()]
        if event.keyval == gtk.keysyms.Return:
            if object:
                playback.play(object)
            return True
        else:
            self.status_bar.pop(0)
            self.status_bar.push(0, gtk.gdk.keyval_name(event.keyval))
        return False
    def octave_up_cb(self, action):
        object = self.displayedObjects[self.get_insert_offset()]
        if object and hasattr(object, 'pitch') and object.pitch:
            self.change_octave(object, 1)
    def octave_down_cb(self, action):
        object = self.displayedObjects[self.get_insert_offset()]
        if object and hasattr(object, 'pitch') and object.pitch:
            self.change_octave(object, -1)
    def change_octave(self, note, amount):
        note.pitch.octave += amount
        self.update_buffer()
    def file_open_cb(self, action):
        dialog = gtk.FileChooserDialog("Open..", self,
                                       gtk.FILE_CHOOSER_ACTION_OPEN,
                                       (gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL,
                                        gtk.STOCK_OPEN, gtk.RESPONSE_OK))
        dialog.set_default_response(gtk.RESPONSE_OK)

        dialog.add_filter(MusicXMLFilter())

        #dialog.hide()

        if dialog.run() == gtk.RESPONSE_OK:
            filename = dialog.get_filename()
            self.open(filename)
        dialog.destroy()

    def open_recent_cb(self, rec_chooser):
        ri = rec_chooser.get_current_item()
        fname = ri.get_uri()
        #if ri.exists():
        self.open(fname)

    def file_save_cb(self, action):
        self.score.save()

    def play_cb(self, action):
        playback.play(self.score)

    def file_quit_cb(self, action):
        raise SystemExit

    def help_about_cb(self, action):
        about = gtk.AboutDialog()
        for prop, val in {"name": "FreeDots",
                          "version": "0.1beta5",
                          "comments": "MusicXML to Braille music translation",
                          "copyright": "Copyright  2008 Mario Lang",
                          "license": __license__,
                          "authors": ["Mario Lang <mlang@delysid.org>"],
                          "website": "http://delysid.org/freedots.html"
                         }.iteritems():
            about.set_property(prop, val)
        about.connect("response", lambda self, *args: self.destroy())
        about.set_screen(self.get_screen())
        about.show_all()

    def delete_event_cb(self, window, event):
        gtk.main_quit()

if __name__ == '__main__':
    app = Application()
    app.run()
