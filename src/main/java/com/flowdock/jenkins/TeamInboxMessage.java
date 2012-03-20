package com.flowdock.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.AbstractProject;
import hudson.tasks.test.AbstractTestResultAction;
import java.io.UnsupportedEncodingException;

public class TeamInboxMessage extends FlowdockMessage {

    /*
        Default sender email addresses for displaying Gravatar icons in build
        notification based on the build status. You can also setup your own email
        addresses and configure custom Gravatar icons for them.
    */
    public static final String FLOWDOCK_BUILD_OK_EMAIL = "build+ok@flowdock.com";
    public static final String FLOWDOCK_BUILD_FAIL_EMAIL = "build+fail@flowdock.com";

    protected String source;
    protected String project;
    protected String subject;
    protected String link;
    protected String fromAddress;

    public TeamInboxMessage() {
        this.source = "Jenkins CI";
        this.fromAddress = FLOWDOCK_BUILD_OK_EMAIL;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String asPostData() throws UnsupportedEncodingException {
        StringBuffer postData = new StringBuffer();
        postData.append("subject=").append(urlEncode(subject));
        postData.append("&content=").append(urlEncode(content));
        postData.append("&from_address=").append(urlEncode(fromAddress));
        postData.append("&source=").append(urlEncode(source));
        postData.append("&project=").append(urlEncode(project));
        postData.append("&link=").append(urlEncode(link));
        postData.append("&tags=").append(urlEncode(tags));
        return postData.toString();
    }

    public static TeamInboxMessage fromBuild(AbstractBuild build) {
        TeamInboxMessage msg = new TeamInboxMessage();
        msg.setProject(build.getProject().getName());
        msg.setSubject("Build: " + build.getProject().getName() + " " + build.getDisplayName() + " is " + build.getResult().toString());

        String rootUrl = Hudson.getInstance().getRootUrl();
        String buildLink = (rootUrl == null) ? null : rootUrl + build.getUrl();
        if(buildLink != null) msg.setLink(buildLink);

        if(build.getResult().isWorseThan(Result.SUCCESS))
            msg.setFromAddress(FLOWDOCK_BUILD_FAIL_EMAIL);

        StringBuffer content = new StringBuffer();
        content.append("Project: ").append(build.getProject().getName()).append("<br />");
        content.append("Build: ").append(build.getDisplayName()).append("<br />");
        content.append("Result: ").append(build.getResult().toString()).append("<br />");
        if(buildLink != null) {
            content.append("URL: <a href=\"").append(buildLink).append("\">").append(buildLink).append("</a>").append("<br />");
        }
        msg.setContent(content.toString());

        return msg;
    }
}