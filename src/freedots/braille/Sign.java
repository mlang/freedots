package freedots.braille;

public interface Sign extends CharSequence {
  /** Gets a human readable description of the characters making up a sign.
   */
  String getDescription();

  /** Indicates that a {@link freedots.braille.GuideDot} might need to be
   *  inserted between this and the next sign.
   */
  boolean mightNeedAdditionalDot();

  /** Gets the parent of this sign if it is part of a
   *  {@link freedots.braille.Compound}.
   * @return null if this sign has not been added to a parent yet
   */
  Compound getParent();

  /** Sets the {@link freedots.braille.Compound} which contains this sign.
   */
  void setParent(Compound parent);

  /** Gets the visual score object responsible for the creation of this sign.
   */
  Object getScoreObject();
}
