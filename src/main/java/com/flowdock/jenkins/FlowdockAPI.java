package com.flowdock.jenkins;

import com.flowdock.jenkins.exception.FlowdockException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.MalformedURLException;

public class FlowdockAPI {
    private String apiUrl;
    private String flowToken;

    public FlowdockAPI(String apiUrl, String flowToken) {
        this.apiUrl = apiUrl;
        this.flowToken = trimFlowTokens(flowToken);
    }

    public void pushTeamInboxMessage(TeamInboxMessage msg) throws FlowdockException {
        try {
            doPost("/messages/team_inbox/", msg.asPostData());
        } catch(UnsupportedEncodingException ex) {
            throw new FlowdockException("Cannot encode request data: " + ex.getMessage());
        }
    }

    public void pushChatMessage(ChatMessage msg) throws FlowdockException {
        try {
            doPost("/messages/chat/", msg.asPostData());
        } catch(UnsupportedEncodingException ex) {
            throw new FlowdockException("Cannot encode request data: " + ex.getMessage());
        }
    }

    private void doPost(String path, String data) throws FlowdockException {
        URL url;
        HttpURLConnection connection = null;
        String flowdockUrl = apiUrl + path + flowToken;
        try {
            // create connection
            url = new URL(flowdockUrl);
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

            if(connection.getResponseCode() != 200) {
                StringBuffer responseContent = new StringBuffer();
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    String responseLine;
                    while ((responseLine = in.readLine()) != null) {
                        responseContent.append(responseLine);
                    }
                    in.close();
                } catch(Exception ex) {
                    // nothing we can do about this
                } finally {
                    throw new FlowdockException("Flowdock returned an error response with status " +
                    connection.getResponseCode() + " " + connection.getResponseMessage() + ", " +
                    responseContent.toString() + "\n\nURL: " + flowdockUrl);
                }
            }
        } catch(MalformedURLException ex) {
            throw new FlowdockException("Flowdock API URL is invalid: " + flowdockUrl);
        } catch(ProtocolException ex) {
            throw new FlowdockException("ProtocolException in connecting to Flowdock: " + ex.getMessage());
        } catch(IOException ex) {
            throw new FlowdockException("IOException in connecting to Flowdock: " + ex.getMessage());
        }
    }

    public static String trimFlowTokens(String flowTokens) {
        return flowTokens.replaceAll("\\s", "");
    }
}
