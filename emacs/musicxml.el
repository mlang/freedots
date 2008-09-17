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

;; However, to achieve this we first need to define layer of MusicXML handling
;; functionality.

;;; TODO:

;; * Get rid of xml-sub-parser.

;;; Code:

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
      (let* ((node-name (list (match-string-no-properties 1)
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
		(let ((end (concat "</" (car node-name) "\\s-*>")))
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

(defun musicxml-node-name (node)
  (caar node))

(defun musicxml-children (node)
  "Return a list of all children of NODE with text content removed."
  (remove-if (lambda (elem) (stringp (car elem))) (cddr node)))


(defun musicxml-get-child (node name)
  "Return all NAME children of NODE."
  (remove-if (lambda (elem)
	       (or (stringp (car elem))
		   (not (string= (musicxml-node-name elem) name))))
	     (cddr node)))

(defun musicxml-get-first-child (node name)
  "Return the first child of NODE with name NAME."
  (let ((children (cddr node)))
    (catch 'found
      (while children
	(let ((child (car children)))
	  (if (and (not (stringp (car child)))
		   (string= (musicxml-node-name child) name))
	      (throw 'found child)
	    (setq children (cdr children))))))))

(defun musicxml-node-text (node)
  "Return the (first) text element in NODE."
  (let ((first-child (caddr node)))
    (when (and first-child (stringp (car first-child)))
      first-child)))

(defun musicxml-node-text-string (text-node)
  "Get the text of a TEXT-NODE as a string."
  (or (nth 0 (musicxml-node-text text-node)) ""))

(defun musicxml/work ()
  "XML node /*/work."
  (musicxml-get-first-child musicxml-root-node "work"))
(defun musicxml/work/work-number ()
  "XML node /*/work/work-number."
  (musicxml-get-first-child (musicxml/work) "work-number"))
(defun musicxml/work/work-title ()
  "XML node /*/work/work-title."
  (musicxml-get-first-child (musicxml/work) "work-title"))
(defun musicxml/part-list ()
  "XML node /*/part-list."
  (or (musicxml-get-first-child musicxml-root-node "part-list")
      (error "Required element `part-list' not present")))
(defun musicxml/part-list/score-part (id)
  "XML node /*/part-list/score-part[@id=ID]."
  (loop for node in (musicxml-children (musicxml/part-list))
	when (and (string= (musicxml-node-name node) "score-part")
		  (string= (xml-get-attribute node 'id) id))
	return node))
(defun musicxml/part-list/score-part/part-name (id)
  "XML node /*/part-list/score-part[@id=ID]/part-name."
  (musicxml-get-first-child (musicxml/part-list/score-part id) "part-name"))
(defun musicxml/part ()
  "XML nodes /*/part."
  (musicxml-get-child musicxml-root-node "part"))

(defun musicxml-note-p (node)
  (string= (musicxml-node-name node) "note"))
(defun musicxml-grace-note-p (node)
  (and (musicxml-note-p node) (musicxml-get-first-child node "grace")))

(defun musicxml-note-dots (node)
  (length (musicxml-get-child node "dot")))
(defun musicxml-rest-p (node)
  (and (musicxml-note-p node)
       (musicxml-get-first-child node "rest")))
(defun musicxml-pitched-p (node)
  (and (musicxml-note-p node)
       (musicxml-get-first-child node "pitch")))
(defun musicxml-note-midi-pitch (node)
  (and (musicxml-pitched-p node)
       (let ((pitch (musicxml-get-first-child node "pitch")))
	 (let ((step (cdr (assoc (downcase (musicxml-node-text-string
					    (musicxml-get-first-child
					     pitch "step")))
				 '(("c" . 0) ("d" . 2) ("e" . 4) ("f" . 5)
				   ("g" . 7) ("a" . 9) ("b" . 11)))))
	       (octave (string-to-number
			(musicxml-node-text-string
			 (musicxml-get-first-child pitch "octave"))))
	       (alter (string-to-number
		       (musicxml-node-text-string
			(musicxml-get-first-child pitch "alter")))))
	   (+ (* octave 12) step alter)))))

(defun musicxml-note-type (node)
  (and (musicxml-note-p node)
       (musicxml-node-text-string (musicxml-get-first-child node "type"))))

(defun musicxml-duration (node)
  (let ((duration (musicxml-get-first-child musicdata "duration")))
    (if duration
	(string-to-number (musicxml-node-text-string duration))
      (error "No duration: %S" node))))

(defun musicxml-as-smf ()
  (let ((ppqn (apply #'lcm
		     (loop for part in (musicxml/part)
			   append
			   (loop for measure in
				 (musicxml-get-child part "measure")
				 append
				 (loop for attributes in
				       (musicxml-get-child measure
							   "attributes")
				       append
				       (loop for divisions in
					     (musicxml-get-child attributes
								 "divisions")
					     collect
					     (string-to-number
					      (musicxml-node-text-string
					       divisions))))))))
	tracks)
    (dolist (part (musicxml/part) (append (list 1 ppqn) (nreverse tracks)))
      (let ((tick 0) (divisions-multiplier 1) events)
	(dolist (measure (musicxml-get-child part "measure"))
	  (dolist (musicdata (musicxml-children measure))
	    (cond
	     ((string= (musicxml-node-name musicdata) "attributes")
	      (dolist (divisions (musicxml-get-child musicdata "divisions"))
		(setq divisions-multiplier
		      (/ ppqn (string-to-number
			       (musicxml-node-text-string divisions))))))
	     ((string= (musicxml-node-name musicdata) "backup")
	      (setq tick (- tick (* (musicxml-duration musicdata) divisions-multiplier))))
	     ((or (string= (musicxml-node-name musicdata) "forward")
		  (musicxml-rest-p musicdata))
	      (setq tick (+ tick (* (musicxml-duration musicdata) divisions-multiplier))))
	     ((and (musicxml-pitched-p musicdata)
		   (not (musicxml-grace-note-p musicdata)))
	      (push (list tick
			  'Note
			  0
			  (musicxml-note-midi-pitch musicdata)
			  127
			  (musicxml-duration musicdata)
			  0)
		    events)
	      (setq tick (+ tick (* (musicxml-duration musicdata) divisions-multiplier)))))))
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
    map)
  "Keymap for `musicxml-mode'.")

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

;;;; Braille music

(defcustom braille-music-symbol-table
  (append
   (loop for (value . lower) in
	 '((1or16 . 36) (2or32 . 3) (4or64 . 6) (8or128 . 0))
	 append
	 (loop for (name . upper) in
	       '((c . 145) (d . 15) (e . 124)
		 (f . 1245)(g . 125)(a . 24) (b . 245))
	       collect
	       (cons (intern (concat (symbol-name name) (symbol-name value)))
		     (let ((unicode #x2800))
		       (labels ((add-decimal-dots (dots)
			        (while (> dots 0)
				  (setq unicode (logior unicode
							(ash 1
							     (1- (% dots 10))))
					dots (/ dots 10)))))
			 (add-decimal-dots upper)
			 (add-decimal-dots lower)
			 (format "%c" (decode-char 'ucs unicode)))))))
   '((r1or16 . "⠍") (r2or32 . "⠥") (r4or64 . "⠧") (r8or128 . "⠭")))
  "A table of braille music symbols."
  :group 'braille-music
  :type '(repeat (cons :tag "Entry" symbol string)))

(defun braille-music-char (symbol)
  (cdr (assq symbol braille-music-symbol-table)))

(defun braille-music-symbol (char)
  (car (rassq char braille-music-symbol-table)))

(defun braille-music-note-value-interpretations (notes time-signature)
  "Calculate all possible interpretations of braille music symbols in NOTES
given a measure duration defined by TIME-SIGNATURE.

Due to the inherent ambiguity of note values in braille music we need to
be able to check if there is only one correct interpretation of the
note values.  This function performs the job.
It returns a list of lists, ideally with just one element."
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
      (generate
       (mapcar (lambda (note)
		 (let ((dots (musicxml-note-dots note)))
		   (mapcar (lambda (str)
			     (let ((undotted-duration
				    (/ 1.0 (string-to-number str))))
			       (cons (- (* undotted-duration 2)
					(/ undotted-duration (expt 2 dots)))
				     (intern
				      (concat
				       (substring
					(symbol-name (car note)) 0 1)
				       str)))))
			   (split-string
			    (substring (symbol-name (car note)) 1)
			    "or"))))
	       notes)
       time))))

(defun braille-music-symbol-from-musicxml-note (note)
  (let ((types '(("whole" . "1or16") ("half" . "2or32")
		 ("quarter" . "4or64") ("eighth" . "8or128")
		 ("16th" . "1or16") ("32nd" . "2or32")
		 ("64th" . "4or64") ("128th" . "8or128")))
	(pitch (musicxml-get-first-child note "pitch")))
    (intern (concat (if (not pitch)
			"r"
		      (downcase (musicxml-node-text-string
				 (musicxml-get-first-child pitch "step"))))
		    (cdr (assoc (musicxml-note-type note) types))))))

(defun braille-music-from-musicdata (node)
  "Return a list of braille music symbols from NODE.

NODE can be
 either /score-partwise/part/measure
     or /score-timewise/measure/part."
  (let (symbols)
    (dolist (child (musicxml-children node) (nreverse symbols))
      (cond
       ((musicxml-note-p child)
	(push (cons (braille-music-symbol-from-musicxml-note child) child)
	      symbols))))))

(defun braille-music-insert-music-symbol (music)
  "Insert MUSIC (a cons cell) as braille music in the current buffer.
The `car' of MUSIC is the braille music symbol and `cdr' is the XML tag
mainly responsible for this symbol having been produced."
  (let ((symbol (car music)) (xml (cdr music))
	(begin (point)))
    (insert (braille-music-char symbol))
    (when (and (musicxml-note-p xml) (musicxml-note-dots xml))
      (dotimes (i (musicxml-note-dots xml))
	(insert ?⠄)))
    (put-text-property begin (point) 'xml-node xml)))

(defun braille-music-goto-musicxml ()
  "Pop to XML location which is responsible for the symbol at point."
  (interactive)
  (let ((node (get-text-property (point) 'xml-node)))
    (if node
	(musicxml-pop-to-tag node)
      (error "No XML node found"))))

(defvar musicxml-braille-music-mode-map
  (let ((map (make-sparse-keymap)))
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
	      (dolist (attributes (musicxml-get-child measure "attributes"))
		(dolist (node (musicxml-children (musicxml-get-first-child
						 attributes "time")))
		  (cond
		   ((string= (musicxml-node-name node) "beats")
		    (setf (car current-time-signature)
			  (string-to-number (musicxml-node-text-string node))))
		   ((string= (musicxml-node-name node) "beat-type")
		    (setf (cdr current-time-signature)
			  (string-to-number (musicxml-node-text-string node)))))))
	      (let* ((measure-number (xml-get-attribute measure 'number))
		     (braille-music (braille-music-from-musicdata measure))
		     (interpretations
		      (braille-music-note-value-interpretations
		       braille-music current-time-signature)))
		(when (/= (length interpretations) 1)
		  (if (= (length interpretations) 0)
		      (lwarn 'musicxml-braille-music :warning
			     "Measure %s has no possible interpretations"
			     measure-number)
		    (lwarn 'musicxml-braille-music :error
			   "Unresolved ambigious measure %s: %S"
			   measure-number interpretations)))

		(mapc #'braille-music-insert-music-symbol braille-music)

		(if (< (- (point) (line-beginning-position)) fill-column)
		    (insert " ")
		  (insert "\n")))))
	    (insert "\n")))
      (goto-char (point-min)))))

(define-key musicxml-minor-mode-map (kbd "C-c | b") 'musicxml-to-braille-music)

(provide 'musicxml)
;;; musicxml.el ends here
