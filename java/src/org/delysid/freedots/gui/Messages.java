package org.delysid.freedots.gui;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;

public class Messages {
  private static final String BUNDLE_NAME = "org.delysid.freedots.gui.messages"; //$NON-NLS-1$

  private static final
  ResourceBundle RESOURCE_BUNDLE = PropertyResourceBundle
    .getBundle(BUNDLE_NAME, Locale.getDefault());

  private Messages() { }

  public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
