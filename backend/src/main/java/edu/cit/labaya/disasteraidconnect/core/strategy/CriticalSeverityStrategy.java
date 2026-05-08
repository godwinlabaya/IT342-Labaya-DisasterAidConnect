package edu.cit.labaya.disasteraidconnect.core.strategy;

import org.springframework.stereotype.Component;

@Component
public class CriticalSeverityStrategy implements SeverityStrategy {
    @Override public String getLevel() { return "Critical"; }
    @Override public void handle(String disasterId, String title) {
        System.out.println("[CRITICAL] Disaster '" + title + "' (" + disasterId + ") requires immediate response!");
    }
}