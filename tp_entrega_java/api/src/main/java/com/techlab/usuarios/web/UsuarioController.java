package com.techlab.usuarios.web;

import com.techlab.pedidos.model.Pedido;
import com.techlab.usuarios.model.Usuario;
import com.techlab.pedidos.service.PedidoService;
import com.techlab.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    private final UsuarioService usuarios;
    private final PedidoService pedidos;

    public UsuarioController(UsuarioService usuarios, PedidoService pedidos) {
        this.usuarios = usuarios;
        this.pedidos = pedidos;
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarios.listar();
    }

    @PostMapping
    public Usuario crear(@Valid @RequestBody Usuario usuario) {
        return usuarios.crear(usuario);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Integer id, @Valid @RequestBody Usuario usuario) {
        return usuarios.actualizar(id, usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        usuarios.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{usuarioId}/pedidos")
    public List<Pedido> historial(@PathVariable Integer usuarioId) {
        return pedidos.historial(usuarioId);
    }
}