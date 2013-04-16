package com.flowdock.jenkins;

import java.util.List;
import java.util.ArrayList;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
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
    protected String fromName;

    public TeamInboxMessage() {
        this.source = "Jenkins";
        this.fromAddress = FLOWDOCK_BUILD_OK_EMAIL;
        this.fromName = "CI";
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

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String asPostData() throws UnsupportedEncodingException {
        StringBuffer postData = new StringBuffer();
        postData.append("subject=").append(urlEncode(subject));
        postData.append("&content=").append(urlEncode(content));
        postData.append("&from_address=").append(urlEncode(fromAddress));
        postData.append("&from_name=").append(urlEncode(fromName));
        postData.append("&source=").append(urlEncode(source));
        postData.append("&project=").append(urlEncode(project));
        postData.append("&link=").append(urlEncode(link));
        postData.append("&tags=").append(urlEncode(tags));
        return postData.toString();
    }

    public static TeamInboxMessage fromBuild(AbstractBuild build, BuildResult buildResult) {
        TeamInboxMessage msg = new TeamInboxMessage();
        msg.setProject(build.getProject().getName().replaceAll("[^a-zA-Z0-9\\-_ ]", ""));
        String buildNo = build.getDisplayName().replaceAll("#", "");
        msg.setSubject(build.getProject().getName() + " build " + buildNo + " " + buildResult.getHumanResult());

        String rootUrl = Hudson.getInstance().getRootUrl();
        String buildLink = (rootUrl == null) ? null : rootUrl + build.getUrl();
        if(buildLink != null) msg.setLink(buildLink);

        if(build.getResult().isWorseThan(Result.SUCCESS))
            msg.setFromAddress(FLOWDOCK_BUILD_FAIL_EMAIL);

        StringBuffer content = new StringBuffer();
        content.append("<h2>").append(build.getProject().getName()).append("</h2>");
        content.append("Build: ").append(build.getDisplayName()).append("<br />");
        content.append("Result: ").append(buildResult.toString()).append("<br />");
        if(buildLink != null)
            content.append("URL: <a href=\"").append(buildLink).append("\">").append(buildLink).append("</a>").append("<br />");

        List<Entry> commits = parseCommits(build);
        if(commits != null) {
            content.append("<h3>Changes</h3><div class=\"commits\"><ul class=\"commit-list clean\">");
            for(Entry commit : commits) {
                content.append("<li class=\"commit\"><span class=\"commit-details\">");
                content.append("<span class=\"author-info\">").
                    append("<span>").append(commit.getAuthor()).append("</span>").
                append("</span> &nbsp;");
                content.append("<span title=\"" + commitId(commit) + "\" class=\"commit-sha\">").
                    append(commitId(commit, 7)).
                append("</span> &nbsp;");
                content.append("<span class=\"commit-message\">").append(commit.getMsg()).append("</span>");
                content.append("</span></li>");
            }
            content.append("</ul></div>");
        }

        msg.setContent(content.toString());

        return msg;
    }

    public static List<Entry> parseCommits(AbstractBuild build) {
        final ChangeLogSet<? extends Entry> cs = build.getChangeSet();
        if(cs == null || cs.isEmptySet())
            return null;

        List<Entry> commits = new ArrayList();
        for (final Entry entry : cs) {
            // reverse order in order to have recent commits first
            commits.add(0, entry);
        }
        return commits;
    }

    private static String commitId(Entry commit) {
      String id = commit.getCommitId();
      if (id == null) {
        return "unknown";
      } else {
        return id;
      }
    }

    private static String commitId(Entry commit, int length) {
      String id = commitId(commit);
      return id.substring(0, Math.max(length, id.length()));
    }
}