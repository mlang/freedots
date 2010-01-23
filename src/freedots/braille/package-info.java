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
/** The main goal is to get more fine-grained information about the meaning
 * of individual braille signs.  First of all, all braille signs have an
 * associated description string now.  Additionally, there is now a distinction
 * between "score objects" (those objects that relate to the visual
 * representation of a score) and individual braille signs that represent
 * them.  {@link freedots.braille.BrailleList#getSignAtIndex} can be used to
 * retrieve information about the braille at a character index, and
 * {@link freedots.braille.BrailleList#getScoreObjectAtIndex} can be used
 * to retrieve the smallest possible logical score object (like
 * {@link freedots.musicxml.Note}.
 * <p>
 * Interface {@link freedots.braille.BrailleSequence} is the heart of this
 * package.
 * To add further properties to braille signs (like colour for visual markup)
 * modify this interface and extend its implementors accordingly.
 */
package freedots.braille;
