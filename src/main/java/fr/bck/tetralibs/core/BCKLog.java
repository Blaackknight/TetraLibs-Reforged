package fr.bck.tetralibs.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.regex.Pattern;

public final class BCKLog {

    public static final class Utils {
        // ta couleur
        public static final String color = "§f";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    // Prefix et suffix en codes § (minecraft) : §e = jaune, §d = magenta, §r = reset
    private static final String PREFIX = "§6[" + BCKCore.TITLES_COLORS.title(TetraLibs. class) + "§6/";
    private static final String SUFFIX = "§6] §r";
    private static final String RESET = "\u001B[0m";

    /* ────────── Codes Minecraft → ANSI ────────── */
    private static final Map<Character, String> MC_CODES = Map.ofEntries(Map.entry('0', "\u001B[30m"), // noir
            Map.entry('1', "\u001B[34m"), // bleu foncé
            Map.entry('2', "\u001B[32m"), // vert foncé
            Map.entry('3', "\u001B[36m"), // cyan foncé
            Map.entry('4', "\u001B[31m"), // rouge foncé
            Map.entry('5', "\u001B[35m"), // magenta foncé
            Map.entry('6', "\u001B[38;5;208m"), // orange
            Map.entry('7', "\u001B[37m"), // gris clair
            Map.entry('8', "\u001B[90m"), // gris foncé
            Map.entry('9', "\u001B[94m"), // bleu clair
            Map.entry('a', "\u001B[92m"), // vert clair
            Map.entry('b', "\u001B[96m"), // cyan clair
            Map.entry('c', "\u001B[91m"), // rouge clair
            Map.entry('d', "\u001B[95m"), // magenta clair
            Map.entry('e', "\u001B[93m"), // jaune clair
            Map.entry('f', "\u001B[97m"), // blanc
            Map.entry('k', ""),             // obfuscated (non supporté)
            Map.entry('l', "\u001B[1m"),   // gras
            Map.entry('m', "\u001B[9m"),   // barré
            Map.entry('n', "\u001B[4m"),   // souligné
            Map.entry('o', "\u001B[3m"),   // italique
            Map.entry('r', RESET)             // reset
    );
    private static final Pattern MC_PATTERN = Pattern.compile("§([0-9a-frlmnok])", Pattern.CASE_INSENSITIVE);

    /* ────────── API publique ────────── */

    public static void info(String cat, Object msg) {
        LOGGER.info(format(cat, msg));
    }

    public static void info(String cat, Object msg, Throwable t) {
        LOGGER.info(format(cat, msg), t);
    }

    public static void debug(String cat, Object msg) {
        LOGGER.debug(format(cat, msg));
    }

    public static void debug(String cat, Object msg, Throwable t) {
        LOGGER.debug(format(cat, msg), t);
    }

    public static void warn(String cat, Object msg) {
        LOGGER.warn(format(cat, msg));
    }

    public static void warn(String cat, Object msg, Throwable t) {
        LOGGER.warn(format(cat, msg), t);
    }

    public static void error(String cat, Object msg) {
        LOGGER.error(format(cat, msg));
    }

    public static void error(String cat, Object msg, Throwable t) {
        LOGGER.error(format(cat, msg), t);
    }

    public static void fatal(String cat, Object msg) {
        LOGGER.error(format("§l§4FATAL§r§6/" + cat, msg));
    }

    public static void fatal(String cat, Object msg, Throwable t) {
        LOGGER.error(format("§l§4FATAL§r§6/" + cat, msg), t);
    }

    /* ────────── Implémentation ────────── */

    private static String format(String cat, Object raw) {
        String txt = String.valueOf(raw);
        String head = colorize(PREFIX + cat + SUFFIX);
        txt = colorize(txt);
        if (!txt.endsWith(RESET)) txt += RESET;
        return head + txt;
    }

    /**
     * Remplace chaque code Minecraft §x par la séquence ANSI correspondante.
     */
    private static String colorize(String in) {
        var m = MC_PATTERN.matcher(in);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (m.find()) {
            out.append(in, last, m.start());
            char code = Character.toLowerCase(m.group(1).charAt(0));
            String ansi = MC_CODES.getOrDefault(code, "");
            out.append(ansi);
            last = m.end();
        }
        out.append(in, last, in.length());
        return out.toString();
    }

    private BCKLog() {
    }
}