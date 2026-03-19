package com.appointment.model;

public enum LocationType {
    PROVINCE,   // Level 1: Top level
    DISTRICT,   // Level 2: Under Province
    SECTOR,     // Level 3: Under District
    CELL,       // Level 4: Under Sector
    VILLAGE     // Level 5: Under Cell
}
