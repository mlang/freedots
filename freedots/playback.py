from utils import midi
from music import *

class MidiWriter:
    def __init__(self, filename):
        self.midistream = None
        self.filename = filename
        self.tick = 0
    def write(self, what):
        getattr(self, 'write_%s' % what.__class__.__name__.lower())(what)
    def write_score(self, score):
        map(self.write, score)
    def write_list(self, system):
        map(self.write, system)
    def write_part(self, part):
        self.tick = 0
        if not self.midistream:
            self.midistream = midi.new_stream(50, part.score.divisions)
            e = midi.ProgramChangeEvent()
            e.value = 1#part.midi.program
            self.midistream.add_event(e)
        else:
            self.midistream.add_track()
            e = midi.ProgramChangeEvent()
            e.value = 1#part.midi.program
            self.midistream.add_event(e)
        map(self.write, part)
    def write_measure(self, measure):
        if not self.midistream:
            self.midistream = midi.new_stream(90, measure.part.score.divisions)
            e = midi.ProgramChangeEvent()
            e.value = 1#measure.midi.program
            self.midistream.add_event(e)
        self.write(measure.musicdata)
        self.tick += measure.ticks()
    def write_timestepsequence(self, musicdata):
        map(self.write, musicdata)
    def write_clef(self, clef):
        pass
    def write_attributes(self, attributes):
        pass
    def write_barline(self, barline):
        pass
    def write_direction(self, direction):
        pass
    def write_print(self, pr):
        pass
    def write_chord(self, chord):
        if not self.midistream:
            self.midistream = midi.new_stream(90, chord.note(0).divisions)
            e = midi.ProgramChangeEvent()
            e.value = chord.notes[0].midi.program
            self.midistream.add_event(e)
        map(self.write, chord.notes)
    def write_note(self, note):
        if not self.midistream:
            self.midistream = midi.new_stream(90, note.measure.part.score.divisions)
            e = midi.ProgramChangeEvent()
            e.value = 1#note.midi.program
            self.midistream.add_event(e)
        if note.pitch:
            pitch = note.pitch.getMIDIpitch()
            e = midi.NoteOnEvent()
            e.pitch = pitch
            e.velocity = 64
            e.tick = self.tick+note.startTick
            self.midistream.add_event(e)
            e = midi.NoteOffEvent()
            e.pitch = pitch
            e.velocity = 64
            e.tick = self.tick+note.startTick+note.duration
            self.midistream.add_event(e)
    def eof(self):
        midi.write_midifile(self.midistream, self.filename)
                                                               
import pygame
import tempfile
pygame.mixer.init(44100,-16,1024)
clock = pygame.time.Clock()

def play(musicObject):
    """Play a music object (Score, Part, Measure)."""
    mf = MidiWriter(tempfile.mktemp(".mid"))
    mf.write(musicObject)
    mf.eof()
    pygame.mixer.music.load(mf.filename)
    pygame.mixer.music.play()

def spin():
    """Block until playback has finished."""
    try:
        while pygame.mixer.music.get_busy():
            clock.tick(1)
    except KeyboardInterrupt:
        pass
