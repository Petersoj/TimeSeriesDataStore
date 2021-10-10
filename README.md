<p align="center"><a href="https://petersoj.github.io/TimeSeriesDataStore/" target="_blank"><img width="70%" src="https://raw.githubusercontent.com/Petersoj/TimeSeriesDataStore/main/.github/images/logo.png" alt="TimeSeriesDataStore"></a></p>

# Overview
TimeSeriesDataStore is a Java API to store and iterate over time-series data retrieved from a data feed and stored in a database. Data from a data feed is fetched if data within a requested time range is not in a database. This data is then stored in a database for future requests. Data from a database is fetched if data within a requested time range is in a database, thus serving data much quicker than the data feed. 

<p align="center">
    <a href="https://search.maven.org/artifact/net.jacobpeterson/timeseriesdatastore" target="_blank"><img alt="Maven Central" src="https://img.shields.io/maven-central/v/net.jacobpeterson/timeseriesdatastore"></a>
    <a href="https://javadoc.io/doc/net.jacobpeterson/timeseriesdatastore" target="_blank"><img src="https://javadoc.io/badge/net.jacobpeterson/timeseriesdatastore.svg" alt="Javadocs"></a>
    <a href="https://travis-ci.com/github/Petersoj/timeseriesdatastore" target="_blank"><img src="https://travis-ci.com/Petersoj/timeseriesdatastore.svg?branch=main" alt="Build Status"></a>
    <a href="https://codecov.io/gh/petersoj/timeseriesdatastore"><img src="https://codecov.io/gh/petersoj/timeseriesdatastore/branch/main/graph/badge.svg" alt="CodeCov badge"/></a>
    <a href="https://opensource.org/licenses/MIT" target="_blank"><img alt="GitHub" src="https://img.shields.io/github/license/petersoj/timeseriesdatastore"></a>    
</p>

# Gradle and Maven Integration
If you are using Gradle as your build tool, add the following dependency to your `build.gradle` file:

```
dependencies {
    implementation group: 'net.jacobpeterson', name: 'timeseriesdatastore', version: '1.0'
}
```

If you are using Maven as your build tool, add the following dependency to your `pom.xml` file:

```
<dependency>
    <groupId>net.jacobpeterson</groupId>
    <artifactId>timeseriesdatastore</artifactId>
    <version>1.0</version>
</dependency>
```

# Logger
For logging, this library uses [SLF4j](http://www.slf4j.org/) which serves as an interface for various logging frameworks. This enables you to use whatever logging framework you would like. However, if you do not add a logging framework as a dependency in your project, the console will output a message stating that SLF4j is defaulting to a no-operation (NOP) logger implementation. To enable logging, add a logging framework of your choice as a dependency to your project such as [Log4j 2](http://logging.apache.org/log4j/2.x/index.html), [SLF4j-simple](http://www.slf4j.org/manual.html), or [Apache Commons Logging](https://commons.apache.org/proper/commons-logging/).

# Contributing
Contributions are welcome!

If you are creating a Pull Request, be sure to create a new branch in your forked repository for your feature or bug fix instead of committing directly to the `main` branch in your fork. 
