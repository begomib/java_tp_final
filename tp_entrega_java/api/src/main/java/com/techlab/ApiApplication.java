package com.techlab;

import com.techlab.productos.model.Categoria;
import com.techlab.productos.model.Producto;
import com.techlab.usuarios.model.Usuario;
import com.techlab.productos.repository.CategoriaRepository;
import com.techlab.productos.repository.ProductoRepository;
import com.techlab.usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;
import java.io.InputStream;
import java.util.List;

@SpringBootApplication
public class ApiApplication {
    public static void main(String[] args) { SpringApplication.run(ApiApplication.class, args); }

    @Bean
    CommandLineRunner cargarDatosIniciales(ProductoRepository productos, CategoriaRepository categorias,
            UsuarioRepository usuarios, ObjectMapper mapper, TransactionTemplate transaction) {
        return args -> {
            List<Producto> semillas = leer(mapper, "/productos.json", new TypeReference<List<Producto>>() {});
            List<String> anteriores = leer(mapper, "/catalogo-anterior.json", new TypeReference<List<String>>() {});
            transaction.executeWithoutResult(status -> {
                boolean vacio = productos.count() == 0;
                boolean migrar = anteriores.stream().anyMatch(n -> productos.findFirstByNombreIgnoreCase(n).isPresent())
                        || productos.findFirstByNombreIgnoreCase("Gatitos en adopción responsable").isPresent();
                if (vacio || migrar) {
                    for (int i = 0; i < semillas.size(); i++) {
                        Producto semilla = semillas.get(i);
                        Producto destino = productos.findFirstByNombreIgnoreCase(semilla.getNombre()).orElse(null);
                        if (destino != null) continue;
                        destino = i < anteriores.size() ? productos.findFirstByNombreIgnoreCase(anteriores.get(i)).orElse(null) : null;
                        if (destino == null && i == 0) {
                            destino = productos.findFirstByNombreIgnoreCase("Gatitos en adopción responsable").orElse(null);
                        }
                        if (destino == null) destino = new Producto();
                        Integer stockAnterior = destino.getStock();
                        destino.setNombre(semilla.getNombre());
                        destino.setDescripcion(semilla.getDescripcion());
                        destino.setPrecio(semilla.getPrecio());
                        destino.setImagen(semilla.getImagen());
                        destino.setCategoriaRelacion(null);
                        destino.setCategoria(semilla.getCategoria());
                        destino.setStock(stockAnterior == null ? semilla.getStock() : stockAnterior);
                        productos.save(destino);
                    }
                }
                for (Producto producto : productos.findAll()) {
                    if (producto.getImagen() != null && producto.getImagen().matches("assets/navidad/producto-\\d+\\.svg")) {
                        producto.setImagen(producto.getImagen().replace(".svg", ".png"));
                    }
                    String nombre = producto.getCategoria();
                    Categoria categoria = categorias.findByNombreIgnoreCase(nombre).orElseGet(() -> {
                        Categoria nueva = new Categoria();
                        nueva.setNombre(nombre);
                        return categorias.save(nueva);
                    });
                    producto.setCategoriaRelacion(categoria);
                    productos.save(producto);
                }
                // Solo se retiran categorías de la temática anterior que ya no tienen productos.
                List<String> obsoletas = List.of("Adopciones", "Comida", "Baño e higiene", "Rascadores y juego", "Ropa", "Paseo", "Salud");
                for (Categoria categoria : categorias.findAll()) {
                    if (obsoletas.contains(categoria.getNombre()) && productos.findAll().stream()
                            .noneMatch(p -> categoria.getNombre().equals(p.getCategoria()))) categorias.delete(categoria);
                }
                if (usuarios.count() == 0) {
                    Usuario demo = new Usuario();
                    demo.setNombre("Cliente Demo");
                    demo.setEmail("cliente@techlab.local");
                    usuarios.save(demo);
                }
            });
        };
    }

    private <T> T leer(ObjectMapper mapper, String ruta, TypeReference<T> tipo) throws Exception {
        try (InputStream archivo = getClass().getResourceAsStream(ruta)) {
            if (archivo == null) throw new IllegalStateException("No se encontró " + ruta);
            return mapper.readValue(archivo, tipo);
        }
    }
}