package ujaen.spslidar.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;
import ujaen.spslidar.Exceptions.*;

/**
 * Class that intercepts defined exceptions and generates a ResponseEntity
 * with a specific status code and error message.
 */
@ControllerAdvice
public class ExceptionsHandler{

    @ExceptionHandler(WorkspaceExistsException.class)
    public Mono<ResponseEntity> workspaceAlreadyExistsExceptionHandler() {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(WorkspaceExistsException.getMsg()));
    }

    @ExceptionHandler(WorkspaceNotFoundException.class)
    public Mono<ResponseEntity> notFoundHandlerException() {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(WorkspaceNotFoundException.getMsg()));
    }

    @ExceptionHandler(ElementNotFound.class)
    public Mono<ResponseEntity> elementNotFoundHandlerException() {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ElementNotFound.getMsg()));
    }

    @ExceptionHandler(DatasetAlreadyExists.class)
    public Mono<ResponseEntity> datasetAlreadyExists() {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(DatasetAlreadyExists.getMsg()));
    }

    @ExceptionHandler(DatasetHasDataAssociated.class)
    public Mono<ResponseEntity> datasetHasData() {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(DatasetHasDataAssociated.getMsg()));
    }

    @ExceptionHandler(NoUTMZoneInFile.class)
    public Mono<ResponseEntity> noUTMZoneInFile() {
        return Mono.just(ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(NoUTMZoneInFile.getMsg()));
    }

    @ExceptionHandler(UncompletedCoordinatesInQueryException.class)
    public Mono<ResponseEntity> uncompletedCoordinatesInQuery() {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(UncompletedCoordinatesInQueryException.getMsg()));
    }

    @ExceptionHandler(DifferentUTMZone.class)
    public Mono<ResponseEntity> differentUTMZones() {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(DifferentUTMZone.getMsg()));
    }

    @ExceptionHandler(BuildingOctreeException.class)
    public Mono<ResponseEntity> errorBuildingOctree() {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(BuildingOctreeException.getMsg()));
    }

    @ExceptionHandler(DatablockLockedException.class)
    public Mono<ResponseEntity> lockedDatablockException(){
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(DatablockLockedException.getMsg()));
    }

    @ExceptionHandler(InvalidCoordinateException.class)
    public Mono<ResponseEntity> invalidCoordinateException(){
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(InvalidCoordinateException.getMsg()));
    }

}
