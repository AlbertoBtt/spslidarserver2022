package ujaen.spslidar.controllers;

import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ujaen.spslidar.repositories.PerformanceStatsServiceInterface;
import ujaen.spslidar.repositories.SchemaManagerInterface;
import ujaen.spslidar.utils.properties.OctreeProperties;

/**
 * Auxiliar controller that exposes some utility methods for experiments
 */
@RestController
@RequestMapping("/spslidar/")
public class AdminController {

    final ReactiveMongoOperations operations;
    final PerformanceStatsServiceInterface performanceStatsServiceInterface;
    final OctreeProperties octreeProperties;
    final SchemaManagerInterface schemaManager;

    public AdminController(ReactiveMongoOperations operations, PerformanceStatsServiceInterface performanceStatsServiceInterface, OctreeProperties octreeProperties, SchemaManagerInterface schemaManager) {
        this.operations = operations;
        this.performanceStatsServiceInterface = performanceStatsServiceInterface;
        this.octreeProperties = octreeProperties;
        this.schemaManager = schemaManager;
    }

    /**
     * Resets the database
     * @return
     */
    @DeleteMapping(value = "database")
    public Mono<ResponseEntity> resetDatabase() {

        return schemaManager.dropSchema()
                .then(schemaManager.buildSchema())
                .thenReturn(ResponseEntity.ok("Reseting database"));
    }

    /**
     * Returns the size of the database
     * @return
     */
    @GetMapping(value = "database")
    public Mono<ResponseEntity> getDatabaseSize() {

        Mono<String> databaseSize = performanceStatsServiceInterface.databaseSize();
        return databaseSize.map(dbSize -> ResponseEntity.status(HttpStatus.OK).body(dbSize));
    }


    /**
     * Returns the maximum depth defined for the octrees that will be built
     * @param workspace_name
     * @param dataset_name
     * @return
     */
    @GetMapping("/workspaces/{workspace_name}/datasets/{dataset_name}/size")
    public Mono<ResponseEntity> getOctreeSize(@PathVariable String workspace_name,
                                              @PathVariable String dataset_name) {

        Mono<Long> longMono = performanceStatsServiceInterface.getOctreeSize(workspace_name, dataset_name);
        return longMono.map(aLong -> ResponseEntity.status(HttpStatus.OK).body(aLong));

    }

    /**
     * Returns the max depth reached for a dataset that has been previously processed
     * @param workspace_name
     * @param dataset_name
     * @return
     */
    @GetMapping("/workspaces/{workspace_name}/datasets/{dataset_name}/depth")
    public Mono<ResponseEntity> getMaxDepth(@PathVariable String workspace_name,
                                            @PathVariable String dataset_name) {

        Mono<Integer> integerMono = performanceStatsServiceInterface.getMaxDepth(workspace_name, dataset_name);
        return integerMono.map(integer -> ResponseEntity.status(HttpStatus.OK).body(integer));

    }


    /**
     * Updates the value of maximum depth for the octree
     * @param size
     * @return
     */
    @PutMapping("octree/{size}")
    public Mono<Integer> updateMaxOctreeSize(@PathVariable Integer size) {
        System.out.println("Previous max octree size value was: " + size);
        octreeProperties.setMaxDepth(size);
        System.out.println("New max octree size value is: " + size);
        return Mono.just(octreeProperties.getMaxDepth());
    }


}
