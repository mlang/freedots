# Copyright giles hall <ghall@csh.rit.edu>
#
# Permission is hereby granted, free of charge, to any person obtaining
# a copy of this software and associated documentation files (the
# "Software"), to deal in the Software without restriction, including
# without limitation the rights to use, copy, modify, merge, publish,
# distribute, sublicense, and/or sell copies of the Software, and to
# permit persons to whom the Software is furnished to do so, subject to
# the following conditions:
#
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
# LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
# OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
# WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

"""MIDI file IO module."""

import copy
import time
from cStringIO import StringIO 
from struct import unpack, pack
from math import sqrt

##
## Constants
##

BEATNAMES = ['whole', 'half', 'quarter', 'eighth', 'sixteenth', 'thiry-second', 'sixty-fourth']
BEATVALUES = [4, 2, 1, .5, .25, .125, .0625]

DEFAULT_MIDI_HEADER_SIZE = 14

class Event(object):
    """
    EventMIDI : Concrete class used to describe MIDI Events.
    Inherits from Event.
    """
    length = 0
    name = "Generic MIDI Event"
    statusmsg = 0x0

    class __metaclass__(type):
        def __init__(cls, name, bases, dict):
            if name not in ['Event', 'MetaEvent', 'NoteEvent']:
                EventFactory.register_event(cls, bases)

    def __init__(self):
        """ event type derived from __class__ """
        self.type       = self.__class__.__name__
        """ midi channel """
        self.channel    = 0
        """ midi tick """
        self.tick       = 0
        """ delay in ms """
        self.msdelay    = 0
        """ data after statusmsg """
        self.data       = ''
        """ track number """
        self.track      = 0
        """ sort order """
        self.order      = None

    def copy(self):
        return copy.deepcopy(self)

    def is_event(cls, statusmsg):
        return (cls.statusmsg == (statusmsg & 0xF0))
    is_event = classmethod(is_event)

    def __str__(self):
        return "%s @%d %dms C%d T%d" % (self.name, 
                            self.tick,
                            self.msdelay,
                            self.channel,
                            self.track)

    def __cmp__(self, other):
        if self.tick < other.tick: return -1
        elif self.tick > other.tick: return 1
        return 0
        #elif self.tick == 0:
        #    # both are 0 -- go by order
        #    if (self.order == None) and (other.order != None): return 1
        #    elif (other.order == None) and (self.order != None): return -1
        #    elif self.order < other.order: return -1
        #    elif self.order > other.order: return 1
        #return 0

    def adjust_msdelay(self, tempo):
        rtick = self.tick - tempo.tick
        self.msdelay = int((rtick * tempo.mpt) + tempo.msdelay)
     
    def encode(self, delta=0, running=False):
        encstr = ''
        if not running:
            encstr += chr((self.statusmsg & 0xF0) | (0x0F & self.channel))
        return self.encode_tick(delta=delta) + encstr + self.encode_data()

    def decode(self, tick, statusmsg, track, runningstatus=''):
        assert(self.is_event(statusmsg))
        self.tick = tick
        self.channel = statusmsg & 0x0F
        self.data = ''
        if runningstatus:
            self.data += runningstatus
        remainder = self.length - len(self.data)
        if remainder:
            self.data += str.join('',[track.next() for x in range(remainder)])
        self.decode_data()

    def encode_tick(self, delta=0):
        return write_varlen(self.tick + delta)

    def decode_data(self):
        pass

    def encode_data(self):
        return self.data
    
"""
MetaEvent is a special subclass of Event that is not meant to
be used as a concrete class.  It defines a subset of Events known
as the Meta  events.
"""
    
