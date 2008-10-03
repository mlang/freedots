;;; musicxml.el --- MusicXML

;; Copyright (C) 2008  Mario Lang

;; Author: Mario Lang <mlang@delysid.org>
;; Keywords: xml

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
;; the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
;; Boston, MA 02110-1301, USA.

;;; Commentary:

;; Primary goal of this program is to transcribe MusicXML documents to
;; braille music notation.

;; However, to achieve this we first need to define a layer of
;; MusicXML handling functionality.

;;; Code:

(let ((dir (file-name-directory (or load-file-name buffer-file-name))))
  (add-to-list 'load-path dir 'append)
  (eval-after-load 'nxml-mode
    `(progn
       (require 'rng-loc)
       (add-to-list 'rng-schema-locating-files
		    ,(abbreviate-file-name
		      (expand-file-name "schemas.xml" (concat dir "rnc/"))))
       (add-hook 'nxml-mode-hook #'turn-on-musicxml-minor-mode t))))

(require 'midi)
(require 'xml)

;;;; XML Parsing

;; We borrow and extend a bit of code from xml.el.
;; The main goal is to keep markers for start and end positions of XML tags
;; and text in the XML sexp representation.

(defun musicxml-parse-string ()
  "Parse the next whatever.  Could be a string, or an element."
  (let* ((start (point)) end
	 (string (progn (if (search-forward "<" nil t)
			    (forward-char -1)
			  (goto-char (point-max)))
			(buffer-substring-no-properties
			 start (setq end (point))))))
    ;; Clean up the string.  As per XML specifications, the XML
    ;; processor should always pass the whole string to the
    ;; application.  But \r's should be replaced:
    ;; http://www.w3.org/TR/2000/REC-xml-20001006#sec-line-ends
    (let ((pos 0))
      (while (string-match "\r\n?" string pos)
	(setq string (replace-match "\n" t t string))
	(setq pos (1+ (match-beginning 0)))))
    (list (list (xml-substitute-special string)
		(copy-marker start) (copy-marker end)))))

(defun musicxml-parse-tag (&optional parse-dtd)
  "Parse the tag at point.
If PARSE-DTD is non-nil, the DTD of the document, if any, is parsed and
returned as the first element in the list.
Returns one of:
 - a list : the matching node
 - nil    : the point is not looking at a tag.
 - a pair : the first element is the DTD, the second is the node."
  (let ((xml-validating-parser (or parse-dtd xml-validating-parser)))
    (cond
     ;; Processing instructions (like the <?xml version="1.0"?> tag at the
     ;; beginning of a document).
     ((looking-at "<\\?")
      (search-forward "?>")
      (skip-syntax-forward " ")
      (musicxml-parse-tag parse-dtd))
     ;;  Character data (CDATA) sections, in which no tag should be interpreted
     ((looking-at "<!\\[CDATA\\[")
      (let ((pos (match-end 0)))
	(unless (search-forward "]]>" nil t)
	  (error "XML: (Not Well Formed) CDATA section does not end anywhere in the document"))
	(concat
	 (buffer-substring-no-properties pos (match-beginning 0))
	 (xml-parse-string))))
     ;;  DTD for the document
     ((and parse-dtd (looking-at "<!DOCTYPE"))
      (let ((dtd (xml-parse-dtd)))
	(skip-syntax-forward " ")
	(if xml-validating-parser
	    (cons dtd (musicxml-parse-tag nil))
	  (musicxml-parse-tag nil))))
     ;;  skip comments
     ((looking-at "<!--")
      (search-forward "-->")
      nil)
     ;;  opening tag
     ((looking-at "<\\([^/>[:space:]]+\\)")
      (goto-char (match-end 1))

      ;; Parse this node
      (let ((node-name (list (intern (match-string-no-properties 1))
			     (copy-marker (match-beginning 0))
			     nil))
	    ;; Parse the attribute list.
	    (attrs (xml-parse-attlist))
	    children pos)
	;; is this an empty element ?
	(if (looking-at "/>")
	    (progn
	      (forward-char 2)
	      (setf (nth 2 node-name) (copy-marker (point)))
	      (list node-name attrs))
	  (setq children (list attrs node-name))
	  ;; is this a valid start tag ?
	  (if (eq (char-after) ?>)
	      (progn
		(forward-char 1)
		(skip-syntax-forward " ")
		;;  Now check that we have the right end-tag. Note that this
		;;  one might contain spaces after the tag name
		(let ((end (concat "</" (symbol-name (car node-name)) "\\s-*>")))
		  (while (not (looking-at end))
		    (if (= (char-after) ?<)
			(if (= (char-after (1+ (point))) ?/)
			    (error "XML: (Not Well-Formed) Invalid end tag (expecting %s) at pos %d"
				   (car node-name) (point))
			  (let ((tag (musicxml-parse-tag nil)))
			    (when tag
			      (push tag children))
			    (skip-syntax-forward " ")))
		      (let ((expansion (musicxml-parse-string)))
			(when expansion
			  (setq children
				(if (stringp expansion)
				    (if (stringp (car children))
					;; The two strings were separated by a comment.
					(setq children (append (list (concat (car children) expansion))
							       (cdr children)))
				      (setq children
					    (append (list expansion)
						    children)))
				  (setq children (append expansion
							 children))))))))

		  (goto-char (match-end 0))
		  (setf (nth 2 node-name) (copy-marker (point)))
		  (nreverse children)))
	    ;;  This was an invalid start tag (Expected ">", but didn't see it.)
	    (error "XML: (Well-Formed) Couldn't parse tag: %s"
		   (buffer-substring-no-properties (- (point) 10) (+ (point) 1)))))))
     (t	;; (Not one of PI, CDATA, Comment or Start tag)
      (error "XML: (Well-Formed) Invalid character")))))

(defun musicxml-parse-region (beg end &optional parse-dtd)
  "Parse the region from BEG to END in the current buffer.
Returns the XML list for the region, or raises an error if the region
is not well-formed XML.
If PARSE-DTD is non-nil, the DTD is parsed rather than skipped,
and returned as the first element of the list."
  ;; Use fixed syntax table to ensure regexp char classes and syntax
  ;; specs DTRT.
  (with-syntax-table (standard-syntax-table)
    (let ((case-fold-search nil)	; XML is case-sensitive.
 	  xml result dtd)
      (save-excursion
 	(save-restriction
 	  (narrow-to-region beg end)
	  (goto-char (point-min))
	  (while (not (eobp))
	    (if (search-forward "<" nil t)
		(progn
		  (forward-char -1)
		  (setq result (musicxml-parse-tag parse-dtd))
		  (if (and xml result (not xml-sub-parser))
		      ;;  translation of rule [1] of XML specifications
		      (error "XML: (Not Well-Formed) Only one root tag allowed")
		    (cond
		     ((null result))
		     ((and (listp (car result))
			   parse-dtd)
		      (setq dtd (car result))
		      (if (cdr result)	; possible leading comment
			  (add-to-list 'xml (cdr result))))
		     (t
		      (add-to-list 'xml result)))))
	      (goto-char (point-max))))
	  (if parse-dtd
	      (cons dtd (nreverse xml))
	    (nreverse xml)))))))

(defun musicxml-reparse-tag (tag)
  (with-current-buffer (marker-buffer (nth 1 (car tag)))
    (save-restriction
      (widen)
      (narrow-to-region (nth 1 (car tag)) (nth 2 (car tag)))
      (goto-char (point-min))
      (let ((new (musicxml-parse-tag)))
	(setcar tag (car new))
	(setcdr tag (cdr new))))))

(defun musicxml-pop-to-tag (tag)
  "Jump to TAG source location."
  (pop-to-buffer (marker-buffer (nth 1 (car tag))))
  (goto-char (nth 1 (car tag))))

(defsubst musicxml-node-name (node)
  (caar node))

(defun musicxml-children (node)
  "Return a list of all children of NODE with text content removed."
  (remove-if (lambda (elem) (stringp (car elem))) (cddr node)))

(defun musicxml-get-child (node name)
  "Return all NAME children of NODE."
  (remove-if (lambda (elem)
	       (or (stringp (car elem))
		   (not (eq (musicxml-node-name elem) name))))
	     (cddr node)))

(defun musicxml-get-first-child (node name)
  "Return the first child of NODE with name NAME."
  (let ((children (cddr node)))
    (catch 'found
      (while children
	(let ((child (car children)))
	  (if (and (not (stringp (car child)))
		   (eq (musicxml-node-name child) name))
	      (throw 'found child)
	    (setq children (cdr children))))))))

(defun musicxml-node-text (node)
  "Return the (first) text element in NODE."
  (let ((first-child (caddr node)))
    (and first-child (stringp (car first-child))
	 first-child)))

(defun musicxml-node-text-string (text-node)
  "Get the text of a TEXT-NODE as a string."
  (or (nth 0 (musicxml-node-text text-node)) ""))

(defun musicxml/work (&optional score)
  "XML node /*/work."
  (musicxml-get-first-child (or score musicxml-root-node) 'work))
(defun musicxml/work/work-number (&optional score)
  "XML node /*/work/work-number."
  (musicxml-get-first-child (musicxml/work score) 'work-number))
(defun musicxml/work/work-title (&optional score)
  "XML node /*/work/work-title."
  (musicxml-get-first-child (musicxml/work score) 'work-title))
(defun musicxml/part-list (&optional score)
  "XML node /*/part-list."
  (or (musicxml-get-first-child (or score musicxml-root-node) 'part-list)
      (error "Required element `part-list' not present")))
(defun musicxml/part-list/score-part (id &optional score)
  "XML node /*/part-list/score-part[@id=ID]."
  (loop for node in (musicxml-children (musicxml/part-list score))
	when (and (eq (musicxml-node-name node) 'score-part)
		  (string= (xml-get-attribute node 'id) id))
	return node))
(defun musicxml/part-list/score-part/part-name (id &optional score)
  "XML node /*/part-list/score-part[@id=ID]/part-name."
  (musicxml-get-first-child (musicxml/part-list/score-part id score)
			    'part-name))
(defun musicxml/part (&optional score)
  "XML nodes /*/part."
  (musicxml-get-child (or score musicxml-root-node) 'part))

(defun musicxml-part-ids (&optional score)
  (mapcar (lambda (part) (xml-get-attribute part 'id)) (musicxml/part score)))

(defun musicxml-goto-part (id)
  (interactive (completing-read "Part id: " (musicxml-part-ids)))
  (let ((parts (musicxml/part)))
    (while parts
      (if (string= id (xml-get-attribute (car parts) 'id))
	  (progn
	    (musicxml-goto-tag (car parts))
	    (setq parts nil))
	(setq parts (cdr parts))))))

(defmacro musicxml-bind (children expr &rest body)
  (declare (indent 2))
  (let ((node (gensym "xml-node-")))
    `(let* ((,node ,expr)
	    ,@(mapcar (lambda (child)
			`(,child (musicxml-get-first-child ,node ',child)))
		      children))
       ,@body)))

(defun musicxml-children-unique-child-text (node child-name)
  (let (strings)
    (dolist (child (musicxml-children node) (nreverse strings))
      (let ((subelement (musicxml-get-first-child child child-name)))
	(when subelement
	  (add-to-list 'strings (musicxml-node-text-string subelement)))))))

(defun musicxml-staves (node &optional staves)
  (cond
   ((musicxml-measure-p node)
    (let ((staves-node (catch 'node
			 (dolist (attributes
				  (musicxml-get-child node 'attributes))
			   (let ((staves-node (musicxml-get-first-child
					       attributes 'staves)))
			     (when staves-node
			       (throw 'node staves-node)))))))
      (or (and staves-node (string-to-number
			    (musicxml-node-text-string staves-node)))
	  (or staves 1))))
   (t (error "`musicxml-staves' called on non-measure node"))))
	       
(defmacro define-musicxml-predicates (names)
  (cons 'progn
	(mapcar
	 (lambda (name)
	   `(defun ,(intern (concat "musicxml-" (symbol-name name) "-p")) (node)
	      (eq (musicxml-node-name node) ',name))) names)))

(define-musicxml-predicates (attributes direction note backup forward))
(define-musicxml-predicates (measure))

(defun musicxml-grace-note-p (node)
  (and (musicxml-note-p node) (musicxml-get-first-child node 'grace)))

(defun musicxml-note-dots (node)
  (length (musicxml-get-child node 'dot)))

(defun musicxml-rest-p (node)
  (and (musicxml-note-p node)
       (musicxml-get-first-child node 'rest)))

(defun musicxml-pitched-p (node)
  (and (musicxml-note-p node)
       (musicxml-get-first-child node 'pitch)))

(defconst musicxml-step-to-chromatic
  '((?C . 0) (?D . 2) (?E . 4) (?F . 5) (?G . 7) (?A . 9) (?B . 11))
  "Maps step names to chromatic step counts.")

(defun musicxml-note-midi-pitch (note)
  (check-type note musicxml-note)
  (let ((pitch (musicxml-get-first-child note 'pitch)))
    (when pitch
      (musicxml-bind (step octave alter) pitch
	(round
	 (+ (* (1+ (string-to-number (musicxml-node-text-string octave))) 12)
	    (cdr (assq (upcase (aref (musicxml-node-text-string step) 0))
		       musicxml-step-to-chromatic))
	    (string-to-number (musicxml-node-text-string alter))))))))

(defvar musicxml-note-type-names
  '("long" "breve"
    "whole" "half" "quarter" "eighth" "16th" "32nd" "64th" "128th"
    "256th"))

(defun musicxml-note-type (note)
  "Return the note value type (a integer)."
  (check-type note musicxml-note)
  (musicxml-bind (type) note
    (when type
      (or (position (musicxml-node-text-string type) musicxml-note-type-names
		    :test #'string=)
	  (error "Invalid note type %s" (musicxml-node-text-string type))))))

(defvar musicxml-divisions-multiplier nil
  "XML duration tag values are multiplied by this value for normalisation.
MusicXML allows for changing the divisions value at any arbitrary point.
To allow for normalisation of all the duration values in a score, this
variable holds the current value durations should be multiplied with.")
(make-variable-buffer-local 'musicxml-divisions-multiplier)

(defun musicxml-duration (node)
  (check-type node (or musicxml-note musicxml-backup musicxml-forward))
  (musicxml-bind (duration) node
    (if duration
	(* (string-to-number (musicxml-node-text-string duration))
	   musicxml-divisions-multiplier)
      (error "No duration: %S" node))))

(defun musicxml-infer-symbolic-duration (duration divisions)
  "Infer a ratio and the amount of augmentation dots from DURATION.
DURATION is the number of DIVISIONS per quarter note.
Return a cons of the form ((NUMERATOR . DENOMINATOR) . DOTS)."
  (check-type duration (integer 1))
  (check-type divisions (integer 1))
  (let* ((numerator duration) (denominator (* 4 divisions)) (dots 0)
	 (gcd (gcd numerator denominator)))
    (setq numerator (/ numerator gcd) denominator (/ denominator gcd))
    (when (memq denominator '(2 4 8 16 32 64 128 256))
      (loop for dot from 10 downto 1
	    when (= numerator (1- (expt 2 (1+ dot))))
	    do (setq numerator (1- numerator) dots (1+ dots)
		     gcd (gcd numerator denominator)
		     numerator (/ numerator gcd)
		     denominator (/ denominator gcd))))
    (when (= denominator 1)
      (case numerator
	(3 (setq numerator 2 dots (1+ dots)))
	(6 (setq numerator 4 dots (1+ dots)))
	(7 (setq numerator 4 dots (+ dots 2)))))
    (cons (cons numerator denominator) dots)))

(defconst musicxml-smf-type 1
  "Standard MIDI file type for MusicXML MIDI export.")

(defun musicxml-lcm-divisions (&optional score)
  (apply #'lcm
	 (loop for part in (musicxml/part score)
	       append
	       (loop for measure in (musicxml-get-child part 'measure)
		     append
		     (loop for attributes
			   in (musicxml-get-child measure 'attributes)
			   append
			   (loop for divisions in
				 (musicxml-get-child attributes 'divisions)
				 collect
				 (string-to-number
				  (musicxml-node-text-string divisions))))))))

(defun musicxml-as-smf (&optional score)
  (let ((ppqn (musicxml-lcm-divisions score))
	tracks)
    (dolist (part (musicxml/part score)
		  (append (list musicxml-smf-type ppqn) (nreverse tracks)))
      (setq musicxml-divisions-multiplier 1)
      (let* ((tick 0)
	     (score-part (musicxml/part-list/score-part
			  (xml-get-attribute part 'id) score))
	     (midi-instrument (musicxml-get-first-child
			       score-part 'midi-instrument))
	     (midi-channel (musicxml-get-first-child
			    midi-instrument 'midi-channel))
	     (midi-program (musicxml-get-first-child
			    midi-instrument 'midi-program))
	     (channel (or (and midi-channel (1- (string-to-number
						 (musicxml-node-text-string
						  midi-channel))))
			  0))
	     (program (or (and midi-program (string-to-number
					     (musicxml-node-text-string
					      midi-program)))
			  1))
	     (velocity 64)
	     events)
	(push (list tick
		    'TrackName (musicxml-node-text-string
				(musicxml-get-first-child
				 score-part 'part-name)))
	      events)
	(push (list tick 'PC channel program) events)
	(dolist (measure (musicxml-get-child part 'measure))
	  (let ((next-time-change 0))
	    (dolist (musicdata (musicxml-children measure))
	      (unless (and (musicxml-note-p musicdata)
			   (musicxml-bind (rest) musicdata rest))
		(setq tick (+ tick next-time-change)
		      next-time-change 0))
	      (cond
	       ((musicxml-attributes-p musicdata)
		(dolist (divisions (musicxml-get-child musicdata 'divisions))
		  (setq musicxml-divisions-multiplier
			(/ ppqn (string-to-number
				 (musicxml-node-text-string divisions))))))
	       ((musicxml-direction-p musicdata)
		(musicxml-bind (sound) musicdata
		  (when sound
		    (let ((dynamics (xml-get-attribute-or-nil sound 'dynamics)))
		      (when dynamics
			(setq velocity (string-to-number dynamics)))))))
	       ((musicxml-backup-p musicdata)
		(setq next-time-change (- (musicxml-duration musicdata))))
	       ((or (musicxml-forward-p musicdata)
		    (musicxml-rest-p musicdata))
		(setq next-time-change (musicxml-duration musicdata)))
	       ((and (musicxml-pitched-p musicdata)
		     (not (musicxml-grace-note-p musicdata)))
		(push (list tick
			    'Note
			    channel
			    (musicxml-note-midi-pitch musicdata)
			    velocity
			    (musicxml-duration musicdata)
			    0)
		      events)
		(unless (musicxml-bind (chord) musicdata chord)
		  (setq next-time-change (musicxml-duration musicdata))))))))
	(push (nconc (list "MTrk")
		     (sort (nreverse events) #'car-less-than-car)) tracks)))))

(defvar musicxml-player-process nil)

(defun musicxml-playing-p ()
  (and musicxml-player-process
       (eq (process-status musicxml-player-process) 'run)))

(defun musicxml-stop-playback ()
  (interactive)
  (and (musicxml-playing-p) (kill-process musicxml-player-process)))

(defun musicxml-play-score ()
  "Play the complete score."
  (interactive)
  (musicxml-stop-playback)
  (setq musicxml-player-process (smf-play (musicxml-as-smf))))

;;;; MusicXML minor mode

(defvar musicxml-dtd nil
  "The DTD information for the XML document in this buffer.")
(make-variable-buffer-local 'musicxml-dtd)

(defvar musicxml-root-node nil
  "The root XML node of the MusicXML document in this buffer.
Functions like `musicxml/work', `musicxml/part-list', `musicxml/part' and
others use the value of this variable to directly access parsed XML.")
(make-variable-buffer-local 'musicxml-root-node)

(defvar musicxml-minor-mode-map
  (let ((map (make-sparse-keymap)))
    (define-key map (kbd "C-c | C-p") 'musicxml-play-score)
    (define-key map (kbd "C-c | C-s") 'musicxml-stop-playback)
    map)
  "Keymap for `musicxml-minor-mode'.")

(easy-menu-define musicxml-menu musicxml-minor-mode-map
  "Menu for MusicXML documents."
  '("MusicXML"
    ["Goto part..." musicxml-goto-part]
    "---"
    ["Play score" musicxml-play-score]
    ["Stop playback" musicxml-stop-playback :active (musicxml-playing-p)]))

(define-minor-mode musicxml-minor-mode
  "If enabled, special functions for MusicXML handling can be used."
  :lighter " Music"
  (if musicxml-minor-mode
      (let ((xml (musicxml-parse-region (point-min) (point-max) t)))
	(if xml
	    (setq musicxml-dtd (nth 0 xml)
		  musicxml-root-node (nth 1 xml))
	  (error "Unable to parse XML data"))
	)
    (setq musicxml-dtd nil
	  musicxml-root-node nil)))

(defconst musicxml-root-node-names '("score-partwise" "score-timewise"))

(defun turn-on-musicxml-minor-mode ()
  "Turn on `musicxml-minor-mode' if the current buffer is a MusicXML document."
  (case major-mode
    (nxml-mode (let ((root-node-name (nth 2 (rng-document-element))))
		 (when (member root-node-name musicxml-root-node-names)
		   (musicxml-minor-mode 1))))
    (t (when (save-excursion
	       (goto-char (point-min))
	       (re-search-forward
		(concat "<" (regexp-opt musicxml-root-node-names)) nil t))
	 (musicxml-minor-mode 1)))))

;;;; Braille music

(defconst braille-music-step-dot-patterns
  '(("c" . 145) ("d" . 15) ("e" . 124) ("f" . 1245) ("g" . 125) ("a" . 24)
    ("b" . 245)))

(defconst braille-music-value-dot-patterns
  '(("whole-or-16th"   . 36)
    ("half-or-32nd"    . 3)
    ("quarter-or-64th" . 6)
    ("eighth-or-128th" . 0)))

(defconst braille-music-rhythmic-signs
  (append
   (loop for (step . upper-dots) in braille-music-step-dot-patterns
	 collect
	 (cons
	  (upcase step)
	  (loop for exponent from 0
		for (value . lower-dots) in braille-music-value-dot-patterns
		for denominators = (list (expt 2 exponent)
					 (expt 2 (+ 4 exponent)))
		collect
		(cons
		 denominators
		 (let ((symbol (intern (concat value "-" step)))
		       (unicode #x2800))
		   (put symbol 'denominators denominators)
		   (put symbol 'step step)
		   (set symbol (dolist (dots (list upper-dots lower-dots)
					     (char-to-string
					      (decode-char 'ucs unicode)))
				 (while (> dots 0)
				   (setq unicode (logior unicode
							 (ash 1 (1-
								 (% dots 10))))
					 dots (/ dots 10)))))
		   symbol)))))
   (list (cons nil ; rests
	       (loop for sign in '("⠍" "⠥" "⠧" "⠭")
		     for exponent from 0
		     for denominators = (list (expt 2 exponent)
					      (expt 2 (+ 4 exponent)))
		     collect
		     (cons
		      denominators
		      (let ((symbol
			     (intern
			      (concat
			       (car (nth exponent
					 braille-music-value-dot-patterns))
			       "-rest"))))
			(put symbol 'denominators denominators)
			(set symbol sign)
			symbol)))))))

(defun braille-music-rhythmic-sign (denominator &optional step)
  (cdr (assoc* denominator (cdr (assoc-string step braille-music-rhythmic-signs))
	       :test #'memq)))

(defun braille-music-rhythmic-sign-p (sign)
  (consp (get sign 'denominators)))

(defun braille-music-rhythmic-sign-denominators (sign)
  (get sign 'denominators))

(defun braille-music-char (symbol)
  (symbol-value symbol))

(defun braille-music-mismatch* (seqs)
  "Compare sequences in SEQS, return index of first mismatching element.
Return nil if the sequences match.  If one sequence is a prefix of another,
the return value indicates the end of the shortest sequence.
`equal' is used for comparison."
  (reduce (lambda (x y) (if x (if y (min x y) x) y))
	  (mapcar (lambda (s) (mismatch (car seqs) s :test #'equal))
		  (cdr seqs))))

(defun braille-music-dotted-duration (denominator dots)
  "The float representation of note value 1/DENOMINATOR with DOTS."
  (check-type denominator (integer 1))
  (check-type dots (integer 0))
  (let ((dotted-denominator (* denominator (expt 2 dots))))
    (let ((numerator (- (* 2 dotted-denominator) denominator)))
      (setq denominator (* denominator dotted-denominator))
      (let ((gcd (gcd numerator denominator)))
	(/ (float (/ numerator gcd)) (/ denominator gcd))))))

(defun braille-music-disambiguate (elements time-signature)
  "Due to the inherent ambiguity of rhythmic signs in braille music we need
to check if there are several possible interpretations for a list of
braille-music-elements.

If we find an ambiguity, we currently \"brute force\" a resolution
by using explicit value-distinction symbols (`braille-music-larger-values'
and `braille-music-smaller-values')."
  (let ((time (* (car time-signature) (/ 1.0 (cdr time-signature))))
	results)
    (labels ((generate (lists sum)
	       (if (null lists)
		   nil
		 (let ((choices (car lists)) result)
		   (if (endp (cdr lists))
		       (dolist (choice choices result)
			 (when (= (car choice) sum)
			   (push (list choice) result)))
		     (dolist (choice choices result)
		       (let ((time (car choice)))
			 (when (<= time sum)
			   (mapc (lambda (elt)
				   (push (cons choice elt) result))
				 (generate (cdr lists) (- sum time)))))))))))
      (let ((interpretations
	     (generate
	      (mapcar (lambda (note)
			(if (eq (braille-music-element-type note) 'note)
			    (let ((dots (musicxml-note-dots
					 (braille-music-element-get
					  note :xml))))
			      (mapcar (lambda (denominator)
					(cons (braille-music-dotted-duration
					       denominator dots)
					      note))
				      (let ((denominators
					     (braille-music-rhythmic-sign-denominators
					      (braille-music-element-get
					       note :rhythmic-sign)))
					    (head (braille-music-element-get
						   note :head)))
					(cond
					 ((memq 'braille-music-larger-values
						head)
					  (list (apply #'min denominators)))
					 ((memq 'braille-music-smaller-values
						head)
					  (list (apply #'max denominators)))
					 (t denominators)))))
			  (cons 0 note)))
		      elements)
	      time)))
	(if (not (null interpretations))
	    (if (= (length interpretations) 1)
		(mapcar #'cdr (car interpretations))
	      (let ((note (nth (braille-music-mismatch* interpretations)
			       elements)))
		(braille-music-element-push
		 note :head
		 (if (< (musicxml-note-type
			 (braille-music-element-get note :xml)) 6)
		     'braille-music-larger-values
		   'braille-music-smaller-values))
		(braille-music-disambiguate elements time-signature)))
	  (lwarn 'musicxml-braille-music :warning
		 "Measure %s has no possible interpretations"
		 measure-number)
	  elements)))))

(defun braille-music-symbol-from-musicxml-note (note)
  (check-type note musicxml-note)
  (let ((log (musicxml-note-type note))
	(pitch (musicxml-get-first-child note 'pitch)))
    (braille-music-rhythmic-sign
     ; denominator
     (or (and log (expt 2 (- log 2)))
	 (cdar (musicxml-infer-symbolic-duration (musicxml-duration note)
						 musicxml-lcm-divisions)))
     ; pitch step or rest
     (when pitch
       (musicxml-bind (step) pitch (musicxml-node-text-string step))))))

(defun make-braille-music-element (type &rest plist)
  (cons type plist))

(defun braille-music-element-type (element)
  (car element))
(defun braille-music-element-get (element property)
  (plist-get (cdr element) property))

(defun braille-music-element-put (element property value)
  (setcdr element (plist-put (cdr element) property value)))

(defun braille-music-element-push (element property value)
  (braille-music-element-put
   element property (cons value (braille-music-element-get element property))))

(set 'braille-music-value-distinction "⠣⠂")
(set 'braille-music-larger-values (concat "⠘" braille-music-value-distinction))
(set 'braille-music-smaller-values (concat "⠠" braille-music-value-distinction))
(set 'braille-music-dot "⠄")

(defun braille-music-from-musicdata (node &optional staff voice)
  "Return a list of braille music symbols from NODE.

NODE can be
 either /score-partwise/part/measure
     or /score-timewise/measure/part."
  (let (elements)
    (dolist (child (musicxml-children node) (nreverse elements))
      (cond
       ((musicxml-attributes-p child)
	(dolist (node (musicxml-get-child child 'divisions))
	  (setq musicxml-divisions-multiplier
		(/ musicxml-lcm-divisions
		   (string-to-number (musicxml-node-text-string node))))))
       ((musicxml-note-p child)
	(push (make-braille-music-element 'note
					  :rhythmic-sign (braille-music-symbol-from-musicxml-note child)
					  :head nil
					  :tail (if (musicxml-note-dots child)
						    (make-list (musicxml-note-dots child) 'braille-music-dot)
						  nil)
					  :xml child)
	      elements))))))

(defun braille-music-insert-music-symbol (music)
  "Insert MUSIC (a braille-music-element) as braille music in the current
buffer."
  (let ((begin (point)))
    (mapcar (lambda (symbol) (insert (braille-music-char symbol)))
	    (braille-music-element-get music :head))
    (insert (braille-music-char
	     (braille-music-element-get music :rhythmic-sign)))
    (mapcar (lambda (symbol) (insert (braille-music-char symbol)))
	    (braille-music-element-get music :tail))
    (put-text-property begin (point) 'xml-node (braille-music-element-get
						music :xml))))

(defun braille-music-goto-musicxml ()
  "Pop to XML location which is responsible for the symbol at point."
  (interactive)
  (let ((node (get-text-property (point) 'xml-node)))
    (if node
	(musicxml-pop-to-tag node)
      (error "No XML node found"))))

(defvar musicxml-braille-music-mode-map
  (let ((map (make-sparse-keymap)))
    (define-key map (kbd "C-c C-p") 'musicxml-play-score)
    (define-key map (kbd "C-c C-x") 'braille-music-goto-musicxml)
    map)
  "Keymap for `musicxml-braille-music-mode'.")

(define-derived-mode musicxml-braille-music-mode fundamental-mode
  "MusicBraille"
  "Major mode for music braille produced from MusicXML document content."
  )

(defun musicxml-to-braille-music ()
  "Convert the current MusicXML document to braille music."
  (interactive)
  (if (not musicxml-minor-mode)
      (error "Current buffer not in musicxml-minor-mode")
    (let ((root musicxml-root-node))
      (pop-to-buffer (generate-new-buffer (format "*Braille music for %s*"
						  (buffer-name))))
      (musicxml-braille-music-mode)
      (setq musicxml-root-node root
	    musicxml-lcm-divisions (musicxml-lcm-divisions root))

      (insert (musicxml-node-text-string (musicxml/work/work-title)) "\n")

      (dolist (part (musicxml/part))
	(let ((part-id (xml-get-attribute part 'id)))
	  (insert (musicxml-node-text-string
		   (musicxml/part-list/score-part/part-name part-id)) ":\n")
	  (let ((current-time-signature (cons 4 4))
		(alterations (list 0 0 0 0 0 0 0)))
	    (insert "  ")
	    (dolist (measure (musicxml-children part))
	      ;; Check for a time signature
	      (dolist (attributes (musicxml-get-child measure 'attributes))
		(dolist (node (musicxml-children (musicxml-get-first-child
						 attributes 'time)))
		  (cond
		   ((eq (musicxml-node-name node) 'beats)
		    (setf (car current-time-signature)
			  (string-to-number (musicxml-node-text-string node))))
		   ((eq (musicxml-node-name node) 'beat-type)
		    (setf (cdr current-time-signature)
			  (string-to-number (musicxml-node-text-string node)))))))
	      (let* ((measure-number (xml-get-attribute measure 'number))
		     (braille-music (braille-music-disambiguate
				     (braille-music-from-musicdata measure)
				     current-time-signature)))
		(mapc #'braille-music-insert-music-symbol braille-music)

		(if (< (- (point) (line-beginning-position)) fill-column)
		    (insert " ")
		  (insert "\n")))))
	    (insert "\n")))
      (goto-char (point-min)))))

(define-key musicxml-minor-mode-map (kbd "C-c | b") 'musicxml-to-braille-music)

(provide 'musicxml)
;;; musicxml.el ends here
