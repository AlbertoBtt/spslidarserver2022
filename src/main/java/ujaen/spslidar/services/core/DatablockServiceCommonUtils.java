package ujaen.spslidar.services.core;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ujaen.spslidar.DTOs.http.DatablockDTO;
import ujaen.spslidar.Exceptions.DifferentUTMZone;
import ujaen.spslidar.entities.AbstractDatablock;
import ujaen.spslidar.entities.Datablock;
import ujaen.spslidar.entities.GeorefBox;
import ujaen.spslidar.repositories.DatablockRepositoryInterface;
import ujaen.spslidar.repositories.DatasetRepositoryInterface;
import ujaen.spslidar.repositories.FileRepositoryInterface;
import ujaen.spslidar.repositories.mongo.GridFileStorageService;
import ujaen.spslidar.services.tools.LasToolsService;
import ujaen.spslidar.services.tools.SystemFileStorageService;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;


/**
 * Class with methods that may be used by more than one implementation of the Datablock Service
 */
@Service
public class DatablockServiceCommonUtils {

    Logger logger = LoggerFactory.getLogger(DatablockServiceCommonUtils.class);

    private DatasetRepositoryInterface datasetRepositoryInterface;
    private DatablockRepositoryInterface datablockRepositoryInterface;
    private FileRepositoryInterface fileRepositoryInterface;
    private SystemFileStorageService systemFileStorageService;
    private LasToolsService lasToolsService;

    public DatablockServiceCommonUtils(DatasetRepositoryInterface datasetRepositoryInterface,
                                       DatablockRepositoryInterface datablockRepositoryInterface,
                                       FileRepositoryInterface fileRepositoryInterface,
                                       SystemFileStorageService systemFileStorageService,
                                       LasToolsService lasToolsService) {

        this.datablockRepositoryInterface = datablockRepositoryInterface;
        this.fileRepositoryInterface = fileRepositoryInterface;
        this.systemFileStorageService = systemFileStorageService;
        this.lasToolsService = lasToolsService;
        this.datasetRepositoryInterface = datasetRepositoryInterface;
    }

    /**
     * Checks if a datablock exists in the database
     *
     * @param workspaceName
     * @param datasetName
     * @param id
     * @return
     */
    public Mono<Boolean> dataBlockExists(String workspaceName, String datasetName, int id, String southWest, String northEast) {
        GeorefBox grid = new GeorefBox(southWest, northEast);

        return datablockRepositoryInterface.existsByWorkspaceAndDatasetAndNodeAndGridCell(workspaceName, datasetName, id, grid);
    }

    /**
     * Get metadata of a datablock. If no coordinates are specified, it will return all the datablocks of the dataset
     * that share the same node identifier. For instance, if the dataset overlaps four cells of the grid, four octrees
     * will be built and therefore, there will be for datablocks node identifier equal to 0.
     * If coordinates are specified, it will return the specific datablock of the octree that correltaes to those coordinates
     *
     * @param workspaceName
     * @param datasetName
     * @param id
     * @param southWest
     * @param northEast
     * @return
     */
    public Flux<DatablockDTO> getDatablockData(String workspaceName, String datasetName, int id, String southWest, String northEast) {

        Flux<AbstractDatablock> datablockFlux;

        if (southWest.equals("") || northEast.equals("")) {
            datablockFlux = datablockRepositoryInterface
                    .findDatablockByWorkspaceAndDatasetAndNode(workspaceName, datasetName, id);
        } else {
            GeorefBox grid = new GeorefBox(southWest, northEast);
            datablockFlux = Flux.from(datablockRepositoryInterface
                    .findDatablockByWorkspaceAndDatasetAndNodeAndGridCell(workspaceName, datasetName, id, grid));
        }

        return datablockFlux.map(DatablockDTO::new);

    }

    /**
     * Get the file associated to a datablock.
     * @param workspaceName
     * @param datasetName
     * @param id
     * @param southWest
     * @param northEast
     * @return
     */
    public Flux<DataBuffer> getDatablockFile(String workspaceName, String datasetName, int id, String southWest, String northEast) {
        GeorefBox grid = new GeorefBox(southWest, northEast);

        if (fileRepositoryInterface instanceof GridFileStorageService) {
            Mono<Datablock> datablock = datablockRepositoryInterface
                    .findDatablockByWorkspaceAndDatasetAndNodeAndGridCell(workspaceName, datasetName, id, grid)
                    .cast(Datablock.class);

            return datablock
                    .map(Datablock::getObjectId)
                    .flatMapMany(objectId -> fileRepositoryInterface.getFile(objectId));

        } else {
            return fileRepositoryInterface.getFile(workspaceName, datasetName, id, grid);
        }
    }


    /**
     * Returns a file with the complete dataset merged in a single file
     * @param workspaceName
     * @param datasetName
     * @return
     */
    public Flux<DataBuffer> getCompleteDataset(String workspaceName, String datasetName) {

        Flux<ObjectId> files = datablockRepositoryInterface
                .findAllDatablocksInDataset(workspaceName, datasetName)
                .cast(Datablock.class)
                .map(Datablock::getObjectId);

        return mergeFiles(files, workspaceName, datasetName);

    }


