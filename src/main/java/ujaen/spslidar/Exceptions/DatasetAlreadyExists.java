package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class DatasetAlreadyExists extends RuntimeException {

    @Getter
    private static String msg = "A dataset with this ID already exists";

    public DatasetAlreadyExists() {
        super(msg);
    }

}

