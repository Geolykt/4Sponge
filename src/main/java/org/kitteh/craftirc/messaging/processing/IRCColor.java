/*
 * * Copyright (C) 2014-2018 Matt Baxter http://kitteh.org
 * * Copyright (C) 2020 Emeric Werner https://geolykt.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.craftirc.messaging.processing;

import com.google.common.collect.ImmutableMap;

import net.minestom.server.chat.ChatColor;

/**
 * Converts IRC colors into MC colors
 */
public final class IRCColor implements Preprocessor {

    private final boolean toIRC;

    public IRCColor(boolean isToIRC) {
        toIRC = isToIRC;
    }

    @Override
    public final void preProcess(PreprocessedMessage msg) {
        if (toIRC) {
            msg.setMessage(toIRC(msg.getMessage()));
        } else {
            msg.setMessage(toMC(msg.getMessage()));
        }
    }

    public static final char IRC_COLOR_ESCAPE_SEQUENCE = 0x03; // Also known as ETX (End of text), often represented as ^C
    public static final char MC_COLOR_ESCAPE_SEQUENCE = 0xA7; // Also known as the paragraph sign, often represented as §

    // Using mIRC's specifications (https://www.mirc.com/colors.html)
    public static final ImmutableMap<Character, String> MC_TO_IRC = new ImmutableMap.Builder<Character, String>()
            .put('0', "1") // black
            .put('1', "2") // dark blue
            .put('2', "3") // dark green
            .put('3', "10") // dark aqua/cyan
            .put('4', "5") // dark red
            .put('5', "6") // dark purple
            .put('6', "7") // gold
            .put('7', "15") // gray
            .put('8', "14") // dark gray
            .put('9', "12") // blue
            .put('a', "9") // green
            .put('b', "11") // aqua / cyan
            .put('c', "4") // red
            .put('d', "13") // light purple
            .put('e', "8") // yellow
            .put('f', "0") // white
            .build();

    public static final ImmutableMap<String, ChatColor> IRC_TO_MC = new ImmutableMap.Builder<String, ChatColor>()
            .put("00", ChatColor.WHITE) // white
            .put("01", ChatColor.BLACK) // black
            .put("02", ChatColor.DARK_BLUE) // dark blue
            .put("03", ChatColor.DARK_GREEN) // dark green
            .put("04", ChatColor.RED) // red
            .put("05", ChatColor.DARK_RED) // dark red
            .put("06", ChatColor.PURPLE) // dark purple
            .put("07", ChatColor.GOLD) // gold
            .put("08", ChatColor.YELLOW) // yellow
            .put("09", ChatColor.BRIGHT_GREEN) // green
            .put("10", ChatColor.DARK_CYAN) // dark aqua/cyan
            .put("11", ChatColor.CYAN) // aqua / cyan
            .put("12", ChatColor.BLUE) // blue
            .put("13", ChatColor.PINK) // light purple
            .put("14", ChatColor.DARK_GRAY) // dark gray
            .put("15", ChatColor.GRAY) // gray
            .build();

    private static String toIRC(String input) {
        // TODO bold, underline, etc.
        char[] original = input.toCharArray();
        StringBuilder out = new StringBuilder(original.length);
        for (int i = 0; i < original.length; i++) {
            char current = original[i];
            if ((current == '&' || current == MC_COLOR_ESCAPE_SEQUENCE) && (i + 1) < original.length) {
                String str = MC_TO_IRC.get(Character.toLowerCase(original[i + 1]));
                if (str == null) {
                    out.append(current);
                } else {
                    out.append(IRC_COLOR_ESCAPE_SEQUENCE);
                    out.append(str);
                    i++;
                }
            } else {
                out.append(current);
            }
        }
        return out.toString();
    }

    private static String toMC(String input) {
        // TODO bold, underline, etc.
        char[] original = input.toCharArray();
        StringBuilder out = new StringBuilder(original.length);
        for (int i = 0; i < original.length; i++) {
            if (original[i] == IRC_COLOR_ESCAPE_SEQUENCE) {
                if ((i + 2) >= original.length) {
                    break;
                }
                if (Character.isDigit(original[i + 2])) {
                    out.append(IRC_TO_MC.getOrDefault(original[++i] + "" + original[++i], ChatColor.WHITE));
                } else {
                    out.append(IRC_TO_MC.getOrDefault("0" + original[++i], ChatColor.WHITE));
                }
            } else {
                out.append(original[i]);
            }
        }
        return out.toString();
    }
}
