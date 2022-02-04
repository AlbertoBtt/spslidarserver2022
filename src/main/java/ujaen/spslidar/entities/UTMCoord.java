package ujaen.spslidar.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import ujaen.spslidar.Exceptions.InvalidCoordinateException;

import java.io.Serializable;

/**
 * UTM Coordinate class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UTMCoord implements Serializable {

    @NonNull
    private double easting;

    @NonNull
    private double northing;

    @NonNull
    private String zone;

    private double height;

    /**
     * Build a UTMCoord object from a String that follows the pattern ZZREE..ENNN..N, being ZZ the zone code, R the region,
     * EE..E the easting and NNN..N the northing
     *
     * @param coord string to parse
     * @return UTMCoord object
     */
     public UTMCoord(String coord) {

        try{
            //Solves problems with decimal-like string coords
            String cleanCoord = coord.replaceAll("\\.", "");

            String UTMZone = cleanCoord.substring(0, 3);
            int resolutionLenghtOfCoordinates = Math.floorDiv(cleanCoord.length()-UTMZone.length(), 2);
            int delimitator = UTMZone.length() + resolutionLenghtOfCoordinates;
            double resolutionToMetersMultiplier = Math.pow(10, 6 - resolutionLenghtOfCoordinates);
            String eastingSubString = cleanCoord.substring(3, delimitator);
            String northingSubString = cleanCoord.substring(delimitator);

            if(northingSubString.length() != eastingSubString.length()+1){
                throw new InvalidCoordinateException();
            }

            this.easting = Double.parseDouble(eastingSubString) * resolutionToMetersMultiplier;
            this.northing = Double.parseDouble(northingSubString) * resolutionToMetersMultiplier;
            this.zone = UTMZone;

        }catch(RuntimeException runtimeException){
            throw new InvalidCoordinateException();
        }


    }


    /**
     * Default UTMCoord for a northEast coordinate
     * @return
     */
    public static UTMCoord defaultNorthEast(){
        return UTMCoord.builder()
                .easting(Double.MAX_VALUE)
                .northing(Double.MAX_VALUE)
                .zone("60X")
                .build();
    }

    /**
     * Default UTMCoord for a southWest coordinate
     * @return
     */
    public static UTMCoord defaultSouthWest(){
        return UTMCoord.builder()
                .easting(Double.MIN_VALUE)
                .northing(Double.MIN_VALUE)
                .zone("00C")
                .build();

    }




}
