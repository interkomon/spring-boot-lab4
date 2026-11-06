package ru.kovyazin.lab4.service1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Systems {
    ERP("Enterprise Resource Planning"),
    CRM("Customer Relationship Management"),
    WMS("Warehouse Management System"),
    @JsonProperty("Service 1")
    SERVICE_1("First REST service");

    private final String description;

    Systems(String description) { this.description = description; }

    @Override
    public String toString() { return this == SERVICE_1 ? "Service 1" : name(); }

    public String getDescription() { return description; }
}
