package com.eip.workflow.saga;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Definition of the Claim Processing saga — 8 steps.
 *
 * Flow:
 *   FNOL_RECEIVED → FRAUD_SCORED → COVERAGE_VERIFIED → ADJUSTER_ASSIGNED
 *     → RESERVE_SET → APPROVED → PAYMENT_INITIATED → CLOSED
 */
@Component
public class ClaimProcessingSaga {

    public static final String FNOL_RECEIVED      = "FNOL_RECEIVED";
    public static final String FRAUD_SCORED       = "FRAUD_SCORED";
    public static final String COVERAGE_VERIFIED  = "COVERAGE_VERIFIED";
    public static final String ADJUSTER_ASSIGNED  = "ADJUSTER_ASSIGNED";
    public static final String RESERVE_SET        = "RESERVE_SET";
    public static final String APPROVED           = "APPROVED";
    public static final String PAYMENT_INITIATED  = "PAYMENT_INITIATED";
    public static final String CLOSED             = "CLOSED";

    private static final List<String> STEPS = List.of(
            FNOL_RECEIVED, FRAUD_SCORED, COVERAGE_VERIFIED, ADJUSTER_ASSIGNED,
            RESERVE_SET, APPROVED, PAYMENT_INITIATED, CLOSED
    );

    private static final Map<String, String> NEXT_COMMAND_TOPICS = Map.of(
            FNOL_RECEIVED,     "fraud.commands.score-claim",
            FRAUD_SCORED,      "claims.commands.verify-coverage",
            COVERAGE_VERIFIED, "claims.commands.assign-adjuster",
            ADJUSTER_ASSIGNED, "claims.commands.set-reserve",
            RESERVE_SET,       "claims.commands.await-approval",   // internal — waits for 4-eyes
            APPROVED,          "payment.commands.pay-claim",
            PAYMENT_INITIATED, "claims.commands.close"
            // CLOSED is terminal
    );

    private static final Map<String, String> COMPENSATION_TOPICS = Map.of(
            FRAUD_SCORED,      "fraud.commands.cancel-score",
            COVERAGE_VERIFIED, "claims.commands.reopen",
            ADJUSTER_ASSIGNED, "claims.commands.unassign-adjuster",
            RESERVE_SET,       "claims.commands.release-reserve",
            APPROVED,          "claims.commands.revoke-approval",
            PAYMENT_INITIATED, "payment.commands.cancel-claim-payment"
    );

    public List<String> getStepNames() {
        return STEPS;
    }

    public String getNextStep(String currentStep) {
        int idx = STEPS.indexOf(currentStep);
        if (idx < 0 || idx >= STEPS.size() - 1) return null;
        return STEPS.get(idx + 1);
    }

    public String getNextCommandTopic(String completedStep) {
        return NEXT_COMMAND_TOPICS.get(completedStep);
    }

    public String getCompensationTopic(String stepName) {
        return COMPENSATION_TOPICS.get(stepName);
    }

    public boolean isTerminalStep(String stepName) {
        return CLOSED.equals(stepName);
    }
}
