package ujaen.spslidar.services.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ujaen.spslidar.Exceptions.BuildingOctreeException;
import ujaen.spslidar.utils.properties.FileStorageProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service defined to interact with the file storage system
 */
@Service
public class SystemFileStorageService {

    Logger logger = LoggerFactory.getLogger(SystemFileStorageService.class);

    private final Path fileStorageLocation;
    private final Path fileMergeLocation;
    private final String suffix = ".laz";

    /**
     * Creates the folder where the laz files will be stored
     *
     * @param fileStorageProperties location of the folder to be created
     */
    @Autowired
    public SystemFileStorageService(FileStorageProperties fileStorageProperties) {

        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        this.fileMergeLocation = Paths.get(fileStorageProperties.getMergeDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStorageLocation);
            Files.createDirectories(fileMergeLocation);
        } catch (Exception e) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", e);
        }

    }


    public String getBasePath() {
        return this.fileStorageLocation.toString();
    }

    private String buildDirectory(Path baseLocation, String directory) {
        try {
            Path newDirectory = Paths.get(baseLocation.toString(), directory);
            Files.createDirectories(newDirectory);
            return newDirectory.toString();
        } catch (IOException ioException) {
            logger.error(ioException.toString());
            throw new BuildingOctreeException();
        }

    }


    /**
     * Builds a directory
     *
     * @param directory requires to start with \\
     * @return the full path of the new directory
     */
    public String buildDirectory(String directory) {
        return buildDirectory(fileStorageLocation, directory);

    }

    public String buildMergeDirectory(String workspace, String dataset) {
        String mergeFolder = workspace + "_" + dataset;
        return buildDirectory(fileMergeLocation, mergeFolder);
    }


    public String renameFiles(String oldFileName, String newFileName) {
        String newFileCompletePath = FilenameUtils.getFullPath(oldFileName) + newFileName;
        File oldFile = new File(oldFileName);
        File newFile = new File(newFileCompletePath);

        return (oldFile.renameTo(newFile) ? newFileCompletePath : oldFileName);

    }


    public Flux<String> storeMultipleFiles(Flux<FilePart> files, String workspace, String dataset) {
        logger.info("Started storage of files method");
        AtomicInteger fileCounter = new AtomicInteger();
        String folder = workspace + "_" + dataset;

        Path path = Path.of(buildDirectory(fileStorageLocation, folder));

        return files.map(file -> {
            logger.info("Storing file:" + file.filename());
            fileCounter.getAndIncrement();
            return storeSingleFile(file, workspace, dataset, fileCounter.get(), path);
        });

    }


    public String storeSingleFile(FilePart filePart, String workspace,
                                  String dataset, int fileID, Path path) {

        String fileName = workspace + "_" + dataset + "_" + fileID + suffix;

        Path targetLocation = path.resolve(fileName).normalize();

        filePart.transferTo(targetLocation)
                .then().subscribe();

        return targetLocation.toString();
    }


    public String moveFileToDirectory(File file, String baseDirectorySubFolder) {
        String destinationFile = this.fileStorageLocation.resolve(baseDirectorySubFolder + file.getName()).toString();
        try {
            FileUtils.moveFileToDirectory(file, new File(destinationFile), true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return FilenameUtils.getPath(destinationFile);

    }

    public void cleanDirectory(String workspaceName, String datasetName) {

        String folder = workspaceName + "_" + datasetName;
        Path pathToDelete = Paths.get(fileStorageLocation.toString(), folder);
        cleanDirectory(pathToDelete);

    }

    public void cleanDirectory(Path pathToDelete) {
        try {
            FileUtils.deleteDirectory(new File(pathToDelete.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFiles(List<String> files) {
        try {
            for (String s : files) {
                Files.deleteIfExists(Path.of(s));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void deleteFiles(String file) {
        try {
            Files.deleteIfExists(Path.of(file));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


}
