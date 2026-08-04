


# Service Layer Testing Plan

## Suggested Order

| # | Class | Test type | New dependency needed? |
|---|---|---|---|
| 1 | `UserDetailsServiceImpl` | Unit (Mockito) | No |
| 2 | `JwtService` | Unit (ReflectionTestUtils) | No |
| 3 | `CoverLetterService` | Unit (Mockito + SecurityContext) | No |
| 4 | `AnthropicClient` | Unit (MockWebServer) | Yes — `okhttp3:mockwebserver` |

---

## 1. `UserDetailsServiceImpl`

### What it does
Implements Spring's `UserDetailsService` contract. One method: `loadUserByUsername(email)`,
which calls `UserRepository.findByEmail()` and wraps the empty result in a
`UsernameNotFoundException`. Spring Security calls this during every authenticated request
to look up who the token belongs to.

### Why it's worth testing despite being small
It's tiny, but it sits on the critical authentication path. Spring Security requires
`UsernameNotFoundException` specifically — if you throw a different exception type, Spring's
error handling changes behaviour in subtle ways. A test pins that contract explicitly.

### Key test cases
- **Happy path — found user:** Mock the repository to return `Optional.of(user)`. Assert
  the returned `UserDetails` is the same User object and its username equals the email.
- **Failure — user not found:** Mock the repository to return `Optional.empty()`. Assert
  that `UsernameNotFoundException` is thrown. This is the case that makes Spring Security
  return 401 rather than 500.

### Mocking
Mock `UserRepository`. You want to test `UserDetailsServiceImpl`'s logic in isolation, not
also test database connectivity. Two cases, two controlled return values.

---

## 2. `JwtService`

### What it does
The security primitive everything else depends on. Three public responsibilities:
- `generateToken(UserDetails)` — builds a signed JWT with subject (email), issuedAt, expiration
- `extractEmail(String token)` — parses the JWT and returns the subject claim
- `isTokenValid(String token, UserDetails)` — compound check: email matches AND token not expired

### Why it's high priority
If this class has a bug — wrong claim name, wrong expiry logic, broken signature verification —
your entire authentication system silently fails. `isTokenValid` has two independent conditions
that can each break separately.

### Key test cases
- **Happy path — generate then extract:** Generate a token, call `extractEmail()` on it. The
  returned email should match the user's username. Verifies the sign/parse round-trip.
- **Happy path — valid token passes validation:** Generate a token, call `isTokenValid()` with
  the same user. Should return `true`.
- **Failure — wrong user fails validation:** Generate a token for user A, validate against user B.
  Should return `false`. Guards against token reuse across accounts.
- **Failure — expired token fails validation:** Generate a token with an expiration of -1ms,
  call `isTokenValid()`. Should return `false`. This is the most important edge case — a bug
  here only shows up in production days or weeks later.
- **Failure — tampered token throws:** Pass a structurally invalid string to `extractEmail()`.
  JJWT will throw `JwtException`. Documents the expected contract.

### Mocking
No mocking — `JwtService` has zero dependencies on other beans. The only challenge is the
`@Value` fields (`secretKey`, `expirationMs`), which Spring normally injects. Use
`ReflectionTestUtils.setField()` to inject them directly in your test setup. You'll need
a valid base64-encoded secret (at least 32 bytes for HMAC-SHA256) as a test constant.

---

## 3. `CoverLetterService`

### What it does
`generate(jobApplicationId, request)` does four things in sequence:
1. Gets the current user from the SecurityContext via `SecurityUtils`
2. Looks up the job application by ID **scoped to that user** (owner check)
3. Builds a prompt string using the job's fields and an optional user bio
4. Calls `AnthropicClient.generateCoverLetter()` and wraps the result in `CoverLetterResponse`

### Why it's high priority
Two reasons:
1. It has real branching logic — the prompt builder has multiple `if` checks on nullable fields
   (`location`, `workType`, `notes`, `userBio`). Any one of these could cause an NPE or produce
   an incomplete prompt silently.
2. It has a security invariant: the owner-scoped lookup. If that check is ever broken, users can
   generate cover letters using another user's job data (and leak it to the AI). That must be
   tested explicitly.

