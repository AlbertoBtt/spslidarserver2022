package ujaen.spslidar.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Datablock extends AbstractDatablock {

    //Additional attribute that references a temporary file used during the out-of-core algorithm
    private String tmpOpsFile;

    public Datablock(int id, GeorefBox georefBox, GeorefBox UTMZoneLocalGrid) {
        this.id = id;
        this.georefBox = georefBox;
        this.numberOfPoints = 0;
        this.UTMZoneLocalGrid = UTMZoneLocalGrid;
        this.depth = 0;

    }

    public Datablock(int parentIndex, int localIndex, GeorefBox georefBox, GeorefBox UTMZoneLocalGrid, int depth) {
        this.id = parentIndex * 8 + localIndex + 1;
        this.georefBox = georefBox;
        this.numberOfPoints = 0;
        this.UTMZoneLocalGrid = UTMZoneLocalGrid;
        this.depth = depth;
    }


    /**
     * Returns a list of datablocks which are the children of this current datablock
     * also changing the state of this datablock object, updating the childrenIDs list
     *
     * @return List of datablocks (children of the datablock object that called the method)
     */
    public List<Datablock> createSubRegions() {

        List<Datablock> datablockList = new ArrayList<>();
        String UTMZone = this.UTMZoneLocalGrid.getSouthWestBottom().getZone();
        GeorefBox georefBox;
        for (int i = 0; i < 8; i++) {
            georefBox = this.georefBox.getSubRegions(i, UTMZone);
            Datablock datablock = new Datablock(this.id, i, georefBox, this.UTMZoneLocalGrid, this.depth + 1);
            datablockList.add(datablock);

        }
        return datablockList;
    }


}
