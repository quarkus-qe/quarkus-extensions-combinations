# Contributing guide

**Want to contribute? Great!** 
We try to make it easy, and all contributions, even the smaller ones, are more than welcome.
This includes bug reports, fixes, documentation, examples... 
But first, read this page (including the small print at the end).

## Legal

All original contributions to Quarkus are licensed under the
[ASL - Apache License](https://www.apache.org/licenses/LICENSE-2.0),
version 2.0 or later, or, if another license is specified as governing the file or directory being
modified, such other license.

All contributions are subject to the [Developer Certificate of Origin (DCO)](https://developercertificate.org/).

## Reporting an issue

This project uses GitHub issues to manage the issues. Open an issue directly in GitHub.

If you believe you found a bug, and it's likely possible, please indicate a way to reproduce it, what you are seeing and what you would expect to see.
Don't forget to indicate your Quarkus, Java, Openshift, Kubernetes, Maven/Gradle and GraalVM version. 

## Before you contribute

To contribute, use GitHub Pull Requests, from your **own** fork.

Also, make sure you have set up your Git authorship correctly:

```
git config --global user.name "Your Full Name"
git config --global user.email your.email@example.com
```

If you use different computers to contribute, please make sure the name is the same on all your computers.

We use this information to acknowledge your contributions in release announcements.

### Code reviews

All submissions, including submissions by project members, need to be reviewed before being merged.

### Coding Guidelines

 * We decided to disallow `@author` tags in the Javadoc: they are hard to maintain, especially in a very active project, and we use the Git history to track authorship. GitHub also has [this nice page with your contributions](https://github.com/quarkusio/quarkus/graphs/contributors). For each major Quarkus release, we also publish the list of contributors in the announcement post.
 * Commits should be atomic and semantic. Please properly squash your pull requests before submitting them. Fixup commits can be used temporarily during the review process but things should be squashed at the end to have meaningful commits.
 We use merge commits so the GitHub Merge button cannot do that for us. If you don't know how to do that, just ask in your pull request, we will be happy to help!

## Setup

If you have not done so on this machine, you need to follow the same setup as the [Quarkus Setup guide](https://github.com/quarkusio/quarkus/blob/main/CONTRIBUTING.md#setup).

### IDE Config and Code Style

Quarkus has a strictly enforced code style. Code formatting is done by the Eclipse code formatter, using the config files
found in the `independent-projects/ide-config` directory. By default when you run `./mvnw install` the code will be formatted automatically.
When submitting a pull request the CI build will fail if running the formatter results in any code changes, so it is
recommended that you always run a full Maven build before submitting a pull request.

If you want to run the formatting without doing a full build, you can run `./mvnw process-sources`.

#### Eclipse Setup

Open the *Preferences* window, and then navigate to _Java_ -> _Code Style_ -> _Formatter_. Click _Import_ and then
select the [`eclipse-format.xml`](https://raw.githubusercontent.com/quarkusio/quarkus/main/independent-projects/ide-config/src/main/resources/eclipse-format.xml) file.

Next navigate to _Java_ -> _Code Style_ -> _Organize Imports_. Click _Import_ and select the [`eclipse.importorder`](https://raw.githubusercontent.com/quarkusio/quarkus/main/independent-projects/ide-config/src/main/resources/eclipse.importorder) file.

#### IDEA Setup

Open the _Preferences_ window (or _Settings_ depending on your edition) , navigate to _Plugins_ and install the [Eclipse Code Formatter Plugin](https://plugins.jetbrains.com/plugin/6546-eclipse-code-formatter) from the Marketplace.

Restart your IDE, open the *Preferences* (or *Settings*) window again and navigate to _Other Settings_ -> _Eclipse Code Formatter_.

Select _Use the Eclipse Code Formatter_, then change the _Eclipse Java Formatter Config File_ to point to the
[`eclipse-format.xml`](https://raw.githubusercontent.com/quarkusio/quarkus/main/independent-projects/ide-config/src/main/resources/eclipse-format.xml). Make sure the _Optimize Imports_ box is ticked, and
select [`eclipse.importorder`](https://raw.githubusercontent.com/quarkusio/quarkus/main/independent-projects/ide-config/src/main/resources/eclipse.importorder) file as the import order config file.

#### VS Code Setup

Install the [Java Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) extension which contains the most popular extensions for most Java developers. 

Then, configure the java settings using the next settings.json configuration:

```json
{
  "java.semanticHighlighting.enabled": true,
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-11",
      "path": "/usr/lib/jvm/java-11-openjdk",
      "default": true
    }
  ],
  "java.home": "/usr/lib/jvm/java-11-openjdk",
  "editor.formatOnPaste": true,
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "vscjava.vscode-java-pack",
  "java.format.settings.url": "https://raw.githubusercontent.com/quarkusio/quarkus/main/independent-projects/ide-config/src/main/resources/eclipse-format.xml",
  "java.completion.importOrder": [
    "java",
    "javax",
    "org",
    "com",
  ],
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true,
    "source.organizeImports": true
  }
}
```

| Note that the java location might be different in a local setup.
