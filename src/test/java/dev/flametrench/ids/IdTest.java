// Copyright 2026 NDC Digital, LLC
// SPDX-License-Identifier: Apache-2.0

package dev.flametrench.ids;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Java ids API. Mirrors the Node + PHP + Python unit
 * suites; behavior is consistent across all four SDKs.
 */
class IdTest {

    @Test
    void encodeProducesCanonicalWireFormat() {
        String result = Id.encode("usr", "0190f2a8-1b3c-7abc-8123-456789abcdef");
        assertEquals("usr_0190f2a81b3c7abc8123456789abcdef", result);
    }

    @Test
    void encodeLowercasesUppercaseInput() {
        String result = Id.encode("org", "0190F2A8-1B3C-7ABC-8123-456789ABCDEF");
        assertEquals("org_0190f2a81b3c7abc8123456789abcdef", result);
    }

    @Test
    void encodeRejectsUnregisteredType() {
        assertThrows(InvalidTypeError.class,
                () -> Id.encode("foo", "0190f2a8-1b3c-7abc-8123-456789abcdef"));
    }

    @Test
    void encodeRejectsMalformedUuid() {
        assertThrows(InvalidIdError.class,
                () -> Id.encode("usr", "not-a-uuid"));
    }

    @Test
    void decodeRoundTrip() {
        String original = "0190f2a8-1b3c-7abc-8123-456789abcdef";
        String encoded = Id.encode("usr", original);
        DecodedId decoded = Id.decode(encoded);
        assertEquals("usr", decoded.type());
        assertEquals(original, decoded.uuid());
    }

    @Test
    void decodeRejectsMissingSeparator() {
        assertThrows(InvalidIdError.class,
                () -> Id.decode("usr0190f2a81b3c7abc8123456789abcdef"));
    }

    @Test
    void decodeRejectsUppercaseHex() {
        assertThrows(InvalidIdError.class,
                () -> Id.decode("usr_0190F2A81B3C7ABC8123456789ABCDEF"));
    }

    @Test
    void decodeRejectsUnregisteredPrefix() {
        assertThrows(InvalidTypeError.class,
                () -> Id.decode("foo_0190f2a81b3c7abc8123456789abcdef"));
    }

    @Test
    void decodeRejectsNilUuid() {
        // All-zeros: version nibble is 0
        assertThrows(InvalidIdError.class,
                () -> Id.decode("usr_00000000000000000000000000000000"));
    }

    @Test
    void decodeRejectsMaxUuid() {
        assertThrows(InvalidIdError.class,
                () -> Id.decode("usr_ffffffffffffffffffffffffffffffff"));
    }

    @Test
    void isValidReturnsTrueForValidId() {
        assertTrue(Id.isValid("usr_0190f2a81b3c7abc8123456789abcdef"));
    }

    @Test
    void isValidReturnsFalseForUnregisteredPrefix() {
        assertFalse(Id.isValid("foo_0190f2a81b3c7abc8123456789abcdef"));
    }

    @Test
    void isValidWithExpectedTypeMatch() {
        assertTrue(Id.isValid("usr_0190f2a81b3c7abc8123456789abcdef", "usr"));
        assertFalse(Id.isValid("usr_0190f2a81b3c7abc8123456789abcdef", "org"));
    }

    @Test
    void typeOfReturnsThePrefix() {
        assertEquals("usr", Id.typeOf("usr_0190f2a81b3c7abc8123456789abcdef"));
    }

    @Test
    void typeOfRaisesForMalformed() {
        assertThrows(InvalidIdError.class, () -> Id.typeOf("not an id"));
    }

    @Test
    void generateProducesWellFormedIdForEveryRegisteredType() {
        Pattern pattern = Pattern.compile("^[a-z]+_[0-9a-f]{32}$");
        for (String type : Id.TYPES.keySet()) {
            String id = Id.generate(type);
            assertTrue(pattern.matcher(id).matches(),
                    "id " + id + " did not match the wire-format pattern");
            assertTrue(Id.isValid(id, type),
                    "id " + id + " did not pass isValid for type " + type);
        }
    }

    @Test
    void generateRejectsUnregisteredType() {
        assertThrows(InvalidTypeError.class, () -> Id.generate("foo"));
    }

    @Test
    void generatedIdsAreUnique() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            ids.add(Id.generate("usr"));
        }
        assertEquals(50, ids.size());
    }

    // ─── decodeAny / isValidShape — adapter helpers ───

    private static final String SAMPLE_HEX = "0190f2a81b3c7abc8123456789abcdef";

    @Test
    void decodeAnyDecodesRegisteredPrefixSameAsDecode() {
        DecodedId result = Id.decodeAny("usr_" + SAMPLE_HEX);
        assertEquals("usr", result.type());
        assertEquals("0190f2a8-1b3c-7abc-8123-456789abcdef", result.uuid());
    }

    @Test
    void decodeAnyAcceptsApplicationDefinedPrefix() {
        // 'proj' is not in TYPES — strict decode throws InvalidTypeError;
        // decodeAny accepts it.
        DecodedId result = Id.decodeAny("proj_" + SAMPLE_HEX);
        assertEquals("proj", result.type());
    }

    @Test
    void decodeAnyRejectsMalformedShape() {
        assertThrows(InvalidIdError.class, () -> Id.decodeAny("no-separator"));
        assertThrows(InvalidIdError.class,
                () -> Id.decodeAny("usr_" + SAMPLE_HEX.toUpperCase()));
        assertThrows(InvalidIdError.class, () -> Id.decodeAny("_" + SAMPLE_HEX));
        assertThrows(InvalidIdError.class,
                () -> Id.decodeAny("usr_00000000000000000000000000000000"));
    }

    @Test
    void isValidShapeAcceptsApplicationDefinedPrefix() {
        assertTrue(Id.isValidShape("proj_" + SAMPLE_HEX));
        assertTrue(Id.isValidShape("doc_" + SAMPLE_HEX));
    }

    @Test
    void isValidShapeAcceptsRegisteredPrefix() {
        assertTrue(Id.isValidShape("usr_" + SAMPLE_HEX));
    }

    @Test
    void isValidShapeRejectsMalformed() {
        assertFalse(Id.isValidShape("not an id"));
        assertFalse(Id.isValidShape("usr_" + SAMPLE_HEX.toUpperCase()));
        assertFalse(Id.isValidShape("usr_ffffffffffffffffffffffffffffffff"));
    }
}
