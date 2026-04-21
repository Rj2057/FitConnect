package com.fitconnect.service.command;

import com.fitconnect.dto.BookingResponse;

/**
 * Command Pattern — BookingCommandInvoker (Invoker)
 *
 * Knows only the BookingCommand interface. Calls execute() without
 * caring whether it is Accept or Reject. Decouples the trigger
 * (BookingService) from the actual action logic.
 */
public class BookingCommandInvoker {

    /**
     * Invokes the given command and returns its result.
     *
     * @param command The concrete booking command to execute
     * @return The BookingResponse resulting from the command
     */
    public BookingResponse invoke(BookingCommand command) {
        return command.execute();
    }
}
