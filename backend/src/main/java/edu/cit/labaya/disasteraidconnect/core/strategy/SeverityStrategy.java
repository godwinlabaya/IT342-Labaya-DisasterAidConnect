package edu.cit.labaya.disasteraidconnect.strategy;

/**
 * Strategy pattern for severity-level business rules.
 * Each severity (Low / Medium / High / Critical) can define
 * its own notification or escalation behaviour.
 */
public interface SeverityStrategy {

    /** Human-readable level name, e.g. "High" */
    String getLevel();

    /**
     * Called after a disaster is created or updated to this level.
     * Implement notification, escalation, or logging logic here.
     */
    void handle(String disasterId, String title);
}