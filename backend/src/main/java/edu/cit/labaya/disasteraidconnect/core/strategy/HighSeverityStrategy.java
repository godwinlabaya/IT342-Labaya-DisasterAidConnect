package edu.cit.labaya.disasteraidconnect.core.strategy;

import org.springframework.stereotype.Component;

@Component
public class HighSeverityStrategy implements SeverityStrategy {
    @Override public String getLevel() { return "High"; }
    @Override public void handle(String disasterId, String title) {
        System.out.println("[HIGH] Disaster '" + title + "' (" + disasterId + ") flagged for urgent review.");
    }
}