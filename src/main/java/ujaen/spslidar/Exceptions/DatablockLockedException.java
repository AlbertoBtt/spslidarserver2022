package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class DatablockLockedException extends RuntimeException{

    @Getter
    private static String msg = "The datablock is locked";

    public DatablockLockedException() {
        super(msg);
    }

}
