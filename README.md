# Jenkins Flowdock Plugin

Jenkins Flowdock Plugin is a tool for sending build notification from Jenkins to your flow. It hooks to Post-build Actions of the build,
so you can use with any number of different builds. All you need to configure is the API token of the flow where you want the notifications
to go. See [Tokens](https://www.flowdock.com/account/tokens) page for list of your API tokens.

## Install

### With Jenkins Plugin manager

 * Go to Manage Jenkins -> Manage Plugins -> Available
 * Find "Flowdock plugin" and install it
 * Restart Jenkins

### From Jenkins Plugin repository

 * Download [the latest plugin](http://updates.jenkins-ci.org/latest/jenkins-flowdock-plugin.hpi)
 * Install it by uploading the package in admin: `Manage Jenkins/Hudson -> Manage Plugins -> Advanced -> Upload Plugin`
 * Restart Jenkins/Hudson

### From source

To compile this plugin from source, you need to have at Maven 3.0.4 or newer installed.

 * Clone the Github repo
 * Run `mvn install` to build a .hpi-plugin package
 * Install it by uploading the package in admin: `Manage Jenkins/Hudson -> Manage Plugins -> Advanced -> Upload Plugin`
 * Restart Jenkins/Hudson

## Configure

 * Go to Configure page inside a build
 * Scroll down to Post-build Actions
 * Select "Flowdock notification" from the list and fill out your the API token of your flow
 * Save the changes and click "Build Now"
 * Your flow should now receive notification. If not, see the Console Output of the build for errors.

## Troubleshooting by Console Output

```
ERROR: Publisher com.flowdock.jenkins.FlowdockNotifier aborted due to exception
 java.lang.NullPointerException
 at com.flowdock.jenkins.FlowdockNotifier.shouldNotify(FlowdockNotifier.java:117)
 at com.flowdock.jenkins.FlowdockNotifier.perform(FlowdockNotifier.java:108)
 at hudson.tasks.BuildStepMonitor$1.perform(BuildStepMonitor.java:19)
 at hudson.model.AbstractBuild$AbstractBuildExecution.perform(AbstractBuild.java:717)
 at hudson.model.AbstractBuild$AbstractBuildExecution.performAllBuildSteps(AbstractBuild.java:692)
 at hudson.model.Build$BuildExecution.cleanUp(Build.java:192)
 at hudson.model.Run.execute(Run.java:1546)
 at hudson.model.FreeStyleBuild.run(FreeStyleBuild.java:46)
 at hudson.model.ResourceController.execute(ResourceController.java:88)
 at hudson.model.Executor.run(Executor.java:236)
Finished: FAILURE
```

The way of storing the plugin configuration has changed in the recent versions. However, after 
upgrading both Jenkins and Flowdock plugin, the old configuration data may remain and cause exceptions 
like this. The quickest way to resolve this is to re-install the Flowdock plugin, which should flush the 
cached configuration.


```
Flowdock: failed to send notification
Flowdock: response status: 401 Unauthorized
```

Most likely the API token is not valid or you do not have the permission to access the flow.

```
Flowdock: failed to send notification
Flowdock: response status: 400 Bad Request, _RESPONSE_
```

Validation of the notification message failed. This can occur when customizing the build notification content as it needs to conform to
 Flowdock Push API format described in [the API documentation](https://www.flowdock.com/api/push). The _RESPONSE_ text should help in debugging
 why the message was rejected.

## Write your own custom notifier

It's easy, just fork the Github repo of Jenkins Flowdock Plugin and start hacking!

To simply modify the contents of Team Inbox or Chat messages sent by the plugin, see the `fromBuild` method in `TeamInboxMessage` and `ChatMessage` classes.

Jenkins Flowdock Plugin contains a full implementation of the [Flowdock Push API](https://www.flowdock.com/api/push) which is great for posting notifications.
If you need to go beyond simple notifications, have a look at what the [REST API](https://www.flowdock.com/api/rest) can offer you.

