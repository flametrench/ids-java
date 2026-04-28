# flametrench-ids (Java)

Java SDK for the [Flametrench](https://github.com/flametrench/spec) wire-format identifier specification — fourth in the language family alongside Node, PHP, and Python.

The wire format is `{type}_{32-hex}`, where the hex payload is a UUIDv7 (so generated IDs sort by creation time). The same identifiers travel unchanged across all four SDKs; the conformance fixture corpus enforces this mechanically.

```java
import dev.flametrench.ids.Id;
import dev.flametrench.ids.DecodedId;

String id = Id.generate("usr");
// → "usr_0190f2a81b3c7abc8123456789abcdef"

DecodedId decoded = Id.decode(id);
// → DecodedId[type=usr, uuid=0190f2a8-1b3c-7abc-8123-456789abcdef]

boolean ok = Id.isValid(id, "usr");        // → true
boolean wrong = Id.isValid(id, "org");     // → false
```

## Installation

Maven:

```xml
<dependency>
    <groupId>dev.flametrench</groupId>
    <artifactId>ids</artifactId>
    <version>0.2.0-rc.2</version>
</dependency>
```

Gradle:

```groovy
implementation 'dev.flametrench:ids:0.2.0-rc.2'
```

Requires Java 17+. UUIDv7 generation uses `com.fasterxml.uuid:java-uuid-generator`, the reference implementation aligned with RFC 9562.

## Registered type prefixes

| Prefix  | Meaning                | Spec version |
| ------- | ---------------------- | ------------ |
| `usr`   | user                   | v0.1         |
| `org`   | organization           | v0.1         |
| `mem`   | membership             | v0.1         |
| `inv`   | invitation             | v0.1         |
| `ses`   | session                | v0.1         |
| `cred`  | credential             | v0.1         |
| `tup`   | authorization tuple    | v0.1         |
| `mfa`   | MFA factor             | v0.2         |
| `shr`   | share token            | v0.2         |

The registry is normative; see [docs/ids.md](https://github.com/flametrench/spec/blob/main/docs/ids.md) for the full rules.

## Conformance

```bash
mvn test
```

Runs the same 48 fixture tests that gate `@flametrench/ids` (Node), `flametrench/ids` (PHP), and `flametrench-ids` (Python). The conformance fixtures are vendored under `src/test/resources/conformance/fixtures/ids/`; CI verifies they match the upstream spec repo.

## License

Apache-2.0. See [LICENSE](./LICENSE) and [NOTICE](./NOTICE).

Copyright 2026 NDC Digital, LLC.
