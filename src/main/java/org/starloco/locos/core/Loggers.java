package org.starloco.locos.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class Loggers {

    public static final Logger objects = LoggerFactory.getLogger("Object");
    public static final Logger globalMessage = LoggerFactory.getLogger("GlobalMessage");
    public static final Logger command = LoggerFactory.getLogger("Command");
    public static final Logger tchat = LoggerFactory.getLogger("Tchat");
    public static final Logger craft = LoggerFactory.getLogger("Craft");
    public static final Logger game = LoggerFactory.getLogger("Game");
    public static final Logger error = LoggerFactory.getLogger("Error");

}