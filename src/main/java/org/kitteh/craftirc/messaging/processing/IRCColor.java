package org.kitteh.craftirc.messaging.processing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kitteh.irc.client.library.util.Format;

/**
 * Converts IRC colors into MC colors
 */
public final class IRCColor implements Preprocessor {

    private static enum Matches {
        BLACK(Format.BLACK, '0'),
        DARK_BLUE(Format.DARK_BLUE, '1'),
        DARK_GREEN(Format.DARK_GREEN, '2'),
        DARK_AQUA(Format.TEAL, '3'),
        DARK_RED(Format.BROWN, '4'),
        DARK_PURPLE(Format.PURPLE, '5'),
        GOLD(Format.OLIVE, '6'),
        GRAY(Format.LIGHT_GRAY, '7'),
        DARK_GRAY(Format.DARK_GRAY, '8'),
        BLUE(Format.BLUE, '9'),
        GREEN(Format.GREEN, 'A'),
        AQUA(Format.CYAN, 'B'),
        RED(Format.RED, 'C'),
        LIGHT_PURPLE(Format.MAGENTA, 'D'),
        YELLOW(Format.YELLOW, 'E'),
        WHITE(Format.WHITE, 'F');

        private Format irc;
        private char mc;

        Matches(Format irc, char mc) {
            this.irc = irc;
            this.mc = mc;
        }

        private static final Map<Integer, String> IRC_MAP;
        private static final Pattern IRC_PATTERN = Pattern.compile(Format.COLOR_CHAR + "([0-9]{1,2})(?:,[0-9]{1,2})?");
        private static final Map<Character, String> MC_MAP;
        private static final Pattern MC_PATTERN = Pattern.compile("\u00A7([a-z0-9])", Pattern.CASE_INSENSITIVE);

        static {
            IRC_MAP = new HashMap<>();
            MC_MAP = new HashMap<>();
            for (Matches matches : values()) {
                IRC_MAP.put(matches.irc.getColorChar(), "\u00A7" + matches.mc);
                MC_MAP.put(matches.mc, matches.irc.toString());
            }
        }

        static String getIRCByMC(char mc) {
            return MC_MAP.get(mc);
        }

        static String getMCByIRC(int irc) {
            return IRC_MAP.get(irc);
        }
    }

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

    private static String toIRC(String input) {
        Matcher matcher = Matches.MC_PATTERN.matcher(input);
        int currentIndex = 0;
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            int next = matcher.start();
            if (currentIndex < next) {
                builder.append(input.substring(currentIndex, next));
            }
            currentIndex = matcher.end();
            char s = matcher.group(1).toUpperCase().charAt(0);
            if (s <= 'F') {
                builder.append(Matches.getIRCByMC(s));
            } else if (s == 'R') {
                builder.append(Format.RESET);
            }
        }
        if (currentIndex < input.length()) {
            builder.append(input.substring(currentIndex));
        }
        return builder.append(Format.RESET).toString();
    }

    private static String toMC(String input) {
        input = input.replace(Format.BOLD.toString(), "");
        input = input.replace(Format.UNDERLINE.toString(), "");
        input = input.replace(Format.REVERSE.toString(), "");
        input = input.replace(Format.RESET.toString(), "\u00A7r");
        Matcher matcher = Matches.IRC_PATTERN.matcher(input);
        int currentIndex = 0;
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            int next = matcher.start();
            if (currentIndex < next) {
                builder.append(input.substring(currentIndex, next));
            }
            currentIndex = matcher.end();
            int i;
            try {
                i = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                continue;
            }
            builder.append(Matches.getMCByIRC(i)); // TODO HEX Colors!
        }
        if (currentIndex < input.length()) {
            builder.append(input.substring(currentIndex));
        }
        return builder.append("\u00A7r").toString();
    }
}