class MetaEvent(Event):
    statusmsg = 0xFF
    metacommand = 0x0
    name = 'Meta Event'

    def is_event(cls, statusmsg):
        return (cls.statusmsg == statusmsg)
    is_event = classmethod(is_event)

    def is_meta_event(cls, metacmd):
        return (cls.metacommand == metacmd)
    is_meta_event = classmethod(is_meta_event)

    def encode(self, delta=0, running=False):
        tick = self.encode_tick(delta=delta)
        data = self.encode_data()
        datalen = chr(len(data))
        smsg = chr(self.statusmsg)
        mcmd = chr(self.metacommand)
        return str.join("", (tick, smsg, mcmd, datalen, data))

    def decode(self, tick, command, track):
        assert(self.is_meta_event(command))
        self.tick = tick
        self.channel = 0
        if not hasattr(self, 'order'):
            self.order = None
        len = read_varlen(track)
        self.data = str.join('', [track.next() for x in range(len)])
        self.decode_data()

    def encode_data(self):
        return self.data

"""
EventFactory is a singleton that you should not instantiate.  It is
a helper class that assists you in building MIDI event objects.
"""

class EventFactory(object):
    EventRegistry = []
    MetaEventRegistry = []
    
    def __init__(self):
        self.RunningStatus = None
        self.RunningTick = 0

    def register_event(cls, event, bases):
        if MetaEvent in bases:
            cls.MetaEventRegistry.append(event)
        elif (Event in bases) or (NoteEvent in bases):
            cls.EventRegistry.append(event)
        else:
            raise ValueError, "Unknown bases class in event type: "+event.name
    register_event = classmethod(register_event)

    def parse_midi_event(self, track):
        # first datum is varlen representing delta-time
        tick = read_varlen(track)
        self.RunningTick += tick
        # next byte is status message
        stsmsg = ord(track.next())
        # is the event a MetaEvent?
        if MetaEvent.is_event(stsmsg):
            # yes, figure out which one
            cmd = ord(track.next())
            for etype in self.MetaEventRegistry:
                if etype.is_meta_event(cmd):
                    evi = etype()
                    evi.decode(self.RunningTick, cmd, track)
                    return evi
            else:
                raise Warning, "Unknown Meta MIDI Event: " + `cmd`
        elif SysExEvent.is_event(stsmsg):
            evi = SysExEvent()
            evi.decode(self.RunningTick, stsmsg, track)
            return evi
        # not a Meta MIDI event, must be a general message
        else:
            for etype in self.EventRegistry:
                if etype.is_event(stsmsg):
                    self.RunningStatus = (stsmsg, etype)
                    evi = etype()
                    evi.decode(self.RunningTick, stsmsg, track)
                    return evi
            else:
                if self.RunningStatus:
                    cached_stsmsg, etype = self.RunningStatus
                    evi = etype()
                    evi.decode(self.RunningTick, 
                            cached_stsmsg, track, chr(stsmsg))
                    return evi
                else:
                    raise Warning, "Unknown MIDI Event: " + `stsmsg`

class NoteEvent(Event):
    length = 2
    fields = ['pitch', 'velocity']

    def __str__(self):
        return "%s [ (%s) %d ]" % \
                            (super(NoteEvent, self).__str__(),
                             self.pitch,
                             self.velocity)

    def decode_data(self):
        self.pitch = ord(self.data[0])
        self.velocity = ord(self.data[1])

    def encode_data(self):
        return chr(self.pitch) + chr(self.velocity)

class NoteOnEvent(NoteEvent):
    statusmsg = 0x90
    name = 'Note On'

class NoteOffEvent(NoteEvent):
    statusmsg = 0x80
    name = 'Note Off'

class AfterTouchEvent(Event):
    statusmsg = 0xA0
    length = 2
    name = 'After Touch'

    def __str__(self):
        return "%s [ %s %s ]" % \
                            (super(AfterTouchEvent, self).__str__(),
                                hex(ord(self.data[0])),
                                hex(ord(self.data[1])))

class ControlChangeEvent(Event):
    statusmsg = 0xB0
    length = 2
    name = 'Control Change'
    
    def __str__(self):
        return "%s [ %s %s ]" % \
                            (super(ControlChangeEvent, self).__str__(),
                                hex(ord(self.data[0])),
                                hex(ord(self.data[1])))

    def decode_data(self):
        self.control = ord(self.data[0])
        self.value = ord(self.data[1])

    def encode_data(self):
        return chr(self.control) + chr(self.value)

