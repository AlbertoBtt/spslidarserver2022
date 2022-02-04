/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ujaen.spslidar.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ujaen.spslidar.DTOs.http.DatablockDTO;
import ujaen.spslidar.Exceptions.*;
import ujaen.spslidar.services.core.DatablockService;
import ujaen.spslidar.services.core.DatablockServiceCommonUtils;
import ujaen.spslidar.services.core.DatasetService;

/**
 * Controller class to manage the requests associated to the Datablock entity
 */
@RestController
@RequestMapping("/spslidar/workspaces/")
public class DatablockController {

    private final DatablockService datablockService;
    private final DatablockServiceCommonUtils datablockServiceCommonUtils;
    private final DatasetService datasetService;
    private final String messageAddedData = "Added dataset";


    Logger logger = LoggerFactory.getLogger(DatablockController.class);

    public DatablockController(DatablockService datablockService,
                               DatablockServiceCommonUtils datablockServiceCommonUtils,
                               DatasetService datasetService) {
        this.datablockService = datablockService;
        this.datablockServiceCommonUtils = datablockServiceCommonUtils;
        this.datasetService = datasetService;
    }


    /**
     * Search a datablock associated to a workspace and dataset
     *
     * @param workspace_name name of the workspace
     * @param dataset_name   name of the dataset
     * @param datablock_id   id of the datablock
     * @param sw_coord       south west coordinate of the geoquery
     * @param ne_coord       north east coordinate of the geoquery
     * @return If operation is successful, it will return one or more datablocks depending on whether the coordinates
     * of a grid were specified (will return at max 1) or not (will return at max as many datablocks as octrees were generated
     * for the dataset). If no workspace, dataset or datablock was found, an error handler will manage the request.
     */
    @ResponseStatus(code = HttpStatus.OK)
    @GetMapping(value = "{workspace_name}/datasets/{dataset_name}/datablocks/{datablock_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<DatablockDTO> getDatablock(@PathVariable String workspace_name,
                                           @PathVariable String dataset_name,
                                           @PathVariable String datablock_id,
                                           @RequestParam(defaultValue = "", required = false) String sw_coord,
                                           @RequestParam(defaultValue = "", required = false) String ne_coord) {

        logger.info("getDatablock invoked");
        return datablockService
                .getDatablockData(workspace_name, dataset_name, Integer.parseInt(datablock_id), sw_coord, ne_coord)
                .switchIfEmpty(Mono.error(new ElementNotFound()));

    }


    /**
     * Retrieve the data associated to a datablock
     *
     * @param workspace_name name of the workspace
     * @param dataset_name   name of the dataset
     * @param datablock_id   id of the datablock
     * @param sw_coord       south west coordinate of the geoquery
     * @param ne_coord       north east coordinate of the geoquery
     * @return If the operation is successful, will return the data of the point associated to the datablock.
     * If no workspace, dataset or datablock was found, an error handler will manage the request.
     */
    @ResponseStatus(code = HttpStatus.OK)
    @GetMapping(value = "{workspace_name}/datasets/{dataset_name}/datablocks/{datablock_id}/data")
    public Flux<DataBuffer> getDatablockData(@PathVariable String workspace_name,
                                             @PathVariable String dataset_name,
                                             @PathVariable int datablock_id,
                                             @RequestParam String sw_coord,
                                             @RequestParam String ne_coord) {

        logger.info("getDatablockData invoked");

        return datablockServiceCommonUtils.dataBlockExists(workspace_name, dataset_name, datablock_id, sw_coord, ne_coord)
                .flatMapMany(aBoolean -> {
                    if (!aBoolean)
                        return Mono.error(ElementNotFound::new);
                    else
                        return datablockService
                                .getDatablockFile(workspace_name, dataset_name, datablock_id, sw_coord, ne_coord)
                                .doOnComplete(() -> {
                                    logger.info("Served file: " + workspace_name + "_" + dataset_name + "_" + datablock_id);
                                });
                });

    }


