package com.flowdock.jenkins;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.AbstractProject;
import hudson.tasks.test.AbstractTestResultAction;
import java.io.UnsupportedEncodingException;

public class TeamInboxMessage extends FlowdockMessage {
  protected String source;
  protected String subject;
  protected String link;
  protected String fromAddress;

  public TeamInboxMessage() {
    this.source = "Jenkins CI";
    this.fromAddress = "cijoe.build+ok@gmail.com";
  }

  public void setSource(String source) {
    this.source = source;
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
    postData.append("&link=").append(urlEncode(link));
    postData.append("&tags=").append(urlEncode(tags));
    return postData.toString();
  }

  public static TeamInboxMessage fromBuild(AbstractBuild build) {
    TeamInboxMessage msg = new TeamInboxMessage();
    msg.setSubject("Build: " + build.getProject().getName() + " " + build.getDisplayName() + " is " + build.getResult().toString());

    String rootUrl = Hudson.getInstance().getRootUrl();
    String buildLink = (rootUrl == null) ? null : rootUrl + build.getUrl();
    if(buildLink != null) msg.setLink(buildLink);

    if(build.getResult().isWorseThan(Result.SUCCESS))
      msg.setFromAddress("cijoe.build+failed@gmail.com");

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