class ProgramChangeEvent(Event):
    statusmsg = 0xC0
    length = 1
    name = 'Program Change'

    def __str__(self):
        return "%s [ %s ]" % \
                            (super(ProgramChangeEvent, self).__str__(),
                                hex(ord(self.data[0])))

    def decode_data(self):
        self.value = ord(self.data[0])

    def encode_data(self):
        return chr(self.value)

class ChannelAfterTouchEvent(Event):
    statusmsg = 0xD0
    length = 1
    name = 'Channel After Touch'

    def __str__(self):
        return "%s [ %s ]" % \
                            (super(ChannelAfterTouchEvent,self).__str__(),
                                hex(ord(self.data[0])))

class PitchWheelEvent(Event):
    statusmsg = 0xE0
    length = 2
    name = 'Pitch Wheel'

    def __str__(self):
        return "%s [ %d ]" % \
                            (super(PitchWheelEvent, self).__str__(),
                                self.value)

    def decode_data(self):
        first = ord(self.data[0]) 
        second = ord(self.data[1])
        self.value = ((second << 7) | first) - 0x2000

    def encode_data(self):
        value = self.value + 0x2000
        first = chr(value & 0xFF)
        second = chr((value >> 7) & 0xFF)
        return first + second

class SysExEvent(Event):
    statusmsg = 0xF0
    name = 'SysEx'

    def __str__(self):
        value = str.join('', ["%c" % chr for chr in self.data])
        return "%s [ %s ]" % (super(SysExEvent, self).__str__(), value)

    def is_event(cls, statusmsg):
        return (cls.statusmsg == statusmsg)
    is_event = classmethod(is_event)

    def decode(self, tick, statusmsg, track):
        self.tick = tick
        self.channel = statusmsg & 0x0F
        len = read_varlen(track)
        self.data = str.join('', [track.next() for x in range(len)])

class SequenceNumberMetaEvent(MetaEvent):
    name = 'Sequence Number'
    metacommand = 0x00

class TextMetaEvent(MetaEvent):
    name = 'Text'
    metacommand = 0x01

    def __str__(self):
        return "%s [ %s ]" % \
                            (super(TextMetaEvent, self).__str__(),
                            self.data)

class CopyrightMetaEvent(MetaEvent):
    name = 'Copyright Notice'
    metacommand = 0x02

class TrackNameEvent(MetaEvent):
    name = 'Track Name'
    metacommand = 0x03
    order = 3

    def __str__(self):
        return "%s [ %s ]" % \
                            (super(TrackNameEvent, self).__str__(),
                            self.data)

class InstrumentNameEvent(MetaEvent):
    name = 'Instrument Name'
    metacommand = 0x04
    order = 4

    def __str__(self):
        return "%s [ %s ]" % \
                            (super(InstrumentNameEvent, self).__str__(),
                            self.data)


class LryricsEvent(MetaEvent):
    name = 'Lyrics'
    metacommand = 0x05

    def __str__(self):
        return "%s [ %s ]" % \
                            (super(LryricsEvent, self).__str__(),
                            self.data)


class MarkerEvent(MetaEvent):
    name = 'Marker'
    metacommand = 0x06

class CuePointEvent(MetaEvent):
    name = 'Cue Point'
    metacommand = 0x07

class SomethingEvent(MetaEvent):
    name = 'Something'
    metacommand = 0x09

class ChannelPrefixEvent(MetaEvent):
    name = 'Cue Point'
    metacommand = 0x20

class ChannelPrefixEvent(MetaEvent):
    name = 'Cue Point'
    metacommand = 0x20

class PortEvent(MetaEvent):
    fields = ['port']
    name = 'MIDI Port/Cable'
    metacommand = 0x21
    order = 5

    def __str__(self):
        return "%s [ port: %d ]" % \
                            (super(PortEvent, self).__str__(),
                            self.port)

    def decode_data(self):
        assert(len(self.data) == 1)
        self.port = ord(self.data[0])

