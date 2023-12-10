package com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter;

import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;

import java.util.List;
import java.util.Map;

/**
 * JIS key interpreter
 *
 * @author maeda6uiui
 */
public class JISKeyInterpreter extends KeyInterpreter {
    public JISKeyInterpreter() {

    }

    @Override
    public String getInputLetter(
            Map<KeyCode, Integer> keyboardPressingCounts,
            List<KeyCode> specialKeys,
            int repeatDelayFrames) {
        KeyCode minPressingKey = this.getMinPressingKey(keyboardPressingCounts);
        if (minPressingKey == KeyCode.UNKNOWN) {
            return "";
        }

        int minPressingCount = keyboardPressingCounts.get(minPressingKey);
        if (!(minPressingCount == 1 || minPressingCount > repeatDelayFrames)) {
            return "";
        }

        if (specialKeys.contains(minPressingKey)) {
            return minPressingKey.name();
        }

        //Check what letter should be output
        boolean shiftPressed = keyboardPressingCounts.get(KeyCode.LEFT_SHIFT) > 0
                || keyboardPressingCounts.get(KeyCode.RIGHT_SHIFT) > 0;

        String outputLetter;
        if (shiftPressed) {
            outputLetter = switch (minPressingKey) {
                case SPACE -> " ";
                case APOSTROPHE -> "*";
                case COMMA -> "<";
                case MINUS -> "=";
                case PERIOD -> ">";
                case SLASH -> "?";
                case SEMICOLON -> "+";
                case EQUAL -> "~";
                case LEFT_BRACKET -> "`";
                case BACKSLASH -> "}";
                case RIGHT_BRACKET -> "{";
                case GRAVE_ACCENT -> "`";
                case KEY_1 -> "!";
                case KEY_2 -> "\"";
                case KEY_3 -> "#";
                case KEY_4 -> "$";
                case KEY_5 -> "%";
                case KEY_6 -> "&";
                case KEY_7 -> "'";
                case KEY_8 -> "(";
                case KEY_9 -> ")";
                case A -> "A";
                case B -> "B";
                case C -> "C";
                case D -> "D";
                case E -> "E";
                case F -> "F";
                case G -> "G";
                case H -> "H";
                case I -> "I";
                case J -> "J";
                case K -> "K";
                case L -> "L";
                case M -> "M";
                case N -> "N";
                case O -> "O";
                case P -> "P";
                case Q -> "Q";
                case R -> "R";
                case S -> "S";
                case T -> "T";
                case U -> "U";
                case V -> "V";
                case W -> "W";
                case X -> "X";
                case Y -> "Y";
                case Z -> "Z";
                default -> "";
            };
        } else {
            outputLetter = switch (minPressingKey) {
                case SPACE -> " ";
                case APOSTROPHE -> ":";
                case COMMA -> ",";
                case MINUS -> "-";
                case PERIOD -> ".";
                case SLASH -> "/";
                case SEMICOLON -> ";";
                case EQUAL -> "^";
                case LEFT_BRACKET -> "@";
                case BACKSLASH -> "]";
                case RIGHT_BRACKET -> "[";
                case GRAVE_ACCENT -> "@";
                case KEY_0 -> "0";
                case KEY_1 -> "1";
                case KEY_2 -> "2";
                case KEY_3 -> "3";
                case KEY_4 -> "4";
                case KEY_5 -> "5";
                case KEY_6 -> "6";
                case KEY_7 -> "7";
                case KEY_8 -> "8";
                case KEY_9 -> "9";
                case A -> "a";
                case B -> "b";
                case C -> "c";
                case D -> "d";
                case E -> "e";
                case F -> "f";
                case G -> "g";
                case H -> "h";
                case I -> "i";
                case J -> "j";
                case K -> "k";
                case L -> "l";
                case M -> "m";
                case N -> "n";
                case O -> "o";
                case P -> "p";
                case Q -> "q";
                case R -> "r";
                case S -> "s";
                case T -> "t";
                case U -> "u";
                case V -> "v";
                case W -> "w";
                case X -> "x";
                case Y -> "y";
                case Z -> "z";
                default -> "";
            };
        }

        return outputLetter;
    }
}
