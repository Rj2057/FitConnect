# FitConnect State Diagram: Trainer Booking Lifecycle

This is an extensive state diagram that maps out the complete, end-to-end lifecycle of a **Trainer Booking Session**. It covers the longest chain of possible states an entity can go through in your system—from initial slot selection, through approvals, payment handling, rescheduling loops, the actual session, cancellations, and post-session reviews/refunds.

```mermaid
stateDiagram-v2
    %% Initial Setup and Approval
    [*] --> Selecting_Slot : User browses availability
    Selecting_Slot --> [*] : User abandons process
    Selecting_Slot --> Pending_Trainer_Approval : User requests slot
    
    Pending_Trainer_Approval --> Cancelled_By_User : User cancels request
    Pending_Trainer_Approval --> Rejected_By_Trainer : Trainer declines slot
    Rejected_By_Trainer --> [*]
    
    %% Payment Flow
    Pending_Trainer_Approval --> Pending_Payment : Trainer approves slot
    Pending_Payment --> Payment_Processing : User submits payment details
    Payment_Processing --> Payment_Failed : Gateway rejects payment
    Payment_Failed --> Pending_Payment : User retries payment
    Payment_Failed --> Cancelled_By_User : User gives up
    
    %% Scheduling and Modifications
    Payment_Processing --> Confirmed_Scheduled : Payment succeeds
    Confirmed_Scheduled --> Reschedule_Requested : User or Trainer wants new time
    Reschedule_Requested --> Pending_Trainer_Approval : New time proposed
    
    %% Cancellation and Refund Flows
    Confirmed_Scheduled --> Cancelled_By_User : User cancels before session
    Confirmed_Scheduled --> Cancelled_By_Trainer : Trainer cancels (emergency)
    
    Cancelled_By_Trainer --> Refund_Processing : Automatic full refund
    Cancelled_By_User --> Refund_Processing : Cancelled within allowable window
    Cancelled_By_User --> Closed_No_Refund : Cancelled too late
    
    Refund_Processing --> Refund_Completed : Funds returned to user
    Refund_Completed --> [*]
    Closed_No_Refund --> [*]

    %% Session Execution
    Confirmed_Scheduled --> In_Progress : Start time reached & Check-in
    
    In_Progress --> No_Show : User fails to attend
    No_Show --> Closed_No_Refund : Penalty applied
    
    In_Progress --> Completed_Pending_Review : Session finishes normally
    
    %% Post-Session
    Completed_Pending_Review --> Completed : User leaves a rating/review
    Completed_Pending_Review --> Completed : Auto-closed after 7 days
    
    Completed --> [*]
```
