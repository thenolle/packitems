# Security Policy

## Supported Versions

Only the latest version of PackItems is actively supported with security updates.

| Version | Supported |
|---------|-----------|
| 1.0.x   | ✅ |
| < 1.0   | ❌ |

---

## Reporting a Vulnerability

If you discover a security vulnerability, do NOT open a public issue.

Instead, report it privately:

- GitHub Security Advisories (preferred):
  https://github.com/thenolle/packitems/security/advisories/new

or

- Contact the maintainer directly via GitHub.

---

## What counts as a vulnerability

Includes (but is not limited to):

- Remote code execution via parsing or placeholders
- Exploitable command injection via `/text`
- Permission bypass issues
- Unsafe deserialization or component injection
- Server crash exploits (DoS vectors in parser or renderer)

---

## Response timeline

- Acknowledgement: within 72 hours
- Initial fix / mitigation: depends on severity (critical issues prioritized)
- Patch release: as soon as stable fix is available

---

## Security assumptions

PackItems runs in a trusted server environment (Spigot/Paper plugin context), but:

- Input from players is always considered untrusted
- Placeholder resolvers may execute arbitrary logic
- Chat formatting system must not be used for backend execution

---

## Safe usage guidelines for contributors

- Never evaluate user input as code
- Avoid reflection-based dynamic execution
- Validate all tag parsing and placeholder keys
- Ensure no Bukkit API calls are made from async threads unless explicitly safe
