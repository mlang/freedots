package freedots.braille;

public interface Sign extends CharSequence {
  String getDescription();
  boolean mightNeedAdditionalDot();
  Sign getParent();
  void setParent(Sign parent);
  Object getScoreObject();
}
