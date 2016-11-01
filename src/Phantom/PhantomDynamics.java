package Phantom;

import gHost.Repository;
import java.util.List;
import java.util.Map;

/*
* Phantom Dynamics handles complex grave variables which may require more complicated
* operations than a simple injection. These include iterative lists, user interaction,
* and variables requiring access to java methods / integrations.
* */
class PhantomDynamics implements Repository {
    private String grave = "";
    Object graveClean(String grave){
        this.grave = grave;
        this.grave = grave.replaceAll("``", "");
        return graveIdentify();
    }

    /* Identify and return correct grave operation. Empty string if invalid. */
    private String graveIdentify(){
        if(graves.get(grave) != null){
            Object graveIdentified = graves.get(grave);
            if (graveIdentified instanceof String){
                return graveString();
            }
            if (graveIdentified instanceof List){
                return graveList();
            }
            if (graveIdentified instanceof Map){
                return graveMap();
            }
            /* Final catch for unknown object operations. */
            return "";
        }
        /* Remove grave variable if it is not defined. */
        else {
            return "";
        }
    }

    /* Return the string value of a grave variable. Empty string if the grace does not exist. */
    private String graveString(){
            return (String) graves.get(grave);
    }

    private String graveList(){
        String graveResult = "";
        for (Object o : (List) graves.get(grave)) {
            graveResult += "<br/>" + o.toString();
        }
        return graveResult;
    }

    private String graveMap(){
        String graveResult = "";
        Map<Object,Object> graveIdentified = (Map<Object,Object>) graves.get(grave);
        for (Map.Entry<Object,Object> e : graveIdentified.entrySet()){
            graveResult += "<br/>" + e.getKey() + " : " + e.getValue();
        }
        return graveResult;
    }
}
