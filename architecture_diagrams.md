# FitConnect Architecture Diagrams

Here are the complete class diagrams derived from your use case diagram, along with the specific design pattern implementations requested.

## 1. Complete System Class Diagram
This diagram represents the core domain model, capturing all actors, entities, and their relationships based on the functionalities outlined in the use case diagram (Booking, Memberships, Fitness Tracking, Marketplace, etc.).

```mermaid
classDiagram
    %% Core Entities
    class User {
        <<abstract>>
        -String userId
        -String name
        -String email
        -String password
        -String role
        +registerAccount()
        +login()
    }

    class GymUser {
        -int rewardPoints
        -ActivityStreak streak
        +bookTrainerSession(Trainer, TimeSlot)
        +rateAndReview(Target, Rating)
        +purchaseMembership(Gym, Plan)
        +requestRefund(PaymentId)
        +checkInToGym(Gym)
        +logWorkout(WorkoutDetails)
    }

    class GymOwner {
        -List~Gym~ managedGyms
        +createGym(Details)
        +updateGymDetails(GymId, Details)
        +viewGymMembers(GymId)
        +viewMonthlyRevenue(GymId)
        +manageEquipment(GymId)
        +postMarketplaceListing(Equipment)
    }

    class GymTrainer {
        -String profileDetails
        -List~TimeSlot~ availability
        +createUpdateProfile(Details)
        +acceptBooking(BookingId)
        +rejectBooking(BookingId)
        +viewClientBookings()
    }

    User <|-- GymUser
    User <|-- GymOwner
    User <|-- GymTrainer

    %% Gym & Facilities
    class Gym {
        -String gymId
        -String name
        -String location
        -GymOwner owner
        -List~Equipment~ equipmentList
        +getDetails()
    }

    %% Bookings & Sessions
    class Booking {
        -String bookingId
        -GymUser client
        -GymTrainer trainer
        -DateTime timeSlot
        -String status
        +validateTimeSlot()
        +checkConflict()
        +updateStatus(String)
    }

    %% Membership & Payments
    class Membership {
        -String membershipId
        -GymUser user
        -Gym gym
        -Date startDate
        -Date endDate
        -String status
        +validateActiveStatus()
    }

    class Payment {
        -String paymentId
        -float amount
        -String status
        -DateTime timestamp
        +processPayment()
        +verifySuccess()
        +issueRefund()
    }

    %% Fitness Tracking
    class Workout {
        -String workoutId
        -DateTime date
        -int durationMins
        -String workoutType
        -float caloriesBurned
        +calculateCalories()
    }

    class ActivityStreak {
        -int currentStreak
        -DateTime lastCheckIn
        +updateStreak()
    }

    %% Marketplace & Equipment
    class Equipment {
        -String equipmentId
        -String name
        -String condition
        +updateDetails()
    }

    class MarketplaceListing {
        -String listingId
        -Equipment item
        -String listingType
        -float price
        -String status
        +cancelListing()
    }

    %% Relationships
    GymOwner "1" *-- "many" Gym : owns
    Gym "1" *-- "many" GymTrainer : employs
    Gym "1" *-- "many" Equipment : houses
    GymUser "1" o-- "many" Membership : has
    GymUser "1" o-- "many" Booking : makes
    GymTrainer "1" o-- "many" Booking : receives
    GymUser "1" *-- "many" Workout : logs
    GymUser "1" *-- "1" ActivityStreak : maintains
    Membership "1" -- "1" Payment : requires
    GymOwner "1" o-- "many" MarketplaceListing : posts
    MarketplaceListing "1" o-- "1" Equipment : lists
```

## 2. Design Patterns Implementation Diagram
This diagram highlights how specific software design patterns (Factory, Builder, Singleton, Command) are integrated into the FitConnect architecture to ensure modularity, scalability, and robust object creation/execution.

```mermaid
classDiagram
    %% --------------------------------
    %% 1. Singleton Pattern
    %% Used for centralized, single-instance services like Database Connections or Payment Gateways.
    %% --------------------------------
    class DatabaseConnection {
        <<Singleton>>
        -static DatabaseConnection instance
        -Connection connection
        -DatabaseConnection()
        +static getInstance() DatabaseConnection
        +query(String sql) Result
    }

    %% --------------------------------
    %% 2. Factory Pattern
    %% Used to encapsulate the creation logic of different types of users based on registration input.
    %% --------------------------------
    class UserFactory {
        <<Interface>>
        +createUser(String type, String name, String email) User
    }
    
    class FitConnectUserFactory {
        +createUser(String type, String name, String email) User
    }
    
    class User { <<Abstract>> }
    class GymUser
    class GymOwner
    class GymTrainer

    UserFactory <|.. FitConnectUserFactory
    FitConnectUserFactory ..> GymUser : creates
    FitConnectUserFactory ..> GymOwner : creates
    FitConnectUserFactory ..> GymTrainer : creates
    User <|-- GymUser
    User <|-- GymOwner
    User <|-- GymTrainer

    %% --------------------------------
    %% 3. Builder Pattern
    %% Used to construct complex objects step-by-step, such as customized Workouts.
    %% --------------------------------
    class WorkoutBuilder {
        <<Interface>>
        +setType(String) WorkoutBuilder
        +setDuration(int) WorkoutBuilder
        +setDate(Date) WorkoutBuilder
        +setIntensity(String) WorkoutBuilder
        +build() Workout
    }
    
    class CustomWorkoutBuilder {
        -Workout workout
        +setType(String) WorkoutBuilder
        +setDuration(int) WorkoutBuilder
        +build() Workout
    }
    
    class Workout {
        -String type
        -int duration
        -Date date
    }
    
    WorkoutBuilder <|.. CustomWorkoutBuilder
    CustomWorkoutBuilder ..> Workout : builds

    %% --------------------------------
    %% 4. Command Pattern
    %% Used to encapsulate requests (like Payments or Bookings) as objects, allowing for queueing, logging, or undoing operations.
    %% --------------------------------
    class Command {
        <<Interface>>
        +execute()
        +undo()
    }
    
    class ProcessPaymentCommand {
        -PaymentService receiver
        -PaymentDetails details
        +execute()
        +undo()
    }
    
    class BookSessionCommand {
        -BookingService receiver
        -BookingDetails details
        +execute()
        +undo()
    }
    
    class Invoker {
        -Command command
        +setCommand(Command)
        +executeCommand()
    }
    
    class PaymentService {
        <<Receiver>>
        +process(PaymentDetails)
        +refund(PaymentDetails)
    }

    Command <|.. ProcessPaymentCommand
    Command <|.. BookSessionCommand
    ProcessPaymentCommand --> PaymentService : invokes
    Invoker o-- Command : executes
```
