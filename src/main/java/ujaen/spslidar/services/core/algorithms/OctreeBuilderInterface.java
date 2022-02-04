package ujaen.spslidar.services.core.algorithms;

import reactor.core.publisher.Flux;
import ujaen.spslidar.entities.Datablock;

import java.util.List;

public interface OctreeBuilderInterface {

    Flux<Datablock> octreeBuildingAlgorithm(Datablock datablock, int dataBlockSize);

    Flux<Datablock> octreeBuildingWithDistribution(Datablock datablock, List<Integer> sizes);
}
