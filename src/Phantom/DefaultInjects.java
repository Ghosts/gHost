package Phantom;

import gHost.Repository;
import gHost.Server;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
* Phantom Injects serve as replaceable and dynamic content created through HTML
* documents. Below are the default injects that are provided with Phantom. Over time
* these will grow and - hopefully - become more useful.
* */
public class DefaultInjects implements Repository {

    /* Loads default injects to be used by HTML. */
    public DefaultInjects() {
        addDefaults();
    }

    /* Default out-of-the-box Phantom Injects */
    private void addDefaults() {
        /* Phantom for Current Date */
        Date currentDate = new Date();
        SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy");
        String dateInject = date.format(currentDate);
        defaultInjects.put("<%Date%>", dateInject);

        /* Phantom for Current Time */
        SimpleDateFormat time12 = new SimpleDateFormat("hh:mm a");
        String time12Inject = time12.format(currentDate);
        SimpleDateFormat time24 = new SimpleDateFormat("HH:mm");
        String tim24Inject = time24.format(currentDate);
        defaultInjects.put("<%Time12%>", time12Inject);
        defaultInjects.put("<%Time24%>", tim24Inject);

        /* Phantom for user IP */
        defaultInjects.put("<%IP%>", (((InetSocketAddress) Server.client.getRemoteSocketAddress()).getAddress()).toString().replace("/", ""));

        /* Phantom for Fragment Inject */
        defaultInjects.put("<%Fragment%>", "");
        /* Phantom for Dynamic Element */
        defaultInjects.put("<%Dynamic%>","");

        /* Phantom graves Checker */
        defaultInjects.put("``", "");
    }
}
