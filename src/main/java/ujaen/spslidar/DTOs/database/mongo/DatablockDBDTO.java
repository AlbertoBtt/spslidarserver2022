package ujaen.spslidar.DTOs.database.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import ujaen.spslidar.entities.AbstractDatablock;
import ujaen.spslidar.entities.Datablock;
import ujaen.spslidar.entities.GeorefBox;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatablockDBDTO {

    @NotNull
    private String datasetName;
    @NotNull
    private int node;
    @NotNull
    private GeorefBox cell;
    @NotNull
    private GeorefBox bbox;
    @NotNull
    private long numberOfPoints;

    private List<Integer> children;
    @NotNull
    private ObjectId gridFileId;
    @NotNull
    private int depth;


    public DatablockDBDTO(AbstractDatablock datablock, String datasetName, ObjectId gridFileId) {

        this.datasetName = datasetName;
        this.node = datablock.getId();
        this.bbox = datablock.getGeorefBox();
        this.numberOfPoints = datablock.getNumberOfPoints();
        this.children = datablock.getChildren();
        this.cell = datablock.getUTMZoneLocalGrid();
        this.gridFileId = gridFileId;
        this.depth = datablock.getDepth();

    }

    public Datablock fromDatablockDBDTO() {

        Datablock datablock = new Datablock();

        datablock.setId(this.node);
        datablock.setGeorefBox(this.bbox);
        datablock.setNumberOfPoints(numberOfPoints);
        datablock.setChildren(this.children);
        datablock.setDepth(this.depth);
        datablock.setUTMZoneLocalGrid(this.cell);
        datablock.setObjectId(this.gridFileId);
        return datablock;
    }

}
