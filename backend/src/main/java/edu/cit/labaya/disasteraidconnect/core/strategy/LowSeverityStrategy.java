package edu.cit.labaya.disasteraidconnect.core.strategy;

import org.springframework.stereotype.Component;

@Component
public class LowSeverityStrategy implements SeverityStrategy {
    @Override public String getLevel() { return "Low"; }
    @Override public void handle(String disasterId, String title) {
        System.out.println("[LOW] Disaster '" + title + "' (" + disasterId + ") recorded.");
    }
}