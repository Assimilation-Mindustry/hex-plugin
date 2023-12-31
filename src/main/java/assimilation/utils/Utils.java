package assimilation.utils;

import arc.util.Log;

import java.lang.reflect.Array;

import static arc.util.Log.format;

public class Utils {
    public static int rangeXY(int x1, int y1, int x2, int y2) {

        return Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
    }
    public static <T> T randomChoice(T[] array) {

        int choiceIndex = (int) Math.floor(Math.random() * array.length);

        return array[choiceIndex];
    }

    public static int randomIntRange(int min, int max) {

        return (int) Math.floor(Math.random() * (max - min) + min);
    }

    public static void info(String text, Object... values) {

        Log.infoTag("info", format(text, values));
    }
    public static void error(String text, Object... values) {

        Log.errTag("error", format(text, values));
    }
}