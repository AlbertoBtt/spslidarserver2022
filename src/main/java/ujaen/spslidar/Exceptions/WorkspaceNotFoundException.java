package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class WorkspaceNotFoundException extends RuntimeException {

    @Getter
    private static String msg = "Unknown workspace";

    public WorkspaceNotFoundException() {
        super(msg);
    }
}
