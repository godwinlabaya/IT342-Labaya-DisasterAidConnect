package edu.cit.labaya.disasteraidconnect.core.strategy;

public interface SeverityStrategy {
    String getLevel();
    void handle(String disasterId, String title);
}