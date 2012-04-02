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
Flowdock: failed to send notification
Flowdock: response status: 401
```

Most likely the API token is not valid or you do not have the permission to access the flow.

```
Flowdock: failed to send notification
Flowdock: response status: 400
```

Validation of the notification message failed. This can occur when customizing the build notification content as it needs to conform to
 Flowdock Push API format described in [the API documentation](https://www.flowdock.com/api/push).

## Write your own custom notifier

It's easy, just fork the Github repo of Jenkins Flowdock Plugin and start hacking!

To simply modify the contents of Team Inbox or Chat messages sent by the plugin, see the `fromBuild` method in `TeamInboxMessage` and `ChatMessage` classes.

Jenkins Flowdock Plugin contains a full implementation of the [Flowdock Push API](https://www.flowdock.com/api/push) which is great for posting notifications.
If you need to go beyond simple notifications, have a look at what the [REST API](https://www.flowdock.com/api/rest) can offer you.

