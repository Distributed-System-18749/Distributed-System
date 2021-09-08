package com.cmu.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author gongyiming
 */
@Data
@AllArgsConstructor
public class HeartbeatMessage implements Serializable {
    private Integer replicaId;
    private Integer num;

    public void incNum() {
        num++;
    }

    @Override
    public String toString() {
        return "<" +
                "replicaId=" + replicaId +
                ", num=" + num +
                '>';
    }
}
