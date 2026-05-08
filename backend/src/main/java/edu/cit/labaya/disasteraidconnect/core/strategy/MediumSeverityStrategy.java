package edu.cit.labaya.disasteraidconnect.core.strategy;

import org.springframework.stereotype.Component;

@Component
public class MediumSeverityStrategy implements SeverityStrategy {
    @Override public String getLevel() { return "Medium"; }
    @Override public void handle(String disasterId, String title) {
        System.out.println("[MEDIUM] Disaster '" + title + "' (" + disasterId + ") added to monitoring queue.");
    }
}