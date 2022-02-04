package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class QueryNotAllowedMissingUTMCell extends RuntimeException {

    @Getter
    private static String msg = "You must also specify a UTMCell";

    public QueryNotAllowedMissingUTMCell() {
        super(msg);
    }

}
