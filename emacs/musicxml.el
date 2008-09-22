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

;;; TODO:

;; * Get rid of xml-sub-parser.

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
  (let* ((start (point))
	 (string (progn (if (search-forward "<" nil t)
			    (forward-char -1)
			  (goto-char (point-max)))
			(buffer-substring-no-properties start (point))))
	 (end (point)))
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
     ((looking-at "<!DOCTYPE")
      (let ((dtd (xml-parse-dtd)))
	(skip-syntax-forward " ")
	(if xml-validating-parser
	    (cons dtd (musicxml-parse-tag nil))
	  (musicxml-parse-tag nil))))
     ;;  skip comments
     ((looking-at "<!--")
      (search-forward "-->")
      nil)
     ;;  end tag
     ((looking-at "</")
      '())
     ;;  opening tag
     ((looking-at "<\\([^/>[:space:]]+\\)")
      (goto-char (match-end 1))

      ;; Parse this node
      (let* ((node-name (list (intern (match-string-no-properties 1))
			      (copy-marker (match-beginning 0))
			      nil))
	     ;; Parse the attribute list.
	     (attrs (xml-parse-attlist))
	     children pos)

	(setq children (list attrs node-name))

	;; is this an empty element ?
	(if (looking-at "/>")
	    (progn
	      (forward-char 2)
	      (setf (nth 2 node-name) (copy-marker (point)))
	      (nreverse children))

	  ;; is this a valid start tag ?
	  (if (eq (char-after) ?>)
	      (progn
		(forward-char 1)
		;;  Now check that we have the right end-tag. Note that this
		;;  one might contain spaces after the tag name
		(let ((end (concat "</" (symbol-name (car node-name)) "\\s-*>")))
		  (while (not (looking-at end))
		    (cond
		     ((looking-at "</")
		      (error "XML: (Not Well-Formed) Invalid end tag (expecting %s) at pos %d"
			     node-name (point)))
		     ((= (char-after) ?<)
		      (let ((tag (musicxml-parse-tag nil)))
			(when tag
			  (push tag children))))
		     (t
		      (let ((expansion (musicxml-parse-string)))
			(setq children
			      (if (stringp expansion)
				  (if (stringp (car children))
				      ;; The two strings were separated by a comment.
				      (setq children (append (list (concat (car children) expansion))
							     (cdr children)))
				    (setq children (append (list expansion) children)))
				(setq children (append expansion children))))))))

		  (goto-char (match-end 0))
		  (setf (nth 2 node-name) (copy-marker (point)))
		  (nreverse children)))
	    ;;  This was an invalid start tag (Expected ">", but didn't see it.)
	    (error "XML: (Well-Formed) Couldn't parse tag: %s"
		   (buffer-substring-no-properties (- (point) 10) (+ (point) 1)))))))
     (t	;; (Not one of PI, CDATA, Comment, End tag, or Start tag)
      (unless xml-sub-parser		; Usually, we error out.
	(error "XML: (Well-Formed) Invalid character"))

      ;; However, if we're parsing incrementally, then we need to deal
      ;; with stray CDATA.
      (xml-parse-string)))))

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

