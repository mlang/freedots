package freedots.braille;

/** Identifies a sequence of Unicode braille characters.
 */
public interface BrailleSequence extends CharSequence {
  /** Gets a human readable description of the characters making up a sign.
   */
  String getDescription();

  /** Checks if a {@link freedots.braille.GuideDot} needs to be
   *  inserted between this and the next sign.
   */
  boolean needsGuideDot(BrailleSequence next);

  /** Gets the parent of this sign if it is part of a
   *  {@link freedots.braille.BrailleList}.
   * @return null if this sign has not been added to a parent yet
   */
  BrailleList getParent();

  /** Sets the {@link freedots.braille.BrailleList} which contains this sign.
   */
  void setParent(BrailleList parent);

  /** Gets the visual score object responsible for the creation of this sign.
   */
  Object getScoreObject();
}
