const API_URL = "http://localhost:8080/api/articulos";
let articulos = [];

document.addEventListener("DOMContentLoaded", listarArticulos);
document.getElementById("form-articulo").addEventListener("submit", guardarArticulo);
document.getElementById("cancelar").addEventListener("click", limpiarFormulario);
document.getElementById("form-busqueda").addEventListener("submit", buscarArticulos);
document.getElementById("limpiar-busqueda").addEventListener("click", () => {
    document.getElementById("busqueda").value = "";
    ocultarDetalle();
    renderizarArticulos(articulos);
    limpiarMensaje();
});
document.getElementById("tabla-articulos").addEventListener("click", manejarAccionTabla);

async function listarArticulos() {
    try {
        const respuesta = await fetch(API_URL);
        if (!respuesta.ok) throw new Error("No se pudo cargar el catálogo.");
        articulos = await respuesta.json();
        if (!Array.isArray(articulos)) throw new Error("La respuesta de la API no es una lista de productos.");
        renderizarArticulos(articulos);
        limpiarMensaje();
    } catch (error) {
        mostrarMensaje(error.message, "danger");
    }
}

function renderizarArticulos(lista) {
    const tbody = document.getElementById("tabla-articulos");
    tbody.replaceChildren();
    lista.forEach(articulo => {
        const fila = document.createElement("tr");
        agregarCelda(fila, articulo.id);
        agregarCelda(fila, articulo.nombre);
        agregarCelda(fila, articulo.categoria);
        agregarCelda(fila, formatearPrecio(articulo.precio));
        agregarCelda(fila, articulo.stock);

        const acciones = document.createElement("td");
        acciones.append(crearBoton("Ver", "ver", articulo.id, "btn-outline-info"));
        acciones.append(" ", crearBoton("Editar", "editar", articulo.id, "btn-warning"));
        acciones.append(" ", crearBoton("Eliminar", "eliminar", articulo.id, "btn-danger"));
        fila.appendChild(acciones);
        tbody.appendChild(fila);
    });
}

function agregarCelda(fila, valor) {
    const celda = document.createElement("td");
    celda.textContent = valor ?? "-";
    fila.appendChild(celda);
}

function crearBoton(texto, accion, id, clase) {
    const boton = document.createElement("button");
    boton.type = "button";
    boton.className = `btn ${clase} btn-sm`;
    boton.textContent = texto;
    boton.dataset.accion = accion;
    boton.dataset.id = id;
    return boton;
}

function formatearPrecio(precio) {
    const valor = Number(precio);
    return Number.isFinite(valor) ? valor.toFixed(2) : "-";
}

async function guardarArticulo(event) {
    event.preventDefault();
    const id = document.getElementById("idArticulo").value;
    const nombre = document.getElementById("nombre").value.trim();
    const precio = parseFloat(document.getElementById("precio").value);
    const stock = Number(document.getElementById("stock").value);
    const categoria = document.getElementById("categoria").value.trim();
    const descripcion = document.getElementById("descripcion").value.trim();
    const imagen = document.getElementById("imagen").value.trim();

    if (!nombre || !categoria || !descripcion || !Number.isFinite(precio) || precio < 0 || !Number.isInteger(stock) || stock < 0) {
        mostrarMensaje("Revisá los campos: el precio debe ser mayor o igual a cero y el stock un entero no negativo.", "warning");
        return;
    }

    const articulo = { nombre, descripcion, precio, categoria, imagen, stock };
    const url = id ? `${API_URL}/${id}` : API_URL;
    const metodo = id ? "PUT" : "POST";

    try {
        const respuesta = await fetch(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(articulo)
        });
        if (!respuesta.ok) throw new Error("No se pudo guardar el producto.");
        limpiarFormulario();
        await listarArticulos();
        mostrarMensaje(id ? "Producto actualizado." : "Producto agregado al catálogo.", "success");
    } catch (error) {
        mostrarMensaje(error.message, "danger");
    }
}

async function obtenerArticulo(id) {
    const respuesta = await fetch(`${API_URL}/${encodeURIComponent(id)}`);
    if (!respuesta.ok) throw new Error("No se encontró el producto solicitado.");
    return respuesta.json();
}

