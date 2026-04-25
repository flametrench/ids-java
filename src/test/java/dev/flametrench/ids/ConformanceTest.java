// Copyright 2026 NDC Digital, LLC
// SPDX-License-Identifier: Apache-2.0

package dev.flametrench.ids;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Flametrench v0.1 conformance suite — Java / JUnit 5 harness.
 *
 * <p>Exercises the IDs capability against the fixture corpus vendored from
 * github.com/flametrench/spec/conformance/fixtures/ids/. The fixtures
 * under src/test/resources/conformance/fixtures/ are a snapshot; the
 * drift-check CI job verifies they match the upstream spec repo.
 *
 * <p>Each fixture is exposed as a JUnit 5 dynamic test container, so
 * failures point directly at a spec-linked fixture id like
 * "[encode.canonical.usr] description text".
 */
class ConformanceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode loadFixture(String relativePath) throws IOException {
        String resource = "/conformance/fixtures/" + relativePath;
        try (InputStream in = ConformanceTest.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Fixture not found on classpath: " + resource);
            }
            return MAPPER.readTree(in);
        }
    }

    private static Class<? extends RuntimeException> errorClassForSpecName(String name) {
        return switch (name) {
            case "InvalidIdError" -> InvalidIdError.class;
            case "InvalidTypeError" -> InvalidTypeError.class;
            default -> throw new IllegalArgumentException(
                    "Unknown spec error name: " + name);
        };
    }

    // ─── ids.encode ───

    @TestFactory
    List<DynamicTest> encodeConformance() throws IOException {
        JsonNode fixture = loadFixture("ids/encode.json");
        List<DynamicTest> tests = new ArrayList<>();
        for (JsonNode t : fixture.get("tests")) {
            String id = t.get("id").asText();
            String desc = t.get("description").asText();
            tests.add(DynamicTest.dynamicTest("[" + id + "] " + desc, () -> {
                JsonNode input = t.get("input");
                JsonNode expected = t.get("expected");
                String type = input.get("type").asText();
                String uuid = input.get("uuid").asText();
                if (expected.has("error")) {
                    Class<? extends RuntimeException> ctor =
                            errorClassForSpecName(expected.get("error").asText());
                    assertThrows(ctor, () -> Id.encode(type, uuid));
                } else {
                    assertEquals(expected.get("result").asText(), Id.encode(type, uuid));
                }
            }));
        }
        return tests;
    }

    // ─── ids.decode (positive + round-trip) ───

    @TestFactory
    List<DynamicTest> decodePositiveConformance() throws IOException {
        JsonNode fixture = loadFixture("ids/decode.json");
        List<DynamicTest> tests = new ArrayList<>();
        for (JsonNode t : fixture.get("tests")) {
            String id = t.get("id").asText();
            String desc = t.get("description").asText();
            tests.add(DynamicTest.dynamicTest("[" + id + "] " + desc, () -> {
                JsonNode input = t.get("input");
                JsonNode expected = t.get("expected").get("result");
                DecodedId decoded = Id.decode(input.get("id").asText());
                assertEquals(expected.get("type").asText(), decoded.type());
                assertEquals(expected.get("uuid").asText(), decoded.uuid());
            }));
        }
        return tests;
    }

    // ─── ids.decode (rejection) ───

    @TestFactory
    List<DynamicTest> decodeRejectionConformance() throws IOException {
        JsonNode fixture = loadFixture("ids/decode-reject.json");
        List<DynamicTest> tests = new ArrayList<>();
        for (JsonNode t : fixture.get("tests")) {
            String id = t.get("id").asText();
            String desc = t.get("description").asText();
            tests.add(DynamicTest.dynamicTest("[" + id + "] " + desc, () -> {
                JsonNode input = t.get("input");
                Class<? extends RuntimeException> ctor =
                        errorClassForSpecName(t.get("expected").get("error").asText());
                assertThrows(ctor, () -> Id.decode(input.get("id").asText()));
            }));
        }
        return tests;
    }

    // ─── ids.is_valid ───

    @TestFactory
    List<DynamicTest> isValidConformance() throws IOException {
        JsonNode fixture = loadFixture("ids/is-valid.json");
        List<DynamicTest> tests = new ArrayList<>();
        for (JsonNode t : fixture.get("tests")) {
            String id = t.get("id").asText();
            String desc = t.get("description").asText();
            tests.add(DynamicTest.dynamicTest("[" + id + "] " + desc, () -> {
                JsonNode input = t.get("input");
                boolean expected = t.get("expected").get("result").asBoolean();
                boolean result;
                if (input.has("expected_type")) {
                    result = Id.isValid(input.get("id").asText(),
                            input.get("expected_type").asText());
                } else {
                    result = Id.isValid(input.get("id").asText());
                }
                assertEquals(expected, result);
            }));
        }
        return tests;
    }

    // ─── ids.type_of ───

    @TestFactory
    List<DynamicTest> typeOfConformance() throws IOException {
        JsonNode fixture = loadFixture("ids/type-of.json");
        List<DynamicTest> tests = new ArrayList<>();
        for (JsonNode t : fixture.get("tests")) {
            String id = t.get("id").asText();
            String desc = t.get("description").asText();
            tests.add(DynamicTest.dynamicTest("[" + id + "] " + desc, () -> {
                JsonNode input = t.get("input");
                JsonNode expected = t.get("expected");
                if (expected.has("error")) {
                    Class<? extends RuntimeException> ctor =
                            errorClassForSpecName(expected.get("error").asText());
                    assertThrows(ctor, () -> Id.typeOf(input.get("id").asText()));
                } else {
                    assertEquals(expected.get("result").asText(),
                            Id.typeOf(input.get("id").asText()));
                }
            }));
        }
        return tests;
    }
}
