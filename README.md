# yandisk4j
``yandisk4j`` is a lightweight Java 17 wrapper for the Yandex Disk cloud storage API.
This is a modernization of [yandex-disk-restapi-java](https://github.com/yandex-disk/yandex-disk-restapi-java) with
less dependencies, better documentation and better syntax.

## Usage
### Adding Dependency
#### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.wasabithumb:yandisk4j:0.2.1")
}
```

#### Gradle (Groovy DSL)
```groovy
dependencies {
    implementation 'io.github.wasabithumb:yandisk4j:0.2.1'
}
```

#### Maven
```xml
<dependency>
    <groupId>io.github.wasabithumb</groupId>
    <artifactId>yandisk4j</artifactId>
    <version>0.2.1</version>
    <scope>compile</scope>
</dependency>
```

### Authenticating
The Yandex Disk API is OAuth authenticated, which means it requires a unique OAuth token for each combination of
application & user account. ``yandisk4j`` includes a helper for generating OAuth tokens. If you have your own method,
feel free to skip to [Entry](#entry).

Using the auth helper requires having an [application](https://oauth.yandex.com/) with a client ID, client secret, and
permission to use the ``cloud_api:disk.*`` scopes.
```java
// CODE : Retrieve the authorization code from a redirect URL you control
// SCREEN_CODE : Retrieve the authorization code from the end user
// LOCAL_CODE : Automatically retrieve the authorization code from a temporary local HTTP server

final AuthHandler auth = YanDisk.auth(AuthScheme.SCREEN_CODE)
        .clientID("YOUR_CLIENT_ID_HERE")
        .clientSecret("YOUR_CLIENT_SECRET_HERE")
        .scopes(AuthScope.INFO, AuthScope.READ /* etc */)
        .build();

// Open URL in the system browser
auth.openURL();

String code;
// CODE        : ¯\_(ツ)_/¯
// SCREEN_CODE : code = promptUserForAuthCode();
// LOCAL_CODE  : code = auth.awaitCode().code();

AuthResponse response = auth.exchange(code);
System.out.println("OAuth token: " + response.accessToken());
```

#### Note about ``LOCAL_CODE``
The default redirect URL for ``LOCAL_CODE`` is ``http://127.0.0.1:8127/``. Make sure to add this to your application
or specify a local redirect URL you control when building the ``AuthHandler``.

The default HTTP server is [``com.sun.net.httpserver.HttpServer``](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html).
Due to the nature of ``com.sun.*`` packages, it's not a guarantee that your JRE will have it (though as of writing,
Oracle & [OpenJDK](https://github.com/openjdk/jdk/blob/6c59185475eeca83153f085eba27cc0b3acf9bb4/src/jdk.httpserver/share/classes/com/sun/net/httpserver/HttpServer.java) both do). If this behavior is not acceptable, you can use
[NanoHTTPD](https://github.com/NanoHttpd/nanohttpd) as a fallback provider.
Example with Gradle:
```kotlin
implementation("io.github.wasabithumb:yandisk4j:0.2.1") {
    capabilities {
        requireCapability("io.github.wasabithumb:yandisk4j-nanohttpd")
    }
}
```

### Entry
To create a new ``YanDisk`` instance:
```java
YanDisk yd = YanDisk.yanDisk(oauthToken);
```

To create a ``YanDisk`` instance with a dynamic (refreshing) OAuth token:
```java
YanDisk yd = YanDisk.yanDisk(() -> {
    // Return your OAuth token here
});
```

## Examples

### List all files
```java
YanDisk yd = YanDisk.yanDisk(/* ... */);
for (Node n : yd.listAll()) {
    System.out.println("- " + n.path());
}
```

### List files & directories
```java
YanDisk yd = YanDisk.yanDisk(/* ... */);
for (Node n : yd.list("disk:/path/to/dir")) {
    if (n.isDirectory())
        System.out.println("D > " + n.name());
    if (n.isFile())
        System.out.println("F > " + n.name());
}
```

### Upload a file
Multiple methods are shown below, choose only 1
```java
YanDisk yd = YanDisk.yanDisk(/* ... */);
NodeUploader nu = yd.upload("disk:/remote/path/to/file.bin");
File file = new File("local/path/to/file.bin");

