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
package freedots.braille;

import java.awt.Color;

import freedots.music.AugmentedFraction;

public class ArtificialWholeRest extends BrailleList {
  public ArtificialWholeRest() {
    super();
    add(new Dot5());
    add(new RestSign(new AugmentedFraction(1, 1, 0)));
  }

  @Override public String getDescription() {
    return "Indicates that a whole measure is without any chord symbols";
  }

  public static class Dot5 extends Sign {
    Dot5() { super(braille(5)); }
    public String getDescription() {
      return "Signifies that the following sign has been added for "
             + "clarity but does not exist in the original print";
    }
	@Override
	public Color getSignColor() {
		return this.signColor;
	}
	@Override
	public void setSignColor() {
		this.signColor = Color.black;
	}
  }
}
