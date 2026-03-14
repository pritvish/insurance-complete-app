package com.eip.workflow.saga;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Definition of the Policy Issuance saga — 6 steps.
 *
 * Flow:
 *   QUOTE_CREATED → PREMIUM_CALCULATED → PAYMENT_COLLECTED
 *     → POLICY_ISSUED → BILLING_CREATED → DOCUMENTS_GENERATED
 */
@Component
public class PolicyIssuanceSaga {

    public static final String QUOTE_CREATED       = "QUOTE_CREATED";
    public static final String PREMIUM_CALCULATED  = "PREMIUM_CALCULATED";
    public static final String PAYMENT_COLLECTED   = "PAYMENT_COLLECTED";
    public static final String POLICY_ISSUED       = "POLICY_ISSUED";
    public static final String BILLING_CREATED     = "BILLING_CREATED";
    public static final String DOCUMENTS_GENERATED = "DOCUMENTS_GENERATED";

    private static final List<String> STEPS = List.of(
            QUOTE_CREATED, PREMIUM_CALCULATED, PAYMENT_COLLECTED,
            POLICY_ISSUED, BILLING_CREATED, DOCUMENTS_GENERATED
    );

    /** Command topics emitted when a step completes to trigger the next service. */
    private static final Map<String, String> NEXT_COMMAND_TOPICS = Map.of(
            QUOTE_CREATED,      "premiumcalc.commands.calculate",
            PREMIUM_CALCULATED, "payment.commands.collect-premium",
            PAYMENT_COLLECTED,  "policy.commands.issue",
            POLICY_ISSUED,      "billing.commands.create-schedule",
            BILLING_CREATED,    "document.commands.generate"
            // DOCUMENTS_GENERATED is terminal — no outbound command
    );

    /** Compensation command topics emitted in reverse order on saga failure. */
    private static final Map<String, String> COMPENSATION_TOPICS = Map.of(
            PREMIUM_CALCULATED, "premiumcalc.commands.cancel-calculation",
            PAYMENT_COLLECTED,  "payment.commands.refund",
            POLICY_ISSUED,      "policy.commands.void",
            BILLING_CREATED,    "billing.commands.cancel-schedule",
            DOCUMENTS_GENERATED,"document.commands.revoke"
    );

    public List<String> getStepNames() {
        return STEPS;
    }

    /** Returns the next step name, or {@code null} if {@code currentStep} is terminal. */
    public String getNextStep(String currentStep) {
        int idx = STEPS.indexOf(currentStep);
        if (idx < 0 || idx >= STEPS.size() - 1) return null;
        return STEPS.get(idx + 1);
    }

    /** Returns the Kafka topic for the command that drives the next service, or {@code null} for terminal step. */
    public String getNextCommandTopic(String completedStep) {
        return NEXT_COMMAND_TOPICS.get(completedStep);
    }

    /** Returns the compensation command topic for a completed step, or {@code null} if none. */
    public String getCompensationTopic(String stepName) {
        return COMPENSATION_TOPICS.get(stepName);
    }

    public boolean isTerminalStep(String stepName) {
        return DOCUMENTS_GENERATED.equals(stepName);
    }
}