async function manejarAccionTabla(event) {
    const boton = event.target.closest("button[data-accion]");
    if (!boton) return;

    try {
        const id = boton.dataset.id;
        if (boton.dataset.accion === "ver") {
            mostrarDetalle(await obtenerArticulo(id));
        } else if (boton.dataset.accion === "editar") {
            cargarFormulario(await obtenerArticulo(id));
        } else if (boton.dataset.accion === "eliminar") {
            await eliminarArticulo(id);
        }
    } catch (error) {
        mostrarMensaje(error.message, "danger");
    }
}

async function buscarArticulos(event) {
    event.preventDefault();
    const termino = document.getElementById("busqueda").value.trim();
    ocultarDetalle();

    if (!termino) {
        renderizarArticulos(articulos);
        limpiarMensaje();
        return;
    }

    if (/^\d+$/.test(termino)) {
        try {
            const articulo = await obtenerArticulo(termino);
            renderizarArticulos([articulo]);
            mostrarDetalle(articulo);
            limpiarMensaje();
        } catch (error) {
            renderizarArticulos([]);
            mostrarMensaje(error.message, "warning");
        }
        return;
    }

    const coincidencias = articulos.filter(articulo =>
        String(articulo.nombre ?? "").toLocaleLowerCase().includes(termino.toLocaleLowerCase())
    );
    renderizarArticulos(coincidencias);
    if (coincidencias.length === 0) {
        mostrarMensaje("No se encontraron productos con ese nombre.", "warning");
    } else {
        limpiarMensaje();
    }
}

function mostrarDetalle(articulo) {
    const panel = document.getElementById("detalle-producto");
    panel.replaceChildren();
    panel.className = "mb-4 border rounded p-3 bg-white";

    const titulo = document.createElement("h2");
    titulo.className = "h4";
    titulo.textContent = articulo.nombre ?? "Producto";
    panel.appendChild(titulo);

    const datos = document.createElement("dl");
    datos.className = "row mb-0";
    [
        ["ID", articulo.id],
        ["Descripción", articulo.descripcion],
        ["Precio", formatearPrecio(articulo.precio)],
        ["Categoría", articulo.categoria],
        ["Stock", articulo.stock]
    ].forEach(([etiqueta, valor]) => {
        const termino = document.createElement("dt");
        termino.className = "col-sm-3";
        termino.textContent = etiqueta;
        const detalle = document.createElement("dd");
        detalle.className = "col-sm-9";
        detalle.textContent = valor ?? "-";
        datos.append(termino, detalle);
    });
    panel.appendChild(datos);

    if (articulo.imagen) {
        const imagen = document.createElement("img");
        imagen.src = articulo.imagen;
        imagen.alt = `Imagen de ${articulo.nombre ?? "producto"}`;
        imagen.className = "img-thumbnail mt-3";
        imagen.style.maxWidth = "180px";
        imagen.addEventListener("error", () => imagen.remove(), { once: true });
        panel.appendChild(imagen);
    }
}

function ocultarDetalle() {
    const panel = document.getElementById("detalle-producto");
    panel.replaceChildren();
    panel.className = "mb-4 d-none";
}

function cargarFormulario(articulo) {
    document.getElementById("idArticulo").value = articulo.id;
    document.getElementById("nombre").value = articulo.nombre ?? "";
    document.getElementById("descripcion").value = articulo.descripcion ?? "";
    document.getElementById("precio").value = articulo.precio ?? "";
    document.getElementById("categoria").value = articulo.categoria ?? "";
    document.getElementById("imagen").value = articulo.imagen ?? "";
    document.getElementById("stock").value = articulo.stock ?? "";
    document.getElementById("nombre").focus();
    window.scrollTo({ top: 0, behavior: "smooth" });
}

async function eliminarArticulo(id) {
    if (!confirm("¿Deseás eliminar este producto?")) return;
    const respuesta = await fetch(`${API_URL}/${encodeURIComponent(id)}`, { method: "DELETE" });
    if (!respuesta.ok) throw new Error("No se pudo eliminar el producto.");
    ocultarDetalle();
    await listarArticulos();
    mostrarMensaje("Producto eliminado.", "success");
}

function limpiarFormulario() {
    document.getElementById("form-articulo").reset();
    document.getElementById("idArticulo").value = "";
}

function mostrarMensaje(texto, tipo) {
    const mensaje = document.getElementById("mensaje");
    mensaje.textContent = texto;
    mensaje.className = `alert alert-${tipo}`;
}

function limpiarMensaje() {
    const mensaje = document.getElementById("mensaje");
    mensaje.textContent = "";
    mensaje.className = "alert d-none";
}
