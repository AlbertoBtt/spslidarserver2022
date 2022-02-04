package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class UncompletedCoordinatesInQueryException extends RuntimeException {

    @Getter
    private static String msg = "Coordinates were missing in the spatial query specified by the user";

    public UncompletedCoordinatesInQueryException() {
        super(msg);
    }


}

