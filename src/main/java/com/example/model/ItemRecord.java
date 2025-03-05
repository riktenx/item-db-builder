package com.example.model;

import lombok.Builder;

@Builder
public class ItemRecord {
    private final int id;
    private final String name;
    private final String iconPng;
}
