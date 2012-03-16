package com.flowdock.jenkins;

import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FlowdockNotifier extends Notifier {

    private final String flowToken;
    private final String notificationTags;
    private final Boolean chatNotification;

    private final Boolean notifySuccess;
    private final Boolean notifyFailure;
    private final Boolean notifyUnstable;
    private final Boolean notifyAborted;
    private final Boolean notifyNotBuilt;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public FlowdockNotifier(String flowToken, String notificationTags, Boolean chatNotification,
      Boolean notifySuccess, Boolean notifyFailure, Boolean notifyUnstable, Boolean notifyAborted, Boolean notifyNotBuilt) {
        this.flowToken = flowToken;
        this.notificationTags = notificationTags;
        this.chatNotification = chatNotification;

        this.notifySuccess = notifySuccess;
        this.notifyFailure = notifyFailure;
        this.notifyUnstable = notifyUnstable;
        this.notifyAborted = notifyAborted;
        this.notifyNotBuilt = notifyNotBuilt;
    }

    public String getFlowToken() {
      return flowToken;
    }

    public String getNotificationTags() {
      return notificationTags;
    }

    public Boolean getChatNotification() {
      return chatNotification;
    }

    public Boolean getNotifySuccess() {
      return notifySuccess;
    }
    public Boolean getNotifyFailure() {
      return notifyFailure;
    }
    public Boolean getNotifyUnstable() {
      return notifyUnstable;
    }
    public Boolean getNotifyAborted() {
      return notifyAborted;
    }
    public Boolean getNotifyNotBuilt() {
      return notifyNotBuilt;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        if(shouldNotify(build.getResult())) {
            notifyFlowdock(build, listener);
        } else {
            listener.getLogger().println("No Flowdock notification configured for build status: " + build.getResult().toString());
        }
        return true;
    }

    public boolean shouldNotify(Result buildResult) {
        Map<Result, Boolean> notifyMap = new HashMap<Result, Boolean>() {{
            put(Result.SUCCESS, notifySuccess);
            put(Result.FAILURE, notifyFailure);
            put(Result.UNSTABLE, notifyUnstable);
            put(Result.ABORTED, notifyAborted);
            put(Result.NOT_BUILT, notifyNotBuilt);
        }};
        return notifyMap.get(buildResult);
    }

    protected void notifyFlowdock(AbstractBuild build, BuildListener listener) {
      FlowdockAPI api = new FlowdockAPI(getDescriptor().apiUrl(), flowToken, listener.getLogger());
      TeamInboxMessage msg = TeamInboxMessage.fromBuild(build);
      msg.setTags(notificationTags);

      if(build.getResult() != Result.SUCCESS && chatNotification) {
        ChatMessage chatMsg = ChatMessage.fromBuild(build);
        chatMsg.setTags(notificationTags);
        if(api.pushChatMessage(chatMsg))
            listener.getLogger().println("Flowdock: Chat notification sent successfully");
      }
      if(api.pushTeamInboxMessage(msg))
        listener.getLogger().println("Flowdock: Team Inbox notification sent successfully");
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String apiUrl = "https://api.flowdock.com/";

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Flowdock notification";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            apiUrl = formData.getString("apiUrl");
            save();
            return super.configure(req,formData);
        }

        public String apiUrl() {
            return apiUrl;
        }
    }
}
