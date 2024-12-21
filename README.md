# yandisk4j
``yandisk4j`` is a lightweight Java 17 wrapper for the Yandex Disk cloud storage API.
This is a modernization of [yandex-disk-restapi-java](https://github.com/yandex-disk/yandex-disk-restapi-java) with
less dependencies, better documentation and better syntax.

## Usage
### Adding Dependency
#### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.wasabithumb:yandisk4j:0.1.0")
}
```

#### Gradle (Groovy DSL)
```groovy
dependencies {
    implementation 'io.github.wasabithumb:yandisk4j:0.1.0'
}
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

final AuthHandler auth = YanDisk.auth(AuthScheme.SCREEN_CODE)
        .clientID("YOUR_CLIENT_ID_HERE")
        .clientSecret("YOUR_CLIENT_SECRET_HERE")
        .scopes(AuthScope.INFO, AuthScope.READ /* etc */)
        .build();

auth.openURL();
String code = promptUserForAuthCode();

AuthResponse response = h.exchange(code);
System.out.println("OAuth token: " + response.accessToken());
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
```java
YanDisk yd = YanDisk.yanDisk(/* ... */);
yd.upload("disk:/remote/path/to/file.bin")
                .write(new File("/local/path/to/file.bin"));
// Also supports InputStream
```

### Download a file
```java
YanDisk yd = YanDisk.yanDisk(/* ... */);
yd.download("disk:/remote/path/to/file.bin")
                        .read(new File("/local/path/to/file.bin"));
// Also supports InputStream
```

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