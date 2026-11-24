package com.techlab.productos.repository;

import com.techlab.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByStockLessThanEqual(Integer stock);
    Optional<Producto> findFirstByNombreIgnoreCase(String nombre);
    boolean existsByCategoriaRelacionId(Integer categoriaId);
}