package assimilation;

public class Utils {
    public static int rangeXY(int x1, int y1, int x2, int y2) {

        return Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
    }
}