class TrackLoopEvent(MetaEvent):
    name = 'Track Loop'
    metacommand = 0x2E

class EndOfTrackEvent(MetaEvent):
    name = 'End of Track'
    metacommand = 0x2F
    order = 2

class SetTempoEvent(MetaEvent):
    fields = ['mpqn', 'tempo']
    name = 'Set Tempo'
    metacommand = 0x51
    order = 1

    def __str__(self):
        return "%s [ mpqn: %d tempo: %d ]" % \
                            (super(SetTempoEvent, self).__str__(),
                            self.mpqn, self.tempo)

    def __setattr__(self, item, value):
        if item == 'mpqn':
            self.__dict__['mpqn'] = value
            self.__dict__['tempo'] = float(6e7) / value 
        elif item == 'tempo':
            self.__dict__['tempo'] = value
            self.__dict__['mpqn'] = int(float(6e7) / value)
        else:
            self.__dict__[item] = value

    def decode_data(self):
        assert(len(self.data) == 3)
        self.mpqn = (ord(self.data[0]) << 16) + (ord(self.data[1]) << 8) \
                        + ord(self.data[2])
        self.tempo = float(6e7) / self.mpqn

    def encode_data(self):
        self.mpqgn = int(float(6e7) / self.tempo)
        return chr((self.mpqn & 0xFF0000) >> 16) + \
                    chr((self.mpqn & 0xFF00) >> 8) + \
                    chr((self.mpqn & 0xFF))

class SmpteOffsetEvent(MetaEvent):
    name = 'SMPTE Offset'
    metacommand = 0x54

class TimeSignatureEvent(MetaEvent):
    fields = ['numerator', 'denominator', 'metronome', 'thirtyseconds']
    name = 'Time Signature'
    metacommand = 0x58
    order = 0

    def __str__(self):
        return "%s [ %d/%d  metro: %d  32nds: %d ]" % \
                            (super(TimeSignatureEvent, self).__str__(),
                                self.numerator, self.denominator,
                                self.metronome, self.thirtyseconds)

    def decode_data(self):
        assert(len(self.data) == 4)
        self.numerator = ord(self.data[0])
        # Weird: the denominator is two to the power of the data variable
        self.denominator = 2 ** ord(self.data[1])
        self.metronome = ord(self.data[2])
        self.thirtyseconds = ord(self.data[3])

    def encode_data(self):
        return chr(self.numerator) + \
                    chr(int(sqrt(self.denominator))) + \
                    chr(self.metronome) + \
                    chr(self.thirtyseconds)


class KeySignatureEvent(MetaEvent):
    name = 'Key Signature'
    metacommand = 0x59

class BeatMarkerEvent(MetaEvent):
    name = 'Beat Marker'
    metacommand = 0x7F

class SequencerSpecificEvent(MetaEvent):
    name = 'Sequencer Specific'
    metacommand = 0x7F


class TempoMap(list):
    def __init__(self, stream):
        self.stream = stream

    def add_and_update(self, event):
        self.add(event)
        self.update()

    def add(self, event):
        # get tempo in microseconds per beat
        tempo = event.mpqn
        # convert into milliseconds per beat
        tempo = tempo / 1000.0
        # generate ms per tick
        event.mpt = tempo / self.stream.resolution
        self.append(event)

    def update(self):
        self.sort()
        # adjust running time
        last = None
        for event in self:
            if last:
                event.msdelay = last.msdelay + \
                    int(last.mpt * (event.tick - last.tick))
            last = event

    def get_tempo(self, offset=0):
        last = self[0]
        for tm in self[1:]:
            if tm.tick > offset:
                return last
            last = tm
        return last

