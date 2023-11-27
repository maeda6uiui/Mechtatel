package com.github.maeda6uiui.mechtatel.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

/**
 * Provides some methods to change logback settings at runtime.
 *
 * @author maeda6uiui
 */
public class MttLogging {
    public static void setRootLoggerLogLevel(String logLevel) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.exists(org.slf4j.Logger.ROOT_LOGGER_NAME);
        Level level = Level.toLevel(logLevel, Level.INFO);
        logger.setLevel(level);
    }
}
