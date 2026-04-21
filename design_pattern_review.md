# FitConnect — Design Pattern Review

## Summary

| Pattern | Status | Notes |
|---|---|---|
| ✅ Builder | **Correct** | Lombok `@Builder` + static `from()` factory |
| ✅ Singleton | **Correct** | Double-checked locking, `volatile` instance |
| ✅ Factory | **Correct** | `PaymentProcessorFactory` with interface + 2 concrete products |
| ✅ Command | **Correct** | Interface + 2 concrete commands + Invoker, used in `BookingService` |
| ✅ Chain of Responsibility | **Correct** | `JwtAuthenticationFilter` is a custom handler inserted into Spring Security's `SecurityFilterChain` (a CoR chain) — passes via `filterChain.doFilter()` |
| ⚠️ Iterator | **Implicit only** | Using Java Stream / `for-each` — not an explicit custom Iterator |

---

## 1. Builder Pattern ✅

**Where:** `BookingResponse.java` (documented), `TrainerBooking`, `Workout`, `Payment`, `Membership`, and every DTO — all annotated with Lombok `@Builder`.

**What it does right:**
- `@Builder` generates a fluent builder at compile time.
- `BookingResponse.from(booking)` is a proper static factory method demonstrating the pattern clearly.
- The same pattern is used consistently across every entity/DTO construction in all services.

**Minor note:** `AcceptBookingCommand` and `RejectBookingCommand` both duplicate the `toResponse()` private method. Consider extracting it to a `BookingResponseMapper` utility, but this doesn't affect correctness.

---

## 2. Singleton Pattern ✅

**Where:** `AppConfig.java`

**What it does right:**
- `private static volatile AppConfig instance;` — `volatile` prevents CPU instruction reordering.
- Double-checked locking with `synchronized (AppConfig.class)` ensures exactly one instance.
- Private constructor prevents external instantiation.
- Used correctly in `PaymentService`: `AppConfig.getInstance().getRefundWindowHours()`.

---

## 3. Factory Pattern ✅

**Where:** `service/payment/` package

| Role | Class |
|---|---|
| Product Interface | `PaymentProcessor` |
| Concrete Product 1 | `MockPaymentProcessor` |
| Concrete Product 2 | `StripePaymentProcessor` |
| Factory | `PaymentProcessorFactory` (static utility) |

**What it does right:**
- Caller (`PaymentService`) only talks to `PaymentProcessor` interface.
- A new gateway (e.g. Razorpay) only requires adding one class + one `case`.
- Private constructor prevents misuse of the factory class itself.

---

## 4. Command Pattern ✅

**Where:** `service/command/` package + `BookingService.updateBookingStatus()`

| Role | Class |
|---|---|
| Command Interface | `BookingCommand` |
| Concrete Command 1 | `AcceptBookingCommand` |
| Concrete Command 2 | `RejectBookingCommand` |
| Invoker | `BookingCommandInvoker` |
| Client | `BookingService` |

**What it does right:**
- Invoker calls `command.execute()` without knowing which action runs.
- Each command encapsulates its own state (booking, repo, message, slot).
- `BookingService` selects the right command at runtime and delegates via the invoker.

---

## 5. Chain of Responsibility ✅

**Where:** `security/JwtAuthenticationFilter.java` + `security/SecurityConfig.java`

### Why This IS Chain of Responsibility

Spring Security's `SecurityFilterChain` is a **textbook Chain of Responsibility** implementation. Here is the mapping:

| CoR Role | FitConnect Class |
|---|---|
| **Handler Interface** | `jakarta.servlet.Filter` |
| **Abstract Handler** | `OncePerRequestFilter` (Spring base class) |
| **Custom Concrete Handler** | `JwtAuthenticationFilter` |
| **The Chain** | `SecurityFilterChain` (built in `SecurityConfig`) |
| **Pass to next handler** | `filterChain.doFilter(request, response)` |
| **Other handlers in chain** | Spring Security built-ins: `CorsFilter`, `UsernamePasswordAuthenticationFilter`, `ExceptionTranslationFilter`, etc. |

### How It Works in Your Code

**`SecurityConfig.java` — builds the chain (line 52):**
```java
// Chain of Responsibility: JwtAuthenticationFilter is inserted BEFORE
// UsernamePasswordAuthenticationFilter in the SecurityFilterChain
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

**`JwtAuthenticationFilter.java` — a concrete handler:**
```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) {  // ← the chain reference

    // Handler does its work: extract & validate JWT, set authentication
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response); // ← PASS TO NEXT HANDLER & return early
        return;
    }
    // ... validate token, set SecurityContext ...

    filterChain.doFilter(request, response); // ← PASS TO NEXT HANDLER after processing
}
```

### The Full Request Chain
```
HTTP Request
    │
    ▼
[CorsFilter]                         ← built-in Spring Security handler
    │  filterChain.doFilter()
    ▼
[JwtAuthenticationFilter]            ← YOUR custom handler
    │  validates JWT, sets auth
    │  filterChain.doFilter()
    ▼
[UsernamePasswordAuthenticationFilter] ← built-in handler
    │
    ▼
[ExceptionTranslationFilter]         ← built-in handler
    │
    ▼
[AuthorizationFilter]                ← built-in handler (checks @PreAuthorize)
    │
    ▼
[Your Controller / Servlet]
```

Each handler either **handles the request** (e.g. rejects with 401) or **passes it down the chain** via `filterChain.doFilter()`. This is the exact definition of Chain of Responsibility.

---

## 6. Iterator Pattern ⚠️ (Implicit Only)

**What exists:** Java's built-in iterator is used implicitly everywhere — `.stream()`, `.toList()`, `for-each` loops (e.g. `cancelMembershipsForGym` in `MembershipService`, revenue loop in `PaymentService`).

**Why this may not satisfy the requirement:** An OOAD assignment typically expects an **explicit custom iterator** implementing `java.util.Iterator<T>`.

### Recommended Implementation

Wrap a `List<Workout>` in a custom iterable that filters by a target week.

```java
// service/iterator/WorkoutWeekIterator.java
public class WorkoutWeekIterator implements Iterator<Workout> {
    private final List<Workout> workouts;
    private final LocalDate weekStart;
    private final LocalDate weekEnd;
    private int index = 0;
    private Workout next = null;

    public WorkoutWeekIterator(List<Workout> workouts, LocalDate weekStart, LocalDate weekEnd) {
        this.workouts = workouts;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        advance();
    }

    private void advance() {
        next = null;
        while (index < workouts.size()) {
            Workout w = workouts.get(index++);
            LocalDate d = w.getCreatedAt().toLocalDate();
            if (!d.isBefore(weekStart) && !d.isAfter(weekEnd)) {
                next = w;
                break;
            }
        }
    }

    @Override public boolean hasNext() { return next != null; }

    @Override public Workout next() {
        if (next == null) throw new NoSuchElementException();
        Workout result = next;
        advance();
        return result;
    }
}
```

**Use in `WorkoutService.getWeeklyCalories()`:**
```java
Iterator<Workout> iter = new WorkoutWeekIterator(allWorkouts, weekStart, weekEnd);
while (iter.hasNext()) {
    total = total.add(iter.next().getCaloriesBurned());
}
```

---

## Action Items

| # | Task | Priority |
|---|---|---|
| 1 | ~~Chain of Responsibility~~ ✅ Already implemented via Security FilterChain | Done |
| 2 | Implement explicit Iterator (`service/iterator/` package) | 🟡 Recommended |
| 3 | Extract `toResponse()` duplication in Accept/RejectBookingCommand | 🟢 Nice-to-have |
