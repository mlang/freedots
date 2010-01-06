"""A module for the MusicXML file format.

The main class to use is Score which wraps a MusicXML file in a Python
object.  Loading from URLs directly is supported as well.

All classes use parsed etree.Element objects as storage to preserve the
original XML as much as possible.
Properties are used to query and modify the XML in parsed form
directly.
Container classes instantiate child objects dynamically at iteration time.
Methods do typically implement higher level operations that require
special knowledge about the underlying file format.

This way we provide a useful abstraction layer on top
of raw MusicXML storage while still preserving all sorts of
not yet directly handled MusicXML features when saving Score objects
back to disk."""

import cStringIO
import itertools
import os.path
import urllib2
import zipfile

from utils.rational import Rational, lcm
try:
    from xml.etree import cElementTree as etree
except:
    from utils import ElementTree as etree

### Constants:

W3C_XML_SCHEMA_URL = 'http://www.musicxml.org/xsd/musicxml.xsd'

ACCENT, BREATH_MARK, STACCATO, STACCATISSIMO, TENUTO = range(5)

### Classes:

## Container classes (Score, Part, Measure)

class Score(object):
    """Top level object representing a MusicXML score contained in a file."""
    def __init__(self, filename):
        """Open and parse the given MusicXML file.
           score-timewise documents are automatically converted to a partwise
           representation for internal storage."""
        self._filename = filename
        self.archive = None
        self.document = None
        self.readOnly = False
        if not os.path.exists(filename):
            request = urllib2.Request(filename, None, {'User-Agent':
                                                       'FreeDots/0.5'})
            file = urllib2.urlopen(request)
            self.readOnly = True
        else:
            file = open(filename, "r")
        # Compressed MusicXML (.mxl)
        if os.path.splitext(filename)[1] == '.mxl':
            archive = zipfile.ZipFile(cStringIO.StringIO(file.read()), "r")
            container = etree.XML(archive.read("META-INF/container.xml"))
            rootfile = container.find("rootfiles/rootfile").get("full-path")
            self.document = etree.parse(cStringIO.StringIO(archive.read(rootfile)))
        else:
            self.document = etree.parse(file)
        file.close()
        root = self.document.getroot()
        self.wasTimewise = False
        if root.tag == 'score-timewise':
            parts = dict((part.get('id'),
                          etree.SubElement(root, part.tag, part.attrib))
                         for part in root.find('measure'))
            for measure in list(root.findall('measure')):
                for part in measure:
                    etree.SubElement(parts[part.get('id')],
                                     measure.tag, measure.attrib).extend(part)
                root.remove(measure)
            root.tag = 'score-partwise'
            self.wasTimewise = True
        if root.tag == 'score-partwise':
            self.partwise = root

    def write(self, filename):
        """Write this MusicXML score back to disk."""
        # Add XMLSchema location
        self.partwise.set('xmlns:xsi',
                          'http://www.w3.org/2001/XMLSchema-instance')
        self.partwise.set('xsi:noNamespaceSchemaLocation', W3C_XML_SCHEMA_URL)
        self.document.write(filename, xml_declaration=True)
    def save(self):
        if self.readOnly:
            raise IOError("File is not writeable")
        elif self.archive is not None:
            raise NotImplementedError
        else:
            self.write(self._filename)

    ## Properties:
    # work/work-number
    def _getWorkNumber(self):
        return self.partwise.findtext("work/work-number")
    def _setWorkNumber(self, newNumber):
        work = self.partwise.find("work")
        if work is not None:
            worknumber = work.find("work-number")
            if worknumber is not None:
                worknumber.text = newNumber
            else:
                etree.SubElement(work, "work-number").text = newNumber
    def _delWorkNumber(self):
        work = self.partwise.find("work")
        if work is not None:
            worknumber = work.find("work-number")
            if worknumber is not None:
                work.remove(worknumber)
                if len(work) == 0:
                    self.partwise.remove(work)
    worknumber = property(_getWorkNumber, _setWorkNumber, _delWorkNumber,
        """Work number.""")

    # work/work-title
    def _getWorkTitle(self):
        return self.partwise.findtext("work/work-title")
    def _setWorkTitle(self, newTitle):
        work = self.partwise.find("work")
        if work is not None:
            worktitle = work.find("work-title")
            if worktitle is not None:
                worktitle.text = newTitle
            else:
                etree.SubElement(work, "work-title").text = newTitle
    def _delWorkTitle(self):
        work = self.partwise.find("work")
        worktitle = work.find("work-title")
        if work is not None and worktitle is not None:
            work.remove(worktitle)
            if len(work) == 0:
                self.partwise.remove(work)
    worktitle = property(_getWorkTitle, _setWorkTitle, _delWorkTitle,
        """Work title.""")

    # movement-number
    def _getMovementNumber(self):
        return self.partwise.findtext("movement-number")
    def _setMovementNumber(self, newNumber):
        elem = self.partwise.find("movement-number")
        if elem is not None:
            elem.text = newNumber
        else:
            etree.SubElement(self.partwise, "movement-number").text = newNumber
    def _delMovementNumber(self):
        elem = self.partwise.find("movement-number")
        if elem is not None:
            self.partwise.remove(elem)
    movementnumber = property(_getMovementNumber, _setMovementNumber,
                              _delMovementNumber,
        """Movement number.""")

    # movement-title
    def _getMovementTitle(self):
        return self.partwise.findtext("movement-title")
    def _setMovementTitle(self, newTitle):
        elem = self.partwise.find("movement-title")
        if elem is not None:
            elem.text = newNumber
        else:
            etree.SubElement(self.partwise, "movement-title").text = newTitle
    def _delMovementTitle(self):
        elem = self.partwise.find("movement-title")
        if elem is not None:
            self.partwise.remove(elem)
    movementtitle = property(_getMovementTitle, _setMovementTitle,
                             _delMovementTitle,
        """Movement title.""")

    # identification/creator[@type='composer']
    def _getComposer(self):
        identification = self.partwise.find("identification")
        if identification is not None:
            for item in identification:
                if item.tag=="creator" and item.get("type")=="composer":
                    return item.text
    def _setComposer(self, composer):
        identification = self.partwise.find("identification")
        if identification is not None:
            for item in identification:
                if item.tag=="creator" and item.get("type")=="composer":
                    item.text = composer
                    return None
        raise NotImplementedError
    def _delComposer(self):
        identification = self.partwise.find("identification")
        if identification is not None:
            for item in list(identification):
                if item.tag=="creator" and item.get("type")=="composer":
                    identification.remove(item)
    composer = property(_getComposer, _setComposer, _delComposer,
        """Composer.""")

    def __iter__(self):
        """Iterate over all parts contained in this score."""
        for node in self.partwise.findall("part"):
            yield Part(self, node)

    def _getDivisions(self):
        divisions = set(int(e.text) for e in
                        self.partwise.findall("part/measure/attributes/divisions"))
        if len(divisions) > 1:
            return lcm(*divisions)
        else:
            return divisions.pop()
    divisions = property(_getDivisions, None, None,
        """The least common mupltiple of all present divisons.""")

    def __delitem__(self, index):
        """Delete a specific part either by name or numerical index."""
        if isinstance(index, basestring):
            for part in self:
                if part.name == index:
                    node = part.xml
            raise IndexError(index)
        else:
            node = list(self)[index].xml
        self.partwise.remove(node)
        partList = self.partwise.find('part-list')
        for elem in list(partList):
            if elem.tag=='score-part' and elem.get('id')==node.get('id'):
                partList.remove(elem)
    def __getitem__(self, index):
        """Retrieve a specific part either by name or numerical index."""
        if isinstance(index, basestring):
            for part in self:
                if part.name == index:
                    return part
            raise IndexError(index)
        return list(self)[index]

    def __repr__(self):
        if self.worktitle and self.worknumber:
            title = "title="+repr((self.worknumber, self.worktitle))
        elif self.worktitle:
            title = "title="+repr(self.worktitle)
        elif self.worknumber:
            title = "title="+repr(self.worknumber)
        else:
            title = ""
        if self.movementtitle and self.movementnumber:
            movement = "movement="+repr((self.movementnumber, self.movementtitle))
        elif self.movementtitle:
            movement = "movement="+repr(self.movementtitle)
        elif self.movementnumber:
            movement = "movement="+repr(self.movementnumber)
        else:
            movement = ""
        return "<%s %s>" % (self.__class__.__name__,
                               " ".join(filter(lambda x: x!="",
                                               [title,
                                                movement,
                                                repr(list(self))])))
    def read_part(self, node):
        part = Part()
        # Traverse the part-list for information matching our part ID
        for child in self.partList:
            if child.tag=="score-part" and child.get("id")==node.get("id"):
                for element in child:
                    if element.tag=="midi-instrument":
                        for item in element:
                            if item.tag=="midi-channel":
                                part.midi.channel = int(item.text)
                            elif item.tag=="midi-program":
                                part.midi.program = int(item.text)
        self.activeSlur = {}
        # Iterate over all children
        for child in node:
            if child.tag=="measure":
                measure = self.read_measure(child)
                measure.parent = part
                part.measures.append(measure)
        self.score.add(part)
    def read_measure(self, node):
        tick = 0
        measure = Measure(number=node.get("number"),
                          implicit=node.get('implicit', "no").lower() == "yes")
        self.currentMeasure = measure
        for child in node:
            if child.tag == 'print':
                if child.get('new-system', "no").lower() == "yes":
                    measure.newSystem = True
                elif child.get('new-page', "no").lower() == "yes":
                    measure.newSystem = True
            elif child.tag == 'attributes':
                for attrib in child:
                    if attrib.tag == 'transpose':
                        measure.transpose = Transposition(
                            chromaticSteps=int(attrib.findtext("chromatic")),
                            octave=int(attrib.findtext("octave-change") or 0))
                        if attrib.find("diatonic"):
                            measure.transpose.diatonicSteps = int(
                                attrib.findtext("diatonic"))
            elif child.tag == 'forward':
                tick += (int(child.findtext("duration"))
                         * self.durationMultiplier)
        return measure
    def readNote(self, node):
        for child in node:
            if child.tag == 'notations':
                for notation in child:
                    if notation.tag == 'slur':
                        number = int(notation.get('number', '1'))
                        if notation.get('type')=="start":
                            self.activeSlur[number] = Slur()
                        elif notation.get('type')=="stop" and number in self.activeSlur:
                            self.activeSlur[number].add(note)
                            del self.activeSlur[number]
        # FIXME: Calculate head and dots statically if not given
        for slur in self.activeSlur.itervalues():
            slur.add(note)
        return note

