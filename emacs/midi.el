;;; midi.el --- MIDI

;; Copyright (C) 2005  Free Software Foundation, Inc.

;; Author: Mario Lang <mlang@delysid.org>
;; Keywords: multimedia, files

;; This file is free software; you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation; either version 2, or (at your option)
;; any later version.

;; This file is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.

;; You should have received a copy of the GNU General Public License
;; along with GNU Emacs; see the file COPYING.  If not, write to
;; the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
;; Boston, MA 02111-1307, USA.

;;; Commentary:

;; `midi-mode' provides a view on standard MIDI file data and some limited
;; abilities to edit data.

;; MIDI file data is parsed and returned as a self-contained object.
;; These "objects" can be manipulated programmatically as usual.  In addition
;; they can be displayed in a buffer.  MIDI file objects can also be written
;; back out to a file.

;; `midi-mode' tries to provide a mode for editing MIDI files based on the
;; above mentioned services.  The core services for parsing and writing
;; MIDI data do work quite reliably at the time of this writing, but
;; `midi-mode' is lacking lots of possible features.  For instance, currently,
;; there is no way to edit and/or save a file in `midi-mode', you can
;; only view the data and manipulate it programmatically.

;; At the end of this file, there are currently some functions not quite
;; directly related to `midi-mode'.  They are functions which use services
;; provided by this file to operate on musical data in MIDI format
;; and are only here for now since it is more convinient to have them in
;; this file.

;;; History:
;;
;; New in version 0.1:
;; * Make use of running-status in `smf-write' resulting in smaller output.
;; * Implement reading SysEx events.

;;; Code:

