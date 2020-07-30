package retwis.util;

public class TimeUtils {
    public static final long YEAR = 31536000000L;
    public static final long MONTH = 2678400000L;
    public static final long DAY = 86400000L;
    public static final long HOUR = 3600000L;
    public static final long MINUTE = 60000L;
    public static final long SECOND = 1000L;

    public static String convertTime(long time) {
        long gap = System.currentTimeMillis() - time;
        StringBuilder sb = new StringBuilder("posted ");
        if (gap > YEAR) {
            sb.append(gap / YEAR).append(" year");
        } else if (gap > MONTH) {
            sb.append(gap / MONTH).append(" month");
        } else if (gap > DAY) {
            sb.append(gap / DAY).append(" day");
        } else if (gap > HOUR) {
            sb.append(gap / HOUR).append(" hour");
        } else if (gap > MINUTE) {
            sb.append(gap / MINUTE).append(" minute");
        } else if (gap > SECOND) {
            sb.append(gap / SECOND).append(" second");
        } else {
            sb.append("1 second");
        }
        return sb.append(" ago via web").toString();
    }
}
