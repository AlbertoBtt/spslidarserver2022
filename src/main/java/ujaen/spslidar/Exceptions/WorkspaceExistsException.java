/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ujaen.spslidar.Exceptions;

import lombok.Getter;

public class WorkspaceExistsException extends RuntimeException {

    @Getter
    private static String msg = "Workspace already exists";

    public WorkspaceExistsException() {
        super(msg);
    }

}
