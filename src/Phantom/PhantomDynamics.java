package Phantom;

import gHost.Repository;

import java.lang.reflect.Array;

/*
* Phantom Dynamics handles complex grave variables which may require more complicated
* operations than a simple injection. These include iterative lists, user interaction,
* and variables requiring access to java methods / integrations.
* */
class PhantomDynamics implements Repository {
    private String grave = "";
    public String graveClean(String grave){
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
            if (graveIdentified instanceof Iterable){
                return graveIterate();
            }
            if (graveIdentified instanceof Number){
                return graveNumber();
            }
            if (graveIdentified instanceof Array[]){
                return graveArray();
            }
            /* If not identified, assume a toString() method exists. */
            return graves.get(grave).toString();
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

    private String graveIterate(){
        Iterable graveIdentified = (Iterable) graves.get(grave);
        final String[] graveIterate = {""};
        graveIdentified.forEach(o -> graveIterate[0] += o.toString()+ ", ");
        return graveIterate[0];
    }

    private String graveNumber() {
        return graves.get(grave).toString();
    }

    private String graveArray() {
        String graveResult = "";
        for (int i = 0; i < ((Array[]) graves.get(grave)).length - 1 ; i++) {
            graveResult += ((Array[]) graves.get(grave))[i].toString() + " ,";
        }
        return graveResult;
    }
}
