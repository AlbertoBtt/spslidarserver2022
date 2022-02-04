package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class DatasetHasDataAssociated extends RuntimeException {

    @Getter
    private static String msg = "Dataset already has data associated";

    public DatasetHasDataAssociated() {
        super(msg);
    }
}