class EventStreamIterator(object):
    def __init__(self, stream, window):
        self.stream = stream
        self.trackpool = stream.trackpool
        self.window_length = window
        self.window_edge = 0
        self.leftover = None
        self.events = self.stream.iterevents()
        # First, need to look ahead to see when the
        # tempo markers end
        self.ttpts = []
        for tempo in stream.tempomap[1:]:
            self.ttpts.append(tempo.tick)
        # Finally, add the end of track tick.
        self.ttpts.append(stream.endoftrack.tick)
        self.ttpts = iter(self.ttpts)
        # Setup next tempo timepoint
        self.ttp = self.ttpts.next()
        self.tempomap = iter(self.stream.tempomap)
        self.tempo = self.tempomap.next()
        self.endoftrack = False

    def __iter__(self):
        return self

    def __next_edge(self):
        if self.endoftrack:
            raise StopIteration
        lastedge = self.window_edge
        self.window_edge += int(self.window_length / self.tempo.mpt)
        if self.window_edge > self.ttp:
            # We're past the tempo-marker.
            oldttp = self.ttp
            try:
                self.ttp = self.ttpts.next()
            except StopIteration:
                # End of Track!
                self.window_edge = self.ttp
                self.endoftrack = True
                return
            # Calculate the next window edge, taking into
            # account the tempo change.
            msused = (oldttp - lastedge) * self.tempo.mpt
            msleft = self.window_length - msused
            self.tempo = self.tempomap.next()
            ticksleft = msleft / self.tempo.mpt
            self.window_edge = ticksleft + self.tempo.tick

    def next(self):
        ret = []
        self.__next_edge()
        if self.leftover:
            if self.leftover.tick > self.window_edge:
                return ret
            ret.append(self.leftover)
            self.leftover = None
        for event in self.events:
            if event.tick > self.window_edge:
                self.leftover = event
                return ret
            ret.append(event)
        return ret



