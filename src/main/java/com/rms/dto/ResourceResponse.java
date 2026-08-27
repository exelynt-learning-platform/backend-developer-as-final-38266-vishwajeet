package com.rms.dto;

import java.math.BigDecimal;

public class ResourceResponse {

    private Long id;
    private String name;
    private String description;
    private String type;
    private Boolean available;
    private BigDecimal price;

    public ResourceResponse() {
    }

    public ResourceResponse(
            Long id,
            String name,
            String description,
            String type,
            Boolean available,
            BigDecimal price) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.available = available;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}