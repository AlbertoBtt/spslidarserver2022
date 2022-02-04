package ujaen.spslidar.DTOs.database.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;
import ujaen.spslidar.entities.Dataset;
import ujaen.spslidar.entities.GeorefBox;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasetDBDTO {

    @Id @NotNull
    private String datasetName;

    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;
    @NotNull
    private GeorefBox bbox;
    @NotNull
    private int dataBlockSize;
    @NotNull
    private String dataBlockFormat;

    private Map<String, List<GeorefBox>> gridsAssociated = new HashMap<>();
    @NotNull
    private Dataset.State dataAssociated = Dataset.State.NO_DATA;


    public DatasetDBDTO(Dataset dataset) {
        this.datasetName = dataset.getDatasetName();
        this.description = dataset.getDescription();
        this.date = dataset.getDate();
        this.bbox = dataset.getBbox();
        this.dataBlockSize = dataset.getDataBlockSize();
        this.dataBlockFormat = dataset.getDataBlockFormat();
        this.gridsAssociated = dataset.getRootDatablocks();
        this.dataAssociated = dataset.getDataAssociated();
    }

    public Dataset datasetFromDTO() {
        return new Dataset(datasetName, description, date, bbox,
                dataBlockSize, dataBlockFormat, gridsAssociated, dataAssociated);

    }

}