class Part(object):
    """A part of a MusicXML score."""
    def __init__(self, score, xml):
        self.score, self.xml = score, xml
    def _getName(self):
        for scorePart in self.score.partwise.findall("part-list/score-part"):
            if scorePart.get('id') == self.xml.get('id'):
                name = scorePart.findtext("part-name")
                if name is not None:
                    return name.strip()
    def _setName(self, newName):
        for scorePart in self.score.partwise.findall("part-list/score-part"):
            if scorePart.get('id') == self.xml.get('id'):
                partName = scorePart.find("part-name")
                if partName is not None:
                    partName.text = newName
                else:
                    etree.SubElement(scorePart, "part-name").text = newName
    def _delName(self):
        for scorePart in self.score.partwise.findall("part-list/score-part"):
            if scorePart.get('id') == self.xml.get('id'):
                partName = scorePart.find("part-name")
                if partName is not None:
                    scorePart.remove(partName)
    name = property(_getName, _setName, _delName,
        """Part name.""")

    def __repr__(self):
        return "<%s %s>" % (self.__class__.__name__,
                            self.name or repr(self.xml.get('id')))

    def __iter__(self):
        divisionMultiplier = 1
        currentClef = Clef('G')
        currentKeySignature = 0
        currentTimeSignature = (4,4)
        for node in self.xml.findall('measure'):
            measure = Measure(self, node)
            measure.divisionMultiplier = divisionMultiplier
            measure.currentClef = currentClef
            measure.currentKeySignature = currentKeySignature
            measure.currentTimeSignature = currentTimeSignature
            yield measure
            finalObject = list(measure)[-1]
            divisionMultiplier = finalObject.divisionMultiplier
            currentClef = finalObject.currentClef
            currentKeySignature = finalObject.currentKeySignature
            currentTimeSignature = finalObject.currentTimeSignature
    def key(self):
        for obj in self:
            return obj.key()
    def time(self):
        for obj in self:
            return obj.time()

