package com.cmu.ldf;

import com.cmu.message.MembershipMessage;

/**
 * @author gongyiming
 */
public interface Report {
    /**
     * report to higher level.
     * @param membershipMessage member change message
     */
    default void report(MembershipMessage membershipMessage) {
    }
}
