package ujaen.spslidar.entities;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Base datablock abstract class that can be extended.
 * It allows to create different Datablock classes that may contain
 * additional attributes needed for specific algorithms or in memory approaches.
 */
@Data
public abstract class AbstractDatablock implements Serializable {

    //ID of the datablock inside the data structure hierarchy it will belong to
    protected int id;
    //Bounding box of the octree
    protected GeorefBox georefBox;
    //Number of points that its associated file contains
    protected long numberOfPoints;
    //Path to the associated laz file that will be stored
    protected String lazFileAssociated;
    //List of children datablock
    protected List<Integer> children = new ArrayList<>();
    //Georefbox that associates the datablock to a particular octree/cell of the grid
    protected GeorefBox UTMZoneLocalGrid;
    //Level of the depth of the datablock
    protected int depth;
    //Reference to the GridFS document that contains the file once it has been stored
    private ObjectId objectId;



}
