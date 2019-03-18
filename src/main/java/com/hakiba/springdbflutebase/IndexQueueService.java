package com.hakiba.springdbflutebase;

import java.util.List;

/**
 * @author hakiba
 */
public interface IndexQueueService {
    void enqueue(List<String> bodyList);
}
