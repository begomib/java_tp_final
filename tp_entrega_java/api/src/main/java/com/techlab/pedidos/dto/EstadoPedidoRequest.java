package com.techlab.pedidos.dto;

import com.techlab.pedidos.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public class EstadoPedidoRequest {
    @NotNull
    private EstadoPedido estado;

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
}