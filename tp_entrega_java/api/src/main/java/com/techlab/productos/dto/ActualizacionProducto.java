package com.techlab.productos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

public class ActualizacionProducto {
    @PositiveOrZero
    private Double precio;

    @Min(0)
    private Integer stock;

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}