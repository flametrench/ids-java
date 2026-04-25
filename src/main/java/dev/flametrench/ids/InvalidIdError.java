// Copyright 2026 NDC Digital, LLC
// SPDX-License-Identifier: Apache-2.0

package dev.flametrench.ids;

/**
 * Thrown when a string is not a syntactically valid Flametrench
 * wire-format ID — wrong length, non-hex payload, missing separator,
 * Nil/Max UUID, or non-v1-v8 version nibble.
 */
public class InvalidIdError extends RuntimeException {
    public InvalidIdError(String message) {
        super(message);
    }
}
