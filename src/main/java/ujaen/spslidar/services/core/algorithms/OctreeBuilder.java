package ujaen.spslidar.services.core.algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ujaen.spslidar.entities.Datablock;
import ujaen.spslidar.services.tools.LasToolsService;
import ujaen.spslidar.services.tools.SystemFileStorageService;
import ujaen.spslidar.utils.properties.OctreeProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@Component
public class OctreeBuilder implements OctreeBuilderInterface {

    Logger logger = LoggerFactory.getLogger(OctreeBuilder.class);

    LasToolsService lasToolsService;
    SystemFileStorageService systemFileStorageService;
    OctreeProperties octreeProperties;

    Sampler sampler;

    public OctreeBuilder(LasToolsService lasToolsService, SystemFileStorageService systemFileStorageService, OctreeProperties octreeProperties) {
        this.lasToolsService = lasToolsService;
        this.systemFileStorageService = systemFileStorageService;
        this.octreeProperties = octreeProperties;
        setSampler();
    }

    private void setSampler() {
        if (octreeProperties.getProgram().equals("custom")) {
            sampler = (datablock, maxDatablockSize) -> lasToolsService.sampleDataWithCustomLASsampler(datablock, maxDatablockSize);
        } else if (octreeProperties.getProgram().equals("lastools")) {
            sampler = (datablock, maxDatablockSize) -> lasToolsService.sampleDataFromFileWithKeepNth(datablock, maxDatablockSize);
        }

    }


    @Override
    public Flux<Datablock> octreeBuildingAlgorithm(Datablock datablock, int dataBlockSize) {
        Flux<Datablock> datablockFlux = Flux.empty();
        if (datablock.getDepth() == octreeProperties.getMaxDepth()) {
            logger.info("Max depth touched, no children incoming from " + datablock.getId());
            Mono<Datablock> datablockMono = lasToolsService.convertMaxDepthFileToBDReady(datablock);
            return datablockFlux.concatWith(datablockMono);

        } else {

            return sampler.sampleFile(datablock, dataBlockSize)
                    .flatMapMany(dblock -> {
                        if (!dblock.getTmpOpsFile().isEmpty()) {
                            List<Datablock> children = dblock.createSubRegions();

                            return datablockFlux
                                    .concatWith(Flux.fromIterable(children)
                                            .flatMap(child -> lasToolsService.createChildNode(dblock.getTmpOpsFile(), child)
                                                    .filter(childCheck -> Files.exists(Path.of(childCheck.getTmpOpsFile())))
                                                    .doOnNext(childDatablock -> dblock.getChildren().add(childDatablock.getId()))
                                                    .flatMapMany(childRecursive ->
                                                            octreeBuildingAlgorithm(childRecursive, dataBlockSize)
                                                    )))
                                    .doOnComplete(() -> systemFileStorageService.deleteFiles(dblock.getTmpOpsFile()))
                                    .concatWith(Mono.just(dblock));
                        } else {
                            return datablockFlux.concatWith(Mono.just(dblock));
                        }
                    });
        }
    }


    @Override
    public Flux<Datablock> octreeBuildingWithDistribution(Datablock datablock, List<Integer> sizes) {
        Integer dataBlockSize = sizes.get(datablock.getDepth());
        Flux<Datablock> datablockFlux = Flux.empty();

        if (datablock.getDepth() == octreeProperties.getMaxDepth()) {
            logger.info("Max depth touched, no children incoming from " + datablock.getId());
            Mono<Datablock> datablockMono = lasToolsService.convertMaxDepthFileToBDReady(datablock);
            return datablockFlux.concatWith(datablockMono);

        } else {
            return lasToolsService.sampleDataFromFileWithKeepNth(datablock, dataBlockSize)
                    .flatMapMany(dblock -> {
                        if (dblock.getTmpOpsFile() != "") {
                            List<Datablock> children = dblock.createSubRegions();

                            return datablockFlux
                                    .concatWith(Flux.fromIterable(children)
                                            .flatMap(child -> lasToolsService.createChildNode(dblock.getTmpOpsFile(), child)
                                                    .filter(childCheck -> Files.exists(Path.of(childCheck.getTmpOpsFile())))
                                                    .doOnNext(childDatablock -> dblock.getChildren().add(childDatablock.getId()))
                                                    .flatMapMany(childRecursive -> octreeBuildingWithDistribution(childRecursive, sizes)
                                                    )))
                                    .doOnComplete(() -> systemFileStorageService.deleteFiles(dblock.getTmpOpsFile()))
                                    .concatWith(Mono.just(dblock));

                        } else {
                            return datablockFlux.concatWith(Mono.just(dblock));
                        }
                    });
        }
    }


}