class Measure(object):
    def __init__(self, part, xml):
        self.part, self.xml = part, xml

    ## Properties:
    def _getNumber(self):
        return self.xml.get('number')
    def _setNumber(self, newNumber):
        self.xml.put('number', str(newNumber))
    number = property(_getNumber, _setNumber, None,
        """Measure number.""")
    def _getImplicit(self):
        return self.xml.get('implicit', "no").strip() == "yes"
    def _setImplicit(self, boolean):
        self.xml.put('implicit', boolean and "yes" or "no")
    implicit = property(_getImplicit, _setImplicit, None,
        """Boolean implicit attribute.""")

    ## Iteration:
    def __iter__(self):
        """Iterate over all music-data elements in this measure."""
        divisionMultiplier = self.divisionMultiplier
        currentClef = self.currentClef
        currentKeySignature = self.currentKeySignature
        currentTimeSignature = self.currentTimeSignature
        for node in self.xml:
            obj = globals()[node.tag.capitalize()](self, node)
            if isinstance(obj, Attributes):
                if obj.divisions is not None:
                    divisionMultiplier = self.part.score.divisions/obj.divisions
                if obj.clef is not None and obj.clef != currentClef:
                    currentClef = obj.clef
                if obj.key is not None:
                    currentKeySignature = obj.key
                if obj.time is not None:
                    currentTimeSignature = obj.time
            obj.divisionMultiplier = divisionMultiplier
            obj.currentClef = currentClef
            obj.currentKeySignature = currentKeySignature
            obj.currentTimeSignature = currentTimeSignature
            yield obj

    def key(self):
        for obj in self:
            if obj.currentKeySignature is not None:
                return obj.currentKeySignature
    def time(self):
        for obj in self:
            if obj.currentTimeSignature:
                return obj.currentTimeSignature
    def ticks(self):
        return int(Rational(*self.time())/(Rational(1,4)/(self.part.score.divisions*self.divisionMultiplier)))
    def num(self):
        return int(self.number)

    @property
    def musicdata(self):
        tick = 0
        val = TimestepSequence()
        objects = list(self)
        while len(objects)>0:
            obj = objects.pop(0)
            obj.startTick = tick
            if isinstance(obj, Note):
                if len(objects) > 0 and isinstance(objects[0], Note) and \
                   objects[0].chord:
                    currentClef = obj.currentClef
                    currentKeySignature = obj.currentKeySignature
                    currentTimeSignature = obj.currentTimeSignature
                    obj = Chord(obj)
                    obj.currentClef = currentClef
                    obj.currentKeySignature = currentKeySignature
                    obj.currentTimeSignature = currentTimeSignature
                    obj.measure = self
                    obj.startTick = tick
                    while len(objects) > 0 and isinstance(objects[0], Note) and \
                          objects[0].chord:
                        note = objects.pop(0)
                        note.startTick = tick
                        obj.add(note)
                    tick += obj.notes[0].duration
                else:
                    tick += obj.duration
                val.append(obj)
            elif isinstance(obj, Backup):
                tick -= obj.duration
        return val
    def staves(self):
        for obj in self:
            if isinstance(obj, Attributes) and obj.staves is not None:
                return obj.staves
        return 1
    def staff(self, index):
        return self.musicdata.staff(index)
    def lyrics(self):
        string = u""
        for note in self.musicdata.justNotes():
            if note.lyric:
                string += unicode(note.lyric)
        return string
    def newSystem(self):
        for obj in self:
            if isinstance(obj, Print) and obj.newSystem is not None:
                return obj.newSystem

