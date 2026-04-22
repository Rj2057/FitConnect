# FitConnect Class Diagrams

Here are the class diagrams requested, generated using Mermaid.

## 1. Clean Domain Model (Without Design Patterns)
This diagram maps out the core business entities and their relationships.

```mermaid
classDiagram
    class User {
        +Long id
        +String name
        +String email
        +String password
        +Role role
    }

    class Gym {
        +Long id
        +String name
        +String address
        +String facilities
        +BigDecimal monthlyFee
    }

    class Trainer {
        +Long id
        +String specialization
        +BigDecimal hourlyRate
        +String bio
    }

    class Membership {
        +Long id
        +LocalDate startDate
        +LocalDate endDate
        +MembershipStatus status
    }

    class Payment {
        +Long id
        +BigDecimal amount
        +PaymentStatus status
        +LocalDateTime paidAt
    }

    class TrainerBooking {
        +Long id
        +LocalDate date
        +String timeSlot
        +BookingStatus status
    }

    class Workout {
        +Long id
        +String exerciseName
        +Integer reps
        +BigDecimal weight
        +Integer duration
        +BigDecimal caloriesBurned
    }

    class Attendance {
        +Long id
        +LocalDateTime checkInTime
    }

    class Equipment {
        +Long id
        +String name
        +String condition
    }

    class GymReview {
        +Long id
        +Integer rating
        +String comment
    }

    class Streak {
        +Long id
        +Integer currentStreak
        +Integer longestStreak
        +LocalDate lastActivityDate
    }

    User "1" -- "0..*" Gym : owns
    User "1" -- "0..1" Trainer : has profile
    User "1" -- "0..*" Membership : holds
    User "1" -- "0..*" Payment : makes
    User "1" -- "0..*" Workout : logs
    User "1" -- "0..*" Attendance : records
    User "1" -- "0..*" TrainerBooking : books
    User "1" -- "1" Streak : maintains
    
    Gym "1" -- "0..*" Membership : offers
    Gym "1" -- "0..*" Equipment : has
    Gym "1" -- "0..*" GymReview : receives
    Gym "1" -- "0..*" Payment : receives
    Gym "1" -- "0..*" Attendance : hosted
    
    Trainer "1" -- "0..*" TrainerBooking : provides
```

<hr/>

## 2. Design Pattern Implementations

This diagram illustrates how specific design patterns are implemented in the FitConnect platform.

```mermaid
classDiagram
    %% Factory Pattern
    namespace FactoryPattern {
        class PaymentProcessorFactory {
            +getProcessor(String type)$ PaymentProcessor
        }
        
        class PaymentProcessor {
            <<interface>>
            +process() void
        }
        
        class StripePaymentProcessor {
            +process() void
        }
        
        class PaypalPaymentProcessor {
            +process() void
        }
        
        class RazorpayPaymentProcessor {
            +process() void
        }
        
        class MockPaymentProcessor {
            +process() void
        }
        
        class PaymentService {
            +processPayment(PaymentRequest) PaymentResponse
        }
    }
    
    PaymentProcessorFactory ..> PaymentProcessor : creates
    PaymentProcessor <|.. StripePaymentProcessor : implements
    PaymentProcessor <|.. PaypalPaymentProcessor : implements
    PaymentProcessor <|.. RazorpayPaymentProcessor : implements
    PaymentProcessor <|.. MockPaymentProcessor : implements
    PaymentService --> PaymentProcessorFactory : uses
    
    %% Builder Pattern
    namespace BuilderPattern {
        class User {
            -Long id
            -String name
            -String email
            +builder()$ UserBuilder
        }
        
        class UserBuilder {
            -Long id
            -String name
            -String email
            +id(Long) UserBuilder
            +name(String) UserBuilder
            +email(String) UserBuilder
            +build() User
        }
    }
    
    User +-- UserBuilder : inner class
    UserBuilder ..> User : builds
    
    %% Command Pattern
    namespace CommandPattern {
        class BookingCommand {
            <<interface>>
            +execute() BookingResponse
        }
        
        class AcceptBookingCommand {
            -TrainerBooking booking
            -TrainerBookingRepository repo
            +execute() BookingResponse
        }
        
        class RejectBookingCommand {
            -TrainerBooking booking
            -TrainerBookingRepository repo
            +execute() BookingResponse
        }
        
        class BookingCommandInvoker {
            +executeCommand(BookingCommand) BookingResponse
        }
        
        class BookingService {
            +updateBookingStatus() BookingResponse
        }
    }
    
    BookingCommand <|.. AcceptBookingCommand : implements
    BookingCommand <|.. RejectBookingCommand : implements
    BookingCommandInvoker --> BookingCommand : executes
    BookingService --> BookingCommandInvoker : uses
    BookingService ..> AcceptBookingCommand : creates
    BookingService ..> RejectBookingCommand : creates

    %% Chain of Responsibility Pattern
    namespace ChainOfResponsibility {
        class FilterChain {
            <<interface>>
            +doFilter(ServletRequest, ServletResponse)
        }
        
        class OncePerRequestFilter {
            <<abstract>>
            +doFilterInternal()
        }
        
        class JwtAuthenticationFilter {
            -JwtService jwtService
            -CustomUserDetailsService userDetailsService
            +doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)
        }
        
        class SecurityConfig {
            +securityFilterChain(HttpSecurity) SecurityFilterChain
        }
    }
    
    OncePerRequestFilter <|-- JwtAuthenticationFilter : extends
    JwtAuthenticationFilter --> FilterChain : passes to next
    SecurityConfig --> JwtAuthenticationFilter : registers
```
