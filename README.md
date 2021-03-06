# jQAssistant GitHub-Issues Plugin

[![GitHub license](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/b-pos465/jqa-githubissues-plugin/blob/master/LICENSE)
[![Build Status](https://travis-ci.com/b-pos465/jqa-githubissues-plugin.svg?branch=master)](https://travis-ci.com/b-pos465/jqa-githubissues-plugin)
[![codecov](https://codecov.io/gh/b-pos465/jqa-githubissues-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/b-pos465/jqa-githubissues-plugin)

This is a GitHub issue scanner for [jQAssistant](https://jqassistant.org/). 
It enables jQAssistant to scan and analyze GitHub issues.

## Getting Started

To use the __GitHub-Issues__ plugin create a file named `githubissues.xml`. 
The plugin can scan multiple repositories owned by different users. Please note that
the [GitHub REST-API](https://developer.github.com/v3/) requires login credentials to
access any of its functions. Therefore, login credentials can be provided per 
repository.

```xml
<github-repositories>
    <github-repository>
        <user>github-user</user>
        <name>github-repository-name</name>

        <credentials>
            <user>authentication-user</user>
            <password>authentication-password</password>
        </credentials>
    </github-repository>

    <github-repository>
        ...
    </github-repository>
</github-repositories>
```

Put the `githubissues.xml` inside an artifact that shall be scanned 
or simply scan it standalone:


### Standalone 
Download [jQAssistant](https://jqassistant.org/get-started/) for command line usage
and put the plugin _JAR_ in the `plugins` folder. Then run:

```bash
# Scan the GitHub-Repositories
jqassistant-commandline-neo4jv3-1.4.0/bin/jqassistant.sh scan -f githubissues.xml

# Start a Neo4J web UI to explore the result: 
jqassistant-commandline-neo4jv3-1.4.0/bin/jqassistant.sh server
```

## Labels

The __GitHub-Issues__ plugin uses the following labels in the resulting graph:

| Label | Description                                                  | ID |
| ----- | ------------------------------------------------------------ |----|
|GitHub |Parent label for all nodes related to the GitHub-Issues plugin.| -|
|Repository|Represents a GitHub Repository.| "repo-user/repo-name"|
|Issue|Represents a GitHub Issue.| "repo-user/repo-name#issue-number" |
|Milestone|Represents a GitHub Milestone which is a collection of Issues. | "repo-user/repo-name#milestone-id" |
|Comment|Represents a Comment under a GitHub Issue.| - |
|PullRequest|Every PullRequest is an Issue, but not every Issue is a PullRequest.| "repo-user/repo-name#issue-number" |
|User|Represents a GitHub User.| "user-name" |
|Commit|Represents a GitHub Commit.| "repo-user/repo-name#commit-sha" |

Here are the possible relations between those labels:

```java
(Repository)  -[:HAS_ISSUE]       ->    (Issue)
(Repository)  -[:HAS_MILESTONE]   ->    (Milestone)

(Issue)       -[:HAS_LABEL]       ->    (Label)
(Issue)       -[:HAS_COMMENT]     ->    (Comment)
(Issue)       -[:HAS_ASSIGNEE]    ->    (User)
(Issue)       -[:CREATED_BY]      ->    (User)
(Issue)       -[:IS_PART_OF]      ->    (Milestone)

(PullRequest) -[:HAS_LAST_COMMIT] ->    (Commit)

(Milestone)   -[:CREATED_BY]      ->    (User)

(Comment)     -[:FOLLOWED_BY]     ->    (Comment)
(Comment)     -[:CREATED_BY]      ->    (User)
```

## Use Cases

### Overview

List all your open Issues over multiple repositories:

```java
MATCH
    (r:Repository)-[:HAS_ISSUE]->(i:Issue {state:"open"})
RETURN
    r.repositoryId, i.title, i.body
```

Count open Issues per repository:

```java
MATCH
    (r:Repository)-[:HAS_ISSUE]->(Issue {state:"open"})
RETURN
    r.repositoryId, count(*) AS issueCount
ORDER BY
    issueCount DESC
```

List open issues per user:

```java
MATCH
    (Issue {state:"open"})-[:HAS_ASSIGNEE]->(u:User)
RETURN
    u.login, count(*)
```

### Issue quality

Show issues without description:

```java
MATCH
    (i:Issue)
WHERE
    i.body = ""
RETURN
    i.issueId, i.title
```

Show issues without labels:

```java
MATCH 
    (i:Issue)
WHERE 
    NOT (i:Issue)-[:HAS_LABEL]->()
RETURN
    i.title, i.issueId
```

Show issues ordered descending by the amount of comments:
```java
MATCH 
    path=((i:Issue)-[:HAS_COMMENT]->()-[:FOLLOWED_BY*]->())
RETURN
    i.title, i.issueId, length(path) AS pathLength, i.state
ORDER BY
    pathLength DESC, i.state DESC
```

Show durations it needed to resolve an issue:
```java
WITH
    issue, duration.inDays(date(issue.createdAt), date(issue.updatedAt)).days AS duration
RETURN 
    issue.issueId, issue.title, duration + " days" AS timToSolve
ORDER BY
    duration DESC
```

Show issues older than 1 month that are still open:
```java
MATCH
    (issue:Issue {state:"open"})
WHERE
    date(issue.createdAt) <= date('20180713')
RETURN 
    *
```

#### Why are these issues still open?

Let's have a look at a few indicators:

- Do these Issues have labels?
```java
MATCH
    (issue:Issue {state:"open"})
WHERE
    date(issue.createdAt) <= date('20180713') AND NOT (issue:Issue)-[:HAS_LABEL]->()
RETURN 
    *
```
&rarr; If not, then probably no one looked at these issues.

- Is anyone assigned to this issue?
```java
MATCH
    (issue:Issue {state:"open"})
WHERE
    date(issue.createdAt) <= date('20180713') AND NOT (issue:Issue)-[:HAS_ASSIGNEE]->(:User)
RETURN 
    issue
```
&rarr; If not, then probably no one feels responsible for this issue.


## Development

If you want to contribute here are a few tips to get you started:

Build the __GitHub-Issues__ plugin:

```bash
cd plugin

# Build a fat-JAR
mvn clean package

# Copy the resulting JAR into the jQAssistant CLI plugins folder
cp target/jqa-githubissues-plugin-0.1-jar-with-dependencies.jar ../run/jqassistant-commandline-neo4jv3-1.4.0/plugins/
```
Run code coverage via [Corbertura](http://cobertura.github.io/cobertura/):

```bash
mvn cobertura:cobertura
```

The coverage reports can be found under `target/site/cobertura`.