"""
EventStream : Class used to describe a collection of MIDI Events.
"""
class EventStream(object):
    def __init__(self):
        self.format = 1
        self.trackcount = 0
        self.tempomap = TempoMap(self)
        self.curtrack = None
        self.trackpool = []
        self.tracklist = {}
        self.timemap = []
        self.endoftrack = None
        self.beatmap = []
        self.resolution = 220
        self.tracknames = {}

    def __set_resolution(self, resolution):
        # XXX: Add code to rescale notes
        assert(not self.trackpool)
        self.__resolution = resolution
        self.beatmap = []
        for value in BEATVALUES:
            self.beatmap.append(int(value * resolution))

    def __get_resolution(self):
        return self.__resolution
    resolution = property(__get_resolution, __set_resolution, None,
                                "Ticks per quarter note")

    def add_track(self):
        if self.curtrack == None:
            self.curtrack = 0
        else:
            self.curtrack += 1
        self.tracklist[self.curtrack] = []
        self.trackcount += 1
        self.endoftrack = None

    def get_current_track_number(self):
        return self.curtrack

    def get_track_by_number(self, tracknum):
        return self.tracklist[tracknum]

    def get_current_track(self):
        return self.tracklist[self.curtrack]

    def get_track_by_name(self, trackname):
        tracknum = self.tracknames[trackname]
        return self.get_track_by_number(tracknum)

    def replace_current_track(self, track):
        self.tracklist[self.curtrack] = track
        self.__refresh()

    def replace_track_by_number(self, tracknum, track):
        self.tracklist[tracknumber] = track
        self.__refresh()

    def replace_track_by_name(self, trackname, track):
        tracknum = self.tracklist[tracknum]
        self.repdeletelace_track_by_number(tracknum, track)

    def delete_current_track(self, track):
        del self.tracklist[self.curtrack]
        self.trackcount -= 1
        self.__refresh()

    def delete_track_by_number(self, tracknum):
        del self.tracklist[tracknum]
        self.trackcount -= 1
        self.__refresh()

    def delete_track_by_name(self, trackname, track):
        tracknum = self.tracklist[trackname]
        self.delete_track_by_number(tracknum, track)

    def add_event(self, event):
        self.__adjust_endoftrack(event)
        if not isinstance(event, EndOfTrackEvent):
            event.track = self.curtrack
            self.trackpool.append(event)
            self.tracklist[self.curtrack].append(event)
        if isinstance(event, TrackNameEvent):
            self.__refresh_tracknames()
        if isinstance(event, SetTempoEvent):
            self.tempomap.add_and_update(event)
            self.__refresh_timemap()
        else:
            if self.tempomap:
                tempo = self.tempomap.get_tempo(event.tick)
                event.adjust_msdelay(tempo)

    def get_tempo(self, offset=0):
        return self.tempomap.get_tempo(offset)

    def timesort(self):
        self.trackpool.sort()
        for track in self.tracklist.values():
            track.sort()
    
    def textdump(self):
        for event in self.trackpool:
            print event

    def __iter__(self):
        return iter(self.tracklist.values())

    def iterevents(self, mswindow=0):
        self.timesort()
        if mswindow:
            return EventStreamIterator(self, mswindow)
        return iter(self.trackpool)

    def __len__(self):
        assert(len(self.tracklist) == self.trackcount)
        return self.trackcount

    def __getitem__(self, intkey):
        return self.tracklist[intkey]

    def __refresh(self):
        self.__refresh_trackpool()
        self.__refresh_tempomap()
        self.__refresh_timemap()
        self.__refresh_tracknames()

    def __refresh_tracknames(self):
        self.tracknames = {}
        for tracknum in self.tracklist:
            track = self.tracklist[tracknum]
            for event in track:
                if isinstance(event, TrackNameEvent):
                    self.tracknames[event.data] = tracknum
                    break

    def __refresh_trackpool(self):
        self.trackpool = []
        for track in self.tracklist:
            track = self.tracklist[tracknum]
            for event in track:
                self.trackpool.append(event)
        self.trackpool.sort()

    def __refresh_tempomap(self):
        self.endoftrack = None
        self.tempomap = TempoMap(self)
        for event in self.trackpool:
            if isinstance(event, SetTempoEvent):
                self.tempomap.add(event)
            elif isinstance(event, EndOfTrackEvent):
                self.__adjust_endoftrack(event)
            self.tempomap.update()

    def __refresh_timemap(self):
        for event in self.trackpool:
            if not isinstance(event, SetTempoEvent):
                tempo = self.tempomap.get_tempo(event.tick)
                event.adjust_msdelay(tempo)

    def __adjust_endoftrack(self, event):
        if not self.endoftrack:
            if not isinstance(event, EndOfTrackEvent):
                ev = EndOfTrackEvent()
                ev.tick = event.tick
                ev.track = self.curtrack
                self.endoftrack = ev
            else:
                self.endoftrack = event
            self.trackpool.append(self.endoftrack)
            self.tracklist[self.curtrack].append(self.endoftrack)
        else:
            self.endoftrack.tick = max(event.tick + 1, self.endoftrack.tick)
        if self.tempomap:
            tempo = self.tempomap.get_tempo(self.endoftrack.tick)
            self.endoftrack.adjust_msdelay(tempo)

class EventStreamWriter(object):
    def __init__(self, midistream, output):
        if isinstance(output, str):
            output = open(output, 'w')
        self.output = output
        self.midistream = midistream
        self.write_file_header()
        for track in self.midistream:  
            self.write_track(track)
    
    def write(cls, midistream, output):
        cls(midistream, output)
    write = classmethod(write)
        
    def write_file_header(self):
        # First four bytes are MIDI header
        packdata = pack(">LHHH", 6,    
                            self.midistream.format, 
                            self.midistream.trackcount, 
                            self.midistream.resolution)
        self.output.write('MThd%s' % packdata)
            
    def write_track_header(self, trklen):
        self.output.write('MTrk%s' % pack(">L", trklen))

    def write_track(self, track):
        buf = ''
        track = copy.copy(track)
        track.sort()
        last_tick = delta = 0
        smsg = 0
        chn = 0
        for event in track:
            running = ((smsg == event.statusmsg) and (chn == event.channel))
            buf += event.encode(delta=-last_tick, running=running)
            last_tick = event.tick
            smsg = event.statusmsg 
            chn = event.channel
        self.write_track_header(len(buf))
        self.output.write(buf)

