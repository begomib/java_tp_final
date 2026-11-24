package com.techlab.pedidos.web;

import com.techlab.pedidos.dto.EstadoPedidoRequest;
import com.techlab.pedidos.dto.PedidoRequest;
import com.techlab.pedidos.model.Pedido;
import com.techlab.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {
    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Pedido> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Pedido obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequest request) {
        Pedido pedido = service.crear(request);
        return ResponseEntity.created(URI.create("/api/pedidos/" + pedido.getId())).body(pedido);
    }

    @PatchMapping("/{id}/estado")
    public Pedido cambiarEstado(@PathVariable Integer id, @Valid @RequestBody EstadoPedidoRequest request) {
        return service.cambiarEstado(id, request.getEstado());
    }
}