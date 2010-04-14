/*
 
 <This Java Class is part of the jMusic API version 1.5, March 2004.>
 
 Copyright (C) 2000 Andrew Sorensen & Andrew Brown
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or any
 later version.
 
 This program is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 */ 

package jm.util;

import jm.midi.MidiSynth;
import jm.music.data.*;
import jm.JMC;
import jm.audio.*;

/*  Enhanced by the Derryn McMaster 2003 */

public class Play implements JMC{
    /**
    * The current (only) one time playback thread.
     */
    private static PlayThread pt;
    /**
    * The current (only) repeated playback thread.
     */
    private static PlayCycle pc;
	/**
     * True if a midiCycle is currently playing
     */
	private static boolean cyclePlaying = false;
    /**
     * True if a one time MIDI playback is currently playing
     */
	private static boolean midiPlaying = false;
    /** A thread started to time the duration of playbnack */
    private static Thread pauseThread;
	
	/**
     * Constructor
     */
	public Play() {}
	
	/**
     * Used by infinite cycle players (eg midiCycle())
     * to stop the playback loop.
     */
	public static void stopCycle(){
		cyclePlaying = false;
        if (pc != null) pc.stopPlayCycle();
	}
	
	/**
     * Used by infinite cycle player threads to check
     * cyclePlaying status.
     */
	public static boolean cycleIsPlaying(){
		return cyclePlaying;
	}
	
    /**
     * Thread.sleeps for a period of 1 score play length
     * (i.e. the time it would take for the specified 
     * score to play).
     * Can be used in conjunction with midiCycle() if the 
     * score requires a re-compute just before being
     * replayed.  (i.e. sleeps for one loop)  Should 
     * be placed immediately after the Play.midiCycle() 
     * command to ensure that the time waited is for 
     * the currently playing score, and not subject 
     * to any changes made to the score since.
     * @param Score The score used for timing the sleep.
     */
	public static void waitCycle(Score s){
		try{	// wait duration plus 2 second for construction and reverb trail
			Thread.sleep((int)(1000.0 * 60.0 / s.getTempo() * s.getEndTime() + 2000));
		}catch(Exception e){e.printStackTrace();}
	}
	/**
     * Playback a MIDI file from disk.
     * @param fileName The name of the file to play back.
     */
    public static void mid(String fileName) {
        Score score = new Score();
        Read.midi(score, fileName);
        Play.midi(score);
    }
	
	//----------------------------------------------
	// MidiSynth - JavaSound MIDI playback
	//----------------------------------------------
	/**
     * Playback the jMusic score JavaSound MIDI
     * @param Note The note to be played
     */
	public static void midi(Note n) {
        midi(n, true);
	}	
    
	/**
     * Playback the jMusic score JavaSound MIDI
     * @param Phrase The Phrase to be played
     */
	public static void midi(Phrase phr) {
        midi(phr, true);
	}
    
	/**
     * Playback the jMusic score JavaSound MIDI
     * @param Part The Part to be played
     */
	public static void midi(Part p) {
        midi(p, true);
	}
	
	
	/**
     * Playback the jMusic score JavaSound MIDI using the default value of 
     * true for 'exit' - See Play.midi(Score,boolean)
     * @param Score: The score to be played.
     */ 
	public static void midi(Score s) {
		midi(s,true);
	}
	
	
    /**
     * Playback the jMusic score JavaSound MIDI
     * @param Note The note to be played
     * @param exit Crash program after playabck? true or false
     */
	public static void midi(Note n, boolean exit) {
		Score s = new Score("One note score", 60);
        s.addPart(new Part(new Phrase(n)));
        midi(s,exit);
	}	
    
	/**
     * Playback the jMusic score JavaSound MIDI
     * @param Phrase The Phrase to be played
     * @param exit Crash program after playabck? true or false
     */
	public static void midi(Phrase phr, boolean exit) {
		double tempo = 60;
		if(phr.getTempo() != Phrase.DEFAULT_TEMPO) tempo = phr.getTempo();
		Score s = new Score(phr.getTitle() + " score", tempo);
        if (phr.getTempo() != Phrase.DEFAULT_TEMPO) s.setTempo(phr.getTempo());
        s.addPart(new Part(phr));
        midi(s,exit);
	}
    
	/**
     * Playback the jMusic score JavaSound MIDI
     * @param Part The Part to be played
     * @param exit Crash program after playabck? true or false
     */
	public static void midi(Part p, boolean exit) {
		double tempo = 60;
		if(p.getTempo() != Part.DEFAULT_TEMPO) tempo = p.getTempo();
		Score s = new Score(p.getTitle() + " score", tempo);
        if (p.getTempo() != Part.DEFAULT_TEMPO) s.setTempo(p.getTempo());
        s.addPart(p);
        midi(s,exit);
	}
	
