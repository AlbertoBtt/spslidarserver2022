package ujaen.spslidar.repositories;

import org.springframework.stereotype.Service;

/**
 * Class to encapsulate methods related to the construction of collections
 */
@Service
public class CollectionsManager {

    public static String cleanCollectionName(String workspaceName){
        String cleanedWorkspaceName = workspaceName
                .replaceAll("\\$", "")
                .replaceAll("\\.","");

        return cleanedWorkspaceName;
    }

}
