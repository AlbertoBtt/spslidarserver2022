package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class NoUTMZoneInFile extends RuntimeException {

    @Getter
    private static String msg = "No UTM Zone found in file. Make sure to add a VLR to the file specifying" +
            "the UTM Zone in which the coordinates are located";

    public NoUTMZoneInFile() {
        super(msg);
    }
}