    /**
     * Playback the jMusic score JavaSound MIDI.
     * This method exits the application on completion.
     * To avoid this exit call, pass false as the second argument.
     * @param Score: The score to be played.
     * @param boolean exit: If true, System.exit(0) will be called at the end.
     */ 
	public static void midi(Score s, boolean exit) {
        if(midiPlaying) stopMidi();
        midiPlaying = true;
        Score defensiveCopy = s.copy();
        
        System.out.print("-- Constructing MIDI file from'" + s.getTitle() + "'...");
        pt = new PlayThread(defensiveCopy);
        new Thread(pt).start();
        System.out.print(" Playing with JavaSound ...");
        midiWait(s, exit);
        /*
         try {
             waitCycle(defensiveCopy);
             if (exit)System.exit(0); // horrid but less confusing for beginners
         }catch (Exception e) {
             System.err.println("jMusic MIDI Playback Error:" + e);
             return;
         }
         System.out.println(" Completed --");
         */
	}
    
    private static void midiWait(final Score score, final boolean exit) {
        pauseThread = new Thread( new Runnable() {
            public void run() {
                try {
                    pauseThread.sleep((int)(score.getEndTime() * 60.0 /score.getTempo() * 1000.0) + 3000);
                } catch (Exception e) {System.out.println("jMusic Play error in pause thread");}
                System.out.println(" Completed MIDI playback --------");
                if (exit) System.exit(0); // horrid but less confusing for beginners
            }});
        pauseThread.start();
    }
    
    public static void stopMidi() {
        if (pt != null) {
            pt.stopPlayThread();
            midiPlaying = false;
        }
    }	
	
	/**
    * Repeated playback the jMusic score JavaSound MIDI
     * @param Note The note to be played. See midiCycle(Score s)
     */
	public static void midiCycle(Note n) {
		Score s = new Score("One note score");
        s.addPart(new Part(new Phrase(n)));
        midiCycle(s);
	}	
    
	/**
     * Repeated playback the jMusic score JavaSound MIDI
     * @param Phrase The Phrase to be played. See midiCycle(Score s)
     */
	public static void midiCycle(Phrase phr) {
		Score s = new Score(phr.getTitle() + " score");
        s.addPart(new Part(phr));
        midiCycle(s);
	}
    
	/**
     * Repeated playback the jMusic score JavaSound MIDI
     * @param Part The Part to be played.  See midiCycle(Score s)
     */
	public static void midiCycle(Part p) {
		Score s = new Score(p.getTitle() + " score");
        s.addPart(p);
        midiCycle(s);
	}
    
	/**
     * Continually repeat-plays a Score object (i.e. loops).  If the Score object 
     * reference is altered, that alteration will be heard on the following cycle.
     * Score should be a minimum of 1 beat long.
     * NB: It takes a small amount of time to load the Score.  Due to the use of 
     * threads and a timer, no delay is heard between loops (i.e. the score is 
     * prepared just in time to be played).  However, this means that alterations 
     * made to the Score object near the end of the segment (ie final beats) may not
     * be heard for an extra loop.  Putting this another way, if a 1-bar Score is 
     * being looped, the next bar may be pre-loaded as the last beat is being played.
     * This means that any alterations made after this point will not appear in the 
     * very next loop.
     */
	public static void midiCycle(Score s){
		if (cyclePlaying == true) stopCycle();
        cyclePlaying = true;
        pc = new PlayCycle(s);
		new Thread(pc).start();
	}
	
    /**
     * Playback an audio file jMusic audio playback via javaSound.
     * This method requires the javax.sound packages in Java 1.3 or higher.
     * @param String The name of the file to be played.
     */ 
    public static void au(String fileName) {
        au(fileName, true);
    }
    
    /**
     * Playback an audio file jMusic audio playback via javaSound.
     * jMusic currently supports playback of .au, .wav and .aif file formats.
     * This method requires the javax.sound packages in Java 1.3 or higher.
     * By default this method, when complete, will exit the application. To prevent
     * this pass 'false' as the autoClose argument.
     * @param filename The name of the file to be played.
     * @param autoClose A flag for exiting java after the file has played.
     */ 
    public static void au(String fileName, boolean autoClose) {
        jm.gui.wave.WaveFileReader afr = new jm.gui.wave.WaveFileReader(fileName);
        jm.music.rt.RTLine[] lineArray = {new jm.util.AudioRTLine(fileName)};	
        jm.audio.RTMixer mixer = new jm.audio.RTMixer(lineArray) ;//, 4096, si.getSampleRate(), si.getChannels(), 0.01);	
            mixer.begin();
            System.out.println("---------- Playing '" + fileName + "'... Sample rate = "
                               + afr.getSampleRate() + " Channels = " + afr.getChannels() + " ----------");
            if (autoClose) {
                java.io.File audioFile = new java.io.File(fileName);
                try {
                    int byteSize = afr.getBits() - 1;
                    // bytes, sample rate, channels, milliseconds, cautious buffer
                    Thread.sleep((int)((double)audioFile.length() / byteSize / 
                                       afr.getSampleRate() / afr.getChannels() * 1000.0) + 1000);
                } catch (InterruptedException e) {
                    System.err.println("jMusic play.au error: Thread sleeping interupted");
                }
                System.out.println("-------------------- Completed Audio Playback ----------------------");
                System.exit(0); // horrid, but less confusing for beginners
            }
    }
    