class TimestepSequence(object):
    def __init__(self, *items):
        self._data = list(items)
    def append(self, item):
        self._data.append(item)
    def index(self, value):
        return self._data.index(value)
    def __iter__(self):
        return iter(self._data)
    def __len__(self):
        return len(self._data)
    def __getitem__(self, index):
        return self._data[index]
    def __setitem__(self, index, value):
        self._data[index]=value
    def __eq__(self, other):
        return isinstance(other, type(self)) and self._data==other._data
    def justNotes(self):
        return self.__class__(*filter(lambda obj: isinstance(obj, (Note, Chord)),
                                      self._data))
    def equalNotes(self, other):
        return self.justNotes()==other.justNotes()
    def staves(self):
        staff_dict = {}
        for item in self._data:
            staff_dict.update([(item.staff,item)])
        return len(staff_dict)
    def staff(self, index):
        staff_dict = {}
        for item in self._data:
            if isinstance(item, (Note, Chord)):
                if not item.staff in staff_dict:
                    staff_dict[item.staff] = []
                staff_dict[item.staff].append(item)
        return self.__class__(*staff_dict.values()[index])
    def voices(self):
        voice_dict = {}
        for item in self.justNotes():
            voice_dict.update([(item.voice,item)])
        return len(voice_dict)
    def voice(self, index):
        voice_dict = {}
        for item in self.justNotes():
            if not item.voice in voice_dict: voice_dict[item.voice]=[]
            voice_dict[item.voice].append(item)
        return self.__class__(*voice_dict.values()[index])