(defun musicxml-children-unique-child-text (node child-name)
  (let (strings)
    (dolist (child (musicxml-children node) (nreverse strings))
      (let ((subelement (musicxml-get-first-child child child-name)))
	(when subelement
	  (add-to-list 'strings (musicxml-node-text-string subelement)))))))

(defun musicxml-staves (node &optional staves)
  (cond
   ((eq (musicxml-node-name node) 'measure)
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
	       
(defun musicxml-note-p (node)
  (eq (musicxml-node-name node) 'note))

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
  (let ((pitch (musicxml-get-first-child note 'pitch)))
    (when pitch
      (round
       (+ (* (1+ (string-to-number
		  (musicxml-node-text-string
		   (musicxml-get-first-child pitch 'octave)))) 12)
	  (cdr (assq (upcase (aref (musicxml-node-text-string
				    (musicxml-get-first-child
				     pitch 'step)) 0))
		     musicxml-step-to-chromatic))
	  (string-to-number
	   (musicxml-node-text-string
	    (musicxml-get-first-child pitch 'alter))))))))

(defvar musicxml-note-type-names
  '("long" "breve"
    "whole" "half" "quarter" "eighth" "16th" "32nd" "64th" "128th"
    "256th"))

(defun musicxml-note-type (node)
  (and (musicxml-note-p node)
       (musicxml-node-text-string (musicxml-get-first-child node 'type))))

(defun musicxml-duration (node)
  (let ((duration (musicxml-get-first-child musicdata 'duration)))
    (if duration
	(string-to-number (musicxml-node-text-string duration))
      (error "No duration: %S" node))))

(defun musicxml-as-smf (&optional score)
  (let ((ppqn (apply #'lcm
		     (loop for part in (musicxml/part score)
			   append
			   (loop for measure in
				 (musicxml-get-child part 'measure)
				 append
				 (loop for attributes in
				       (musicxml-get-child measure
							   'attributes)
				       append
				       (loop for divisions in
					     (musicxml-get-child attributes
								 'divisions)
					     collect
					     (string-to-number
					      (musicxml-node-text-string
					       divisions))))))))
	tracks)
    (dolist (part (musicxml/part score)
		  (append (list 1 ppqn) (nreverse tracks)))
      (let* ((tick 0) (divisions-multiplier 1)
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
	  (dolist (musicdata (musicxml-children measure))
	    (cond
	     ((eq (musicxml-node-name musicdata) 'attributes)
	      (dolist (divisions (musicxml-get-child musicdata 'divisions))
		(setq divisions-multiplier
		      (/ ppqn (string-to-number
			       (musicxml-node-text-string divisions))))))
	     ((eq (musicxml-node-name musicdata) 'direction)
	      (let ((sound (musicxml-get-first-child musicdata 'sound)))
		(when (and sound (xml-get-attribute-or-nil sound 'dynamics))
		  (setq velocity (string-to-number
				  (xml-get-attribute sound 'dynamics))))))
	     ((eq (musicxml-node-name musicdata) 'backup)
	      (setq tick (- tick (* (musicxml-duration musicdata)
				    divisions-multiplier))))
	     ((or (eq (musicxml-node-name musicdata) 'forward)
		  (musicxml-rest-p musicdata))
	      (setq tick (+ tick (* (musicxml-duration musicdata)
				    divisions-multiplier))))
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
	      (setq tick (+ tick (* (musicxml-duration musicdata)
				    divisions-multiplier)))))))
	(push (append (list "MTrk")
		      (sort (nreverse events) #'car-less-than-car))
	      tracks)))))

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

(defun braille-music-disambiguate (elements time-signature)
  (let ((time (* (car time-signature) (/ 1.0 (cdr time-signature))))
	results)
    (labels ((generate (lists sum)
	       (if (null lists)
		   nil
		 (let ((choices (car lists))
		       result)
		   (if (endp (cdr lists))
		       (loop for choice in choices
			     when (= (car choice) sum) collect (list choice))
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
			    (let ((dots (musicxml-note-dots (braille-music-element-get note :xml))))
			      (mapcar (lambda (denominator)
					(let ((undotted-duration (/ 1.0 denominator)))
					  (cons (- (* undotted-duration 2)
						   (/ undotted-duration (expt 2 dots)))
						note)))
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
		 (if (< (position (musicxml-note-type
				   (braille-music-element-get note :xml))
				  musicxml-note-type-names :test #'string=) 6)
		     'braille-music-larger-values
		   'braille-music-smaller-values))
		(braille-music-disambiguate elements time-signature)))
	  (lwarn 'musicxml-braille-music :warning
		 "Measure %s has no possible interpretations"
		 measure-number)
	  elements)))))

(defun braille-music-symbol-from-musicxml-note (note)
  (let ((types (butlast (nthcdr 2 musicxml-note-type-names)))
	(pitch (musicxml-get-first-child note 'pitch)))
    (braille-music-rhythmic-sign
     (expt 2 (position (musicxml-note-type note) types :test #'string=))
     (and pitch
	  (musicxml-node-text-string
	   (musicxml-get-first-child pitch 'step))))))

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
  (let (symbols)
    (dolist (child (musicxml-children node) (nreverse symbols))
      (cond
       ((musicxml-note-p child)
	(push (make-braille-music-element 'note
					  :rhythmic-sign (braille-music-symbol-from-musicxml-note child)
					  :head nil
					  :tail (if (musicxml-note-dots child)
						    (make-list (musicxml-note-dots child) 'braille-music-dot)
						  nil)
					  :xml child)
	      symbols))))))

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
    (put-text-property begin (point) 'xml-node (braille-music-element-get music :xml))))

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
      (setq musicxml-root-node root)

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
