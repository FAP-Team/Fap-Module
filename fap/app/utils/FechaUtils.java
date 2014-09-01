package utils;

import java.util.Calendar;

public class FechaUtils {
    public static int getAnyoActual() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }
}
