package com.epam.deltix.qsrv.test.messages;

import com.epam.deltix.timebase.messages.SchemaElement;

/**
 * Specifies how long the order remains in effect
 */
@SchemaElement(
        name = "deltix.timebase.api.messages.trade.TimeInForce",
        title = "TimeInForce"
)
public enum TimeInForce {
    /**
     */
    @SchemaElement(
            name = "DAY"
    )
    DAY(0),

    /**
     */
    @SchemaElement(
            name = "GOOD_TILL_CANCEL"
    )
    GOOD_TILL_CANCEL(1),

    /**
     */
    @SchemaElement(
            name = "AT_THE_OPENING"
    )
    AT_THE_OPENING(2),

    /**
     */
    @SchemaElement(
            name = "IMMEDIATE_OR_CANCEL"
    )
    IMMEDIATE_OR_CANCEL(3),

    /**
     */
    @SchemaElement(
            name = "FILL_OR_KILL"
    )
    FILL_OR_KILL(4),

    /**
     */
    @SchemaElement(
            name = "GOOD_TILL_CROSSING"
    )
    GOOD_TILL_CROSSING(5),

    /**
     */
    @SchemaElement(
            name = "GOOD_TILL_DATE"
    )
    GOOD_TILL_DATE(6),

    /**
     */
    @SchemaElement(
            name = "AT_THE_CLOSE"
    )
    AT_THE_CLOSE(7);

    private final int value;

    TimeInForce(int value) {
        this.value = value;
    }

    public int getNumber() {
        return this.value;
    }

    public static TimeInForce valueOf(int number) {
        switch (number) {
            case 0: return DAY;
            case 1: return GOOD_TILL_CANCEL;
            case 2: return AT_THE_OPENING;
            case 3: return IMMEDIATE_OR_CANCEL;
            case 4: return FILL_OR_KILL;
            case 5: return GOOD_TILL_CROSSING;
            case 6: return GOOD_TILL_DATE;
            case 7: return AT_THE_CLOSE;
            default: return null;
        }
    }

    public static TimeInForce strictValueOf(int number) {
        final TimeInForce value = valueOf(number);
        if (value == null) {
            throw new IllegalArgumentException("Enumeration 'TimeInForce' does not have value corresponding to '" + number + "'.");
        }
        return value;
    }
}
