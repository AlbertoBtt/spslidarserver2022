package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class ElementNotFound extends RuntimeException {

    @Getter
    private static String msg = "The workspace or dataset specified does not exist";

    public ElementNotFound() {
        super(msg);
    }


}
