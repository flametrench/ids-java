# Changelog

All notable changes to `dev.flametrench:ids` are recorded here.
Spec-level changes live in [`spec/CHANGELOG.md`](https://github.com/flametrench/spec/blob/main/CHANGELOG.md).

## [v0.4.0] — 2026-06-07

### Added
- Four v0.4 primitive type prefixes registered in `Id.TYPES` (spec@d170484):
  - `aud` → `audit_event` (ADR 0019)
  - `file` → `file_metadata` (ADR 0020)
  - `flag` → `feature_flag` (ADR 0021)
  - `not` → `notification` (ADR 0022)

## [v0.3.0] — 2026-06-06

### Added
- `pat` type prefix registered for personal access tokens (ADR 0016).

## [v0.2.0] — 2026-04-30

### Released
- v0.2 stable cutoff. No functional changes from `v0.2.0-rc.2` — same source, version bumped to drop the `-rc` suffix at the spec v0.2.0 freeze. Maven Central publication is gated on Sonatype Central Portal credential regeneration; until that unblocks, the `0.2.0` jar is built and validated locally (`mvn -P release verify -Dgpg.skip=true`).

## [v0.2.0-rc.2] — 2026-04-27

### Added
- New `shr` type prefix registered in `Id.TYPES` for the v0.2 share-token primitive ([ADR 0012](https://github.com/flametrench/spec/blob/main/decisions/0012-share-tokens.md)). `Id.encode("shr", uuid)`, `Id.decode("shr_…")`, and `Id.generate("shr")` now work; the share token store in `dev.flametrench:authz` consumes this prefix.

## [v0.2.0-rc.1] — 2026-04-25

Initial v0.2 release-candidate. Added the `mfa` prefix per ADR 0008.

For pre-rc history, see git tags.
