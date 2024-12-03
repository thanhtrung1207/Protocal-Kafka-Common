package com.example.until;

public interface Duplicator {
    /**
     * 
     * @param key
     * @return
     */
    boolean check(String key);

    /**
     * open resources
     */
    void open();
    /**
     * close resources
     */
    void close();
}