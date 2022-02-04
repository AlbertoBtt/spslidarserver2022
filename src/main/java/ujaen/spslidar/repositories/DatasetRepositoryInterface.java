package ujaen.spslidar.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ujaen.spslidar.entities.Dataset;
import ujaen.spslidar.entities.GeorefBox;

import java.time.LocalDateTime;

/**
 * Repository interface for the dataset entity
 */
public interface DatasetRepositoryInterface {

    /**
     * Checks if a dataset exists by searching by its workspace and name
     * @param workspaceName name of the workspace
     * @param datasetName name of the dataset
     * @return boolean with the result
     */
    Mono<Boolean> existsByWorkspaceAndDataset(String workspaceName, String datasetName);

    /**
     * Queries by workspace and dataset name returning the dataset that fit
     * @param workspaceName name of the workspace
     * @param datasetName name of the dataset
     * @return Mono with the dataset if exists, otherwise a Mono empty
     */
    Mono<Dataset> findByWorkspaceAndDataset(String workspaceName, String datasetName);

    /**
     * Query by workspace name, dataset name and time window
     * @param workspaceName name of the workspace
     * @param datasetName name of the dataset
     * @param fromDate lower limit of the temporal query
     * @param toDate upper limit of the temporal query
     * @return Mono with the dataset if it exists, otherwise Mono empty
     */
    Mono<Dataset> findByWorkspaceAndDatasetAndTimeWindow(String workspaceName, String datasetName, LocalDateTime fromDate, LocalDateTime toDate);


    /**
     * Returns all the datasets that exist in a specific workspace and fit a spatial and temporal window.
     * These windows may have Maximum and Minimum values when no parameters were applied in the search.
     *
     * @param workspaceName name of the workspace
     * @param fromDate lower limit of the temporal query
     * @param toDate upper limit of the temporal query
     * @return Flux with the datasets that fit the query
     */
    Flux<Dataset> findByWorkspaceNameAndTimeWindow(String workspaceName, LocalDateTime fromDate, LocalDateTime toDate);


    /**
     * Save a new dataset
     * @param dataset information of the dataset
     * @return Metadata of the inserted dataset
     */
    Mono<Dataset> save(Dataset dataset);

    /**
     * Update a dataset.
     * @param dataset information of the dataset
     * @return metadata of the updated dataset
     */
    Mono<Dataset> update(Dataset dataset);


    /**
     * Removes a grid cell association from a dataset. Invoked when a false positive is detected
     * during the processing stage of the dataset files
     * @param dataset
     * @param gridCell
     * @return
     */
    Mono<Boolean> removeGridCellFromDataset(Dataset dataset, GeorefBox gridCell);





}

