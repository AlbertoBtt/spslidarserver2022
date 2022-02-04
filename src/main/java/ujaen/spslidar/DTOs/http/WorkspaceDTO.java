package ujaen.spslidar.DTOs.http;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ujaen.spslidar.entities.Workspace;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceDTO {

    @NotNull
    String name;

    String description;

    @NotNull @Min(100) @Max(1000000)
    int cellSize;


    public WorkspaceDTO(Workspace ws){
        this.name = ws.getName();
        this.description = ws.getDescription();
        this.cellSize = ws.getCellSize();
    }


}
