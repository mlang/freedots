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
package freedots.math;

/** Represents two to the power of a certain integer.
 */
public class PowerOfTwo extends AbstractFraction {
  private int power;
  protected void setPower(final int power) {
    this.power = power;
  }
  public PowerOfTwo(final int power) {
    setPower(power);
  }
  public int getPower() { return power; }

  public int numerator() { return (power < 0)? 1: pow2(power); }
  public int denominator() { return (power >= 0)? 1: pow2(-power); }

  public PowerOfTwo multiply(final PowerOfTwo other) {
    if (other == null) throw new NullPointerException();
    return new PowerOfTwo(power + other.power);
  }

  protected final static int pow2(final int power) {
    return (int)StrictMath.pow(2, power);
  }
}
