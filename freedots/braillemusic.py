# -*- coding: utf8 *-*
"""This module is responsible for braille music formatting.

It contains all the constants and code relevant to braille music."""

import itertools
import textwrap

from musicxml import ACCENT, BREATH_MARK, STACCATO, STACCATISSIMO, TENUTO
from musicxml import TimestepSequence, Clef, Note, Chord
from utils.rational import Rational

# If you are using emacs, go to the end of the following string and hit C-x C-e
"""
(defun brl-insert (dots)
  "Insert a Unicode Braille character.
DOTS specifies the dots in decimal encoding.
If called interactively (M-x brl-insert RET) it prompts for the dot pattern."
  (interactive "nDots: ")
  (let ((unicode #X2800))
    (while (> dots 0)
      (setq unicode (logior unicode (ash 1 (1- (% dots 10))))
            dots (/ dots 10)))
    (ucs-insert unicode)))
"""

# Braille music symbols:
note_symbols = [{2:u'⠽', 3:u'⠝', 4:u'⠹', 5:u'⠙'},
                {2:u'⠵', 3:u'⠕', 4:u'⠱', 5:u'⠑'},
                {2:u'⠯', 3:u'⠏', 4:u'⠫', 5:u'⠋'},
                {2:u'⠿', 3:u'⠟', 4:u'⠻', 5:u'⠛'},
                {2:u'⠷', 3:u'⠗', 4:u'⠳', 5:u'⠓'},
                {2:u'⠮', 3:u'⠎', 4:u'⠪', 5:u'⠊'},
                {2:u'⠾', 3:u'⠞', 4:u'⠺', 5:u'⠚'}]
rest_symbols  = {2:u'⠍', 3:u'⠥', 4:u'⠧', 5:u'⠭'}

octave_symbols = {1: u'⠈⠈',
                  2: u'⠈',
                  3: u'⠘',
                  4: u'⠸',
                  5: u'⠐',
                  6: u'⠨',
                  7: u'⠰',
                  8: u"⠠",
                  9: u"⠠⠠"}
dot_symbol = u'⠄'
accidental_symbols = {'sharp':   u'⠩', 'double-sharp': u'⠩⠩',
                      'flat':    u'⠣', 'double-flat':  u'⠣⠣',
                      'natural': u'⠡'}
accidental_symbols['sharp-sharp'] = accidental_symbols['double-sharp']
accidental_symbols['flat-flat'] = accidental_symbols['double-flat']
commontime_symbol = u'⠨⠉'
interval_symbols = {1: u'⠌',
                    2: u'⠬',
                    3: u'⠼',
                    4: u'⠔',
                    5: u'⠴',
                    6: u'⠒',
                    7: u'⠤'}
clef_symbols = {Clef('G'): u'⠜⠌⠇',
                Clef('G', octaveChange=-1): u'⠜⠌⠇⠼⠦',
                Clef('G', octaveChange=1): u'⠜⠌⠇⠼⠓',
                Clef('F'): u'⠜⠼⠇',
                Clef('C'): u'⠜⠬⠇',
                Clef('C', 4): u'⠜⠬⠐⠇'}
hand_symbols = [u'⠨⠜', u'⠸⠜']
simile_symbol = u'⠶'
voice_separation_symbol = u'⠣⠜'
endofmusic_symbol = u'⠣⠅'
slur_symbol = u'⠉'
finger_symbols = {1:u'⠁', 2:u'⠃', 3:u'⠇', 4:u'⠂', 5:u'⠅'}
articulation_symbols = {ACCENT: u'⠨⠦',
                        BREATH_MARK: u'⠜⠂',
                        STACCATO: u'⠦',
                        STACCATISSIMO: u'⠠⠦',
                        TENUTO: u'⠸⠦'}

def note2braille(note, lastNote=None):
    def omitOctaveSymbol(note, lastNote=None):
        # If the last note printed is unknown, always print an octave sign
        if lastNote is None:
            return False
        halfSteps = note.pitch.getMIDIpitch()-lastNote.pitch.getMIDIpitch()
        # If the interval is greater than a fifth, print an octave sign
        if abs(halfSteps) > 7:
            return False
        if abs(halfSteps) >= 5 and abs(halfSteps) <= 7:
            if note.pitch.octave != lastNote.pitch.octave:
                return False
        return True

    output = u''
    for articulation in note.articulations.intersection(articulation_symbols):
        output += articulation_symbols[articulation]

    if note.pitch:
        if note.accidental:
            output += accidental_symbols[note.accidental]
        if not omitOctaveSymbol(note, lastNote):
            output += octave_symbols[note.pitch.octave+1]
        table = note_symbols[note.pitch.step]
    else:
        table = rest_symbols
    head = note.notehead
    dots = note.dots
    if head > 5 and head < 10:
        head -= 4

    output += table[head] + dot_symbol*dots

    if note.fingering is not None:
        if note.fingering >= 1:
            output += finger_symbols[note.fingering]

    return output

