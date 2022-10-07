package com.vardi.model;

import java.math.BigDecimal;
import java.util.Date;

public class VehicleBuilder {
    private String name;
    private BigDecimal lat;
    private BigDecimal lng;
    private Date timestamp;

    public VehicleBuilder withTimestamp(Date date) {
        this.timestamp = date;
        return this;
    }

    public VehicleBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public VehicleBuilder withLat(BigDecimal lat) {
        this.lat = lat;
        return this;
    }

    public VehicleBuilder withLng(BigDecimal lng) {
        this.lng = lng;
        return this;
    }

    public VehiclePosition build() {
        return new VehiclePosition(name, lat, lng, timestamp);
    }
}
