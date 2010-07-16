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

package freedots.logging;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public final class Logger extends java.util.logging.Logger {
  private static ArrayBlockingQueue<LogRecord> logQueue;
  private static final int LOG_QUEUE_SIZE = 1000;

  private Logger(final String name) { super(name, null); }

  /** Report (and possibly create) the logger related to the provided class.
   *
   * @param clazz the related class
   * @return the logger
   */
  public static Logger getLogger(Class clazz) {
    return getLogger(clazz.getName());
  }

  /**
   * Report (and possibly create) the logger related to the provided name
   * (usually the full class name).
   *
   * @param name the logger name
   * @return the logger found or created
   */
  public static synchronized Logger getLogger(String name) {
    return new Logger(name);
  }

  /**
   * Report the buffer for log messages before they get * displayed by the GUI.
   * @return the LogRecord queue
   */
  public static synchronized BlockingQueue<LogRecord> getQueue() {
    if (logQueue == null)
      logQueue = new ArrayBlockingQueue<LogRecord>(LOG_QUEUE_SIZE);

    return logQueue;
  }

  /**
   * Report the resulting level for the logger, which may be inherited from
   * parents higher in the hierarchy.
   *
   * @return The effective logging level for this logger
   */
  public Level getEffectiveLevel() {
    java.util.logging.Logger logger = this;
    Level                    level = getLevel();

    while (level == null) {
      logger = logger.getParent();

      if (logger == null) return null;

      level = logger.getLevel();
    }

    return level;
  }

  /**
   * Log the provided message and stop.
   *
   * @param msg the (severe) message
   */
  @Override
  public void severe(String msg) {
    super.severe(msg);
    new Throwable().printStackTrace();
  }

  /**
   * Log the provided message and exception, then stop the application.
   *
   * @param msg the (severe) message
   * @param thrown the exception
   */
  public void severe(String msg, Throwable thrown) {
    super.severe(msg + " [" + thrown + "]");
    thrown.printStackTrace();
  }

  private static void setGlobalParameters() {
    // Retrieve the logger at the top of the hierarchy
    java.util.logging.Logger topLogger;
    topLogger = java.util.logging.Logger.getLogger("");

    // Handler for console (reuse it if it already exists)
    Handler consoleHandler = null;

    for (Handler handler : topLogger.getHandlers()) {
      if (handler instanceof ConsoleHandler) {
        consoleHandler = handler;

        break;
      }
    }

    if (consoleHandler == null) {
      consoleHandler = new ConsoleHandler();
      topLogger.addHandler(consoleHandler);
    }

    consoleHandler.setLevel(Level.FINE);

    // Handler for GUI log pane
    topLogger.addHandler(new LogGuiHandler());

    // Default level
    topLogger.setLevel(Level.INFO);
  }
}