    /**
     * Insert a point cloud to a dataset
     *
     * @param workspace_name name of the workspace
     * @param dataset_name   name of the dataset
     * @param files          files that compose the data of the point cloud
     * @return Return code and informative message with the result of the operation
     */
    @PutMapping(value = "{workspace_name}/datasets/{dataset_name}/data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity> addPointCloudToDataset(@PathVariable String workspace_name,
                                                       @PathVariable String dataset_name,
                                                       @RequestPart("files") Flux<FilePart> files) {

        logger.info("addPointCloudToDataset invoked");
        Mono<Boolean> datasetExists = datasetService.datasetExists(workspace_name, dataset_name);
        Mono<Boolean> hasData = datasetService.datasetHasDataAssociated(workspace_name, dataset_name);


        return datasetExists.flatMap(datasetExistsboolean -> {
            if (datasetExistsboolean) { //Check if a dataset exists
                return hasData.flatMap(booleanHasData -> {
                    if (booleanHasData) { //Check if the dataset doesn't already have data associated
                        return Mono.error(new DatasetHasDataAssociated());
                    } else {
                        return datablockService
                                .addDataToDataset(workspace_name, dataset_name, files)
                                .map(aBoolean -> ResponseEntity.status(HttpStatus.OK).body(messageAddedData));
                    }
                });
            } else {
                return Mono.error(new ElementNotFound());
            }
        });

    }

    /**
     * Retrieve the complete point cloud associated to a dataset
     *
     * @param workspace_name name of the workspace
     * @param dataset_name   name of the dataset
     * @return File with the complete point cloud
     */
    @ResponseStatus(code = HttpStatus.OK)
    @GetMapping(value = "{workspace_name}/datasets/{dataset_name}/data")
    public Flux<DataBuffer> getCompleteDataset(@PathVariable String workspace_name,
                                               @PathVariable String dataset_name) {

        logger.info("getCompleteDataset invoked");

        Mono<Boolean> datasetExists = datasetService.datasetExists(workspace_name, dataset_name);
        return datasetExists.flatMapMany(aBoolean ->
                aBoolean ? datablockService.getCompleteDataset(workspace_name, dataset_name) : Mono.error(new ElementNotFound()));
    }


    /**
     * Get the datablocks of a dataset that belong to a spa
     * @param workspace_name name of the workspace
     * @param dataset_name name of the dataset
     * @param sw_coord minimum coordinate
     * @param ne_coord maximum coordinate
     * @return Fkyx of datablocks
     */
    @ResponseStatus(code = HttpStatus.OK)
    @GetMapping(value = "{workspace_name}/datasets/{dataset_name}/datablocks")
    public Flux<DatablockDTO> getDatablocksByRegion(@PathVariable String workspace_name,
                                                    @PathVariable String dataset_name,
                                                    @RequestParam String sw_coord,
                                                    @RequestParam String ne_coord) {

        logger.info("getDatablocksByRegion invoked");
        Mono<Boolean> datasetExists = datasetService.datasetExists(workspace_name, dataset_name);

        return datasetExists.flatMapMany(aBoolean ->
                aBoolean ? datablockService.getDatablocksByRegion(workspace_name, dataset_name, sw_coord, ne_coord)
                        : Mono.error(new ElementNotFound()));

    }


    @ResponseStatus(code = HttpStatus.OK)
    @GetMapping(value = "{workspace_name}/datasets/{dataset_name}/datablocks/data")
    public Flux<DataBuffer> getFilesByRegion(@PathVariable String workspace_name,
                                             @PathVariable String dataset_name,
                                             @RequestParam String sw_coord,
                                             @RequestParam String ne_coord,
                                             @RequestParam(defaultValue = "true") Boolean merged) {
        logger.info("getFilesByRegion invoked");

        Mono<Boolean> datasetExists = datasetService.datasetExists(workspace_name, dataset_name);

        return datasetExists.flatMapMany(aBoolean ->
                aBoolean ? datablockService.
                        getFilesByRegion(workspace_name, dataset_name, sw_coord, ne_coord, merged)
                        : Mono.error(new ElementNotFound()));

    }



}
