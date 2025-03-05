package com.example.model;

import java.util.ArrayList;
import java.util.List;

public class ItemIndex {
    private final List<ItemRecord> byId = new ArrayList<>();

    public void add(ItemRecord record) {
        byId.add(record);
    }
}
