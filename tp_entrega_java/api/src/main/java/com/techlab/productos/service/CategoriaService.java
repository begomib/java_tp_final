package com.techlab.productos.service;

import com.techlab.productos.model.Categoria;
import com.techlab.productos.repository.CategoriaRepository;
import com.techlab.productos.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository repository;
    private final ProductoRepository productos;

    public CategoriaService(CategoriaRepository repository, ProductoRepository productos) {
        this.repository = repository;
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return repository.findAll();
    }

    public Categoria crear(Categoria categoria) {
        if (repository.findByNombreIgnoreCase(categoria.getNombre().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría ya existe.");
        }
        categoria.setNombre(categoria.getNombre().trim());
        categoria.setId(null);
        return repository.save(categoria);
    }

    public Categoria actualizar(Integer id, Categoria datos) {
        Categoria categoria = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada."));
        repository.findByNombreIgnoreCase(datos.getNombre().trim())
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría ya existe.");
                });
        categoria.setNombre(datos.getNombre().trim());
        return repository.save(categoria);
    }

    public void eliminar(Integer id) {
        Categoria categoria = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada."));
        if (productos.existsByCategoriaRelacionId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar una categoría que todavía tiene productos asociados.");
        }
        repository.delete(categoria);
    }
}