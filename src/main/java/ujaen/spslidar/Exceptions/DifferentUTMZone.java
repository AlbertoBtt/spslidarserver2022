package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class DifferentUTMZone extends RuntimeException {

    @Getter
    private static String msg = "This bounding box must have coordinates expressed in the same UTM Zone";

    public DifferentUTMZone() {
        super(msg);
    }


}
