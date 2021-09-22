[![Build Status](https://travis-ci.org/sayurbox/config4live-java.svg?branch=master)](https://travis-ci.org/sayurbox/config4live-java)
[![codecov](https://codecov.io/gh/sayurbox/config4live-java/branch/master/graph/badge.svg?token=TC05HJSAZW)](https://codecov.io/gh/sayurbox/config4live-java)

# Config4live-java
Centralized live **configuration library for Java**. for microservice or distributed system.
Inspired from [https://github.com/cfg4j/cfg4j](https://github.com/cfg4j/cfg4j)

## Features

 - [gRPC](https://grpc.io/) connection
   - Wrapped by grpc protocol (fast and high performance RPC framework) for requesting configuration to config server. 
 - [Hystrix](https://github.com/Netflix/Hystrix)
   - Bundled with hystrix for circuit breaker. avoid cascading failures
 - In-Memory cache
   - Avoid too many requests to config server
   - [Google Guava](https://github.com/google/guava/wiki/CachesExplained) cache 
 - HTTP connection
   - Http rest api connection
   
## gRPC proto file format
```$xslt
syntax = "proto3";

option java_multiple_files = true;
option java_package = "com.sayurbox.config4live";
option java_outer_classname = "LiveConfigurationProto";
option objc_class_prefix = "HLW";

package config4live;

service LiveConfiguration {
  // Find config by name
  rpc FindConfig (ConfigRequest) returns (ConfigResponse) {}
}

message ConfigRequest {
  string name = 1;
}

message ConfigResponse {
  string id = 1;
  string name = 2;
  string value = 3;
  string description = 4;
  enum Format {
      text = 0;
      number = 1;
      bool = 2;
      json = 3;
    }
  Format format = 5;
  string owner = 6;
}

```

## Usage
  
Add the JitPack repository to your root build file
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Add the dependency

```groovy
dependencies {
    implementation 'com.github.sayurbox:config4live-java:<release-version>'
}
```

## Example GRPC source

Create source (grpc url is required, hystrx config is optional) and provider instance
```java
ConfigurationSource source = new GrpcConfigurationSource.Builder()
                .withGrpcUrl("localhost:5055")
                .withHystrixExecutionTimeout(1000)
                .withHystrixCircuitBreakerSleepWindow(500)
                .withHystrixCircuitBreakerRequestVolumeThreshold(10)
                .withHystrixRollingStatisticalWindow(500)
                .withHystrixHealthSnapshotInterval(500)
                .build();
ConfigurationProvider provider = new ConfigurationProvider.Builder().withSource(source)
                .withCache(true)
                .withTimeToLive(10).build();

// find configuration with default value
String value = provider.bind("default_name", "Name default")
System.out.println("value " + value);

```

## Example Http source

Create source (http url is required, hystrx config is optional) and provider instance
```java
ConfigurationSource source = new HttpConfigurationSource.Builder()
                .withUrl("http://localhost:8080")
                .withHystrixExecutionTimeout(1000)
                .withHystrixCircuitBreakerSleepWindow(500)
                .withHystrixCircuitBreakerRequestVolumeThreshold(10)
                .withHystrixRollingStatisticalWindow(500)
                .withHystrixHealthSnapshotInterval(500)
                .build();
ConfigurationProvider provider = new ConfigurationProvider.Builder().withSource(source)
                .withCache(true)
                .withTimeToLive(10).build();

// find configuration with default value
String value = provider.bind("default_name", "Name default")
System.out.println("value " + value);

```