### Classes implementing music-data elements and their children

class Musicdata(object):
    def __init__(self, measure, xml):
        self.measure, self.xml = measure, xml

class Attributes(Musicdata):
    def _getDivisions(self):
        elem = self.xml.find('divisions')
        if elem is not None:
            return int(elem.text)
    def _delDivisions(self):
        elem = self.xml.find('divisions')
        if elem is not None:
            self.xml.remove(elem)
    divisions = property(_getDivisions, None, _delDivisions,
        """A new divisions value.""")
    def _getClef(self):
        node = self.xml.find('clef')
        if node is not None:
            return ClefWrapper(self, node)
    def _delClef(self):
        node = self.xml.find('clef')
        if node is not None:
            self.xml.remove(node)
    clef = property(_getClef, None, _delClef,
       """A clef change.""")

    def _getKey(self):
        key = self.xml.find('key')
        if key is not None:
            fifths = key.find('fifths')
            if fifths is not None:
                return int(fifths.text)
    def _delKey(self):
        key = self.xml.find('key')
        if key is not None:
            self.xml.remove(key)
    key = property(_getKey, None, _delKey,
        "A new key signature.""")

    def _getTime(self):
        elem = self.xml.find('time')
        if elem is not None:
            beats = elem.find('beats')
            beatType = elem.find('beat-type')
            if beats is not None and beatType is not None:
                return (int(beats.text), int(beatType.text))
    def _delTime(self):
        elem = self.xml.find('time')
        if elem is not None:
            self.xml.remove(elem)
    time = property(_getTime, None, _delTime,
        """A new time signature.""")
    def _getStaves(self):
        elem = self.xml.find('staves')
        if elem is not None:
            return int(elem.text)
    def _setStaves(self, newStaves):
        elem = self.xml.find('staves')
        if elem is not None:
            elem.text = int(newStaves)
        else:
            raise NotImplementedError
    def _delStaves(self):
        elem = self.xml.find('staves')
        if elem is not None:
            self.xml.remove(elem)
    staves = property(_getStaves, _setStaves, _delStaves,
        """A new number of staves.""")

class Clef(object):
    """A clef (from the French for "key") is a musical symbol used to indicate
    the pitch of written notes.  Placed on one of the lines at the
    beginning of the staff, it indicates the name and pitch of the notes on
    that line.  This line serves as a reference point by which the names of
    the notes on any other line or space of the staff may be determined."""
    defaultLines = {'G': 2, 'F': 4, 'C': 3}
    def __init__(self, sign='G', line=None, octaveChange=None):
        self.sign = sign
        if line:
            self.line = line
        else:
            if self.sign in Clef.defaultLines:
                self.line = Clef.defaultLines[self.sign]
        self.octaveChange = octaveChange
        self.staff = None
    def __eq__(self, other):
        return self.sign == other.sign and \
               self.line == other.line and \
               self.octaveChange == other.octaveChange
    def __repr__(self):
        return "<%s sign=%r>" % (self.__class__.__name__, self.sign)

