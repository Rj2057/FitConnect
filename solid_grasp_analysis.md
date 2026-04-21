# FitConnect — SOLID & GRASP Design Principles Analysis

---

## SOLID Principles

### S — Single Responsibility Principle (SRP)
> *"A class should have only one reason to change."*

Every layer in FitConnect has exactly one job:

| Class | Single Responsibility |
|---|---|
| `BookingController` | Receive HTTP requests for bookings, delegate to service |
| `BookingService` | Booking business logic (validation, status transitions) |
| `TrainerBookingRepository` | Database access for trainer bookings |
| `JwtService` | JWT token generation, parsing, and validation only |
| `JwtAuthenticationFilter` | Extract JWT from request and set Spring Security context |
| `AppConfig` | Hold application-wide configuration values |
| `PaymentProcessorFactory` | Decide which `PaymentProcessor` to instantiate |
| `MockPaymentProcessor` | Simulate a payment (test/dev environment) |
| `StripePaymentProcessor` | Process real Stripe payments |
| `BookingCommandInvoker` | Call `execute()` on any `BookingCommand` — nothing else |
| `AcceptBookingCommand` | Encapsulate the logic of accepting a booking |
| `RejectBookingCommand` | Encapsulate the logic of rejecting a booking |
| `CurrentUserService` | Retrieve the authenticated user from SecurityContext |
| `BookingResponse` (DTO) | Carry booking data across layer boundary |

**Evidence — `BookingController` only routes, never contains logic:**
```java
@PostMapping
public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
    return ResponseEntity.ok(bookingService.createBooking(request));  // pure delegation
}
```

**Evidence — `JwtService` is only responsible for tokens:**
```java
public String extractUsername(String token) { ... }
public String generateToken(UserDetails userDetails) { ... }
public boolean isTokenValid(String token, UserDetails userDetails) { ... }
```

---

### O — Open/Closed Principle (OCP)
> *"Open for extension, closed for modification."*

**Payment Gateways — `PaymentProcessor` interface:**
```java
// To add Razorpay, you create ONE new class — nothing else changes:
public class RazorpayPaymentProcessor implements PaymentProcessor {
    public void process() { /* Razorpay logic */ }
}

// PaymentProcessorFactory only needs one new case:
case "RAZORPAY" -> new RazorpayPaymentProcessor();
```
`PaymentService` is **never modified** when a new gateway is added.

**Booking Actions — `BookingCommand` interface:**
```java
// To add a RESCHEDULE action, you add one class — BookingCommandInvoker never changes:
public class RescheduleBookingCommand implements BookingCommand {
    public BookingResponse execute() { ... }
}
```

**Security Filter Chain:**
New security concerns (rate limiting, IP filtering) are added as new `Filter` classes inserted into the `SecurityFilterChain` — `JwtAuthenticationFilter` is never touched.

---

### L — Liskov Substitution Principle (LSP)
> *"Subtypes must be substitutable for their base type without altering correctness."*

**`PaymentProcessor` substitution:**
```java
// PaymentService only uses the interface reference:
PaymentProcessorFactory.getProcessor(request.getPaymentType()).process();
// Whether it's Mock or Stripe, the call is identical and correct
```

**`BookingCommand` substitution:**
```java
// BookingCommandInvoker accepts any BookingCommand:
public BookingResponse invoke(BookingCommand command) {
    return command.execute();  // works for Accept, Reject, or any future command
}
```

Both `MockPaymentProcessor` and `StripePaymentProcessor` honour the `process()` contract (throw `PaymentException` on failure, succeed silently). Both command implementations honour the `execute()` contract (return a valid `BookingResponse`).

---

### I — Interface Segregation Principle (ISP)
> *"Clients should not be forced to depend on interfaces they don't use."*

**`PaymentProcessor` — minimal, focused interface:**
```java
public interface PaymentProcessor {
    void process();   // exactly one method — nothing extraneous
}
```

**`BookingCommand` — minimal, focused interface:**
```java
public interface BookingCommand {
    BookingResponse execute();   // exactly one method
}
```

Spring Data `JpaRepository<T, ID>` subinterfaces (e.g. `TrainerBookingRepository`) only declare the query methods each service actually needs — unused repository methods from the parent are never forced onto callers.

---

### D — Dependency Inversion Principle (DIP)
> *"High-level modules should not depend on low-level modules. Both should depend on abstractions."*

**Constructor injection throughout every service — depend on abstraction, not concretion:**
```java
// BookingService depends on the INTERFACE TrainerBookingRepository (JPA),
// not on a specific DB driver or ORM implementation:
public BookingService(TrainerBookingRepository bookingRepository,
                      CurrentUserService currentUserService,
                      TrainerService trainerService) { ... }
```

