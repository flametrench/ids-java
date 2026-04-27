// Copyright 2026 NDC Digital, LLC
// SPDX-License-Identifier: Apache-2.0

package dev.flametrench.ids;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Wire-format identifiers for Flametrench v0.1.
 *
 * <p>The wire format is {@code {type}_{32-hex}}, where the hex payload
 * is a UUIDv7 (so generated IDs sort by creation time). The same
 * identifiers travel unchanged across the Node, PHP, Python, and Java
 * SDKs; the conformance fixture corpus enforces this mechanically.
 *
 * <p>See the upstream specification at
 * <a href="https://github.com/flametrench/spec/blob/main/docs/ids.md">docs/ids.md</a>.
 */
public final class Id {

    /**
     * Registered type prefixes for Flametrench v0.1.
     *
     * <p>Keep synchronized with the spec's reserved prefix registry.
     * Parallel implementations (Node, PHP, Python) use the same prefixes.
     */
    public static final Map<String, String> TYPES = Map.of(
            "usr", "user",
            "org", "organization",
            "mem", "membership",
            "inv", "invitation",
            "ses", "session",
            "cred", "credential",
            "tup", "authorization_tuple",
            // v0.2 — Proposed (ADR 0008)
            "mfa", "mfa_factor",
            // v0.2 — Proposed (ADR 0012)
            "shr", "share_token"
    );

