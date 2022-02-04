package ujaen.spslidar.services.tools;

import reactor.core.publisher.Mono;
import ujaen.spslidar.entities.GeorefBox;


/**
 * Interface for the methods needed to read LAZ files
 */
public interface LazReaderInterface {

    /**
     * Returns the a Georefbox that represent the minimum and maximum limits of
     * the point cloud associated to the file passed by argument
     * @param pathLazFile
     * @return
     */
     Mono<GeorefBox> getGeorefBox(String pathLazFile);

    /**
     * Returns a Georefbox following a regular pattern (distance in both X and Y axis is equal)
     * The northEast coordinate is increased to fit this requirement.
     * @param pathLazFile
     * @return
     */
     Mono<GeorefBox> getRegularGeorefBox(String pathLazFile);


    /**
     * Returns the number of points that the point cloud associated to the file
     * passed by argument has
     * @param pathLazFile
     * @return
     */
     Mono<Long> getNumberOfPoints(String pathLazFile);



}
