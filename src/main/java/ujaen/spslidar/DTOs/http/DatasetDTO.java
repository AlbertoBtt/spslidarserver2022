package ujaen.spslidar.DTOs.http;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ujaen.spslidar.entities.Dataset;
import ujaen.spslidar.entities.GeorefBox;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasetDTO {

    @NotNull
    private String name;

    private String description;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateOfAcquisition;

    @NotNull
    private GeorefBox boundingBox;

    @NotNull @Min(100)
    private int dataBlockSize;

    private String dataBlockFormat = "LAZ";

    private List<GeorefBox> rootDatablocks = new ArrayList<>();

    public DatasetDTO(Dataset m) {

        this.name = m.getDatasetName();
        this.description = m.getDescription();
        this.dateOfAcquisition = m.getDate();
        this.boundingBox = m.getBbox();
        this.dataBlockSize = m.getDataBlockSize();
        this.dataBlockFormat = m.getDataBlockFormat();

        if(m.getDataAssociated().equals(Dataset.State.DATA_ASSOCIATED)){
            for (String key : m.getRootDatablocks().keySet()) {
                rootDatablocks.addAll(m.getRootDatablocks().get(key));
            }
        }

    }

    public DatasetDTO(String name, String description, LocalDateTime date, GeorefBox boundingBox,
                      int dataBlockSize, String dataBlockFormat) {
        this.name = name;
        this.description = description;
        this.dateOfAcquisition = date;
        this.boundingBox = boundingBox;
        this.dataBlockSize = dataBlockSize;
        this.dataBlockFormat = dataBlockFormat;
    }
}
