package com.techlab.pedidos.service;

import com.techlab.pedidos.dto.LineaPedidoRequest;
import com.techlab.pedidos.dto.PedidoRequest;
import com.techlab.pedidos.model.EstadoPedido;
import com.techlab.pedidos.model.LineaPedido;
import com.techlab.pedidos.model.Pedido;
import com.techlab.productos.model.Producto;
import com.techlab.usuarios.model.Usuario;
import com.techlab.excepciones.StockInsuficienteException;
import com.techlab.pedidos.repository.PedidoRepository;
import com.techlab.productos.repository.ProductoRepository;
import com.techlab.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public PedidoService(PedidoRepository pedidos, ProductoRepository productos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    @Transactional
    public Pedido crear(PedidoRequest request) {
        Usuario usuario = usuarios.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(0.0);

        Map<Integer, Integer> cantidadesPorProducto = new LinkedHashMap<>();
        for (LineaPedidoRequest item : request.getLineas()) {
            cantidadesPorProducto.merge(item.getProductoId(), item.getCantidad(), Integer::sum);
        }

        for (Map.Entry<Integer, Integer> item : cantidadesPorProducto.entrySet()) {
            Producto producto = productos.findById(item.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto " + item.getKey() + " no encontrado."));
            if (producto.getStock() < item.getValue()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getStock() + ".");
            }
            LineaPedido linea = new LineaPedido();
            linea.setPedido(pedido);
            linea.setProducto(producto);
            linea.setCantidad(item.getValue());
            linea.setPrecioUnitario(producto.getPrecio());
            linea.setSubtotal(producto.getPrecio() * item.getValue());
            pedido.getLineas().add(linea);
            pedido.setTotal(pedido.getTotal() + linea.getSubtotal());
        }
        return pedidos.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listar() {
        return pedidos.findAll();
    }

    @Transactional(readOnly = true)
    public Pedido obtener(Integer id) {
        return buscar(id);
    }

    @Transactional(readOnly = true)
    public List<Pedido> historial(Integer usuarioId) {
        if (!usuarios.existsById(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }
        return pedidos.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    @Transactional
    public Pedido cambiarEstado(Integer id, EstadoPedido nuevoEstado) {
        Pedido pedido = buscar(id);
        EstadoPedido actual = pedido.getEstado();
        if (actual == nuevoEstado) return pedido;

        if (nuevoEstado == EstadoPedido.CONFIRMADO && actual == EstadoPedido.PENDIENTE) {
            confirmarStock(pedido);
        } else if (nuevoEstado == EstadoPedido.CANCELADO
                && (actual == EstadoPedido.PENDIENTE || actual == EstadoPedido.CONFIRMADO)) {
            if (actual == EstadoPedido.CONFIRMADO) reponerStock(pedido);
        } else if (!((actual == EstadoPedido.CONFIRMADO && nuevoEstado == EstadoPedido.ENVIADO)
                || (actual == EstadoPedido.ENVIADO && nuevoEstado == EstadoPedido.ENTREGADO))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transición de estado no permitida: " + actual + " a " + nuevoEstado + ".");
        }

        pedido.setEstado(nuevoEstado);
        return pedidos.save(pedido);
    }

    private void confirmarStock(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            Producto producto = linea.getProducto();
            if (producto.getStock() < linea.getCantidad()) {
                throw new StockInsuficienteException(
                    "Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getStock() + ".");
            }
        }
        for (LineaPedido linea : pedido.getLineas()) {
            Producto producto = linea.getProducto();
            producto.setStock(producto.getStock() - linea.getCantidad());
            productos.save(producto);
        }
    }

    private void reponerStock(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            Producto producto = linea.getProducto();
            producto.setStock(producto.getStock() + linea.getCantidad());
            productos.save(producto);
        }
    }

    private Pedido buscar(Integer id) {
        return pedidos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado."));
    }
}