// Sync
nu.write(file);

// Sync (stream)
nu.write(new FileInputStream(file));

// Sync (stream)
try (InputStream is = new FileInputStream(file);
     OutputStream os = nu.write()
) {
    // Pipe "is" to "os"
}

// Async
Transfer t = nu.writeAsync(file);

// Async (stream)
Transfer t = nu.writeAsync(new FileInputStream(file), file.length());
```
See [Working with Transfers](#working-with-transfers) for more info.

### Download a file
Multiple methods are shown below, choose only 1
```java
YanDisk yd = YanDisk.yanDisk(/* ... */);
NodeDownloader nd = yd.download("disk:/remote/path/to/file.bin");
File file = new File("local/path/to/file.bin");

// Sync
nd.read(file);

// Sync (stream)
try (InputStream is = nd.open();
     OutputStream os = new FileOutputStream(file)
) {
    // Pipe "is" to "os"
}

// Async
Transfer t = nd.readAsync(file);

// Async (stream)
Transfer t = nd.readAsync(new FileOutputStream(file));
```
See [Working with Transfers](#working-with-transfers) for more info.

### Copy a file or directory
```java
YanDisk yd = YanDisk.yanDisk(/* ... */);
Operation op = yd.copy("disk:/a.txt", "disk:/b.txt");
```
See [Working with Operations](#working-with-operations) for more info.

### Move a file or directory
```java
YanDisk yd = YanDisk.yanDisk(/* ... */);
Operation op = yd.move("disk:/a.txt", "disk:/b.txt");
```
See [Working with Operations](#working-with-operations) for more info.

### Delete a file or directory
```java
YanDisk yd = YanDisk.yanDisk(/* ... */);
Operation op = yd.delete("disk:/stuff");
```
See [Working with Operations](#working-with-operations) for more info.

### Create a directory
```java
YanDisk yd = YanDisk.yanDisk(/* ... */);
yd.mkdir("disk:/path/to/dir");
// This is not recursive! In this case, disk:/path/to must already exist.
```

## Working with Watchables
``Watchable`` defines the common methods of [Operation](#working-with-operations) and [Transfer](#working-with-transfers).
In other words, it is similar to a [CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html).
Their differences are outlined further below.
```java
Watchable<?> w = /* ... */;
w.watch((Watchable<?> ignored) -> {
    // Watchable has changed
    if (w.hasProgress()) {
        double progress = w.progress();
    }
    if (w.isDone()) {
        // Done
    }
});
```

## Working with Operations
A few large operations ([copy](#copy-a-file-or-directory), [move](#move-a-file-or-directory), [delete](#delete-a-file-or-directory))
may return an ``Operation``. An Operation is an asynchronous ``OperationStatus`` accessor that can be queried with
``status()`` and listened to with ``watch(Consumer<Operation>)``. For example:
```java
Operation op = /* ... */;
op.setRefreshInterval(200L); // Refresh every 200ms
// Operation daemon does not start until "status" or "watch" are called. At this point, no requests are scheduled.
op.watch((Operation ignored) -> {
    System.out.println(op.status()); // PENDING, SUCCESS or FAILED
});
```

## Working with Transfers
A ``Transfer`` can be created using the async methods on ``NodeUploader``/``NodeDownloader``. The job of a ``Transfer``
is to monitor a pipe and provide thread-safe access to the pipe's completion state and progress (if possible).
```java
Transfer t = /* ... */;
t.block(); // Wait for transfer to complete (isDone() becomes true)
if (t.error() != null) {
    // Broken pipe
}
```
``Transfer`` is a [Watchable](#working-with-watchables) and can be listened for changes with ``watch(Consumer<Transfer>)``.

## License
```text
Copyright 2024 Wasabi Codes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```