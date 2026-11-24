package com.techlab.productos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String descripcion;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private double precio;

    @Column(name = "categoria", length = 80)
    private String categoriaLegacy;

    @Transient
    private String categoriaEntrada;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoriaRelacion;

    @Size(max = 2048)
    @Column(length = 2048)
    private String imagen;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stock;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    @JsonProperty("categoria")
    public String getCategoria() {
        return categoriaRelacion == null ? categoriaLegacy : categoriaRelacion.getNombre();
    }
    @JsonProperty("categoria")
    public void setCategoria(String categoria) {
        this.categoriaEntrada = categoria;
        this.categoriaLegacy = categoria;
    }
    public String getCategoriaEntrada() { return categoriaEntrada; }
    public Categoria getCategoriaRelacion() { return categoriaRelacion; }
    public void setCategoriaRelacion(Categoria categoriaRelacion) {
        this.categoriaRelacion = categoriaRelacion;
        if (categoriaRelacion != null) this.categoriaLegacy = categoriaRelacion.getNombre();
    }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}