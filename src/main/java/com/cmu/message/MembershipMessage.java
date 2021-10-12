package com.cmu.message;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author gongyiming
 */
@Data
@AllArgsConstructor
public class MembershipMessage {
    /**
     * target server replica id
     */
    private Integer replicaId;
    /**
     * true = add, false = remove
     */
    private Boolean addOrRemove;

    @Override
    public String toString() {
        return "Membership change: " +
                "<replicaId=" + replicaId +
                (addOrRemove ? ", add" : ", remove") +
                '>';
    }
}
