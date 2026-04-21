# FitConnect UML Diagrams

This document contains the Activity, State, and Class diagrams for the FitConnect application.

## 1. Activity Diagram: Trainer Booking Process

This activity diagram illustrates the process of a user booking a gym trainer.

```mermaid
flowchart TD
    Start((Start)) --> Login[User logs into FitConnect]
    Login --> Browse[Browse available Trainers]
    Browse --> Select[Select Trainer, Date, and Time Slot]
    Select --> CheckAvailability{Is slot available?}
    
    CheckAvailability -->|No| Browse
    CheckAvailability -->|Yes| ConfirmDetails[Confirm Booking Details]
    
    ConfirmDetails --> Payment{Proceed to Payment}
    Payment -->|Cancel| CancelFlow[Cancel Booking Process]
    CancelFlow --> EndFlow((End))
    
    Payment -->|Pay| PaymentGateway[Process PaymentGateway]
    PaymentGateway --> PaymentSuccess{Payment Successful?}
    
    PaymentSuccess -->|No| RetryPayment[Prompt to Retry or Cancel]
    RetryPayment --> Payment
    
    PaymentSuccess -->|Yes| UpdateSystem[Update Booking Status to CONFIRMED]
    UpdateSystem --> NotifyUser[Notify User via Email/App]
    UpdateSystem --> NotifyTrainer[Notify Trainer of new booking]
    
    NotifyUser --> EndFlow
    NotifyTrainer --> EndFlow
```

## 2. State Diagram: Trainer Booking Lifecycle

This state diagram shows the different states a `TrainerBooking` object can be in throughout its lifecycle.

```mermaid
stateDiagram-v2
    [*] --> PENDING : User creates booking
    
    PENDING --> CONFIRMED : Payment processed successfully
    PENDING --> CANCELLED : Payment failed / Session timeout
    
    CONFIRMED --> RESCHEDULED : User/Trainer requests new time
    RESCHEDULED --> CONFIRMED : Both parties accept new time
    
    CONFIRMED --> CANCELLED : User or Trainer cancels
    RESCHEDULED --> CANCELLED : User or Trainer cancels
    
    CONFIRMED --> COMPLETED : Session successfully occurs
    
    CANCELLED --> [*]
    COMPLETED --> [*]
```

## 3. General Class Diagram (Without Design Patterns)

This is a general domain model class diagram for the core entities in FitConnect.

```mermaid
classDiagram
    class User {
        -Long id
        -String email
        -String password
        -Role role
        -String name
        +login()
        +updateProfile()
    }

    class Gym {
        -Long id
        -String name
        -String location
        -Double monthlyFee
        +getDetails()
    }

    class Trainer {
        -Long id
        -String specialization
        -Double rating
        -Double hourlyRate
        +updateAvailability()
    }

    class Membership {
        -Long id
        -MembershipPlan plan
        -Date startDate
        -Date endDate
        -MembershipStatus status
        +renew()
        +cancel()
    }

    class TrainerBooking {
        -Long id
        -Date sessionDate
        -BookingStatus status
        +confirm()
        +cancel()
        +complete()
    }

    class Payment {
        -Long id
        -Double amount
        -Date paymentDate
        -PaymentStatus status
        +process()
        +refund()
    }
    
    class Workout {
        -Long id
        -Date date
        -Integer caloriesBurned
        -String exercises
    }

    User "1" --> "*" Membership : has
    User "1" --> "*" TrainerBooking : makes
    User "1" --> "*" Payment : makes
    User "1" --> "*" Workout : logs
    Gym "1" --> "1" User : owned by
    Gym "1" --> "*" Trainer : employs
    Gym "1" --> "*" Membership : offers
    TrainerBooking "*" --> "1" Trainer : involves
    Trainer "1" --> "1" User : is a
```

## 4. Class Diagram (Applying Design Patterns)

This class diagram illustrates how design patterns (Strategy, Observer, and Factory) can be applied to the FitConnect architecture to improve extensibility and maintainability.

```mermaid
classDiagram
    %% 1. Strategy Pattern for Payment Processing
    class PaymentStrategy {
        <<interface>>
        +processPayment(amount: Double): Boolean
    }
    class CreditCardPayment {
        +processPayment(amount: Double): Boolean
    }
    class UPIPayment {
        +processPayment(amount: Double): Boolean
    }
    class WalletPayment {
        +processPayment(amount: Double): Boolean
    }
    class PaymentProcessor {
        -PaymentStrategy strategy
        +setStrategy(strategy: PaymentStrategy)
        +executePayment(amount: Double): Boolean
    }
    
    PaymentStrategy <|.. CreditCardPayment
    PaymentStrategy <|.. UPIPayment
    PaymentStrategy <|.. WalletPayment
    PaymentProcessor o-- PaymentStrategy : uses

    %% 2. Observer Pattern for Event Notifications
    class BookingSubject {
        <<interface>>
        +attach(observer: NotificationObserver)
        +detach(observer: NotificationObserver)
        +notifyObservers(booking: TrainerBooking)
    }
    class TrainerBookingService {
        -List~NotificationObserver~ observers
        +attach(observer: NotificationObserver)
        +notifyObservers(booking: TrainerBooking)
        +confirmBooking(bookingId: Long)
    }
    class NotificationObserver {
        <<interface>>
        +update(booking: TrainerBooking)
    }
    class EmailNotifier {
        +update(booking: TrainerBooking)
    }
    class InAppNotifier {
        +update(booking: TrainerBooking)
    }
    
    BookingSubject <|.. TrainerBookingService
    NotificationObserver <|.. EmailNotifier
    NotificationObserver <|.. InAppNotifier
    TrainerBookingService o-- NotificationObserver : notifies

    %% 3. Factory Method Pattern for object creation
    class BookingFactory {
        <<interface>>
        +createBooking(type: String, user: User, trainer: Trainer): TrainerBooking
    }
    class StandardBookingFactory {
        +createBooking(type: String, user: User, trainer: Trainer): TrainerBooking
    }
    
    BookingFactory <|.. StandardBookingFactory
    StandardBookingFactory --> TrainerBooking : creates

    class TrainerBooking {
        -Long id
        -Date sessionDate
        -BookingStatus status
    }
```
