package ujaen.spslidar.services.core.algorithms;

import reactor.core.publisher.Mono;
import ujaen.spslidar.entities.Datablock;

public interface Sampler {

    Mono<Datablock> sampleFile(Datablock datablock, int maxDatablockSize);
}
