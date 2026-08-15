package com.example.places.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Місце на карті. Зберігається в БД (таблиця {@code places}).
 *
 * <p>Координати ({@link #lat}/{@link #lon}) можуть бути {@code null}, якщо
 * геокодування ще не відбулось або зовнішній API не знайшов адресу —
 * місце все одно валідне й зберігається, догеокодуємо пізніше.
 *
 * <p>Це РЕАЛЬНА реалізація (не інтерфейс): без робочого {@code @Entity}
 * не піднімається БД і не працює гео-шар.
 */
@Entity
@Table(name = "places")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(length = 50)
    private String category; // "кафе", "меморіал", "друг"...

    private Double lat;
    private Double lon;

    protected Place() {
        // потрібен JPA
    }

    public Place(String name, String address, String category) {
        this.name = name;
        this.address = address;
        this.category = category;
    }

    public void setCoordinates(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public boolean hasCoordinates() {
        return lat != null && lon != null;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
