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
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class FlowdockNotifier extends Notifier {

    private final String flowToken;
    private final String notificationTags;
    private final Boolean chatNotification;

    private final Map<Result, Boolean> notifyMap;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public FlowdockNotifier(String flowToken, String notificationTags, Boolean chatNotification,
        final Boolean notifySuccess, final Boolean notifyFailure, final Boolean notifyUnstable,
        final Boolean notifyAborted, final Boolean notifyNotBuilt) {
        this.flowToken = flowToken;
        this.notificationTags = notificationTags;
        this.chatNotification = chatNotification;

        // set notification map: if there's not configuration yet, use the default value
        this.notifyMap = new HashMap<Result, Boolean>() {{
            put(Result.SUCCESS, (notifySuccess == null) ? true : notifySuccess);
            put(Result.FAILURE, (notifyFailure == null) ? true : notifyFailure);
            put(Result.UNSTABLE, (notifyUnstable == null) ? false : notifyUnstable);
            put(Result.ABORTED, (notifyAborted == null) ? false : notifyAborted);
            put(Result.NOT_BUILT, (notifyNotBuilt == null) ? false : notifyNotBuilt);
        }};
    }

    public String getFlowToken() {
        return flowToken;
    }

    public String getNotificationTags() {
        return notificationTags;
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

        private String apiUrl = "https://api.flowdock.com";

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Flowdock notification";
        }

        public FormValidation doTestConnection(@QueryParameter("flowToken") final String flowToken,
            @QueryParameter("notificationTags") final String notificationTags) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            FlowdockAPI api = new FlowdockAPI(apiUrl(), flowToken, ps);
            ChatMessage testMsg = new ChatMessage();
            testMsg.setTags(notificationTags);
            testMsg.setContent("Your plugin is ready!");

            if(api.pushChatMessage(testMsg)) {
                return FormValidation.ok("Success! Flowdock plugin can send notifications to your flow.");
            } else {
                return FormValidation.error(baos.toString());
            }
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
