# Development

Start out with forking and cloning the repository.

```console
git clone https://github.com/YOUR_FORK/sariftool
```

## Development environment and tooling

* IDE:
    * VSCode

* Core:
    * Maven
    * JDK graalvm version
    * asdf-vm
    * podman

Look in '.tool-versions' to see the specific versions in use during development.
You can use the 'asdf-vm' tool to install and let it handle the above versions, or use the 'scripts/asdf-install-tool-versions.sh'.


## Run tests

```console
./mwnw clean test
```

## Build the project

### Building a runnable jar

- Build it and...

```console
./mwnw -Pfatjar clean package
```

- Run it

```console
java -jar target/sariftool-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h
```

### Building a native (jvm-free) binary
- Build it and ...

```console
./mwnw -Pfatjar clean package && ./mvnw -Pnative -DskipTests package
```

- Run it

```console
./target/sariftool-0.0.1-SNAPSHOT -h
```

### Generating graalvm configuration

There is a lot to read up about regarding graalvm, see some links in the references section in the README.
But, one thing to consider is that initial graalvm configuration can't find every runtime path in the initial program and needs a bit of help to find these path.
You can (must) add to the configuration by doing runtime analyze of execution paths, if you have added new functions.

- Basically, you should run:

```console
 ./scripts/graal-native-gen.sh 'CURRENT_VERSION_OF_JAR'
```

to generate new native configuration.
And possible, if you added new input functions, add these to the 'graal-native-gen.sh' script.

## Project health and quality

* Project quality tooling:
    * nodejs (for commitlint linting)
    * reuse
    * megalinter
    * commitlint
    * repolinter

Make it a habit of, before pushing:

### 1. Check various code quality tools

Run 'scripts/check-code-quality.sh' to check project quality health.
The script is dependent on podman, commitlint and repolinter.


### 2. Check dependencies

```console
  mvn versions:display-dependency-updates
```

### 3. Check test coverage

```console
  mvn clean verify
```



Note: In the future, the dependency on commitlint and repolinter could be handled by using an container image, or by adding these by PR in Megalinter.

## Releasing a new version

- To-do
