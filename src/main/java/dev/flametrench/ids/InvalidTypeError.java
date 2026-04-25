// Copyright 2026 NDC Digital, LLC
// SPDX-License-Identifier: Apache-2.0

package dev.flametrench.ids;

/**
 * Thrown when a type prefix is not in the Flametrench v0.1 registered set.
 * The registry lives at https://github.com/flametrench/spec/blob/main/docs/ids.md.
 */
public class InvalidTypeError extends RuntimeException {
    public InvalidTypeError(String message) {
        super(message);
    }
}
