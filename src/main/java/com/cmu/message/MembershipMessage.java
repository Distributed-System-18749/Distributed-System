package com.cmu.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author gongyiming
 */
@Data
@AllArgsConstructor
public class MembershipMessage implements Serializable {
    /**
     * target server replica name
     */
    private String replicaName;
    /**
     * true = add, false = remove
     */
    private Boolean addOrRemove;

    @Override
    public String toString() {
        return "Membership change: " +
                "<replicaName=" + replicaName +
                (addOrRemove ? ", add" : ", remove") +
                '>';
    }
}