    /**
     * Playback a phrase as real time audio.
     * @param phrase The phrase to be played.
     * @param insts An array of instruments to play the phrase with
     */
    public static void audio(Phrase phrase, Instrument[] insts) {
        audio(new Score(new Part(phrase)), insts);
    }
    
    /**
     * Playback a phrase as real time audio.
     * @param phrase The phrase to be played.
     * @param inst An instrument to play the phrase with
     */
    public static void audio(Phrase phrase, Instrument inst) {
        Part part = new Part(phrase);
        if(phrase.getTempo() != Phrase.DEFAULT_TEMPO) part.setTempo(phrase.getTempo());
        audio(part, new Instrument[] {inst});
    }
    
    /**
     * Playback a part as real time audio.
     * @param part The part to be played.
     * @param insts An array of instruments to play the part with
     */
    public static void audio(Part part, Instrument[] insts) {
        Score score = new Score(part);
        if(part.getTempo() != Part.DEFAULT_TEMPO) score.setTempo(part.getTempo());
        audio(score, insts);
    }
    
    /**
     * Playback a part as real time audio.
     * @param part The part to be played.
     * @param inst An instrument to play the part with
     */
    public static void audio(Part part, Instrument inst) {
        audio(new Score(part), new Instrument[] {inst});
    }
    
    /**
     * Playback a score as real time audio.
     * @param score The score to be played.
     * @param inst An instrument to play the score with
     */
    public static void audio(Score score, Instrument inst) {
        audio(score, new Instrument[] {inst});
    }
    /**
     * Playback a score as real time audio.
     * @param score The score to be played.
     * @param insts An array of instrument to play the score with
     */
    public static void audio(Score score, Instrument[] insts) {
        System.out.print("-------- Playing Score as Audio ...");
        // make all instrument real time
        for(int i=0; i<insts.length; i++) {
            insts[i].setOutput(Instrument.REALTIME);
        }
        // get all the phrases in a vector
        java.util.Vector v = new java.util.Vector();
        for(int i=0; i<score.size(); i++) {
            Part p = score.getPart(i);
            for(int j=0; j<p.size(); j++) {
                Phrase phr = p.getPhrase(j);
                if(phr.getInstrument() == Phrase.DEFAULT_INSTRUMENT) 
                    phr.setInstrument(p.getInstrument());
                if(phr.getTempo() == Phrase.DEFAULT_TEMPO) 
                    phr.setTempo(p.getTempo());
                v.addElement(phr);
            }
        }
        // create RTPhrases for each phrase
        jm.music.rt.RTLine[] lines = new jm.music.rt.RTLine[v.size()];
        for(int i=0; i<v.size(); i++) {
            Phrase phr = (Phrase)(v.elementAt(i));
            lines[i] = new jm.music.rt.RTPhrase(phr, insts[phr.getInstrument()]);
        }        
        // create mixer and wait for the end then pause the mixer
        RTMixer mixer = new RTMixer(lines);
        mixer.begin();
        audioWait(score, mixer);
    }
    
    private static void audioWait(final Score score, final RTMixer mixer) {
        pauseThread = new Thread( new Runnable() {
            public void run() {
                try {
                    pauseThread.sleep((int)(score.getEndTime() * 60.0 /score.getTempo() * 1000.0));
                } catch (Exception e) {System.out.println("jMusic Play error in pause thread");}
                System.out.println(" Completed audio playback --------");
                mixer.pause();
            }});
        pauseThread.start();
    }
    
    /**
     * Playback an audio file using Java Applet audioclip playback.
     * A audioClip limitation is that the file must be small enough to fit into RAM.
     * This method is compatibl with Java 1.1 and higher.
     * @param String The name of the file to be played.
     */ 
    public static void audioClip(String fileName) {
        System.out.println("-------- Playing an audio file ----------");
        System.out.println("Loading sound into memory, please wait...");
        java.io.File audioFile = new java.io.File(fileName);
        try {
	    java.net.URI tempURI = audioFile.toURI();
	    java.net.URL tempURL = tempURI.toURL();
            java.applet.AudioClip sound = java.applet.Applet.newAudioClip(tempURL);
            System.out.println("Playing '" + fileName + "' ...");
            sound.play();
        } catch (java.net.MalformedURLException e) {
            System.err.println("jMusic play.au error: malformed URL or filename");
        }
        try {
            // bytes, channels, sample rate, milliseconds, cautious buffer
            Thread.sleep((int)(audioFile.length() / 2.0 / 44100.0 / 2.0 * 1000.0) + 1000);
        } catch (InterruptedException e) {
            System.err.println("jMusic play.au error: Thread sleeping interupted");
        }
        System.out.println("-------------------- Completed Playback ----------------------");
        System.exit(0); // horrid but less confusing for beginners
	}
	
}


