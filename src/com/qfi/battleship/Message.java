package com.qfi.battleship;

/**
 * A Message enumeration representing all the types of string based messages utilized during the communications
 * between opponents. The only other messages that are sent are guesses of positions that are sent back and forth
 * between the opponents.
 *
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public enum Message
{
    HIT("HIT"),
    SET("SET"),
    MISS("MISS"),
    OVER("OVER"),
    READY("READY"),
    SHIPS("SHIPS"),
    CARRIER("CARRIER"),
    CRUISER("CRUISER"),
    SHUTDOWN("SHUTDOWN"),
    CONNECTED("CONNECTED"),
    SUBMARINE("SUBMARINE"),
    DESTROYER("DESTROYER"),
    BATTLESHIP("BATTLESHIP");

    private final String m_msg;

    /**
     * Message constructor.
     *
     * @param msg - A String representing the message enumeration.
     */
    Message(String msg)
    {
        m_msg = msg;
    }

    /**
     * Returns the message represented by the enumeration value.
     *
     * @return String - Returns an uppercase string of the enumeration name.
     */
    public String getMsg()
    {
        return m_msg;
    }
}
