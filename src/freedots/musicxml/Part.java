/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2010 Mario Lang  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details (a copy is included in the LICENSE.txt file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package freedots.musicxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import freedots.logging.Logger;
import freedots.math.Fraction;
import freedots.music.Accidental;
import freedots.music.AccidentalContext;
import freedots.music.ClefChange;
import freedots.music.Event;
import freedots.music.GlobalKeyChange;
import freedots.music.KeyChange;
import freedots.music.KeySignature;
import freedots.music.MusicList;
import freedots.music.StartBar;
import freedots.music.EndBar;
import freedots.music.TimeSignature;
import freedots.music.TimeSignatureChange;
import freedots.musicxml.Note.TupletElementXML;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** A wrapper around the MusicXML part element.
 */
public final class Part {
  private static final Logger LOG = Logger.getLogger(Part.class);

  private final Element scorePart;

  private final Score score;
  /** Gets the parent {@link freedots.musicxml.Score} of this part.
   */
  public Score getScore() { return score; }

  private TimeSignature timeSignature = new TimeSignature(4, 4);
  private final MusicList eventList = new MusicList();

  /** Constructs a new part (and all its relevant child objects).
   *
   * @throws MusicXMLParseException if an unrecoverable error happens
   */
  Part(final Element part, final Element scorePart, final Score score)
    throws MusicXMLParseException {
    this.scorePart = scorePart;
    this.score = score;

    final int divisions = score.getDivisions();
    int durationMultiplier = 1;

    int measureNumber = 0;
    Fraction measureOffset = Fraction.ZERO;
    TimeSignature lastTimeSignature = null;
    int staffCount = 1;
    EndBar endbar = null;
    boolean newMeasure=false;	

    SlurBuilder slurBuilder = new SlurBuilder();
    TupletBuilder tupletBuilder=new TupletBuilder();
    
    for (Node partNode = part.getFirstChild(); partNode != null;
         partNode = partNode.getNextSibling()) {
      if (partNode.getNodeType() == Node.ELEMENT_NODE
       && "measure".equals(partNode.getNodeName())) {
    	newMeasure=true;
        final Element xmlMeasure = (Element)partNode;

        StartBar startBar = new StartBar(measureOffset, ++measureNumber);
        startBar.setStaffCount(staffCount);
        eventList.add(startBar);

        boolean repeatBackward = false;
        int endingStop = 0;

        Chord currentChord = null;
        Fraction offset = Fraction.ZERO;
        Fraction measureDuration = Fraction.ZERO;

        for (Node measureNode = xmlMeasure.getFirstChild();
             measureNode != null; measureNode = measureNode.getNextSibling()) {        
          if (measureNode.getNodeType() == Node.ELEMENT_NODE) {
            Element musicdata = (Element)measureNode;
            String tagName = musicdata.getTagName();
            if ("attributes".equals(tagName)) {
              if (currentChord != null) {
                offset = offset.add(currentChord.get(0).getDuration());
                currentChord = null;
              }

              Attributes attributes = new Attributes(musicdata);
              int newDivisions = attributes.getDivisions();
              Attributes.Time newTimeSignature = attributes.getTime();
              int newStaffCount = attributes.getStaves();
              if (newDivisions > 0) {
                durationMultiplier = divisions / newDivisions;
              }
              if (newStaffCount > 1 && newStaffCount != staffCount) {
                staffCount = newStaffCount;
                startBar.setStaffCount(staffCount);
              }
              if (newTimeSignature != null) {
                if (lastTimeSignature == null) {
                  timeSignature = newTimeSignature;
                }
                lastTimeSignature = newTimeSignature;
                eventList.add(new TimeSignatureChange(measureOffset.add(offset),
                                                      lastTimeSignature));
                if (offset.equals(0)) {
                  startBar.setTimeSignature(newTimeSignature);
                }
              }
              List<Attributes.Clef> clefs = attributes.getClefs();
              if (clefs.size() > 0) {
                for (Attributes.Clef clef:clefs) {
                  eventList.add(new ClefChange(measureOffset.add(offset),
                                               clef, clef.getStaffNumber()));
                }
              }
              List<Attributes.Key> keys = attributes.getKeys();
              if (keys.size() > 0) {
                for (Attributes.Key key: keys) {
                  if (key.getStaffName() == null)
                    eventList.add(new GlobalKeyChange(measureOffset.add(offset), key));
                  else
                    eventList.add(new KeyChange(measureOffset.add(offset),
                                                key,
                                                Integer.parseInt(key.getStaffName()) - 1));
                }
              }
            } else if ("note".equals(tagName)) {
              Note note = new Note(musicdata, divisions, durationMultiplier,
                                   this);

              if (currentChord != null
               && !elementHasChild(musicdata, Note.CHORD_ELEMENT)) {
                offset = offset.add(currentChord.get(0).getDuration());
                currentChord = null;
              }
              note.setMoment(measureOffset.add(offset));
              boolean advanceTime = !note.isGrace();
              boolean addNoteToEventList = true;

              slurBuilder.visit(note);
              tupletBuilder.visitNote(note,newMeasure);
              if (note.getTimeModification()!=null)newMeasure=false;

              if (currentChord != null) {
                if (elementHasChild(musicdata, Note.CHORD_ELEMENT)) {
                  currentChord.add(note);
                  advanceTime = false;
                  addNoteToEventList = false;
                }
              }
              if (currentChord == null && note.isStartOfChord()) {
                currentChord = new Chord(note);
                advanceTime = false;
                eventList.add(currentChord);
                addNoteToEventList = false;
              }
              if (addNoteToEventList) {
                eventList.add(note);
              }
              if (advanceTime) {
                offset = offset.add(note.getDuration());
              }
            } else if ("direction".equals(tagName)) {
              Direction direction = new Direction(musicdata,
                                                  durationMultiplier, divisions,
                                                  measureOffset.add(offset));
              eventList.add(direction);
            } else if ("harmony".equals(tagName)) {
              final Harmony harmony =
                new Harmony(musicdata, durationMultiplier, divisions,
                            measureOffset.add(offset));
              eventList.add(harmony);
            } else if ("backup".equals(tagName)) { 
              if (currentChord != null) {
                offset = offset.add(currentChord.get(0).getDuration());
                currentChord = null;
              }
              final Backup backup =
                new Backup(musicdata, divisions, durationMultiplier);
              offset = offset.subtract(backup.getDuration());
            } else if ("forward".equals(tagName)) {
              if (currentChord != null) {
                offset = offset.add(currentChord.get(0).getDuration());
                currentChord = null;
              }
              Note invisibleRest = new Note(musicdata,
                                            divisions, durationMultiplier,
                                            this);
              invisibleRest.setMoment(measureOffset.add(offset));
              eventList.add(invisibleRest);
              offset = offset.add(invisibleRest.getDuration());
            } else if ("print".equals(tagName)) {
              Print print = new Print(musicdata);
              if (print.isNewSystem()) startBar.setNewSystem(true);
            } else if ("sound".equals(tagName)) {
              Sound sound = new Sound(musicdata, measureOffset.add(offset));
              eventList.add(sound);
            } else if ("barline".equals(tagName)) {
              Barline barline = new Barline(musicdata);

              if (barline.getLocation() == Barline.Location.LEFT) {
                if (barline.getRepeat() == Barline.Repeat.FORWARD) {
                  startBar.setRepeatForward(true);
                }
                if (barline.getEnding() > 0 &&
                    barline.getEndingType() == Barline.EndingType.START) {
                  startBar.setEndingStart(barline.getEnding());
                }
              } else if (barline.getLocation() == Barline.Location.RIGHT) {
                if (barline.getRepeat() == Barline.Repeat.BACKWARD) {
                  repeatBackward = true;
                }
                if (barline.getEnding() > 0 &&
                    barline.getEndingType() == Barline.EndingType.STOP) {
                  endingStop = barline.getEnding();
                }
              }
            } else
              LOG.info("Unsupported musicdata element " + tagName);
            if (offset.compareTo(measureDuration) > 0) measureDuration = offset;
          }
        }

        if (currentChord != null) {
          offset = offset.add(currentChord.get(0).getDuration());
          if (offset.compareTo(measureDuration) > 0) measureDuration = offset;
          currentChord = null;
        }
        TimeSignature activeTimeSignature = lastTimeSignature != null ? lastTimeSignature : timeSignature;
        if (xmlMeasure.getAttribute("implicit").equalsIgnoreCase(Score.YES)
         && measureDuration.compareTo(timeSignature) < 0) {
          measureOffset = measureOffset.add(measureDuration);
        } else {
          if (measureDuration.compareTo(activeTimeSignature) != 0) {
            LOG.warning("Incomplete measure "
                        + xmlMeasure.getAttribute("number") + ": "
                        + timeSignature + " " + measureDuration);
          }
          measureOffset = measureOffset.add(activeTimeSignature);
        }
        if (startBar.getTimeSignature() == null) {
          startBar.setTimeSignature(lastTimeSignature);
        }

        endbar = new EndBar(measureOffset);
        if (repeatBackward) endbar.setRepeat(true);
        if (endingStop > 0) endbar.setEndingStop(endingStop);
        eventList.add(endbar);
      }
    }
    if (endbar != null) endbar.setEndOfMusic(true);

    // Post processing

    slurBuilder.buildSlurs();
    tupletBuilder.buildTuplet();

    if (!score.encodingSupports(Note.ACCIDENTAL_ELEMENT)) {
      int staves = 1;
      KeySignature defaultKeySignature = new KeySignature(0);
      List<AccidentalContext> contexts = new ArrayList<AccidentalContext>();
      for (int i = 0; i < staves; i++) {
        contexts.add(new AccidentalContext(defaultKeySignature));
      }
      for (Event event: eventList) {
        if (event instanceof StartBar) {
          StartBar startBar = (StartBar)event;
          if (startBar.getStaffCount() != staves) {
            if (startBar.getStaffCount() > staves)
              for (int i = 0; i < (startBar.getStaffCount() - staves); i++)
                contexts.add(new AccidentalContext(defaultKeySignature));
            else if (startBar.getStaffCount() < staves)
              for (int i = 0; i < (staves - startBar.getStaffCount()); i++)
                contexts.remove(contexts.size() - 1);
            staves = startBar.getStaffCount();
          }
          for (AccidentalContext accidentalContext: contexts)
            accidentalContext.resetToKeySignature();
        } else if (event instanceof GlobalKeyChange) {
          GlobalKeyChange globalKeyChange = (GlobalKeyChange)event;
          defaultKeySignature = globalKeyChange.getKeySignature();
          for (AccidentalContext accidentalContext: contexts)
            accidentalContext.setKeySignature(defaultKeySignature);
        } else if (event instanceof KeyChange) {
          KeyChange keyChange = (KeyChange)event;
          contexts.get(keyChange.getStaffNumber())
          .setKeySignature(keyChange.getKeySignature());
        } else if (event instanceof Note) {
          calculateAccidental((Note)event, contexts);
        } else if (event instanceof Chord) {
          for (Note note: (Chord)event)
            calculateAccidental(note, contexts);
        }
      }
    }
  }

  private class SlurBuilder {
    private final Collection<SlurBounds> slurs = new LinkedList<SlurBounds>();
    private final Map<Integer, SlurBounds> slurMap =
      new HashMap<Integer, SlurBounds>();
    SlurBuilder() {
    }
    /** Checks if the given note is a slur curve point and remembers it if so.
     */
    void visit(Note note) {
      final Note.Notations notations = note.getNotations();
      if (notations != null) {
        for (Note.Notations.Slur slur: notations.getSlurs()) {
          switch (slur.type()) {
          case START:
            slurMap.put(slur.number(), new SlurBounds(note));
            break;
          case CONTINUE:
            if (slurMap.containsKey(slur.number())) {
              slurMap.get(slur.number()).other().add(note);
            }
            break;
          case STOP:
            if (slurMap.containsKey(slur.number())) {
              final SlurBounds bounds = slurMap.get(slur.number());
              bounds.setEnd(note);
              if (slurs.add(bounds)) {
                slurMap.remove(slur.number());
              }
            }
            break;
          default: throw new AssertionError(slur.type());
          }
        }
      }
    }
    /** Creates slurs from the collected start and end points.
     */
    void buildSlurs() {
      if (slurMap.size() != 0)
        LOG.warning("Unterminated slurs: "+slurMap.size());

      for (SlurBounds bounds: slurs) {
        Note note = bounds.begin();
        final Slur slur = new Slur(note);
        while (note != bounds.end()) {
          final Fraction nextMoment = note.getMoment().add(note.getDuration());
          final Collection<Note> notes = notesAt(nextMoment);
          if (notes.size() == 1) {
            slur.add(note = notes.iterator().next());
          } else if (notes.contains(bounds.end())) {
            slur.add(note = bounds.end());
          } else if (notes.size() == 0) {
            LOG.warning("0 slur targets: '"+nextMoment+"','"
                        +note.getMoment()+"','"+note+"'");
            break;
          } else {
            boolean found = false;
            for (Note n: notes) {
              if (bounds.other().contains(n)
               || n.getVoiceName().equals(note.getVoiceName())) {
                slur.add(note = n);
                found = true;
                break;
              }
            }
            if (!found) {
              LOG.warning("Notes:"+notes);
              slur.add(note = notes.iterator().next());
            }
          }
        }
      }
      slurs.clear();
    }
    private class SlurBounds {
      private Note begin, end;
      private final Collection<Note> other = new ArrayList<Note>();
      SlurBounds(Note begin) { this.begin = begin; }
      void setEnd(Note end) { this.end = end; }
      Note begin() { return begin; }
      Note end() { return end; }
      Collection<Note> other() { return other; }
    }
  }
  
  private class TupletBuilder{
    private final LinkedList<LinkedList<Note>> map = new LinkedList<LinkedList<Note>>();
    
    public TupletBuilder(){}
    
    //Group note with TimeModification by measure in map
    void visitNote(Note note, boolean newMeasure){
      if(note.getTimeModification()!=null){
        if (newMeasure)
          map.add(new LinkedList<Note>());
        map.getLast().add(note);
      }
    }
    
    
    void completeTuplet(Tuplet tuplet, Note note, LinkedList<Note> linkedListNotes){
      if(note!=null){
        if (note.getNotations()!=null && note.getNotations().tupletElementXMLMaxNumber()>1){ //nested tuplet
            System.out.println("Max number :"+note.getNotations().tupletElementXMLMaxNumber());
            boolean hasStart = false;
            boolean firstStop=true; 
            boolean hasStop=false;
            Tuplet lastTuplet=tuplet;
            List<TupletElementXML> tupletElementsXML=note.getNotations().getTupletElementsXML();
            for (TupletElementXML tupletElementXML: tupletElementsXML){  
              switch(tupletElementXML.tupletElementXMLType()){
              case START:
                if (tupletElementXML.tupletElementXMLNumber()!=1){
                  lastTuplet.addTuplet(new Tuplet());
                  lastTuplet=(Tuplet)lastTuplet.getLast();
                }
                lastTuplet.setActualType(tupletElementXML.getActualType());
                lastTuplet.setNormalType(tupletElementXML.getNormalType());      
                hasStart=true;
                break;
              case STOP: 
                if (firstStop){
                  tuplet.addNote(note);
                  firstStop=false;
                  hasStop=true;
                }
                lastTuplet=(Tuplet)tuplet.getParent();
                break;
              default: throw new AssertionError(tupletElementXML.tupletElementXMLType());
              }
            }
            if (hasStart && hasStop)
              throw new AssertionError("A note can't be at the beginning AND the end of tuplets. MusicXML file is corrupted.");
            if(hasStart)	
              lastTuplet.addNote(note);
            if (lastTuplet!=null)//tuplet is not yet complete
              completeTuplet(lastTuplet, nextNoteOfTuplet(linkedListNotes,note),linkedListNotes);
          }
          else{
            tuplet.addNote(note); 
            if (tuplet.getNormalType()==null || tuplet.getActualType()==null){ 
              TupletElementXML tupletElementXML=null;
              if (note.getNotations()!=null && note.getNotations().getTupletElementsXML()!=null && note.getNotations().getTupletElementsXML().size()==1) //1 max
                note.getNotations().getTupletElementsXML().get(0); 
              if (tuplet.getParent()!=null && tupletElementXML!=null){
                tuplet.setActualType(tupletElementXML.getActualType());
                tuplet.setNormalType(tupletElementXML.getNormalType());
              }
            }
            if (!tuplet.completed())
              completeTuplet(tuplet, nextNoteOfTuplet(linkedListNotes,note),linkedListNotes);
        }
      }
      else LOG.warning("Tuplet can't be completed, Notes:"+tuplet);
    }
    
    void buildTuplet(){
      for (LinkedList<Note> linkedListNote: map){
    	  Note note=null;
    	  List<String> voiceList=new LinkedList<String>();
    	  	while((note=firstNoteVoice(linkedListNote,voiceList))!=null){ 
    	  		while(note!=null){
    	  			Tuplet tuplet=new Tuplet();
    	  			completeTuplet(tuplet, note,linkedListNote);
    	  			while(!(tuplet.getLast() instanceof Note))	
    	  				tuplet=(Tuplet)tuplet.getLast();	
    	  			note=(Note)tuplet.getLast();	
    	  			note=nextNoteVoice(linkedListNote,note);
    	  		}  		
    	  	}
      }     
    }
    
    // return next note after note of the tuplet which contains note
    // (nextMoment, no tuplet, same voice)
    // null otherwise
    public Note nextNoteOfTuplet(LinkedList<Note> linkedListNotes, Note note){
    	final Fraction nextMoment = note.getMoment().add(note.getDuration());
    	for (Note n: linkedListNotes){
    		if (n.getMoment().equals(nextMoment)){
    			if (n.getTuplet()==null && n.getVoiceName().equals(note.getVoiceName())){
    				return n;
    			}
    		}
    	}
    	return null;
    }
    
    // return the first note(in the score) of linkedListNotes if it has not tuplet
    // by voice
    // null otherwise
    public Note firstNoteVoice(LinkedList<Note> linkedListNotes, List<String> voiceList){
    	
    	Note firstNote=linkedListNotes.getFirst();
    	Fraction minMoment=firstNote.getMoment();	
    	
    	for (Note note: linkedListNotes){
    		if(note.getMoment().compareTo(minMoment)<0 && voiceList.contains(note.getVoiceName())){
    			firstNote=note;
    			minMoment=firstNote.getMoment();
    		}
    	}
    	voiceList.add(firstNote.getVoiceName());
    	if (firstNote.getTuplet()!=null)
    		return null;	
    	return firstNote;
    }
       
    //  - return the next note of linkedListNotes in the same voice
    //  - return null if there is not next notes without tuplet in
    //  linkedListNotes(same measure and timeModification)
    Note nextNoteVoice(LinkedList<Note> linkedListNotes, Note note){
      Fraction currentMoment=note.getMoment();
      final Fraction nextMoment = currentMoment.add(note.getDuration());  
      for (Note n: linkedListNotes){
    	  if (n.getTuplet()==null && (n.getMoment().compareTo(nextMoment)>0) 
    			  && n.getVoiceName().equals(note.getVoiceName())){ 
          return n;
        } 
    	  
      } 
      return null; 
    } 
  }

  /** Returns a list of all Note objects at a given musical offset.
   * If a chord appears at that offset, all of its notes are returned
   * separately.
   */
  private Collection<Note> notesAt(final Fraction offset) {
    Collection<Note> notes = new ArrayList<Note>();
    for (Event event: eventList.eventsAt(offset)) {
      if (event instanceof Chord) {
        notes.addAll((Chord)event);
      } else if (event instanceof Note) {
        notes.add((Note)event);
      }
    }
    return notes;
  }

  private void calculateAccidental (
    Note note, List<AccidentalContext> contexts
  ) {
    Pitch pitch = note.getPitch();
    if (pitch != null) {
      int staffNumber = note.getStaffNumber();
      AccidentalContext accidentalContext = contexts.get(staffNumber);
      Accidental accidental = null;

      if (pitch.getAlter() != accidentalContext.getAlter(pitch.getOctave(),
                                                         pitch.getStep())) {
        accidental = Accidental.fromAlter(pitch.getAlter());
        if (accidental != null)
          note.setAccidental(accidental);
      }
      accidentalContext.accept(pitch, accidental);
    }
  }

  public MidiInstrument getMidiInstrument(String id) {
    NodeList nodeList = scorePart.getElementsByTagName("midi-instrument");
    for (int index = 0; index < nodeList.getLength(); index++) {
      MidiInstrument instrument = new MidiInstrument((Element)nodeList.item(index));
      if (id == null) return instrument;
      if (id.equals(instrument.getId())) return instrument;
    }

    return null;
  }

  public TimeSignature getTimeSignature() { return timeSignature; }
  public KeySignature getKeySignature() {
    for (Object event:eventList) {
      if (event instanceof GlobalKeyChange) {
        GlobalKeyChange globalKeyChange = (GlobalKeyChange)event;
        return globalKeyChange.getKeySignature();
      }
    }
    return new KeySignature(0);
  }

  /** Gets a flat list of all events occuring in thsi part.
   * Events are ordered from left to right with increasing time.
   * Several events can have the same musical offset.
   * Chords are represented with container objects.
   * TODO: Group events by measure.
   */
  public MusicList getMusicList () { return eventList; }

  /** Gets the name of this part.
   * @return the part name as a string, or null if printing is not adviced
   */
  public String getName() {
    for (Node node = scorePart.getFirstChild(); node != null;
         node = node.getNextSibling()) {
      if (node.getNodeType() == Node.ELEMENT_NODE
       && "part-name".equals(node.getNodeName())) {
        Element partName = (Element)node;
        if (!"no".equals(partName.getAttribute("print-object"))) {
          return partName.getTextContent();
        }
      }
    }
    return null;
  }

  /** Gets a list of all directives contained in this part.
   * Directives are special directions usually occuring at the start of
   * the piece.
   */
  public List<Direction> getDirectives() {
    final List<Direction> directives = new ArrayList<Direction>();
    for (Event event: eventList) {
      if (event instanceof Direction) {
        final Direction direction = (Direction)event;
        if (direction.isDirective()) {
          final String directive = direction.getWords();
          if (directive != null && !directive.isEmpty()) {
            directives.add(direction);
          }
        }
      }
    }
    return directives;
  }
  static boolean elementHasChild(Element element, String tagName) {
    for (Node node = element.getFirstChild(); node != null;
         node = node.getNextSibling())
      if (node.getNodeType() == Node.ELEMENT_NODE
       && node.getNodeName().equals(tagName)) return true;

    return false;
  }
}
