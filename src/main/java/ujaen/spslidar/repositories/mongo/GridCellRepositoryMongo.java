package ujaen.spslidar.repositories.mongo;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ujaen.spslidar.DTOs.database.mongo.GridCellDBDTO;
import ujaen.spslidar.Exceptions.DifferentUTMZone;
import ujaen.spslidar.entities.GeorefBox;
import ujaen.spslidar.entities.GridCell;
import ujaen.spslidar.repositories.CollectionsManager;
import ujaen.spslidar.repositories.GridCellRepositoryInterface;

@Repository
public class GridCellRepositoryMongo implements GridCellRepositoryInterface {

    private static final String collectionExtension = "_grid";
    private ReactiveMongoTemplate reactiveMongoTemplate;

    public GridCellRepositoryMongo(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<GridCell> save(String workspaceName, GridCell gridCell) {
        String collection = CollectionsManager.cleanCollectionName(workspaceName) + collectionExtension;

        GridCellDBDTO gridCellDBDTO = new GridCellDBDTO(gridCell);
        return reactiveMongoTemplate.save(gridCellDBDTO, collection)
                .map(GridCellDBDTO::gridFromDTO);
    }

    @Override
    public Mono<Boolean> removeDatasetFromGridCell(String workspaceName, String datasetName, GeorefBox gridCellGeorefBox) {
        String collection = CollectionsManager.cleanCollectionName(workspaceName) + collectionExtension;
        Update update = new Update();
        update.pull("datasets", datasetName);

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(gridCellGeorefBox));
        return reactiveMongoTemplate.updateFirst(query, update, collection)
                .map(UpdateResult::wasAcknowledged);

    }

    public Mono<GridCell> findById(String workspaceName, GeorefBox box) {
        String collection = CollectionsManager.cleanCollectionName(workspaceName) + collectionExtension;

        if(!box.getNorthEastTop().getZone().equals(box.getSouthWestBottom().getZone())){
            return Mono.error(DifferentUTMZone::new);
        }

        return reactiveMongoTemplate.findById(box, GridCellDBDTO.class, collection)
                .map(GridCellDBDTO::gridFromDTO);
    }


    public Flux<String> findDatasetsByGrid(String workspaceName, GridCell gridCell) {
        String collection = CollectionsManager.cleanCollectionName(workspaceName) + collectionExtension;

        return reactiveMongoTemplate.findById(gridCell.getGridGeorefBox(), GridCellDBDTO.class, collection)
                .flatMapMany(grid1 -> Flux.fromIterable(grid1.getDatasets()));

    }

    @Override
    public Flux<String> findDatasetsByGeorefBox(String workspaceName, GeorefBox georefBox) {
        String collection = CollectionsManager.cleanCollectionName(workspaceName) + collectionExtension;

        if (!georefBox.getSouthWestBottom().getZone().equals(georefBox.getNorthEastTop().getZone())) {
            return Flux.error(new DifferentUTMZone());
        }

        Query query = new Query();

        query.addCriteria(Criteria.where("UTMZone").is(georefBox.getSouthWestBottom().getZone()));
        //West-East check
        query.addCriteria(Criteria.where("_id.southWestBottom.easting").lt(georefBox.getNorthEastTop().getEasting()));
        query.addCriteria(Criteria.where("_id.northEastTop.easting").gte(georefBox.getSouthWestBottom().getEasting()));
        //South-North check
        query.addCriteria(Criteria.where("_id.southWestBottom.northing").lt(georefBox.getNorthEastTop().getNorthing()));
        query.addCriteria(Criteria.where("_id.northEastTop.northing").gte(georefBox.getSouthWestBottom().getNorthing()));


        return reactiveMongoTemplate.find(query, GridCellDBDTO.class, collection)
                .flatMap(gridCellDBDTO -> Flux.fromIterable(gridCellDBDTO.getDatasets()));
    }


}
