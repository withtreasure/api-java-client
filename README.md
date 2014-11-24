# Abiquo API Java Client

This is an API client for the [Abiquo API](http://wiki.abiquo.com/). The Abiquo API is a RESTful API,
so any REST client can be used to connect to it. This project uses Jersey
and just provides some high level functions and enforces some best practices
to make it easier to perform the common tasks.

## Installation

The Abiquo API Java Client is released to the Maven Central and Sonatype snapshot
repositories, so you just have to declare the dependency in your pom.xml as follows:

```xml
<dependency>
    <groupId>com.abiquo</groupId>
    <artifactId>api-java-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

Or if you want to use the latest SNAPSHOT, just declare the snapshot repository in your
pom.xml:

```xml
<repositories>
    <repository>
        <id>oss-sonatype</id>
        <name>oss-sonatype</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>com.abiquo</groupId>
    <artifactId>api-java-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Contributing

This project is still in an early development stage and is still incomplete. All
contributions are welcome, so feel free to [raise a pull request](https://help.github.com/articles/using-pull-requests/).

## License

The Abiquo API Java Client is licensed under the Apache License version 2. For
further details, see the [LICENSE](LICENSE) file.