def brailleNumber(n,d=None):
    n_digits = {0:u'⠚',1:u'⠁',2:u'⠃',
                3:u'⠉',4:u'⠙',5:u'⠑',
                6:u'⠋',7:u'⠛',8:u'⠓',9:u'⠊'}
    d_digits = {0:u'⠴',1:u'⠂',2:u'⠆',
                3:u'⠒',4:u'⠲',5:u'⠢',
                6:u'⠖',7:u'⠶',8:u'⠦',9:u'⠔'}
    symbols = u'⠼'
    n_d = []
    while n > 0:
        n_d.append(n%10)
        n=int(n/10) # must take int because Python 3k will convert it to a float
    n_d.reverse()
    d_d = []
    if d is not None:
        while d > 0:
            d_d.append(d%10)
            d=int(d/10)
        d_d.reverse()
    for digit in n_d:
        symbols+=n_digits[digit]
    for digit in d_d:
        symbols+=d_digits[digit]
    return symbols

def keySignature(fifths):
    if fifths > 3:
        return brailleNumber(fifths)+accidental_symbols['sharp']
    elif fifths > 0:
        return accidental_symbols['sharp']*fifths
    elif fifths < -3:
        return brailleNumber(abs(fifths))+accidental_symbols['flat']
    elif fifths < 0:
        return accidental_symbols['flat']*abs(fifths)
    return u''

def timeSignature(nominator, denominator):
    if nominator==4 and denominator==4:
        return commontime_symbol
    return brailleNumber(nominator, denominator)

class AbstractFormatter(object):
    def __init__(self):
        self.lastNote = None
        self.lastMusicdata = None
    def resetState(self):
        self.lastNote = None
        self.lastMusicdata = TimestepSequence()
    # Abstract methods which can be overriden in concrete implementations
    def startOfScore(self, score):
        pass
    def whitespace(self, count=1, object=None):
        self.addSymbol(u'⠀'*count, object)
    def startOfPart(self, part):
        if part.name:
            self.addSymbol(part.name, part)
            self.whitespace()
        self.addSymbol(keySignature(part.key())+timeSignature(*part.time()))
    def endOfPart(self):
        self.addSymbol(endofmusic_symbol)
    def newLine(self):
        pass
    def newSystem(self, system):
        self.newLine()
        self.addSymbol(brailleNumber(system[0].num())
                      +u'⠤'+brailleNumber(system[-1].num()), system)
    def startOfStaff(self, staff):
        pass
    def startOfMeasure(self, measure):
        pass
    def addSymbol(self, symbol, object=None):
        pass
    def measureElement(self, symbol, object):
        self.addSymbol(symbol, object)

    # Main entry-point of abstract algorithm
    def format(self, object):
        getattr(self, 'format'+object.__class__.__name__)(object)
    def formatScore(self, score):
        self.startOfScore(score)
        for part in score:
	    self.format(part)
    def formatPart(self, part):
        def layoutType(measure):
            return (measure.staves(), bool(measure.lyrics()), False)
        self.resetState()
        self.startOfPart(part)
        layouts = [(type, list(i))
                    for type, i in itertools.groupby(part, layoutType)]
        if len(layouts) > 1:
            index = 0
            while index < len(layouts)-1:
                if layouts[index][0] == (1, False, False) and \
                       layouts[index+1][0] == (1, True, False):
                    layouts[index+1] = (layouts[index+1][0],
                                        layouts[index][1]+layouts[index+1][1])
                    layouts.pop(index)
                elif layouts[index][0] == (1, True, False) and \
                         layouts[index+1][0] == (1, False, False):
                    layouts[index] = (layouts[index][0],
                                      layouts[index][1]+layouts[index+1][1])
                    layouts.pop(index+1)
                else:
                    index += 1
        for layout, measures in layouts:
            self.processLayout(layout, measures)
        self.endOfPart()
    def processLayout(self, (staves, lyric, harmony), measures):
        def groupBySystems(measures):
            system = []
            for measure in measures:
                if measure.newSystem() and len(system):
                    yield system
                    system = []
                system.append(measure)
            if len(system):
                yield system
        def groupByLyricLines(measure, width):
            system = []
            for measure in measures:
                system.append(measure)
                if len(reduce(lambda x,y: x+y.lyrics(), system, "")) > width:
                    if len(system) > 1:
                        tmp = system.pop(-1)
                        yield system
                        system = [tmp]
                    else:
                        yield system
                        system = []
            if len(system):
                yield system
        # Process various layout types individually
        if staves == 1 and not lyric and not harmony:
            for system in groupBySystems(measures):
                self.newSystem(system)
                for measure in system:
                    self.startOfMeasure(measure)
                    self.printBrailleForStaff(measure.staff(0))
        elif staves == 2 and not lyric and not harmony:
            for system in groupBySystems(measures):
                self.newSystem(system)
                for staff in range(2):
                    self.startOfStaff(staff)
                    for i, measure in enumerate(system):
                        self.startOfMeasure(measure)
                        if i == 0:
                            self.addSymbol(hand_symbols[staff], None)
                        self.printBrailleForStaff(measure.staff(staff))
        elif staves == 1 and lyric and not harmony:
            for system in groupByLyricLines(measures, self.width):
                self.newLine()
                [self.addSymbol(unicode(note.lyric), note)
                 for measure in system for note in measure.musicdata.justNotes()
                 if note.lyric]
                self.newLine()
                self.whitespace()
                for measure in system:
                    self.startOfMeasure(measure)
                    self.printBrailleForStaff(measure.staff(0))
        else:
            raise TypeError("Unsupported layout (%d, %r, %r)"
                      % (staves, lyric, harmony))
    def printBrailleForStaff(self, musicdata):
        if musicdata.equalNotes(self.lastMusicdata):
            self.addSymbol(simile_symbol, musicdata)
        else:
            for v in range(musicdata.voices()):
                if self.lastMusicdata.voices() == musicdata.voices() and \
                   self.lastMusicdata.voice(v).equalNotes(musicdata.voice(v)):
                    self.measureElement(simile_symbol, musicdata.voice(v))
                else:
                    self.printBrailleForVoice(musicdata.voice(v))
                if v != musicdata.voices()-1:
                    self.measureElement(voice_separation_symbol, None)
                    self.lastNote = None
            if musicdata.voices()>1:
                self.lastNote = None
        self.lastMusicdata = musicdata
    def printBrailleForVoice(self, musicdata):
        for note in musicdata:
            self.measureElement(
                getattr(self, 'getBrailleFor%s'%note.__class__.__name__)(note),
                note)
    def getBrailleForNote(self, note):
        output = note2braille(note, self.lastNote)
        if note.pitch:
            self.lastNote=note
        if note.slursRight()>0 and note.slursRight()<4:
            output += slur_symbol
        return output
    def getBrailleForChord(self, chord):
        descending = chord.clef() == Clef('G')
        startNote=chord.note(0,descending)
        output = note2braille(startNote, self.lastNote)
        for first, interval, second in chord.iterintervals(descending):
            if second.accidental:
                output += accidental_symbols[second.accidental]
            if abs(interval) > 7:
                output += octave_symbols[second.pitch.octave+1]
            output += interval_symbols[((abs(interval)-1)%7)+1]
            if second.fingering is not None:
                if second.fingering >= 1:
                    output += finger_symbols[second.fingering]
        self.lastNote = startNote
        return output