    private static final int HEX_PAYLOAD_LENGTH = 32;
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-f]{32}$");
    private static final Pattern VERSION_NIBBLE_PATTERN = Pattern.compile("^[1-8]$");
    private static final Pattern UUID_CANONICAL_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private static final TimeBasedEpochGenerator UUID_V7_GEN =
            Generators.timeBasedEpochGenerator();

    private Id() {
        // utility class
    }

    /**
     * Encode a type and UUID into Flametrench wire format.
     *
     * <pre>
     * Id.encode("usr", "0190f2a8-1b3c-7abc-8123-456789abcdef")
     *   → "usr_0190f2a81b3c7abc8123456789abcdef"
     * </pre>
     *
     * @throws InvalidTypeError if the type prefix is not registered
     * @throws InvalidIdError   if the UUID is not a valid UUID string
     */
    public static String encode(String type, String uuid) {
        assertType(type);
        if (uuid == null || !UUID_CANONICAL_PATTERN.matcher(uuid).matches()) {
            throw new InvalidIdError("Value is not a valid UUID: " + uuid);
        }
        try {
            // Validate that it parses as a real UUID (catches well-formed
            // but invalid sequences like all-zero variant bits).
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new InvalidIdError("Value is not a valid UUID: " + uuid);
        }
        String hex = uuid.replace("-", "").toLowerCase();
        return type + "_" + hex;
    }

    /**
     * Decode a Flametrench wire-format ID into its type and canonical UUID.
     *
     * <pre>
     * Id.decode("usr_0190f2a81b3c7abc8123456789abcdef")
     *   → DecodedId[type=usr, uuid=0190f2a8-1b3c-7abc-8123-456789abcdef]
     * </pre>
     *
     * @throws InvalidIdError   if the ID is malformed
     * @throws InvalidTypeError if the type prefix is not registered
     */
    public static DecodedId decode(String id) {
        if (id == null) {
            throw new InvalidIdError("ID is null");
        }
        int sep = id.indexOf('_');
        if (sep == -1) {
            throw new InvalidIdError("ID missing type separator: " + id);
        }
        String type = id.substring(0, sep);
        String hex = id.substring(sep + 1);
        assertType(type);
        if (hex.length() != HEX_PAYLOAD_LENGTH || !HEX_PATTERN.matcher(hex).matches()) {
            throw new InvalidIdError(
                    "ID payload is not 32 lowercase hex characters: " + id);
        }
        // Version nibble (13th hex char, 0-indexed position 12) must be 1-8.
        // This rejects the Nil UUID (v0) and Max UUID (v15/f), which are
        // not meaningful identifiers in the Flametrench wire format.
        String versionChar = String.valueOf(hex.charAt(12));
        if (!VERSION_NIBBLE_PATTERN.matcher(versionChar).matches()) {
            throw new InvalidIdError("ID payload is not a valid UUID: " + id);
        }
        String canonical = hex.substring(0, 8) + "-" +
                hex.substring(8, 12) + "-" +
                hex.substring(12, 16) + "-" +
                hex.substring(16, 20) + "-" +
                hex.substring(20, 32);
        return new DecodedId(type, canonical);
    }

    /**
     * Decode a Flametrench wire-format ID without checking the registered-type set.
     *
     * <p>Use this for backend storage adapters that need to convert
     * wire-format object IDs to canonical UUIDs without knowing the
     * application's domain types in advance — e.g., when an authz tuple
     * has {@code objectType="proj"} and {@code objectId="proj_0190f2a8..."}.
     *
     * <p>Validates wire-format shape (separator, 32-char lowercase hex,
     * version nibble 1–8). Does NOT consult {@link #TYPES}. See
     * {@code spec/docs/ids.md}.
     *
     * @throws InvalidIdError if the ID's structure is malformed. Never
     *                        throws {@link InvalidTypeError}.
     */
    public static DecodedId decodeAny(String id) {
        if (id == null) {
            throw new InvalidIdError("ID is null");
        }
        int sep = id.indexOf('_');
        if (sep == -1) {
            throw new InvalidIdError("ID missing type separator: " + id);
        }
        String type = id.substring(0, sep);
        String hex = id.substring(sep + 1);
        if (type.isEmpty()) {
            throw new InvalidIdError("ID has empty type prefix: " + id);
        }
        if (hex.length() != HEX_PAYLOAD_LENGTH || !HEX_PATTERN.matcher(hex).matches()) {
            throw new InvalidIdError(
                    "ID payload is not 32 lowercase hex characters: " + id);
        }
        String versionChar = String.valueOf(hex.charAt(12));
        if (!VERSION_NIBBLE_PATTERN.matcher(versionChar).matches()) {
            throw new InvalidIdError("ID payload is not a valid UUID: " + id);
        }
        String canonical = hex.substring(0, 8) + "-" +
                hex.substring(8, 12) + "-" +
                hex.substring(12, 16) + "-" +
                hex.substring(16, 20) + "-" +
                hex.substring(20, 32);
        return new DecodedId(type, canonical);
    }

    /**
     * Predicate counterpart to {@link #decodeAny(String)}. Returns true
     * for any well-formed wire-format ID regardless of registry membership.
     *
     * <p>Use this when validating input from external systems that may
     * legitimately reference application-defined object types.
     */
    public static boolean isValidShape(String id) {
        try {
            decodeAny(id);
            return true;
        } catch (InvalidIdError e) {
            return false;
        }
    }

    /**
     * Check whether a string is a valid Flametrench wire-format ID.
     */
    public static boolean isValid(String id) {
        try {
            decode(id);
            return true;
        } catch (InvalidIdError | InvalidTypeError e) {
            return false;
        }
    }

    /**
     * Check whether a string is a valid Flametrench wire-format ID of the
     * given type.
     */
    public static boolean isValid(String id, String expectedType) {
        try {
            DecodedId decoded = decode(id);
            return decoded.type().equals(expectedType);
        } catch (InvalidIdError | InvalidTypeError e) {
            return false;
        }
    }

    /**
     * Extract the type prefix from a wire-format ID.
     *
     * @throws InvalidIdError   if the ID is malformed
     * @throws InvalidTypeError if the type prefix is not registered
     */
    public static String typeOf(String id) {
        return decode(id).type();
    }

    /**
     * Generate a fresh wire-format ID of the given type. Uses UUIDv7 so
     * generated IDs sort by creation time.
     *
     * @throws InvalidTypeError if the type prefix is not registered
     */
    public static String generate(String type) {
        assertType(type);
        return encode(type, UUID_V7_GEN.generate().toString());
    }

    private static void assertType(String type) {
        if (type == null || !TYPES.containsKey(type)) {
            throw new InvalidTypeError(
                    "Unregistered type prefix: '" + type + "'. Registered prefixes: "
                            + String.join(", ", TYPES.keySet()) + ".");
        }
    }
}