(require 'cl)
(require 'ewoc)

;;; MIDI file object documentation
;
; A MIDI file object looks like this:
;  (TYPE PPQN ("MTrk" EVENT ...) ...)
; where
;  TYPE  is a number from 0 to 2, identifying the MIDI file type
;  PPQN  is the number of ticks per quarter note
;  EVENT is a list of the form (TIME TYPE ...) where TIME is a integer
;        and TYPE a symbol identifying the kind of MIDI event type.
;        For instance (0 Note 0 60 127 384 0) at a ppqn of 384 identifies a
;        middle-C played at time 0 at MIDI channel 0 with full velocity for
;        the duration of a quarter note.

;;; Standard MIDI file reader

;; Binary IO

(defsubst smf-read-byte ()
  "Read one byte from the current buffer and advance point."
  (forward-char 1) (preceding-char))

(defun smf-read-bytes (count)
  "Read COUNT bytes as big endian integer and advance point."
  (let ((val 0))
    (dotimes (i count val)
      (setq val (logior (lsh val 8) (smf-read-byte))))))

(defun smf-read-varlen ()
  "Read a variable quantity from the current buffer and advance point."
  (do* ((b (smf-read-byte)) (n (logand b #B01111111)))
      ((/= (logand b #B10000000) #B10000000) n)
    (setq b (smf-read-byte) n (logior (ash n 7) (logand b #B01111111)))))

(defun smf-read-string ()
  "Read a MIDI file string and advance point over it."
  (let ((length (smf-read-varlen)))
    (buffer-substring (point) (progn (forward-char length) (point)))))

;; MIDI parser

(defun smf-read-meta-event ()
  "Read a MIDI file meta event."
  (case (smf-read-byte)
    (#X01 (list 'Text (smf-read-string)))
    (#X02 (list 'Copyright (smf-read-string)))
    (#X03 (list 'TrackName (smf-read-string)))
    (#X04 (list 'Instrument (smf-read-string)))
    (#X05 (list 'Lyric (smf-read-string)))
    (#X06 (list 'Marker (smf-read-string)))
    (#X07 (list 'CuePoint (smf-read-string)))
    (#X08 (list 'PatchName (smf-read-string)))
    (#X09 (list 'DeviceName (smf-read-string)))
    (#X20 (if (= (smf-read-byte) 1)
	      (list 'MIDIChannel (smf-read-byte))
	    (error "Malformed MetaEvent 0X20")))
    (#X21 (list 'UnknownMetaEvent (smf-read-string)))
    (#X2F (if (= (smf-read-byte) 0)
	      t
	    (error "Suspicious EOT")))
    (#X51 (if (= (smf-read-byte) 3)
	      (list 'TempoChange (smf-read-bytes 3))
	    (error "Suspicious TempoChange")))
    (#X54 (if (= (smf-read-byte) 5)
	      (let ((hour (smf-read-byte))
		    (minute (smf-read-byte))
		    (second (smf-read-byte))
		    (frame (smf-read-byte))
		    (subframe (smf-read-byte)))
		(list 'SMPTEOffset hour minute second frame subframe))
	    (error "Suspicious SMPTE Offset")))
    (#X58 (if (= (smf-read-byte) 4)
	      (let ((numerator (smf-read-byte))
		    (denominator (expt 2 (smf-read-byte)))
		    (cc (smf-read-byte))
		    (bb (smf-read-byte)))
		(list 'TimeSig numerator denominator cc bb))
	    (error "Suspicious TimeSig")))
    (#X59 (if (= (smf-read-byte) 2)
	      (let ((sf (smf-read-byte))
		    (mi (smf-read-byte)))
		(list 'KeySig sf mi))
	    (error "Suspicious KeySig")))
    (#X7F (list 'Proprietary (smf-read-string)))
    (t (error "Unhandled meta event %d" (char-before)))))

(defun smf-read-mtrk (length)
  "Read one MTrk chunk.
NoteOn/NoteOff and NoteOn/NoteOn(vel=0) event
pairs are unified into a Note event with a certain duration."
  (let ((end (+ (point) length))
	(notes (make-vector 16 nil))
	(ticks 0) (running-status 0))
    (dotimes (i 16) (aset notes i (make-vector 128 nil)))
    (loop while (< (point) end)
	  do (incf ticks (smf-read-varlen))
	  for event =
	  (let ((status (following-char)))
	    (if (/= (logand status #B10000000) #B10000000)
		(if (= running-status 0)
		    (error "Seen data byte without running status")
		  (setq status running-status))
	      (forward-char 1))
	    (unless (= status #XFF) (setq running-status status))
	    (let ((lower (logand status #X0F)))
	      (case (ash status -4)
		(8  (let* ((note (smf-read-byte)) (vel (smf-read-byte))
			   (old-note (aref (aref notes lower) note)))
		      (if (not old-note)
			  (list 'NoteOff lower note vel)
			(setcar (cdr old-note) 'Note)
			(setcdr (nthcdr 4 old-note)
				(list (- ticks (car old-note)) vel))
			(aset (aref notes lower) note nil))))
		(9  (let* ((note (smf-read-byte)) (vel (smf-read-byte))
			   (data (cons ticks (list 'NoteOn lower note vel))))
		      (if (= vel 0)
			  (let ((old-note (aref (aref notes lower) note)))
			    (if (not old-note)
				(cdr data)
			      (setcar (cdr old-note) 'Note)
			      (setcdr (nthcdr 4 old-note)
				      (list (- ticks (car old-note))))
			      (aset (aref notes lower) note nil)))
			(cdr (aset (aref notes lower) note data)))))
		(10 (list 'At lower (smf-read-byte) (smf-read-byte)))
		(11 (list 'CC lower (smf-read-byte) (smf-read-byte)))
		(12 (list 'PC lower (smf-read-byte)))
		(13 (list 'CP lower (smf-read-byte)))
		(14 (list 'PW lower (logior (smf-read-byte)
					    (lsh (smf-read-byte) 7))))
		(15 (case lower
		      (0  (append (list 'SysEx)
				  (loop repeat (smf-read-varlen) collect
					(smf-read-byte))))
		      (2  (let ((value (logior (smf-read-byte)
					       (lsh (smf-read-byte) 7))))
			    (list 'SongPosition value)))
		      (3  (list 'SongSelect (smf-read-byte)))
		      (6  (list 'TuneRequest))
		      (8  (list 'Clock))
		      (9  (list 'Tick))
		      (10 (list 'Start))
		      (11 (list 'Continue))
		      (12 (list 'Stop))
		      (14 (list 'ActiveSense))
		      (15 (smf-read-meta-event))
		      (t (error "Unknown stuff")))))))
	  until (eq event t) when event collect (cons ticks event))))

(defun smf-read ()
  "Read standard MIDI file data from the current buffer."
  (message "Parsing MIDI data...")
  (let ((inhibit-point-motion-hooks t)
	(notes (make-vector 16 nil))
	(id (buffer-substring (point) (progn (forward-char 4) (point))))
	(length (smf-read-bytes 4))
	type numtracks ppqn chunks)
    (unless (and (string= id "MThd") (= length 6))
      (error "Not a Stnadard MIDI file"))
    (setq type (smf-read-bytes 2)
	  numtracks (smf-read-bytes 2)
	  ppqn (smf-read-bytes 2))
    (when (and (= type 0) (/= numtracks 1))
      (error "Type 0 file with more than 1 track"))
    (while (> (- (point-max) (point)) 8)
      (setq id (buffer-substring (point) (progn (forward-char 4) (point)))
	    length (smf-read-bytes 4))
      (setq chunks
	    (nconc chunks
		   (list
		    (cons
		     id
		     (if (string= id "MTrk")
			 (progn
			   (setq numtracks (1- numtracks))
			   (smf-read-mtrk length))
		       (buffer-substring
			(point) (progn (forward-char length) (point)))))))))
    (assert (= numtracks 0))
    (message "Parsing MIDI data...done")
    (append (list type ppqn) chunks)))

;;; Standard MIDI file writer

;; Binary IO

(defun smf-write-bytes (value count)
  "Write VALUE as COUNT bytes in big endian to current buffer."
  (let (bytes)
    (dotimes (i count (apply #'insert bytes))
      (push (logand value '#XFF) bytes)
      (setq value (ash value -8)))))

(defun smf-write-varlen (value)
  "Write VALUE as variable quantity to the current buffer."
  (loop for bits from 21 downto 7 by 7
	when (>= value (expt 2 bits))
	do (insert-char (logior (logand (ash value (- bits)) 127) 128) 1))
  (insert-char (logand value 127) 1))

(defun smf-write-string (string)
  "Write STRING as MIDI file string to the current buffer."
  (smf-write-varlen (length string))
  (insert string))

(defvar smf-unhandled-types nil) ;;REMOVE ME
;; Writer

(defun smf-track-event-count (track)
  "Return the number of events in a track structure."
  (length (cdr track)))

(defun smf-data-p (data)
  "Return non-nil if DATA is a valid standard MIDI file content structure."
  (and (integerp (car data)) (>= (car data) 0) (<= (car data) 2)
       (integerp (cadr data))
       (consp (caddr data))
       (stringp (caaddr data)) (string= (caaddr data) "MTrk")))

(defun smf-write (data)
  "Write DATA in standard MIDI file format to the current buffer."
  (if (symbol-value 'enable-multibyte-characters)
      (error "Unable to insert MIDI file data in multibyte buffer"))
  (message "Encoding MIDI data...")
  (destructuring-bind (type ppqn &rest tracks) data
    (let ((inhibit-modification-hooks t)
	  (inhibit-point-motion-hooks t)
	  (one-percent (/ (apply #'+ (mapcar #'smf-track-event-count tracks))
			  100))
	  (events-written 0))
      (insert "MThd") (smf-write-bytes 6 4)
      (smf-write-bytes type 2)
      (smf-write-bytes (length tracks) 2)
      (smf-write-bytes ppqn 2)
      (mapc
       (lambda (track)
	 (insert "MTrk")
	 (let ((size-pos (point))
	       (tick 0) (last-status 0)
	       notes-on)
	   (mapc
	    (lambda (event)
	      (destructuring-bind (newtick type &rest data) event
		(setq notes-on
		      (remove-if
		       (lambda (info)
			 (when (>= newtick (car info))
			   (smf-write-varlen (- (car info) tick))
			   (setq tick (car info))
			   (let ((status (logior #X80 (nth 1 info))))
			     (unless (= last-status status)
			       (insert (setq last-status status))))
			   (insert (nth 2 info) (nth 3 info))
			   t))
		       notes-on))
		(smf-write-varlen (- newtick tick))
		(setq tick newtick)
		(case type
		  (Note        (let ((status (logior #X90 (car data))))
				 (unless (= last-status status)
				   (insert (setq last-status status))))
			       (insert (nth 1 data) (nth 2 data))
			       (setq notes-on (sort (cons (list
							   (+ tick (nth 3 data))
							   (nth 0 data) (nth 1 data)
							   (or (nth 4 data) 0))
							  notes-on)
						    #'car-less-than-car)))
		  (NoteOn      (let ((status (logior #X90 (car data))))
				 (unless (= last-status status)
				   (insert (setq last-status status))))
			       (insert (nth 1 data) (nth 2 data)))
		  (NoteOff     (let ((status (logior #X80 (car data))))
				 (unless (= last-status status)
				   (insert (setq last-status status))))
			       (insert (nth 1 data) (nth 2 data)))
		  (CC          (let ((status (logior #XB0 (car data))))
				 (unless (= last-status status)
				   (insert (setq last-status status))))
			       (insert (nth 1 data) (nth 2 data)))
		  (PC          (let ((status (logior #XC0 (car data))))
				 (unless (= last-status status)
				   (insert (setq last-status status))))
			       (insert (nth 1 data)))
		  (PW          (let ((status (logior #XE0 (car data))))
				 (unless (= last-status status)
				   (insert (setq last-status status))))
			       (insert (logand (nth 1 data) #B01111111) (logand (lsh (nth 1 data) -7) #B01111111)))
		  (UnknownMetaEvent (insert #XFF #X21) (setq last-status 0)
				    (smf-write-string (nth 0 data)))
		  (TempoChange (insert #XFF #X51 3) (setq last-status 0)
			       (smf-write-bytes (car data) 3))
		  (SMPTEOffset (setq last-status 0)
			       (insert #XFF #X54 5
				       (nth 0 data) (nth 1 data) (nth 2 data)
				       (nth 3 data) (nth 4 data)))
		  (TimeSig     (setq last-status 0)
			       (insert #XFF #X58 4
				       (nth 0 data) (round (log (nth 1 data) 2))
				       (nth 2 data) (nth 3 data)))
		  (KeySig      (setq last-status 0)
			       (insert #XFF #X59 2 (nth 0 data) (nth 1 data)))
		  (Text        (setq last-status 0)
			       (insert #XFF #X01)
			       (smf-write-string (nth 0 data)))
		  (Copyright   (insert #XFF #X02) (setq last-status 0)
			       (smf-write-string (nth 0 data)))
		  (TrackName   (insert #XFF #X03) (setq last-status 0)
			       (smf-write-string (nth 0 data)))
		  (Instrument  (insert #XFF #X04) (setq last-status 0)
			       (smf-write-string (nth 0 data)))
		  (Lyric       (insert #XFF #X05) (setq last-status 0)
			       (smf-write-string (nth 0 data)))
		  (Marker      (insert #XFF #X06) (setq last-status 0)
			       (smf-write-string (nth 0 data)))
		  (Proprietary (insert #XFF #X7F) (setq last-status 0)
			       (smf-write-string (nth 0 data)))
		  (t (setq smf-unhandled-types (cons type
						     smf-unhandled-types))))
		(incf events-written)
		(if (and (> one-percent 10)
			 (= (% events-written one-percent) 0))
		    (message "Encoding MIDI data...%d%%"
			     (round (/ events-written one-percent))))))
	    (cdr track))
	   (mapc
	    (lambda (info)
	      (smf-write-varlen (- (car info) tick))
	      (setq tick (car info))
	      (let ((status (logior #X80 (nth 1 info))))
		(unless (= last-status status)
		  (insert (setq last-status status))))
	      (insert (nth 2 info) (nth 3 info)))
	    notes-on)
	   (smf-write-varlen tick) (insert #XFF #X2F 0)
	   (let ((size (- (point) size-pos)))
	     (save-excursion
	       (goto-char size-pos)
	       (smf-write-bytes size 4)))))
       tracks)))
  (message "Encoding MIDI data...done")
  t)

;;; Event accessors

(defsubst smf-event-type (event)
  (cadr event))

(defsubst smf-event-note-p (event)
  (eq (smf-event-type event) 'Note))

;;; Ticks

(defun smf-gcd (data)
  "Find the gcd for all tick information in DATA."
  (apply #'gcd (loop for ticks in
		     (loop for track in (cddr data)
			   collect (mapcar #'car (cdr track)))
		     when (> (apply #'max ticks) 0) collect
		     (apply #'gcd ticks))))

(defun smf-apply-event-time-operation (tracks operator arg)
  "Apply OPERATOR to each tick in TRACKS with second arg ARG."
  (mapcar (lambda (track)
	    (cons (car track)
		  (mapcar (lambda (event)
			    (append (list (funcall operator (nth 0 event) arg))
				    (case (nth 1 event)
				      (Note (list 'Note
						  (nth 2 event)
						  (nth 3 event)
						  (nth 4 event)
						  (funcall operator
							   (nth 5 event) arg)
						  (nth 6 event)))
				      (t (cdr event)))))
			  (cdr track))))
	  tracks))

(defun smf-ticks-divide (data amount)
  "Divide all ticks in DATA by AMOUNT."
  (append (list (nth 0 data) (/ (nth 1 data) amount))
	  (smf-apply-event-time-operation (cddr data) #'/ amount)))
	
(defun smf-use-smallest-ppqn (data)
  "Adjust ticks in DATA to smallest possible ppqn without loosing information."
  (smf-ticks-divide data (smf-gcd data)))

;;; Playback

(defun smf-write-data (data filename)
  (with-temp-buffer
    (set-buffer-multibyte nil)
    (smf-write data)
    (write-region (point-min) (point-max) filename)))

(defun smf-play (data)
  "Play SMF data."
  (let ((filename (make-temp-file "midi")))
    (smf-write-data data filename)
    (start-process "timidity" nil "timidity" filename)))

;;; Tempo

(defun smf-make-tempo-map (&rest tracks)
  "Create a \"virtual\" track with tempo and time signature information."
  (sort (apply #'append (loop for track in tracks collect
			      (loop for event in (cdr track) when
				    (or (eq (nth 1 event) 'TempoChange)
					(eq (nth 1 event) 'TimeSig))
				    collect event))) #'car-less-than-car))

;;; `midi-mode'

(defun smf-format-ticks (ticks numer denom ppqn)
  (let* ((ppb (round (/ ppqn (/ denom 4.0))))
	 (beat (/ ticks ppb))
	 (measure (/ beat numer)))
    (format "%3d:%d:%03d" measure (% beat numer) (% ticks ppb))))

(defvar smf-header-lines
  '(("Type: 0, ppqn: " (:eval (format "%d" smf-ppqn)))
    ("Type: 1, ppqn: " (:eval (format "%d" smf-ppqn))
     ", Track " (:eval (format "%d/%d"
			       (1+ (or smf-current-track 0))
			       (length smf-chunks))))
    ("Type: 2, ppqn: " (:eval (format "%d" smf-ppqn))))
  "Header line format for the different MIDI file types.")

(defvar smf-current-track nil)
(make-variable-buffer-local 'smf-current-track)

(defvar smf-type nil)
(make-variable-buffer-local 'smf-type)

(defvar smf-ppqn nil)
(make-variable-buffer-local 'smf-ppqn)

(defvar smf-chunks nil
  "A list of MIDI file chunks, already parsed.")
(make-variable-buffer-local 'smf-chunks)

(defun smf-data ()
  "Reconstruct a complete data object from buffer-local variables."
  (append (list smf-type smf-ppqn) smf-chunks))

(defvar smf-tracks nil)
(make-variable-buffer-local 'smf-tracks)

(defun smf-play-whole-file ()
  (interactive)
  (smf-play (smf-data)))

(defun smf-display-insert-event (tick type &rest args)
  (insert " " (smf-format-ticks tick 4 4 smf-ppqn) " ")
  (insert (format "%S" (append (list type) args))))

(defun smf-set-current-track (number)
  "Set the currently visible track to NUMBER."
  (when (and smf-current-track (< smf-current-track (length smf-tracks)))
    (setcar (nthcdr 2 (nth smf-current-track smf-tracks)) (point)))
  (widen)
  (narrow-to-region (nth 0 (nth number smf-tracks))
		    (nth 1 (nth number smf-tracks)))
  (goto-char (nth 2 (nth number smf-tracks)))
  (setq smf-current-track number))

(defun smf-next-track (&optional n)
  "Make next track visible, optionally advancing from the current track by N."
  (interactive "P")
  (if (= smf-type 0) (error "Type 0 MIDI files do only have one track"))
  (unless n (setq n 1))
  (smf-set-current-track (% (+ smf-current-track n) (length smf-tracks))))

(defun smf-previous-track (&optional n)
  "Make previous track visible, optionally retracting by N."
  (interactive "P")
  (if (= smf-type 0) (error "Type 0 MIDI files do only have one track"))
  (unless n (setq n 1))
  (smf-set-current-track (if (= smf-current-track 0)
			     (1- (length smf-tracks))
			   (1- smf-current-track))))

(defun smf-goto-tick (tick)
  "Put point near an event with time TICK."
  (goto-char (point-min))
  (while (and (not (eobp))
	      (< (car (smf-event-at)) tick))
    (forward-line 1))
  (and (not (eobp))
       (= tick (car (smf-event-at)))))

(defun smf-next-track-preserving-time (&optional n)
  "Make next track visible, optionally advancing from the current track by N."
  (interactive "P")
  (let ((tick (car (smf-event-at))))
    (if (= smf-type 0) (error "Type 0 MIDI files do only have one track"))
    (unless n (setq n 1))
    (smf-set-current-track (% (+ smf-current-track n) (length smf-tracks)))
    (unless (smf-goto-tick tick) (ding))))
    
(defun smf-previous-track-preserving-time (&optional n)
  "Make previous track visible, optionally advancing by N."
  (interactive "P")
  (let ((tick (car (smf-event-at))))
    (if (= smf-type 0) (error "Type 0 MIDI files do only have one track"))
    (unless n (setq n 1))
    (smf-set-current-track (if (= smf-current-track 0)
			       (1- (length smf-tracks))
			     (1- smf-current-track)))
    (unless (smf-goto-tick tick) (ding))))
    
(defun smf-guess-key (track)
  "Guess the key of TRACK (a number) in current buffer.
Return a list of possible keys, each key being a list of (KEY MODE)."
  (let* ((major '(0 2 4 5 7 9 11))
	 (minor '(0 2 3 5 7 8 10))
	 (key (loop for scale in (list major minor) collect
		    (loop for i from 0 to 11 collect
			  (mapcar (lambda (n) (% (+ n i) 12)) scale))))
	 (score (loop repeat 2 collect (loop repeat 12 collect 0))))
    (loop for event in (cdr (nth track smf-chunks))
	  when (smf-event-note-p event) do
	  (let ((note (% (cadddr event) 12)))
	    (loop for mode from 0 to 1 do
		  (loop for root from 0 to 11 do
			(let ((keynats (nth root (nth mode key))))
			  (if (= note (car keynats))
			      (incf (nth root (nth mode score)) 2)
			    (if (member note (cdr keynats))
				(incf (nth root (nth mode score))))))))))
    ;; Find keys with highest score
    (let ((max-score 0) keys)
      (dotimes (mode 2)
	(loop for key from 0 to 11
	      for curscore = (nth key (nth mode score))
	      if (> curscore max-score) do
	      (setq max-score curscore
		    keys (list (list key mode)))
	      else if (= max-score curscore) do
	      (setq keys (append keys (list (list key mode))))))
      keys)))

(defun midi-event-pretty-printer (event)
  (insert (format "%S" event)))
(defun smf-display-track (track)
  (let ((ewoc (ewoc-create #'midi-event-pretty-printer "" "")))
    (mapc (lambda (event) (ewoc-enter-last ewoc event)) (cdr track))
    ewoc))

(defun smf-redisplay ()
  (interactive)
  (let ((old-point (point))
	(inhibit-read-only t))
    (delete-region (point-min) (point-max))
    (smf-display-track (nth smf-current-track smf-chunks))
    (goto-char old-point)))

(defun smf-event-at (&optional point)
  "Retrieve the event object corresponding to text under POINT."
  (get-text-property (or point (point)) 'smf-event))

(define-derived-mode midi-mode fundamental-mode "MIDI"
  "Mode for editing MIDI file content."
  (if (= (buffer-size) 0)
      (let ((type (read-minibuffer "MIDI file type (0, 1 or 2): " "0"))
	    (ppqn (read-minibuffer "MIDI ppqn: " "96")))
	(setq smf-type type
	      smf-ppqn ppqn))
    (destructuring-bind (type ppqn &rest chunks) (smf-read)
      (setq smf-ppqn ppqn)
      (setq smf-type type)
      (setq smf-chunks chunks)
    
      (let ((inhibit-read-only t))
	(setq smf-tracks
	      (mapcar (lambda (track)
			(widen) (goto-char (point-max))
			(narrow-to-region (point) (point))
			(let ((ewoc (smf-display-track track))
			      (beg (point-min-marker))
			      (end (point-max-marker)))
;			  (set-marker-insertion-type beg t)
			  (list beg end beg ewoc)))
		      smf-chunks))
	(set-buffer-modified-p nil))
      (smf-set-current-track 0)))
  (setq header-line-format (nth smf-type smf-header-lines)))

(defun smf-play-note (note vel dur &optional ppqn)
  (unless ppqn (setq ppqn 16))
  (smf-play `(0 ,ppqn ("MTrk" (0 Note 0 ,note ,vel ,dur 0)))))

(defun smf-play-note-at-point ()
  (interactive)
  (let ((event (get-text-property (point) 'smf-event)))
    (if (smf-event-note-p event)
	(smf-play-note (nth 3 event) (nth 4 event) (nth 5 event) smf-ppqn))))

(define-key midi-mode-map (kbd ">") 'smf-next-track)
(define-key midi-mode-map (kbd "<") 'smf-previous-track)
(define-key midi-mode-map (kbd ".") 'smf-next-track-preserving-time)
(define-key midi-mode-map (kbd ",") 'smf-previous-track-preserving-time)
(define-key midi-mode-map (kbd "SPC") 'smf-play-note-at-point)
(define-key midi-mode-map (kbd "C-c C-p") 'smf-play-whole-file)

(add-to-list 'auto-mode-alist '("\\.mid$" . midi-mode))

;;; Utils using the services provided above, mostly SuperCollider related

(defconst smf-key-to-degree
  (loop for mode from 0 to 1 collect
	  (let ((one-side (loop for skel = (if (= mode 0)
					       '(0 0.1 1 1.1 2 3 3.1 4 4.1 5 5.1 6)
					     '(0 0.1 1 2 2.1 3 3.1 4 5 5.1 6 6.1))
				for i from 0 to 4
				append (mapcar (lambda (n)
						 (+ n (* i 7)))
					       skel))))
	    (append (mapcar (lambda (n)
			      (let ((i (- n 35)))
				(if (integerp i)
				    i
				  (string-to-number (format "%f" i)))))
			    one-side)
		    one-side))))

(defun smf-note-to-degree (note key mode)
  (nth (- note key) (nth mode smf-key-to-degree)))

(defun midi-track-as-pbind (track)
  "Convert TRACK (a number) to a SuperCollider Pbind expression."
  (let ((notes (remove-if-not #'smf-event-note-p (cdr (nth track smf-chunks))))
	(key (smf-guess-key track))
	note-list dur-list sus-list)
    (while notes
      (let ((dur (if (cdr notes) (max 1 (- (nth 0 (cadr notes)) (nth 0 (car notes))))
		   (nth 5 (car notes)))))
	(push (nth 3 (car notes)) note-list)
	(push (/ (float dur) (nth 5 (car notes))) sus-list)
	(push (/ (float dur) smf-ppqn) dur-list))
      (setq notes (cdr notes)))
    (sclang-format "Pbind(\\root, %o, \\scale, %o, \\degree, %s, \\dur, %s, \\legato, %s)"
		   (caar key)
		   (if (= (cadar key) 0) '(0 2 4 5 7 9 11) '(0 2 3 5 7 8 10))
		   (sclang-format-pseq
		    (mapcar (lambda (k)
			      (smf-note-to-degree k (caar key) (cadar key)))
			    (nreverse note-list)))
		   (sclang-format-pseq (nreverse dur-list))
		   (sclang-format-pseq (nreverse sus-list)))))

(defvar smf-record-program (executable-find "arecordmidi")
  "*Path to arecordmidi.")
(defvar smf-record-filename nil)
(defvar smf-record-process nil)
(defvar smf-record-result nil
  "Contains the recording result after `smf-record-stop' was called.")

(defun smf-record-sentinel (process string)
  (setq smf-record-process nil)
  (with-temp-buffer
    (insert-file-contents-literally smf-record-filename)
    (delete-file smf-record-filename)
    (setq smf-record-filename nil
	  smf-record-data (smf-read))))

(defun smf-record-start (client &optional port)
  (interactive
   (progn
     (if smf-record-process
	 (if (y-or-n-p "Already recording, Restart? ")
	     (interrupt-process smf-record-process)
	   (error "Aborting...")))
     (list (read-from-minibuffer "Client: ")
	   (read-from-minibuffer "Port: "))))
  (unless smf-record-program
    (error "external program `arecordmidi' is required."))
  (unless port (setq port "0"))
  (setq smf-record-filename (concat (make-temp-name
				     (expand-file-name "midirec-" temporary-file-directory))
				    ".mid"))
  (setq smf-record-process
	(start-process "arecordmidi" nil
		       smf-record-program
		       (format "-p%s:%s" client port)
		       smf-record-filename))
  (set-process-sentinel smf-record-process #'smf-record-sentinel)
  (when (interactive-p)
    (message (substitute-command-keys "Recording started... (Use \\[smf-record-stop] to finish)"))))

(defun smf-record-stop ()
  (interactive)
  (unless smf-record-process
    (error "not recording."))
  (setq smf-record-data nil)
  (interrupt-process smf-record-process)
  (while (not smf-record-data)
    (sit-for 0.1))
  (if (interactive-p)
      (message "Recording stopped.")
    smf-record-data))
    

(provide 'midi)
;;; midi.el ends here
