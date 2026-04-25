// Copyright 2026 NDC Digital, LLC
// SPDX-License-Identifier: Apache-2.0

package dev.flametrench.ids;

import java.util.Objects;

/**
 * The shape returned by {@link Id#decode(String)}. {@code uuid} is in
 * canonical 8-4-4-4-12 dashed form (RFC 4122 / 9562).
 */
public record DecodedId(String type, String uuid) {
    public DecodedId {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(uuid, "uuid");
    }
}
