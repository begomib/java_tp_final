package com.techlab.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PedidoRequest {
    @NotNull
    private Integer usuarioId;

    @NotEmpty
    @Valid
    private List<LineaPedidoRequest> lineas;

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
    public List<LineaPedidoRequest> getLineas() { return lineas; }
    public void setLineas(List<LineaPedidoRequest> lineas) { this.lineas = lineas; }
}