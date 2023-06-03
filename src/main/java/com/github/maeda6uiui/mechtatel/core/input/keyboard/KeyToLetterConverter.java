package com.github.maeda6uiui.mechtatel.core.input.keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for keyboard input
 *
 * @author maeda6uiui
 */
public class KeyToLetterConverter {
    private static final List<String> SPECIAL_KEYS;

    static {
        SPECIAL_KEYS = new ArrayList<>();

        SPECIAL_KEYS.add("ENTER");
        SPECIAL_KEYS.add("BACKSPACE");
        SPECIAL_KEYS.add("DELETE");
        SPECIAL_KEYS.add("RIGHT");
        SPECIAL_KEYS.add("LEFT");
        SPECIAL_KEYS.add("DOWN");
        SPECIAL_KEYS.add("UP");
    }

    public static boolean isSpecialKey(String key) {
        return SPECIAL_KEYS.contains(key);
    }

    public static String getInputLetter(Map<String, Integer> keyboardPressingCounts, int repeatDelayFrames) {
        //The key that has been pressed most recently is the one that should be interpreted
        int minPressingCount = Integer.MAX_VALUE;
        String minPressingKey = "";
        for (var entry : keyboardPressingCounts.entrySet()) {
            String key = entry.getKey();
            int pressingCount = entry.getValue();

            if (pressingCount > 0 && pressingCount < minPressingCount) {
                minPressingKey = key;
                minPressingCount = pressingCount;
            }
        }

        if (minPressingKey.equals("")) {
            return "";
        }

        if (!(minPressingCount == 1 || minPressingCount > repeatDelayFrames)) {
            return "";
        }

        //Special keys
        if (SPECIAL_KEYS.contains(minPressingKey)) {
            return minPressingKey;
        }

        //Check what letter should be output
        boolean shiftPressed = false;
        if (keyboardPressingCounts.get("LEFT_SHIFT") > 0 || keyboardPressingCounts.get("RIGHT_SHIFT") > 0) {
            shiftPressed = true;
        }

        String outputLetter = "";
        switch (minPressingKey) {
            case "SPACE":
                outputLetter = " ";
                break;
            case "APOSTROPHE":
                outputLetter = "'";
                break;
            case "COMMA":
                outputLetter = ",";
                break;
            case "MINUS":
                outputLetter = "-";
                break;
            case "PERIOD":
                outputLetter = ".";
                break;
            case "SLASH":
                outputLetter = "/";
                break;
            case "SEMICOLON":
                outputLetter = ";";
                break;
            case "EQUAL":
                outputLetter = "=";
                break;
            case "LEFT_BRACKET":
                outputLetter = "(";
                break;
            case "BACKSLASH":
                outputLetter = "\\";
                break;
            case "RIGHT_BRACKET":
                outputLetter = ")";
                break;
            case "GRAVE_ACCENT":
                outputLetter = "`";
                break;
        }
        if (!outputLetter.equals("")) {
            return outputLetter;
        }

        if (shiftPressed) {
            switch (minPressingKey) {
                case "0":
                    outputLetter = "";
                    break;
                case "1":
                    outputLetter = "!";
                    break;
                case "2":
                    outputLetter = "\"";
                    break;
                case "3":
                    outputLetter = "#";
                    break;
                case "4":
                    outputLetter = "$";
                    break;
                case "5":
                    outputLetter = "%";
                    break;
                case "6":
                    outputLetter = "&";
                    break;
                case "7":
                    outputLetter = "'";
                    break;
                case "8":
                    outputLetter = "(";
                    break;
                case "9":
                    outputLetter = ")";
                    break;
                case "A":
                    outputLetter = "A";
                    break;
                case "B":
                    outputLetter = "B";
                    break;
                case "C":
                    outputLetter = "C";
                    break;
                case "D":
                    outputLetter = "D";
                    break;
                case "E":
                    outputLetter = "E";
                    break;
                case "F":
                    outputLetter = "F";
                    break;
                case "G":
                    outputLetter = "G";
                    break;
                case "H":
                    outputLetter = "H";
                    break;
                case "I":
                    outputLetter = "I";
                    break;
                case "J":
                    outputLetter = "J";
                    break;
                case "K":
                    outputLetter = "K";
                    break;
                case "L":
                    outputLetter = "L";
                    break;
                case "M":
                    outputLetter = "M";
                    break;
                case "N":
                    outputLetter = "N";
                    break;
                case "O":
                    outputLetter = "O";
                    break;
                case "P":
                    outputLetter = "P";
                    break;
                case "Q":
                    outputLetter = "Q";
                    break;
                case "R":
                    outputLetter = "R";
                    break;
                case "S":
                    outputLetter = "S";
                    break;
                case "T":
                    outputLetter = "T";
                    break;
                case "U":
                    outputLetter = "U";
                    break;
                case "V":
                    outputLetter = "V";
                    break;
                case "W":
                    outputLetter = "W";
                    break;
                case "X":
                    outputLetter = "X";
                    break;
                case "Y":
                    outputLetter = "Y";
                    break;
                case "Z":
                    outputLetter = "Z";
                    break;
            }
        } else {
            switch (minPressingKey) {
                case "0":
                    outputLetter = "0";
                    break;
                case "1":
                    outputLetter = "1";
                    break;
                case "2":
                    outputLetter = "2";
                    break;
                case "3":
                    outputLetter = "3";
                    break;
                case "4":
                    outputLetter = "4";
                    break;
                case "5":
                    outputLetter = "5";
                    break;
                case "6":
                    outputLetter = "6";
                    break;
                case "7":
                    outputLetter = "7";
                    break;
                case "8":
                    outputLetter = "8";
                    break;
                case "9":
                    outputLetter = "9";
                    break;
                case "A":
                    outputLetter = "a";
                    break;
                case "B":
                    outputLetter = "b";
                    break;
                case "C":
                    outputLetter = "c";
                    break;
                case "D":
                    outputLetter = "d";
                    break;
                case "E":
                    outputLetter = "e";
                    break;
                case "F":
                    outputLetter = "f";
                    break;
                case "G":
                    outputLetter = "g";
                    break;
                case "H":
                    outputLetter = "h";
                    break;
                case "I":
                    outputLetter = "i";
                    break;
                case "J":
                    outputLetter = "j";
                    break;
                case "K":
                    outputLetter = "k";
                    break;
                case "L":
                    outputLetter = "l";
                    break;
                case "M":
                    outputLetter = "m";
                    break;
                case "N":
                    outputLetter = "n";
                    break;
                case "O":
                    outputLetter = "o";
                    break;
                case "P":
                    outputLetter = "p";
                    break;
                case "Q":
                    outputLetter = "q";
                    break;
                case "R":
                    outputLetter = "r";
                    break;
                case "S":
                    outputLetter = "s";
                    break;
                case "T":
                    outputLetter = "t";
                    break;
                case "U":
                    outputLetter = "u";
                    break;
                case "V":
                    outputLetter = "v";
                    break;
                case "W":
                    outputLetter = "w";
                    break;
                case "X":
                    outputLetter = "x";
                    break;
                case "Y":
                    outputLetter = "y";
                    break;
                case "Z":
                    outputLetter = "z";
                    break;
            }
        }

        return outputLetter;
    }
}