**`PaymentService` does not import `MockPaymentProcessor` or `StripePaymentProcessor`:**
```java
import com.fitconnect.service.payment.PaymentProcessorFactory;
// PaymentService only knows the PaymentProcessor interface — it never knows which impl runs
PaymentProcessorFactory.getProcessor(request.getPaymentType()).process();
```

**`BookingService` depends on `BookingCommand` interface, not on Accept/Reject:**
```java
BookingCommand command;   // ← abstraction
if ("ACCEPT".equals(action)) command = new AcceptBookingCommand(...);
else                          command = new RejectBookingCommand(...);
return invoker.invoke(command);   // invoker only sees the interface
```

---

## GRASP Principles

### 1. Information Expert
> *"Assign responsibility to the class that has the information needed to fulfil it."*

| Responsibility | Expert Class | Reason |
|---|---|---|
| Build a `BookingResponse` from a `TrainerBooking` | `BookingResponse.from(booking)` | It knows all its own fields |
| Calculate calories burned | `WorkoutService.calculateCalories()` | It knows MET values and the formula |
| Validate JWT token | `JwtService.isTokenValid()` | It holds the secret key and claim logic |
| Check refund eligibility | `PaymentService.refundPayment()` | It has `paidAt` and `REFUND_WINDOW_HOURS` |
| Check active membership | `MembershipService` | It queries the membership repository |
| Compute tiered discount | `MembershipService.getDiscountPercentage()` | It contains the discount tiers |

---

### 2. Creator
> *"Assign to class B the responsibility to create A if B contains, records, or closely uses A."*

| Creator | Creates | Justification |
|---|---|---|
| `BookingService` | `TrainerBooking` | It has trainer, user, date, slot — all constructor data |
| `PaymentService` | `Payment` | It has user, gym, amount — all constructor data |
| `BookingService` | `AcceptBookingCommand` / `RejectBookingCommand` | It has the booking and repo needed to initialise commands |
| `PaymentProcessorFactory` | `MockPaymentProcessor` / `StripePaymentProcessor` | It knows which type to create based on the request |
| `AttendanceService` | `Attendance` | It has user, gym, timestamp |
| `AuthService` | `User` | It has name, email, password, role |

---

### 3. Controller (GRASP)
> *"Assign the responsibility of handling system events to a non-UI class that represents the overall system."*

Every `@RestController` class is the GRASP Controller for its domain:

| GRASP Controller | System Events It Handles |
|---|---|
| `BookingController` | Create booking, rate booking, update booking status |
| `GymController` | Create gym, update gym, review gym |
| `PaymentController` | Process payment, verify payment, request refund |
| `MembershipController` | Create membership, confirm payment, update status |
| `WorkoutController` | Log workout, get history, weekly calories |
| `AttendanceController` | Gym check-in |
| `StreakController` | Update streak, use pause token |
| `TrainerController` | Update trainer profile |

Controllers **never contain business logic** — they delegate to the service layer.

---

### 4. Low Coupling
> *"Assign responsibilities so that coupling remains low."*

| Decoupling Point | How |
|---|---|
| `BookingCommandInvoker` ↔ concrete commands | Invoker only imports `BookingCommand` interface |
| `PaymentService` ↔ payment gateways | Service only imports `PaymentProcessorFactory` |
| All services ↔ database | Services only import JPA repository interfaces |
| `JwtAuthenticationFilter` ↔ authentication logic | Filter delegates validation to `JwtService` |
| Controllers ↔ business logic | Controllers only call service methods |
| `CurrentUserService` ↔ `SecurityContextHolder` | All services call `currentUserService.getCurrentUser()` — only one class touches Spring Security context |

---

### 5. High Cohesion
> *"Keep related responsibilities in one place; avoid 'God classes'."*

Each service class manages **one domain concept**:

| Service | Single Domain |
|---|---|
| `BookingService` | Trainer session bookings |
| `PaymentService` | Payments & refunds |
| `MembershipService` | Gym memberships |
| `WorkoutService` | Workout tracking & calories |
| `StreakService` | Activity streaks & pause tokens |
| `AttendanceService` | Gym check-ins |
| `GymService` | Gym CRUD & reviews |
| `EquipmentMarketplaceService` | Equipment rental marketplace |
| `TrainerService` | Trainer profiles & rating |
| `AuthService` | Registration & login |
| `JwtService` | JWT token operations |
| `CurrentUserService` | Security context user lookup |
| `AppConfig` | Application configuration |

No class is responsible for more than its domain.

---

