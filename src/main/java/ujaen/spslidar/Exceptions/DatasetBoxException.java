package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class DatasetBoxException extends RuntimeException {

    @Getter
    private static String msg = "Dataset box is larger than the grid size";

    public DatasetBoxException() {
        super(msg);
    }

}
