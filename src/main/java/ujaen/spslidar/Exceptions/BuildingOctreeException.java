package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class BuildingOctreeException extends RuntimeException {

    @Getter
    private static String msg = "An error occured during the construction of the octree";

    public BuildingOctreeException() {
        super(msg);
    }
}
