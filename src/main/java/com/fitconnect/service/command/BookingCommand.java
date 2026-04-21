package com.fitconnect.service.command;

import com.fitconnect.dto.BookingResponse;

/**
 * Command Pattern — BookingCommand (Command Interface)
 *
 * Encapsulates a single booking action (ACCEPT or REJECT) as an object.
 * The invoker calls execute() without knowing what action is performed.
 */
public interface BookingCommand {

    /**
     * Execute this booking command and return the resulting booking state.
     */
    BookingResponse execute();
}