class EventStreamReader(object):
    def __init__(self, instream, outstream):
        self.eventfactory = None
        self.parse(instream, outstream)

    def read(cls, instream, outstream=None):
        if not outstream:
            outstream = EventStream()
        cls(instream, outstream)
        return outstream
    read = classmethod(read)
    
    def parse(self, instream, outstream):
        self.midistream = outstream
        if isinstance(instream, str):
            instream = open(instream)
        self.instream = instream
        if isinstance(instream, file):
            self.instream = StringIO(instream.read())
            self.cursor = 0
        # XXX: unicode?
        elif isinstance(instream, string):
            self.instream = StringIO(instream)
        else:
            raise TypeError, "Expecting file, string, or StringIO"
        self.parse_file_header()
        for track in range(self.midistream.trackcount):  
            trksz = self.parse_track_header()
            self.eventfactory = EventFactory()
            self.midistream.add_track()
            self.parse_track(trksz)
        
    def parse_file_header(self):
        # First four bytes are MIDI header
        magic = self.instream.read(4)
        if magic != 'MThd':
            raise TypeError, "Bad header in MIDI file."
        # next four bytes are header size
        # next two bytes specify the format version
        # next two bytes specify the number of tracks
        # next two bytes specify the resolution/PPQ/Parts Per Quarter
        # (in other words, how many ticks per quater note)
        data = unpack(">LHHH", self.instream.read(10))
        hdrsz = data[0]
        self.midistream.format = data[1]
        self.midistream.trackcount = data[2]
        self.midistream.resolution = data[3]
        # XXX: the assumption is that any remaining bytes
        # in the header are padding
        if hdrsz > DEFAULT_MIDI_HEADER_SIZE:
            self.instream.read(hdrsz - DEFAULT_MIDI_HEADER_SIZE)
            
    def parse_track_header(self):
        # First four bytes are Track header
        magic = self.instream.read(4)
        if magic != 'MTrk':
            raise TypeError, "Bad track header in MIDI file: " + magic
        # next four bytes are header size
        trksz = unpack(">L", self.instream.read(4))[0]
        return trksz

    def parse_track(self, trksz):
        track = iter(self.instream.read(trksz))
        while True:
            try:
                event = self.eventfactory.parse_midi_event(track)
                self.midistream.add_event(event)
            except StopIteration:
                break
                
def read_varlen(data):
    NEXTBYTE = 1
    value = 0
    while NEXTBYTE:
        chr = ord(data.next())
        # is the hi-bit set?
        if not (chr & 0x80):
            # no next BYTE
            NEXTBYTE = 0
        # mask out the 8th bit
        chr = chr & 0x7f
        # shift last value up 7 bits
        value = value << 7
        # add new value
        value += chr
    return value

def write_varlen(value):
    chr1 = chr(value & 0x7F)
    value >>= 7
    if value:
        chr2 = chr((value & 0x7F) | 0x80)
        value >>= 7
        if value:
            chr3 = chr((value & 0x7F) | 0x80)
            value >>= 7
            if value:
                chr4 = chr((value & 0x7F) | 0x80)
                res = chr4 + chr3 + chr2 + chr1
            else:
                res = chr3 + chr2 + chr1
        else:
            res = chr2 + chr1
    else:
        res = chr1
    return res

def test_varlen():
    for value in xrange(0x0FFFFFFF):
        if not (value % 0xFFFF):
            print hex(value)
        datum = write_varlen(value)
        newvalue = read_varlen(iter(datum))
        if value != newvalue: 
            hexstr = str.join('', map(hex, map(ord, datum)))
            print "%s != %s (hex: %s)" % (value, newvalue, hexstr)

def new_stream(tempo=120, resolution=480, format=1):
    stream = EventStream()
    stream.format = format
    stream.resolution = resolution
    stream.add_track()
    tempoev = SetTempoEvent()
    tempoev.tempo = tempo
    tempoev.tick = 0
    stream.add_event(tempoev)
    return stream

read_midifile = EventStreamReader.read
write_midifile = EventStreamWriter.write