class ClefWrapper(Clef):
    """A clef (from the French for "key") is a musical symbol used to indicate
    the pitch of written notes.  Placed on one of the lines at the
    beginning of the staff, it indicates the name and pitch of the notes on
    that line.  This line serves as a reference point by which the names of
    the notes on any other line or space of the staff may be determined."""
    def __init__(self, attributes, xml):
        self.attributes, self.xml = attributes, xml
    def _getSign(self):
        node = self.xml.find('sign')
        if node is not None:
            return node.text
    sign = property(_getSign, None, None,
        """The clef sign (G, F, C).""")
    def _getLine(self):
        node = self.xml.find('line')
        if node is not None:
            return int(node.text)
    line = property(_getLine, None, None,
        """The line this clef is on.""")
    def _getOctaveChange(self):
        node = self.xml.find('octave-change')
        if node is not None:
            return int(node.text)
    octaveChange = property(_getOctaveChange, None, None,
        """Octave change implied by this clef.""")

class Barline(Musicdata):
    pass

class Direction(Musicdata):
    pass

class Print(Musicdata):
    def _getNewPage(self):
        return self.xml.get('new-page', "no").lower() == "yes"
    def _setNewPage(self, boolean):
        self.xml.put('new-page', boolean and "yes" or "no")
    newPage = property(_getNewPage, _setNewPage, None,
        """Indicates the start of a new page.""")

    def _getNewSystem(self):
        return self.xml.get('new-system', "no").lower() == "yes"
    def _setNewSystem(self, boolean):
        self.xml.put('new-system', boolean and "yes" or "no")
    newSystem = property(_getNewSystem, _setNewSystem, None,
        """Indicates the start of a new system.""")

class Sound(Musicdata):
    def _getDynamics(self):
        dynamics = self.node.get('dynamics')
        if dynamics is not None:
            return int(dynamics.text)
    def _setDynamics(self, newDynamics):
        self.xml.put('dynamics', str(newDynamics))
    dynamics = property(_getDynamics, _setDynamics, None)

class RhythmicElement(Musicdata):
    def _getDuration(self):
        duration = self.xml.find('duration')
        if duration is not None:
            return int(duration.text)*self.divisionMultiplier
        return 0
    duration = property(_getDuration, None, None,
        """The duration in divisions.""")
    def __repr__(self):
        return "<%s duration=%r>" % (self.__class__.__name__, self.duration)

class Backup(RhythmicElement):
    pass

