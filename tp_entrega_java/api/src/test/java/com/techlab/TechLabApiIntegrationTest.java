package com.techlab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techlab.productos.model.Producto;
import com.techlab.usuarios.model.Usuario;
import com.techlab.productos.model.Categoria;
import com.techlab.productos.repository.CategoriaRepository;
import com.techlab.pedidos.repository.PedidoRepository;
import com.techlab.productos.repository.ProductoRepository;
import com.techlab.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;



import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TechLabApiIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ProductoRepository productos;
    @Autowired private PedidoRepository pedidos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private CategoriaRepository categorias;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private org.springframework.boot.CommandLineRunner cargarDatosIniciales;

    private Usuario usuario;

    @BeforeEach
    void limpiarDatos() {
        pedidos.deleteAll();
        productos.deleteAll();
        categorias.deleteAll();
        usuarios.deleteAll();
                crearCategoria("Perifericos");
        usuario = new Usuario();
        usuario.setNombre("Cliente de prueba");
        usuario = usuarios.save(usuario);
    }

        private void crearCategoria(String nombre) {
                Categoria categoria = new Categoria();
                categoria.setNombre(nombre);
                categorias.save(categoria);
        }

    @Test
    void creaBuscaYValidaProductos() throws Exception {
        String producto = """
                {
                  "nombre": "Teclado",
                  "descripcion": "Teclado mecanico",
                  "precio": 125.50,
                  "categoria": "Perifericos",
                  "imagen": "https://ejemplo.com/teclado.jpg",
                  "stock": 8
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(producto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre", is("Teclado")))
                .andExpect(jsonPath("$.stock", is(8)));

        mockMvc.perform(get("/api/productos").param("nombre", "tecl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre", is("Teclado")));

        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content(producto.replace("\"stock\": 8", "\"stock\": -1")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmaPedidoDescuentaStockYCancelarLoRepone() throws Exception {
        Producto producto = new Producto();
        producto.setNombre("Mouse");
        producto.setDescripcion("Mouse de prueba");
        producto.setPrecio(20.0);
        producto.setCategoria("Perifericos");
        producto.setStock(3);
        producto = productos.save(producto);

        String pedidoJson = """
                {"usuarioId": %d, "lineas": [{"productoId": %d, "cantidad": 2}]}
                """.formatted(usuario.getId(), producto.getId());

        String respuesta = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("PENDIENTE")))
                .andExpect(jsonPath("$.total", is(40.0)))
                .andReturn().getResponse().getContentAsString();
        JsonNode pedido = objectMapper.readTree(respuesta);
        long pedidoId = pedido.get("id").asLong();

        mockMvc.perform(patch("/api/pedidos/{id}/estado", pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CONFIRMADO")));
        mockMvc.perform(get("/api/productos/{id}", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(1)));

        mockMvc.perform(patch("/api/pedidos/{id}/estado", pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CANCELADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")));
        mockMvc.perform(get("/api/productos/{id}", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(3)));
    }
    @Test
    void rechazaPedidoSinStockInclusoConLineasRepetidas() throws Exception {
        Producto producto = new Producto();
        producto.setNombre("Mouse");
        producto.setDescripcion("Mouse de prueba");
        producto.setPrecio(20.0);
        producto.setCategoria("Perifericos");
        producto.setStock(3);
        producto = productos.save(producto);

        for (String lineas : new String[] {
                "[{\"productoId\":%d,\"cantidad\":4}]".formatted(producto.getId()),
                "[{\"productoId\":%d,\"cantidad\":2},{\"productoId\":%d,\"cantidad\":2}]"
                        .formatted(producto.getId(), producto.getId())
        }) {
            mockMvc.perform(post("/api/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"usuarioId\":%d,\"lineas\":%s}".formatted(usuario.getId(), lineas)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", org.hamcrest.Matchers.containsString("Stock insuficiente")));
            org.junit.jupiter.api.Assertions.assertEquals(0, pedidos.count());
            org.junit.jupiter.api.Assertions.assertEquals(3, productos.findById(producto.getId()).orElseThrow().getStock());
        }
    }
    @Test
    void migraCatalogoNavidenoSinCambiarIdsStockNiDuplicarAlReiniciar() throws Exception {
        Producto anterior = new Producto();
        anterior.setNombre("Luna · gatita en adopción");
        anterior.setDescripcion("Catalogo anterior");
        anterior.setCategoria("Adopciones");
        anterior.setPrecio(0);
        anterior.setStock(3);
        anterior = productos.save(anterior);
        Integer id = anterior.getId();
        cargarDatosIniciales.run();
        Producto migrado = productos.findById(id).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("Gatito Noel de cerámica", migrado.getNombre());
        org.junit.jupiter.api.Assertions.assertEquals("Adornos", migrado.getCategoria());
        org.junit.jupiter.api.Assertions.assertEquals(3, migrado.getStock());
        org.junit.jupiter.api.Assertions.assertEquals(15, productos.count());
        cargarDatosIniciales.run();
        org.junit.jupiter.api.Assertions.assertEquals(15, productos.count());
        org.junit.jupiter.api.Assertions.assertEquals(3, productos.findById(id).orElseThrow().getStock());
    }
}