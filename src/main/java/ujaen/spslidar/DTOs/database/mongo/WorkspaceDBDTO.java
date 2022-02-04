package ujaen.spslidar.DTOs.database.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ujaen.spslidar.entities.Workspace;

import javax.validation.constraints.NotNull;

@Document("workspaces")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceDBDTO {

    @Id @NotNull
    private String workspaceName;
    private String description;

    @NotNull
    private int cellSize;


    public WorkspaceDBDTO(Workspace ws){
        this.workspaceName = ws.getName();
        this.description = ws.getDescription();
        this.cellSize = ws.getCellSize();
    }

    public Workspace workspaceFromDTO(){
        return new Workspace(workspaceName, description, cellSize);
    }


}
