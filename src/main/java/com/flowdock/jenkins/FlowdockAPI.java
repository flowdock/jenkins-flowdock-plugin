package com.flowdock.jenkins;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class FlowdockAPI {
    private String apiUrl;
    private String flowToken;
    private PrintStream logger; // for logging to Jenkins output stream

    public FlowdockAPI(String apiUrl, String flowToken, PrintStream logger) {
        this.apiUrl = apiUrl;
        this.flowToken = flowToken;
        this.logger = logger;
    }

    public boolean pushTeamInboxMessage(TeamInboxMessage msg) {
        try {
            return doPost("/messages/team_inbox/", msg.asPostData());
        } catch(UnsupportedEncodingException ex) {
            logger.println("Flowdock: failed to send notification");
            logger.println(ex);
            return false;
        }
    }

    public boolean pushChatMessage(ChatMessage msg) {
        try {
            return doPost("/messages/chat/", msg.asPostData());
        } catch(UnsupportedEncodingException ex) {
            logger.println("Flowdock: failed to send notification");
            logger.println(ex);
            return false;
        }
    }

    private boolean doPost(String path, String data) {
        URL url;
        HttpURLConnection connection = null;
        try {
            // create connection
            url = new URL(apiUrl + path + flowToken);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // send the request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            if(connection.getResponseCode() == 200) {
                return true;
            } else {
                logger.println("Flowdock: failed to send notification");
                logger.println("Flowdock: response status: " + connection.getResponseCode());
                return false;
            }
        } catch(Exception ex) {
            logger.println("Flowdock: failed to send notification");
            ex.printStackTrace(logger);
            return false;
        }
    }
}
