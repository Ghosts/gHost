package Phantom;


import gHost.Logger.Level;
import gHost.Logger.Logger;
import gHost.Server;

import java.util.Arrays;

/*
* Phantom is a templating engine responsible for dynamic interaction
* with web pages in gHost. Currently Phantom offers minimal features,
* however development is active and looking for improvements and additions.
* */
public class StringUtils {

    /* Parses query information from the requested URL. */
    public static String[] formatQuery(String[] request) {
        if (request[1].contains("?")) {
            String query;
            /* Remove query from URL for it to be passed on to routeFilter */
            String requestReplace = request[1];
            query = requestReplace.replaceAll(".*\\?", "");
            /* Fixes request if query exists */
            request[1] = request[1].substring(0, request[1].length() - query.length() - 1);

            /* format the queries to be used in DataHandler*/
            String[] queries = query.split("&");
            for (int i = 0; i < queries.length; i++) {
                queries[i] = queries[i]
                        .replaceAll(".*=", "")
                        .replaceAll("\\+", " ");
            }
            if (Server.debugMode) {
                Logger.log(Level.INFO, "URL Query(s) Found: " + Arrays.toString(queries));
            }
            return queries;
        }
        /* Return nothing if no queries exist, DataHandler will ignore a blank query */
        return new String[0];
    }


    /* Easily convert content to Title Case, such as for headings. */
    public static String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            titleCase.append(c);
        }
        return titleCase.toString();
    }

    /* Useful for iterative auto-filled content, or any need for appending an ordinal to strings. */
    public static String getOrdinal(int i) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];
        }
    }
}
