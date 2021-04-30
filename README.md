# Combinations of Quarkus extensions

Test to try combinations of supported Quarkus extensions out. For each combination, the test will:
 - create the project
 - build project
 - test project
 
| Note that each combination will be executed in parallel, so the more powerful the machine is, the faster will finish the test.
 
## How to run the tests

```
mvn clean test
```

In order to verify Native builds:

```
mvn clean test -Dts.verify-native-mode=true
```

In order to verify Dev mode:

```
mvn clean test -Dts.verify-dev-mode=true
```

## How to run with a concrete version of Quarkus

By default, the test will use the 999-SNAPSHOT Quarkus version. We can configure this version using the `quarkus.version` property:

```
mvn clean test -Dquarkus.version=1.7.1.Final
```

| Note that the extensiosn will be filtered by this version as well.

## How to run with ALL extensions

By default, the test will only use supported Quarkus extensions. In order to use ALL the extensions, we need to use the `ts.only-supported-extensions` property:

```
mvn clean test ts.only-supported-extensions=false
```

## How to select the group of Quarkus extensions

By default, the test will try each combination of 3 Quarkus extensions. We can configure this number using the `ts.extensions-in-groups-of` property:

```
mvn clean test -Dts.extensions-in-groups-of=5
```

Bear in mind, that the larger is the group, the more combinations will be created. As a reference, when there are 137 Quarkus extensions:

| Group Of | Combinations |
| -------- | ------------ |
| 2        | 9453         |
| 3        | 428536       |
| 4        | 14463090     |
| 8        | 2147483647   |


## How to limit the number of combinations

This is useful when you are interested in running the test only using a limited number of combinations. We can configure this number using the `ts.limit-extensions` property:

```
mvn clean test -Dts.limit-extensions=2
```

## How to run the combinations with selected extensions only

This is useful when you are interested in running the test only using a limited number of extensions. We can configure it by using the `ts.includes-combinations-only-with-extensions` property:

```
mvn clean test -Dts.includes-combinations-only-with-extensions=oidc,agroal
```

## How to disable the random order of combinations

By default, the order of the combinations is random. In order to disable this configuration, we need to turn of the property `ts.random-sort-extensions`:

```
mvn clean test -Dts.random-sort-extensions=false
```

## How to write custom properties by extension

By convention, the test suite will try to lookup to a property using the name of the extension. For example, if the quarkus extension is called "quarkus-oidc", then the framework will try to load the custom properties at `src/test/resources/oidc.properties` and append the content into the combination application properties file.

# How to contribute

The build instructions are available in the [contribution guide](CONTRIBUTING.md).
