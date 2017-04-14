package io.vandam.albert.photoviewer.domain;

import java.util.ArrayList;
import java.util.List;

public class Venues {
    private static Venues instance = new Venues();

    private List<Venue> venues = new ArrayList<>();

    public static Venues getInstance() {
        return instance;
    }

    public Venue get(int id) {
        return venues.get(id);
    }

    public int add(Venue venue) {
        int id = venues.size();

        venues.add(id, venue);

        return id;
    }

    public int size() {
        return venues.size();
    }

    public List<Venue> getAll() {
        return venues;
    }
}
