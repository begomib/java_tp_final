package com.techlab.productos.service;

import com.techlab.productos.dto.ActualizacionProducto;
import com.techlab.productos.model.Producto;
import com.techlab.productos.model.Categoria;
import com.techlab.productos.repository.CategoriaRepository;
import com.techlab.productos.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository repository;
    private final CategoriaRepository categorias;

    public ProductoService(ProductoRepository repository, CategoriaRepository categorias) {
        this.repository = repository;
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar(String nombre) {
        return nombre == null || nombre.isBlank()
                ? repository.findAll()
                : repository.findByNombreContainingIgnoreCase(nombre.trim());
    }

    @Transactional(readOnly = true)
    public Producto obtener(Integer id) {
        return buscar(id);
    }

    public Producto crear(Producto producto) {
        producto.setId(null);
        producto.setCategoriaRelacion(resolverCategoria(producto.getCategoria()));
        return repository.save(producto);
    }

    public Producto reemplazar(Integer id, Producto datos) {
        Producto producto = buscar(id);
        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setCategoriaRelacion(resolverCategoria(datos.getCategoria()));
        producto.setImagen(datos.getImagen());
        producto.setStock(datos.getStock());
        return repository.save(producto);
    }

    public Producto actualizar(Integer id, ActualizacionProducto datos) {
        if (datos.getPrecio() == null && datos.getStock() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indicá un precio o un stock para actualizar.");
        }
        Producto producto = buscar(id);
        if (datos.getPrecio() != null) producto.setPrecio(datos.getPrecio());
        if (datos.getStock() != null) producto.setStock(datos.getStock());
        return repository.save(producto);
    }

    public void eliminar(Integer id) {
        repository.delete(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<Producto> stockBajo(int minimo) {
        if (minimo < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mínimo no puede ser negativo.");
        return repository.findByStockLessThanEqual(minimo);
    }

    private Producto buscar(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado."));
    }

    private Categoria resolverCategoria(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría es obligatoria.");
        }
        return categorias.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La categoría '" + nombre.trim() + "' no existe. Creala antes de guardar el producto."));
    }
}