### 6. Polymorphism
> *"When related alternatives vary by type, use polymorphism to handle them."*

**Payment gateway selection — runtime polymorphism:**
```java
// No if-else in the caller. The type determines behaviour:
PaymentProcessorFactory.getProcessor("STRIPE").process();  // → StripePaymentProcessor
PaymentProcessorFactory.getProcessor("MOCK").process();    // → MockPaymentProcessor
```

**Booking action selection — runtime polymorphism:**
```java
// No if-else in the invoker. The concrete type determines behaviour:
invoker.invoke(new AcceptBookingCommand(...));  // → sets CONFIRMED
invoker.invoke(new RejectBookingCommand(...));  // → sets CANCELLED
```

**Security filter polymorphism:**
```java
// FilterChain treats all filters uniformly via the Filter interface
// JwtAuthenticationFilter.doFilterInternal() overrides the template method in OncePerRequestFilter
```

---

### 7. Pure Fabrication
> *"Assign a highly cohesive set of responsibilities to an artificial class not in the problem domain."*

| Fabricated Class | Why It Doesn't Exist in the Domain |
|---|---|
| `PaymentProcessorFactory` | No real-world "payment factory" — created purely to handle creation responsibility |
| `BookingCommandInvoker` | No real-world "booking invoker" — created to decouple trigger from action |
| `AppConfig` | No real-world configuration object — created for Singleton + central config |
| `JwtService` | No real-world JWT service — created to keep token logic cohesive |
| `CurrentUserService` | No domain concept — created to isolate Spring Security context from business services |
| `BookingCommand` (interface) | No real-world "booking command" — created for OCP + Command pattern |

---

### 8. Indirection
> *"Assign responsibility to an intermediate object to decouple two components."*

| Indirection Object | Decouples |
|---|---|
| `PaymentProcessorFactory` | `PaymentService` from `MockPaymentProcessor` / `StripePaymentProcessor` |
| `BookingCommandInvoker` | `BookingService` from `AcceptBookingCommand` / `RejectBookingCommand` |
| `CurrentUserService` | All service classes from Spring `SecurityContextHolder` |
| `JwtService` | `JwtAuthenticationFilter` from raw JJWT library details |
| Repository interfaces | Service layer from Hibernate/JPA implementation |

---

### 9. Protected Variations
> *"Protect elements from variations in other elements by wrapping the point of variation with a stable interface."*

| Point of Variation | Protected By |
|---|---|
| Payment gateway may change (Mock → Stripe → Razorpay) | `PaymentProcessor` interface |
| Booking action may add new types (Reschedule, Cancel) | `BookingCommand` interface |
| Authentication mechanism may change | `jakarta.servlet.Filter` + `SecurityFilterChain` |
| Database implementation may change | Spring Data `JpaRepository<T, ID>` interfaces |
| User identity source may change | `CurrentUserService` abstraction |
| JWT library may change | `JwtService` wraps JJWT — callers never use JJWT directly |

---

## Consolidated Reference Table

| Principle | Type | Key Classes |
|---|---|---|
| Single Responsibility | SOLID-S | All Controllers, Services, DTOs |
| Open/Closed | SOLID-O | `PaymentProcessor`, `BookingCommand` |
| Liskov Substitution | SOLID-L | `MockPaymentProcessor`, `StripePaymentProcessor`, `AcceptBookingCommand`, `RejectBookingCommand` |
| Interface Segregation | SOLID-I | `PaymentProcessor.process()`, `BookingCommand.execute()` |
| Dependency Inversion | SOLID-D | Constructor injection, `PaymentProcessor`, `BookingCommand`, Repository interfaces |
| Information Expert | GRASP | `BookingResponse.from()`, `JwtService`, `MembershipService.calculateMembershipAmount()` |
| Creator | GRASP | `BookingService` (creates `TrainerBooking`, commands), `PaymentProcessorFactory` |
| Controller | GRASP | All `@RestController` classes |
| Low Coupling | GRASP | `BookingCommandInvoker`, `PaymentService`, `CurrentUserService` |
| High Cohesion | GRASP | All service classes (one domain each) |
| Polymorphism | GRASP | `PaymentProcessor`, `BookingCommand`, `Filter` |
| Pure Fabrication | GRASP | `PaymentProcessorFactory`, `BookingCommandInvoker`, `AppConfig`, `CurrentUserService` |
| Indirection | GRASP | `PaymentProcessorFactory`, `BookingCommandInvoker`, `CurrentUserService`, `JwtService` |
| Protected Variations | GRASP | `PaymentProcessor`, `BookingCommand`, `JpaRepository`, `Filter` |
