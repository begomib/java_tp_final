package com.techlab.pedidos.repository;

import com.techlab.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByUsuarioIdOrderByFechaDesc(Integer usuarioId);
    boolean existsByUsuarioId(Integer usuarioId);
}