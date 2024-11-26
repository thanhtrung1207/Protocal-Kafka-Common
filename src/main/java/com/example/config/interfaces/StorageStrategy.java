
package com.example.config.interfaces;
import com.example.model.ErrorRecord;

public interface StorageStrategy {
    void store(ErrorRecord errorRecord);
}