    /**
     * Returns all the datablocks that overlap the bounding box built with the passed coordinates
     * @param workspaceName
     * @param datasetName
     * @param southWest
     * @param northEast
     * @return
     */
    public Flux<DatablockDTO> getDatablocksByRegion(String workspaceName, String datasetName, String southWest, String northEast) {
        return getOverlappingDatablocks(workspaceName, datasetName, southWest, northEast)
                .map(DatablockDTO::new);

    }

    /**
     * Return all the files that overlap the bounding box built with the passed coordinates
     * @param workspaceName
     * @param datasetName
     * @param southWest
     * @param northEast
     * @param merged
     * @return
     */
    public Flux<DataBuffer> getFilesByRegion(String workspaceName, String datasetName, String southWest, String northEast, Boolean merged) {

        if (!merged)
            return getOverlappingDatablocks(workspaceName, datasetName, southWest, northEast)
                    .map(AbstractDatablock::getObjectId)
                    .flatMap(fileRepositoryInterface::getFile);
        else {
            Flux<ObjectId> dblocksFilesIDs = getOverlappingDatablocks(workspaceName, datasetName, southWest, northEast)
                    .map(AbstractDatablock::getObjectId);
            return this.mergeFiles(dblocksFilesIDs, workspaceName, datasetName);
        }
    }


    /**
     * Returns all the datablocks that fit the spatial query
     *
     * @param workspaceName
     * @param datasetName
     * @param southWest
     * @param northEast
     * @return
     */
    private Flux<AbstractDatablock> getOverlappingDatablocks(String workspaceName, String datasetName, String southWest, String northEast) {

        GeorefBox queryBox = new GeorefBox(southWest, northEast);
        if (!queryBox.getSouthWestBottom().getZone().equals(queryBox.getNorthEastTop().getZone()))
            return Flux.error(new DifferentUTMZone());

        return datasetRepositoryInterface.findByWorkspaceAndDataset(workspaceName, datasetName)
                .flatMapMany(dataset -> {
                    List<GeorefBox> georefBoxList = dataset.getRootDatablocks().getOrDefault(queryBox.getSouthWestBottom().getZone(), new ArrayList<>());
                    return Flux.fromIterable(georefBoxList);
                })
                .filter(georefBox -> georefBox.doesOverlap(queryBox))
                .flatMap(georefBox -> datablockRepositoryInterface.findDatablockByWorkspaceAndDatasetAndNodeAndGridCell(workspaceName, datasetName, 0, georefBox))
                .flatMap(datablock -> checkOverlappingChildren(datablock, workspaceName, datasetName, queryBox));
    }

    /**
     * Recursive method to explore the octree and recover overlapping datablocks.
     * It will build a Flux that will recursively add the children datablocks of the branches of the octree
     * that overlap with the bounding box.
     *
     * @param datablock
     * @param workspaceName
     * @param datasetName
     * @param queryBox
     * @return
     */
    private Flux<AbstractDatablock> checkOverlappingChildren(AbstractDatablock datablock, String workspaceName, String datasetName, GeorefBox queryBox) {

        Flux<AbstractDatablock> abstractDatablockFlux = Flux.empty();

        return abstractDatablockFlux
                .concatWith(Flux.fromIterable(datablock.getChildren())
                        .flatMap(integer -> datablockRepositoryInterface
                                .findDatablockByWorkspaceAndDatasetAndNodeAndGridCell(workspaceName, datasetName, integer, datablock.getUTMZoneLocalGrid()))
                        .filter(_datablock -> queryBox.doesOverlap(_datablock.getGeorefBox()))
                        .flatMap(_datablock -> checkOverlappingChildren(_datablock, workspaceName, datasetName, queryBox))
                ).concatWith(Mono.just(datablock));

    }


    /**
     * Merges a number of LAZ files into a single one.
     * @param files
     * @param workspaceName
     * @param datasetName
     * @return
     */
    private Flux<DataBuffer> mergeFiles(Flux<ObjectId> files, String workspaceName, String datasetName) {
        Path folderToMerge = Path.of(systemFileStorageService.buildMergeDirectory(workspaceName, datasetName));
        Path fileToReturn = folderToMerge.resolve(Path.of("merged.laz"));

        return files
                .flatMap(objectId -> {
                    Path resourceFileName = folderToMerge.resolve(Path.of(objectId.toString() + ".laz"));
                    try {
                        AsynchronousFileChannel asynchronousFileChannel =
                                AsynchronousFileChannel.open(resourceFileName, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

                        return DataBufferUtils.write(fileRepositoryInterface.getFile(objectId), asynchronousFileChannel)
                                .map(DataBufferUtils::release)
                                .then(Mono.just(String.valueOf(resourceFileName)));
                    } catch (IOException ioException) {
                        return Flux.error(new RuntimeException("Couldn't merge files"));
                    }
                })
                .collectList()
                .flatMap(strings -> lasToolsService.mergeFilesReturn(strings, fileToReturn)
                        .map(FileSystemResource::new))
                .flatMapMany(fileSystemResource -> DataBufferUtils
                        .read(fileSystemResource, new DefaultDataBufferFactory(), 256))
                .doAfterTerminate(() -> systemFileStorageService.cleanDirectory(folderToMerge));


    }

}