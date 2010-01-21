/** This is work in progress, an attempt to rewrite {@link freedots.Braille}
 *  and {@link freedots.transcription.BrailleList}.
 * <p>
 * The main goal is to get more fine-grained information about the meaning
 * of individual braille signs.  First of all, all braille signs have an
 * associated description string now.  Additionally, there is now a distinction
 * between "score objects" (those objects that relate to the visual
 * representation of a score) and individual braille signs that represent
 * them.  {@link freedots.braille.Compound#getSignAtIndex} can be used to
 * retrieve information about the braille at a character index, and
 * {@link freedots.braille.Compound#getScoreObjectAtIndex} can be used
 * to retrieve the smallest possible logical score object (like
 * {@link freedots.musicxml.Note}.
 * <p>
 * Interface {@link freedots.braille.Sign} is the heart of this package.
 * To add further properties to braille signs (like colour for visual markup)
 * modify this interface and extend its implementors accordingly.
 * <p>
 * TODO: This package is currently unused, it waits for package
 * {@link freedots.transcription} to be converted to use it.
 */
package freedots.braille;
