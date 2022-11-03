package com.qfi.battleship;

/**
 *
 */
public enum Message
{
    HIT("HIT"),
    SET("SET"),
    MISS("MISS"),
    OVER("OVER"),
    SHIPS("SHIPS"),
    CARRIER("CARRIER"),
    CRUISER("CRUISER"),
    SHUTDOWN("SHUTDOWN"),
    CONNECTED("CONNECTED"),
    SUBMARINE("SUBMARINE"),
    DESTROYER("DESTROYER"),
    BATTLESHIP("BATTLESHIP");

    private String m_msg;

    Message(String msg)
    {
        m_msg = msg;
    }

    public String getMsg()
    {
        return m_msg;
    }
}
