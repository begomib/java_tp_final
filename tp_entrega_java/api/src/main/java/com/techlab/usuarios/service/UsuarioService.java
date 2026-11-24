package com.techlab.usuarios.service;

import com.techlab.usuarios.model.Usuario;
import com.techlab.pedidos.repository.PedidoRepository;
import com.techlab.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PedidoRepository pedidos;

    public UsuarioService(UsuarioRepository repository, PedidoRepository pedidos) {
        this.repository = repository;
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Usuario crear(Usuario usuario) {
        usuario.setId(null);
        return repository.save(usuario);
    }

    public Usuario actualizar(Integer id, Usuario datos) {
        Usuario usuario = buscar(id);
        usuario.setNombre(datos.getNombre());
        usuario.setEmail(datos.getEmail());
        return repository.save(usuario);
    }

    public void eliminar(Integer id) {
        Usuario usuario = buscar(id);
        if (pedidos.existsByUsuarioId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar un usuario que tiene pedidos en el historial.");
        }
        repository.delete(usuario);
    }

    private Usuario buscar(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    }
}