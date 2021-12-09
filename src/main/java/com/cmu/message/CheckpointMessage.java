package com.cmu.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
/**
 * @author Yun Hong
 */
@Data
@AllArgsConstructor
public class CheckpointMessage implements Serializable{
    private String primaryName;
    private String backupName;
    private long myState;

    @Override
    public String toString() {
        return " <" + getPrimaryName() + ", " + getBackupName() + ", " + getMyState() + ">";
    }
}