class Note(RhythmicElement):
    headnames = ['long', 'breve', 'whole', 'half', 'quarter', 'eighth',
                 '16th', '32nd', '64th', '128th', '256th']

    ## Properties:
    def _getChord(self):
        return self.xml.find('chord') is not None or False
    chord = property(_getChord, None, None,
        """Boolean indicating if this note is part of a chord.""")

    def _getNoteHead(self):
        notetype = self.xml.find("type")
        if notetype is not None and notetype.text:
            return self.__class__.headnames.index(notetype.text.strip().lower())
    def _setNoteHead(self, newHead):
        notetype = self.xml.find("type")
        if notetype is not None:
            if isinstance(newHead, basestring):
                if newHead in self.__class__.notenames:
                    notetype.text = newHead
                else:
                    raise ValueError("Invalid note type %r" % newHead)
            else:
                notetype.text = self.__class__.notenames[newHead]
        else:
            raise NotImplementedError
    def _delNoteHead(self):
        notetype = self.xml.find("type")
        if notetype is not None:
            self.xml.remove(notetype)
    notehead = property(_getNoteHead, _setNoteHead, _delNoteHead,
        """The note head (a positive integer).""")

    def _getDots(self):
        return len(list(self.xml.findall('dot')))
    def _setDots(self, dotCount):
        if self.dots > 0:
            if dotCount > self.dots:
                for i, elem in enumerate(self.xml):
                    if elem.tag == "dot":
                        for j in range(dotCount - self.dots):
                            self.xml.insert(i, 'dot')
                        return None
            elif dotCount < self.dots:
                for elem in list(self.xml.findall('dot')):
                    if dotCount < self.dots:
                        self.xml.remove(elem)
        else:
            if dotCount > 0:
                raise NotImplementedError
    dots = property(_getDots, _setDots, None,
        """The amount augmentation dots attached to this note.""")

    def getValue(self):
        """Return the note value including possibly augmentations by dots."""
        return (2*Rational(4, 2**self.notehead)
                - Rational(4, 2**self.notehead)/2**self.dots)

    def _getAccidental(self):
        node = self.xml.find('accidental')
        if node is not None:
            return node.text.strip().lower()
    def _setAccidental(self, newAccidental):
        node = self.xml.find('accidental')
        if node is not None:
            node.text = newAccidental.strip().lower()
        else:
            raise NotImplementedError
    def _delAccidental(self):
        node = self.xml.find('accidental')
        if node is not None:
            self.xml.remove(node)
    accidental = property(_getAccidental, _setAccidental, _delAccidental,
        """A graphical accidental.""")

    def _getPitch(self):
        node = self.xml.find('pitch')
        if node is not None:
            return Pitch(self, node)
    pitch = property(_getPitch, None, None,
        """The note pitch (or None if this note is a rest).""")

    def slursRight(self):
        """FIXME: not implemented, interface needs change too."""
        return 0

    def _getLyric(self):
        node = self.xml.find('lyric')
        if node is not None:
            return Lyric(self, node)
    lyric = property(_getLyric, None, None,
        """>Lyric attached to this note.""")

    def _getStaff(self):
        elem = self.xml.find('staff')
        if elem is not None:
            return int(elem.text)
    staff = property(_getStaff, None, None,
        """The staff this object is on.""")

    def _getVoice(self):
        elem = self.xml.find('voice')
        if elem is not None:
            return int(elem.text)
    voice = property(_getVoice, None, None,
        """The voice this object belongs to.""")

    def _getArticulations(self):
        resultSet = set()
        notations = self.xml.find('notations')
        if notations is not None:
            articulations = notations.find('articulations')
            if articulations is not None:
                articulationsMap = {'accent': ACCENT,
                                    'breath-mark': BREATH_MARK,
                                    'staccato': STACCATO,
                                    'staccatissimo': STACCATISSIMO,
                                    'tenuto': TENUTO}
                for articulation in articulations:
                    if articulation.tag in articulationsMap:
                        resultSet.add(articulationsMap[articulation.tag])
        return resultSet
    def _delArticulations(self):
        notations = self.xml.find('notations')
        if notations is not None:
            articulations = notations.find('articulations')
            if articulations is not None:
                notations.remove(articulations)
    articulations = property(_getArticulations, None, _delArticulations,
        """Articulations attached to this note.""")
    def _getFingering(self):
        notations = self.xml.find('notations')
        if notations is not None:
            technical = notations.find('technical')
            if technical is not None:
                if technical.find('fingering'):
                    return int(technical.findtext('fingering'))
                elif technical.find('open-string'):
                    return 0
    fingering = property(_getFingering, None, None,
        """Fingering attached to this note (0 indicates open string).""")

    def __repr__(self):
        info = ""
        if self.notehead is not None:
            info = self.__class__.headnames[self.notehead]
        if self.dots > 0:
            info += " (%d dots, %r)" % (self.dots, self.getValue())
        return "<%s %s>" % (self.__class__.__name__, info)

class Lyric(object):
    def __init__(self, note, xml):
        self.note, self.xml = note, xml

    ## Properties:
    def _getText(self):
        node = self.xml.find('text')
        if node is not None:
            return node.text
    def _setText(self, newText):
        node = self.xml.find('text')
        if node is not None:
            node.text = newText
        else:
            raise NotImplementedError
    def _delText(self):
        node = self.xml.find('text')
        if node is not None:
            self.xml.remove(node)
    text = property(_getText, _setText, _delText,
        """"The text of this lyric element.""")

    def _getSyllabic(self):
        node = self.xml.find('syllabic')
        if node is not None:
            return node.text
    def _setSyllabic(self, newSyllabic):
        node = self.xml.find('syllabic')
        if node is not None:
            node.text = newSyllabic
        else:
            raise NotImplementedError
    def _delSyllabic(self):
        node = self.xml.find('text')
        if node is not None:
            self.xml.remove(node)
    syllabic = property(_getSyllabic, _setSyllabic, _delSyllabic,
        """The syllabic information.""")

    def __str__(self):
        string = self.text
        if self.syllabic == 'single' or self.syllabic == 'end':
            string += " "
        return string