### Key test cases
- **Happy path — full prompt:** Set up a job with location, workType, notes all populated, plus
  a userBio. Assert `AnthropicClient.generateCoverLetter()` was called with a prompt containing
  the job title, company name, user's full name, location, and bio. Confirms the builder uses all
  available data.
- **Happy path — minimal prompt (nullable fields absent):** Null location, null workType, null
  notes, no userBio. Assert the call to `AnthropicClient` still happens with no NPE. Tests
  every `if` branch in the builder.
- **Happy path — response mapping:** Assert the returned `CoverLetterResponse` contains the text
  from `AnthropicClient`, and that `jobTitle` and `companyName` come from the entity (not the
  request).
- **Failure — job not found:** Mock the repository to return `Optional.empty()`. Assert
  `JobApplicationNotFoundException` is thrown. Also verify `AnthropicClient` is **never called**.
  This matters: you don't want a paid API call firing for an already-invalid request.
- **Failure — AnthropicClient throws:** Mock `AnthropicClient.generateCoverLetter()` to throw
  `CoverLetterGenerationException`. Assert the exception propagates — confirms the service doesn't
  accidentally swallow it.

### Mocking
Two mocks: `JobApplicationRepository` and `AnthropicClient`. You also need to manually set the
SecurityContext (same pattern as `CompanyServiceTest`):

```java
SecurityContextHolder.getContext().setAuthentication(
    new UsernamePasswordAuthenticationToken(mockUser, null, List.of())
);
```

Clear it in `@AfterEach` with `SecurityContextHolder.clearContext()`.

**One thing to watch:** `generate()` calls `job.getCompany().getName()` with no null guard on
`company`. Always build your mock `JobApplication` with a `Company` attached, or you'll get a
misleading NPE in the test rather than a real assertion failure.

---

## 4. `AnthropicClient`

### What it does
A thin infrastructure wrapper around Spring's `WebClient`. It:
- Builds the Anthropic-specific request payload (`AnthropicRequest` with model, maxTokens, message)
- Makes a blocking POST to `/v1/messages`
- Guards against null response, empty content list, and content blocks with no `type == "text"`
- Converts `WebClientResponseException` (HTTP 4xx/5xx from Anthropic) into `CoverLetterGenerationException`

### Why it's worth testing
The error-handling paths are the value. You want to know: does a 429 (rate limit) from Anthropic
produce a `CoverLetterGenerationException` with a clear message? Does an empty body? These are
the failures that will happen in production and are completely invisible without tests.

### Key test cases
- **Happy path — valid response:** Stub the server to return well-formed Anthropic JSON with a
  `"text"` content block. Assert the returned string is the text value from that block.
- **Failure — HTTP 4xx/5xx from Anthropic:** Stub the server to return a 429 or 500. Assert
  `CoverLetterGenerationException` is thrown. Tests the `WebClientResponseException` catch block.
- **Failure — response with no "text" block:** Stub a response whose `content` array has only a
  non-text block (e.g. `type: "tool_use"`). Assert `CoverLetterGenerationException` is thrown.
- **Failure — empty content array:** Stub a response with an empty `content: []`. Assert
  `CoverLetterGenerationException` is thrown.

### Mocking approach — this one is different
`AnthropicClient` builds its own `WebClient` in the constructor. You cannot mock `WebClient`
with Mockito without mocking an enormous fluent chain (`post() → uri() → bodyValue() →
retrieve() → bodyToMono() → block()`) which is brittle and unreadable.

The right tool is **`MockWebServer`** from OkHttp (`com.squareup.okhttp3:mockwebserver`).
It starts a real local HTTP server in your test, and you enqueue canned responses. Your
`WebClient` points at `http://localhost:{port}` instead of `api.anthropic.com`. This tests
`AnthropicClient` exactly as it runs in production — real HTTP, real JSON parsing — just
against a local server.

This requires adding one test dependency to `pom.xml`:

```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>mockwebserver</artifactId>
    <scope>test</scope>
</dependency>
```

The version is managed automatically by `spring-boot-starter-parent`.