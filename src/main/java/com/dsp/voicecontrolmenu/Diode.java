package com.dsp.voicecontrolmenu;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aleksander on 2015-01-31.
 */
public enum Diode {
    DIODE_1("one", "ledB"),
    DIODE_2("two", "led1"),
    DIODE_3("three", "led2"),
    DIODE_4("four", "led3"),;

    private static final String PATH = "/sys/class/leds/";
    private static final String BRIGHTNESS = "/brightness";
    private static final String TRIGGER = "/trigger";
    private static final String ON = "1";
    private static final String OFF = "0";
    private static final String NONE = "none";

    private final String command;
    private final String pathToLed;
    private final String pathToTrigger;

    private boolean turnOn;

    Diode(String command, String deviceName) {
        this.command = command;
        this.pathToLed = PATH + deviceName + BRIGHTNESS;
        this.pathToTrigger = PATH + deviceName + TRIGGER;
    }

    public void turnOn() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathToLed))) {
            writer.write(ON);
            turnOn = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void turnOff() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathToLed))) {
            writer.write(OFF);
            turnOn = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disableTrigger() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathToTrigger))) {
            writer.write(NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isTurnOn() {
        return turnOn;
    }

    private static final Map<String, Diode> diodes;

    static {
        diodes = new HashMap<>(values().length);
        for (Diode diode : values()) {
            diodes.put(diode.command, diode);
        }
    }

    public static Diode valueOfCommand(String command) {
        return diodes.get(command);
    }

    public static boolean isDiodeAvailable(String command) {
        return diodes.containsKey(command);
    }
}