class Pitch(object):
    stepnames = ['C', 'D', 'E', 'F', 'G', 'A', 'B']
    def __init__(self, note, xml):
        self.note, self.xml, = note, xml
    ## Properties:
    def _getStep(self):
        return self.__class__.stepnames.index(
            self.xml.findtext('step').strip().upper()
            )
    def _setStep(self, newStep):
        if isinstance(newStep, basestring):
            self.xml.find('step').text = newStep.strip().upper()
        else:
            self.xml.find('step').text = self.__class__.stepnames[newStep]
    step = property(_getStep, _setStep, None,
        """The diatonic step (C to B or 0 to 6).""")

    def _getOctave(self):
        return int(self.xml.findtext('octave'))
    def _setOctave(self, newOctave):
        self.xml.find('octave').text = str(newOctave)
    octave = property(_getOctave, _setOctave, None,
        """The octave.""")

    def _getAlter(self):
        alter = self.xml.find('alter')
        if alter is not None:
            return float(alter.text)
        return 0
    def _setAlter(self, newAlter):
        alter = self.xml.find('alter')
        if alter is not None:
            alter.text = str(newAlter)
        else:
            if newAlter != 0:
                alter = etree.Element('alter')
                alter.text = str(newAlter)
                self.xml.insert(1, alter)
    def _delAlter(self):
        alter = self.xml.find('alter')
        if alter is not None:
            self.xml.remove(alter)
    alter = property(_getAlter, _setAlter, _delAlter,
        """The chromatic alteration (possibly a decimal value).""")

    def __eq__(self, other):
        return isinstance(other, type(self)) and \
               self.step==other.step and \
               self.octave==other.octave and \
               self.alter==other.alter

    def __repr__(self):
        alter = ""
        if self.alter:
            alter = ", alter=%r" % self.alter
        return "%s(%r, %r%s)" % (self.__class__.__name__,
                                 Pitch.stepnames[self.step],
                                 self.octave, alter)

    def getMIDIpitch(self):
        return min(int(((self.octave+1)*12)
                       + [0,2,4,5,7,9,11][self.step]
                       + self.alter),
                   127)

class Chord(object):
    class IntervalIterator(object):
        def __init__(self, chord, descending):
            notes = chord.notes[:]
            notes.sort(semitoneDifference)
            if descending:
                notes.reverse()
            self.noteIter = iter(notes)
            self.fromNote = self.noteIter.next()
        def __iter__(self):
            return self
        def next(self):
            fromNote, toNote = self.fromNote, self.noteIter.next()
            diatonicSteps = (toNote.pitch.step+(toNote.pitch.octave*7)) - (fromNote.pitch.step+(fromNote.pitch.octave*7))
            self.fromNote = toNote
            return (fromNote, diatonicSteps, toNote)
    # FIXME: Add support for chords across multiple staves (and voices?)
    def __init__(self, *notes):
        self.notes = list(notes)
        self.staff = None
        self.voice = None
        self.measure = None
    def add(self, note):
        if note.pitch:
            self.notes.append(note)
        # FIXME
        self.staff = note.staff
        self.voice = note.voice
    def note(self, index, descending=False):
        notes = self.notes[:]
        notes.sort(semitoneDifference)
        if descending:
            notes.reverse()
        return notes[index]
    @property
    def lyric(self):
        self.note(0).lyric
    def iterintervals(self, descending=False):
        return Chord.IntervalIterator(self, descending)
    def clef(self):
        """Return the clef that is active for this chord.

        Iterates backwards in time over all elements to determine the last
        clef change.  If no clef is found, the G-clef is returned."""
        return self.currentClef

def semitoneDifference(a, b):
    return a.pitch.getMIDIpitch()-b.pitch.getMIDIpitch()

class Accidentals(object):
    def __init__(self):
        self.steps = [0]*(10*7)
    def _setSig(self, octaveSegment):
        for octave in range(10):
            for i, value in enumerate(octaveSegment):
                self.steps[(octave*7)+i] = value
    def getAlter(self, octave, step):
        return self.steps[(octave*7)+step]
    def newAccidental(self, octave, step, accidental):
        self.steps[(octave*7)+step] = ['flat','natural','sharp'].index(
            accidental)-1
    def newKeySignature(self, fifths):
        accidentals = [None]*15
        a = ([-1]*6)+[-2]
        j = 6
        for i in xrange(-7, 8):
            a[j] = a[j] + 1
            j = (j+4) % 7
            accidentals[i]=tuple(a)
        self._setSig(accidentals[fifths])
