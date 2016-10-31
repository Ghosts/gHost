package Phantom;

import gHost.Repository;

class PhantomDynamics implements Repository {
    String graveClean(Object grave){
        if(grave instanceof String) {
            grave = grave.toString().replaceAll("``", "");
            if (graves.get(grave) != null) {
                return (String) graves.get(grave);
            }
        }
        return "";
    }
}
