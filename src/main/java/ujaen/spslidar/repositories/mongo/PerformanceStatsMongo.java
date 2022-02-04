package ujaen.spslidar.repositories.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ujaen.spslidar.DTOs.database.mongo.DatablockDBDTO;
import ujaen.spslidar.repositories.CollectionsManager;
import ujaen.spslidar.repositories.PerformanceStatsServiceInterface;

/**
 * Class that collects different metrics from Mongo Database
 */
@Service
public class PerformanceStatsMongo implements PerformanceStatsServiceInterface {

    private static final String collectionExtension = "_datablocks";

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<Long> getOctreeSize(String workspace, String dataset) {
        String collection = CollectionsManager.cleanCollectionName(workspace) + collectionExtension;
        Query query = new Query();
        query.addCriteria(Criteria.where("datasetName").is(dataset));

        return reactiveMongoTemplate.find(query, DatablockDBDTO.class, collection).count();
    }

    @Override
    public Mono<Integer> getMaxDepth(String workspace, String dataset) {
        String collection = CollectionsManager.cleanCollectionName(workspace) + collectionExtension;
        Query query = new Query();
        query.addCriteria(Criteria.where("datasetName").is(dataset));

        return reactiveMongoTemplate.find(query, DatablockDBDTO.class, collection)
                .map(DatablockDBDTO::getDepth)
                .reduce(((integer, integer2) -> {
                    if (integer > integer2) {
                        return integer;
                    } else {
                        return integer2;
                    }
                }));
    }

    @Override
    public Mono<String> databaseSize() {
        String jsonCommand = "{dbStats:1, scale:1048576}"; //Scale : 1024*1024 (Bytes -> MBs)

        return reactiveMongoTemplate
                .executeCommand(jsonCommand)
                .map(document -> {
                    String result =  "Storage size: " + String.valueOf(document.get("storageSize"))
                                    + " - Data size: " + String.valueOf(document.get("dataSize"));
                    return result;
                        }
                );

    }
}
