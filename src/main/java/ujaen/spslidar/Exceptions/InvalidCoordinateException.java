package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class InvalidCoordinateException extends RuntimeException {

    @Getter
    private static String msg = "There was a problem parsing the given UTM coordinate";

    public InvalidCoordinateException() {
        super(msg);
    }

}
