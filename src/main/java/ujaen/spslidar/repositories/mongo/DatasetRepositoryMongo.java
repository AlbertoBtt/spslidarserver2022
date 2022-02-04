package ujaen.spslidar.repositories.mongo;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ujaen.spslidar.DTOs.database.mongo.DatasetDBDTO;
import ujaen.spslidar.Exceptions.DatasetAlreadyExists;
import ujaen.spslidar.entities.Dataset;
import ujaen.spslidar.entities.GeorefBox;
import ujaen.spslidar.repositories.CollectionsManager;
import ujaen.spslidar.repositories.DatasetRepositoryInterface;

import java.time.LocalDateTime;

@Repository
public class DatasetRepositoryMongo implements DatasetRepositoryInterface {

    private static final String collectionExtension = "_datasets";
    private ReactiveMongoTemplate reactiveMongoTemplate;

    public DatasetRepositoryMongo(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Mono<Boolean> existsByWorkspaceAndDataset(String workspaceName, String datasetName) {
        String collection = getCollectionName(workspaceName);
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(datasetName));

        return reactiveMongoTemplate.exists(query, collection);
    }


    @Override
    public Flux<Dataset> findByWorkspaceNameAndTimeWindow(String workspaceName, LocalDateTime fromDate, LocalDateTime toDate) {
        String collection = getCollectionName(workspaceName);

        Query query = new Query();
        query.addCriteria(Criteria.where("date").gte(fromDate).lte(toDate));

        return reactiveMongoTemplate.find(query, DatasetDBDTO.class, collection)
                .map(DatasetDBDTO::datasetFromDTO)
                .map(dataset -> setWorkspace(dataset, workspaceName));


    }

    @Override
    public Mono<Dataset> findByWorkspaceAndDataset(String workspaceName, String datasetName) {
        String collection = getCollectionName(workspaceName);
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(datasetName));
        return reactiveMongoTemplate.findOne(query, DatasetDBDTO.class, collection)
                .map(DatasetDBDTO::datasetFromDTO)
                .map(dataset -> setWorkspace(dataset, workspaceName))
                .doOnNext(System.out::println);
    }

    @Override
    public Mono<Dataset> save(Dataset dataset) {
        String collection = getCollectionName(dataset.getWorkspaceName());
        DatasetDBDTO datasetDBDTO = new DatasetDBDTO(dataset);

        return reactiveMongoTemplate.insert(datasetDBDTO, collection)
                .onErrorMap(throwable -> {
                    throw new DatasetAlreadyExists();
                })
                .map(DatasetDBDTO::datasetFromDTO)
                .map(m -> setWorkspace(m, dataset.getWorkspaceName()));
    }

    @Override
    public Mono<Dataset> update(Dataset dataset) {
        String collection = getCollectionName(dataset.getWorkspaceName());
        DatasetDBDTO datasetDBDTO = new DatasetDBDTO(dataset);
        return reactiveMongoTemplate
                .save(datasetDBDTO, collection)
                .map(DatasetDBDTO::datasetFromDTO)
                .map(m -> setWorkspace(m, dataset.getWorkspaceName()));
    }

    @Override
    public Mono<Boolean> removeGridCellFromDataset(Dataset dataset, GeorefBox gridCell) {
        String collection = getCollectionName(dataset.getWorkspaceName());

        Update update = new Update();
        update.pull("gridsAssociated." + gridCell.getSouthWestBottom().getZone(),
                Query
                        .query(Criteria.where("southWestBottom.easting").is(gridCell.getSouthWestBottom().getEasting())
                        .and("southWestBottom.northing").is(gridCell.getSouthWestBottom().getNorthing())));

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(dataset.getDatasetName()));

        return reactiveMongoTemplate.updateFirst(query, update, collection)
                .map(UpdateResult::wasAcknowledged);


    }

    @Override
    public Mono<Dataset> findByWorkspaceAndDatasetAndTimeWindow(String workspaceName, String datasetName, LocalDateTime fromDate, LocalDateTime toDate) {
        String collection = getCollectionName(workspaceName);

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(datasetName));
        query.addCriteria(Criteria.where("date").gte(fromDate).lte(toDate));

        return reactiveMongoTemplate.findOne(query, DatasetDBDTO.class, collection)
                .map(DatasetDBDTO::datasetFromDTO)
                .map(m -> setWorkspace(m, workspaceName));
    }

    /**
     * Adds the workspaceName to the dataset as that attribute is not stored in Mongo to avoid redundancy
     * with the collection name
     *
     * @param dataset
     * @param workspaceName
     * @return
     */
    private Dataset setWorkspace(Dataset dataset, String workspaceName) {
        dataset.setWorkspaceName(workspaceName);
        return dataset;
    }


    private String getCollectionName(String workspaceName) {
        return CollectionsManager.cleanCollectionName(workspaceName) + collectionExtension;
    }

}
