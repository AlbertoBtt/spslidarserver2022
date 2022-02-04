package ujaen.spslidar.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ujaen.spslidar.DTOs.http.WorkspaceDTO;
import ujaen.spslidar.Exceptions.WorkspaceExistsException;
import ujaen.spslidar.Exceptions.WorkspaceNotFoundException;
import ujaen.spslidar.services.core.WorkspaceService;

import javax.validation.Valid;

/**
 * Controller class to manage the requests associated to the Workspace entity
 */
@RestController
@RequestMapping("/spslidar")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * Retrieves all the workspaces stored in the system
     * @return All the workspaces in the system, if no workspace exists, an empty list will be the result
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "workspaces", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public ResponseEntity<Flux<WorkspaceDTO>> getWorkspaces() {
        Flux<WorkspaceDTO> workspaceDTOFlux = workspaceService.getWorkspaces();
        return new ResponseEntity<>(workspaceDTOFlux, HttpStatus.OK);
    }

    /**
     * Returns a workspace identified by its name
     * @param workspace_name name of the workspace to retrieve
     * @return If the operation is successful, it will return the metadata associated to the workspace searched
     * otherwise an error handler will manage the result of the request
     */
    @GetMapping(value = "workspaces/{workspace_name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> getWorkspace(@PathVariable String workspace_name) {

        return workspaceService.getWorkspace(workspace_name)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response))
                .switchIfEmpty(Mono.error(new WorkspaceNotFoundException()))
                .cast(ResponseEntity.class);
    }

    /**
     * Adds a new workspace to the system
     * @param workspace workspace entity to add
     * @return If the operation was successful it will return the metadata associated to the workspace
     * created, otherwise an error handler will manage the result of the request
     */
    @PostMapping(value = "workspaces", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> addWorkspace(@RequestBody @Valid WorkspaceDTO workspace) {

        return workspaceService
                .addWorkspace(workspace)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .switchIfEmpty(Mono.error(new WorkspaceExistsException()))
                .cast(ResponseEntity.class);
    }



}
