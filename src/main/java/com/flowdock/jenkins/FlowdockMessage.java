package com.flowdock.jenkins;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public abstract class FlowdockMessage {
    protected String content;
    protected String tags;

    public void setContent(String content) {
        this.content = content;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public abstract String asPostData() throws UnsupportedEncodingException;

    protected String urlEncode(String data) throws UnsupportedEncodingException {
        return data == null ? "" : URLEncoder.encode(data, "UTF-8");
    }
}