class Embosser(AbstractFormatter):
    def __init__(self, width=32, lines=24):
        self.width, self.lines = width, lines
        self.pages = []
        self.currentLine = []
        super(Embosser, self).__init__()
    def __unicode__(self):
        self.objectMap = []
        output = u''
        for page in self.pages:
            for line in page:
                for symbol, object in line:
                    output += symbol
                    self.objectMap += [object]*len(symbol)
                output += u'\n'
                self.objectMap += [None]
            output += u'\n'
            self.objectMap += [None]
        return output
    def newPage(self):
        self.currentPage = []
        self.pages.append(self.currentPage)
        # Reset state so that octave signs get reprinted on a new page
        self.lastNote = None
    def addLineToPage(self, line):
        if len(self.currentPage)>=self.lines:
            self.newPage()
        self.currentPage.append(line)            
    def newLine(self):
        self.addLineToPage(self.currentLine)
        self.currentLine = []
    def addCenteredLine(self, text, object=None):
        for line in textwrap.wrap(unicode(text), self.width):
            self.addLineToPage([(line.center(self.width, u'⠀'), object)])
    def startOfScore(self, score):
        self.newPage()
        if score.composer:
            self.addCenteredLine(score.composer)
        if score.worktitle and score.worknumber:
            self.addCenteredLine(u"%s - %s" % (score.worknumber,
                                               score.worktitle))
        elif score.worktitle or score.worknumber:
            self.addCenteredLine(score.worktitle or score.worknumber)
        if score.movementtitle and score.movementnumber:
            self.addCenteredLine(u"%s - %s" % (score.movementnumber,
                                               score.movementtitle))
        elif score.movementtitle:
            self.addCenteredLine(score.movementtitle)
    def endOfPart(self):
        super(Embosser, self).endOfPart()
        self.newLine()
    def startOfStaff(self, staff):
        if staff == 1:
            self.newLine()
            self.whitespace()
    def startOfMeasure(self, measure):
        self.whitespace(object=measure)
    def currentLineLength(self):
        return len(u''.join(elem[0] for elem in self.currentLine))
    def addSymbol(self, symbol, object=None):
        if self.currentLineLength()+len(symbol) > self.width:
            self.newLine()
        self.currentLine += [(symbol, object)]
    def measureElement(self, symbol, object=None):
        if self.currentLineLength()+len(symbol) >= self.width:
            if self.currentLine[-1][0] == u'⠀':
                self.currentLine.pop(-1)
            else:
                self.currentLine += [(u'⠐', None)]
            self.newLine()
        self.currentLine += [(symbol, object)]
