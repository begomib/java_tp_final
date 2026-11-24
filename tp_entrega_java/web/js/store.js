const API_BASE = "http://localhost:8080/api";
const CART_STORAGE_KEY = "michi-co-navidad-cart";
// Fotos del catálogo. Solo se omiten el segundo gato y los artículos de prueba.
const CHRISTMAS_SELECTION = new Map([
    ["Gatito Noel de cerámica", "assets/navidad/producto-1.png"],
    ["Esferas rojas y doradas x6", "assets/navidad/producto-6.png"],
    ["Corona de pino y frutos rojos", "assets/navidad/producto-8.png"],
    ["Árbol de Navidad de mesa", "assets/navidad/producto-10.png"],
    ["Caja de regalo navideña", "assets/navidad/producto-14.png"],
    ["Casita de Navidad", "assets/navidad/producto-3.png"]
]);
function catalogImage(product) {
    return CHRISTMAS_SELECTION.get(product.nombre) || product.imagen || "";
}
const state = {
    catalogOnly: false,
    products: [],
    categories: [],
    users: [],
    cart: readCart()
};

const money = new Intl.NumberFormat("es-AR", {
    style: "currency",
    currency: "ARS",
    maximumFractionDigits: 2
});

document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    renderCart();
    void refreshAll();
});

async function request(path, options = {}) {
    const config = { ...options, headers: { ...(options.headers || {}) } };
    if (config.body && typeof config.body !== "string") {
        config.headers["Content-Type"] = "application/json";
        config.body = JSON.stringify(config.body);
    }

    let response;
    try {
        response = await fetch(`${API_BASE}${path}`, config);
    } catch {
        throw new Error("No se pudo comunicar con la API. Reiniciá la terminal de api y volvé a intentar.");
    }
    if (response.status === 204) return null;

    const contentType = response.headers.get("content-type") || "";
    const body = contentType.includes("application/json") ? await response.json() : null;
    if (!response.ok) {
        throw new Error(body?.detail || body?.message || `Error ${response.status}: no se pudo completar la operación.`);
    }
    return body;
}

function bindEvents() {
    document.addEventListener("click", handleClick);
    document.getElementById("busqueda-productos").addEventListener("input", renderProducts);
    document.getElementById("filtro-categoria").addEventListener("change", renderProducts);
    document.getElementById("usuario-historial").addEventListener("change", () => void loadHistory());
    document.getElementById("actualizar-historial").addEventListener("click", () => void loadHistory());
    document.getElementById("realizar-pedido").addEventListener("click", () => void createOrder());
    document.getElementById("form-producto").addEventListener("submit", saveProduct);
    document.getElementById("cancelar-producto").addEventListener("click", closeProductForm);
    document.getElementById("form-categoria-admin").addEventListener("submit", event => addCategory(event, "nombre-categoria-admin"));
    document.getElementById("form-usuario").addEventListener("submit", createUser);
    document.getElementById("cancelar-usuario").addEventListener("click", resetUserForm);
    document.getElementById("nuevo-producto").addEventListener("click", () => openProductForm());
    document.getElementById("menu-toggle").addEventListener("click", toggleMenu);
}

async function handleClick(event) {
    const viewButton = event.target.closest("[data-view]");
    if (viewButton) {
        event.preventDefault();
        await switchView(viewButton.dataset.view);
        return;
    }

    const actionButton = event.target.closest("[data-action]");
    if (actionButton?.dataset.action === "christmas") {
        document.getElementById("filtro-categoria").value = "Adornos";
        renderProducts();
        document.getElementById("catalogo-productos").scrollIntoView({ behavior: "smooth" });
        return;
    }
    if (actionButton?.dataset.action === "salir") {
        if (confirm("¿Querés salir de Navidad & Co.?")) {
            window.close();
            showMessage("Podés cerrar esta pestaña para salir.", "success");
        }
        return;
    }

    const button = event.target.closest("button[data-command]");
    if (!button) return;

    try {
        const { command, id, category } = button.dataset;
        if (command === "view-product") await showProductDetail(id);
        if (command === "add-cart") addToCart(id);
        if (command === "edit-product") openProductForm(state.products.find(item => String(item.id) === id));
        if (command === "delete-product") await deleteProduct(id);
        if (command === "delete-category") await deleteCategory(id);
        if (command === "edit-user") editUser(id);
        if (command === "delete-user") await deleteUser(id);
        if (command === "filter-category") {
            document.getElementById("filtro-categoria").value = category;
            await switchView("catalogo");
            renderProducts();
        }
        if (command === "cart-plus") changeCartQuantity(id, 1);
        if (command === "cart-minus") changeCartQuantity(id, -1);
        if (command === "cart-remove") removeFromCart(id);
        if (command === "order-status") await changeOrderStatus(id);
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function switchView(name) {
    const sectionName = name === "inicio" ? "catalogo" : name;
    const selected = document.querySelector(`[data-section="${sectionName}"]`);
    if (name === "inicio" || name === "catalogo") {
        state.catalogOnly = name === "catalogo";
        if (name === "inicio") {
            document.getElementById("busqueda-productos").value = "";
            document.getElementById("filtro-categoria").value = "";
        }
        renderProducts();
    }
    if (!selected) return;

    document.querySelectorAll(".view").forEach(view => {
        const active = view === selected;
        view.hidden = !active;
        view.classList.toggle("is-visible", active);
    });
    document.querySelectorAll(".nav-link[data-view]").forEach(link => {
        link.classList.toggle("is-active", link.dataset.view === name);
    });
    document.getElementById("main-nav").classList.remove("is-open");
    document.getElementById("menu-toggle").setAttribute("aria-expanded", "false");
    window.scrollTo({ top: 0, behavior: "smooth" });

    try {
        if (name === "pedidos") await loadHistory();
        if (name === "admin") await loadAdmin();
        if (name === "carrito") renderCart();
    } catch (error) {
        showMessage(error.message, "error");
    }
}

function toggleMenu() {
    const menu = document.getElementById("main-nav");
    const open = menu.classList.toggle("is-open");
    document.getElementById("menu-toggle").setAttribute("aria-expanded", String(open));
}

async function refreshAll() {
    const results = await Promise.allSettled([loadProducts(), loadCategories(), loadUsers()]);
    const failed = results.find(result => result.status === "rejected");
    if (failed) showMessage(failed.reason.message, "error");
    renderCart();
    renderProducts();
}

async function loadProducts() {
    state.products = await request("/productos");
    renderProducts();
    renderProductTable();
}

async function loadCategories() {
    state.categories = await request("/categorias");
    renderCategories();
    renderProductCategorySelect();
}

async function loadUsers() {
    state.users = await request("/usuarios");
    renderUserSelects();
    renderUsers();
}

function renderProducts() {
    const query = document.getElementById("busqueda-productos").value.trim().toLocaleLowerCase();
    const category = document.getElementById("filtro-categoria").value;
    document.getElementById("portada").hidden = state.catalogOnly || Boolean(query || category);
    const filtered = state.products.filter(product => {
        const matchesText = !query || String(product.id) === query ||
            String(product.nombre || "").toLocaleLowerCase().includes(query) ||
            String(product.descripcion || "").toLocaleLowerCase().includes(query);
        return !["Gato navideño colgante", "Adorno navideño de prueba 1", "Adorno navideño de prueba 2"].includes(product.nombre) && matchesText && (!category || product.categoria === category);
    });

    const grid = document.getElementById("catalogo-productos");
    grid.innerHTML = filtered.map(product => `
        <article class="product-card">
            <div class="product-image-wrap">
                <button class="product-open" type="button" data-command="view-product" data-id="${product.id}" aria-label="Ver ${escapeHtml(product.nombre)}"><img class="product-image" src="${escapeHtml(catalogImage(product))}" alt="${escapeHtml(product.nombre)}" loading="lazy"></button>
                <span class="product-category">${escapeHtml(product.categoria)}</span>
            </div>
            <div class="product-info">
                <h2><button class="product-title" type="button" data-command="view-product" data-id="${product.id}">${escapeHtml(product.nombre)}</button></h2>
                <p>${escapeHtml(product.descripcion)}</p>
                <div class="product-buy"><span class="product-price">${formatMoney(product.precio)}</span>
                    <button class="add-cart" type="button" data-command="add-cart" data-id="${product.id}" aria-label="Agregar ${escapeHtml(product.nombre)}" ${product.stock < 1 ? "disabled" : ""}>+</button>
                </div>
                <div class="stock-note">${product.stock > 0 ? `${product.stock} disponibles` : "Sin stock"}</div>
            </div>
        </article>`).join("");

    grid.querySelectorAll(".product-image").forEach(image => image.addEventListener("error", () => image.remove(), { once: true }));
    document.getElementById("catalogo-vacio").hidden = filtered.length > 0;
}

async function showProductDetail(id) {
    const product = await request(`/productos/${encodeURIComponent(id)}`);
    document.getElementById("detalle-producto").innerHTML = `
        <article class="product-detail">
            <img class="detail-image" src="${escapeHtml(catalogImage(product))}" alt="${escapeHtml(product.nombre)}">
            <div class="detail-copy">
                <p class="eyebrow">${escapeHtml(product.categoria)}</p>
                <h1>${escapeHtml(product.nombre)}</h1>
                <p>${escapeHtml(product.descripcion)}</p>
                <p class="detail-price">${formatMoney(product.precio)}</p>
                <p class="stock-note">${product.stock > 0 ? `${product.stock} disponibles` : "Sin stock"}</p>
                <button class="button button-coral" type="button" data-command="add-cart" data-id="${product.id}" ${product.stock < 1 ? "disabled" : ""}>Agregar al carrito</button>
            </div>
        </article>`;
    await switchView("producto");
}
function renderCategories() {
    const filter = document.getElementById("filtro-categoria");
    const currentFilter = filter.value;
    filter.innerHTML = `<option value="">Todas las categorías</option>${state.categories.map(category =>
        `<option value="${escapeHtml(category.nombre)}">${escapeHtml(category.nombre)}</option>`).join("")}`;
    if (state.categories.some(category => category.nombre === currentFilter)) filter.value = currentFilter;

    const categoryMarkup = state.categories.map(category => `
        <div class="category-chip">
            <button type="button" data-command="filter-category" data-category="${escapeHtml(category.nombre)}">${escapeHtml(category.nombre)}</button>
            <button type="button" data-command="delete-category" data-id="${category.id}" aria-label="Eliminar categoría ${escapeHtml(category.nombre)}">×</button>
        </div>`).join("");
    document.getElementById("lista-categorias").innerHTML = categoryMarkup || emptyMessage("Todavía no hay categorías.");
    document.getElementById("admin-categorias").innerHTML = state.categories.map(category => `
        <div class="admin-list-row"><span>${escapeHtml(category.nombre)}</span><button type="button" data-command="delete-category" data-id="${category.id}">Eliminar</button></div>`).join("") || "<p class='subtle'>Sin categorías.</p>";
}

function renderProductCategorySelect(selectedName = "") {
    const select = document.getElementById("producto-categoria");
    select.innerHTML = state.categories.map(category =>
        `<option value="${escapeHtml(category.nombre)}">${escapeHtml(category.nombre)}</option>`).join("");
    if (selectedName) select.value = selectedName;
}

function renderProductTable() {
    document.getElementById("tabla-productos").innerHTML = state.products.map(product => `
        <tr>
            <td>${product.id}</td><td>${escapeHtml(product.nombre)}</td><td>${escapeHtml(product.categoria)}</td>
            <td>${formatMoney(product.precio)}</td><td>${product.stock}</td>
            <td><div class="table-actions"><button type="button" data-command="edit-product" data-id="${product.id}">Editar</button><button type="button" data-command="delete-product" data-id="${product.id}">Eliminar</button></div></td>
        </tr>`).join("") || `<tr><td colspan="6">No hay productos.</td></tr>`;
}

async function loadAdmin() {
    await Promise.all([loadProducts(), loadCategories(), loadUsers(), loadStockAlerts(), loadAdminOrders()]);
}

async function loadStockAlerts() {
    const products = await request("/productos/stock-bajo?minimo=5");
    document.getElementById("alertas-stock").innerHTML = products.map(product => `
        <div class="stock-alert"><strong>${escapeHtml(product.nombre)}</strong>Quedan ${product.stock}</div>`).join("") || "<span class='subtle'>Todo el stock está en niveles normales.</span>";
}

function renderUserSelects() {
    const options = state.users.map(user => `<option value="${user.id}">${escapeHtml(user.nombre)} · #${user.id}</option>`).join("");
    ["usuario-carrito", "usuario-historial"].forEach(id => {
        const select = document.getElementById(id);
        const previous = select.value;
        select.innerHTML = options || "<option value=''>Registrá un usuario en Administración</option>";
        if (state.users.some(user => String(user.id) === previous)) select.value = previous;
    });
}

function renderUsers() {
    document.getElementById("admin-usuarios").innerHTML = state.users.map(user => `
        <div class="admin-list-row"><span>${escapeHtml(user.nombre)}${user.email ? ` · ${escapeHtml(user.email)}` : ""} <span class="subtle">#${user.id}</span></span><div class="table-actions"><button type="button" data-command="edit-user" data-id="${user.id}">Editar</button><button type="button" data-command="delete-user" data-id="${user.id}">Eliminar</button></div></div>`).join("") || "<p class='subtle'>Sin usuarios.</p>";
}

function readCart() {
    try {
        const saved = JSON.parse(localStorage.getItem(CART_STORAGE_KEY) || "[]");
        return Array.isArray(saved) ? saved : [];
    } catch {
        return [];
    }
}

function saveCart() {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(state.cart));
}

function addToCart(id) {
    const product = state.products.find(item => String(item.id) === String(id));
    if (!product) return;
    const line = state.cart.find(item => String(item.productoId) === String(id));
    if ((line?.cantidad || 0) >= product.stock) {
        showMessage("No hay más unidades disponibles de este producto.", "warning");
        return;
    }
    if (line) line.cantidad += 1;
    else state.cart.push({ productoId: product.id, cantidad: 1 });
    saveCart();
    renderCart();
    showMessage("Producto agregado al carrito.");
}

function changeCartQuantity(id, delta) {
    const line = state.cart.find(item => String(item.productoId) === String(id));
    const product = state.products.find(item => String(item.id) === String(id));
    if (!line || !product) return;
    const next = line.cantidad + delta;
    if (next < 1) return removeFromCart(id);
    if (next > product.stock) return showMessage("La cantidad supera el stock disponible.", "warning");
    line.cantidad = next;
    saveCart();
    renderCart();
}

function removeFromCart(id) {
    state.cart = state.cart.filter(item => String(item.productoId) !== String(id));
    saveCart();
    renderCart();
}

function renderCart() {
    const lines = state.cart.map(item => ({
        ...item,
        product: state.products.find(product => String(product.id) === String(item.productoId))
    })).filter(item => item.product);
    const root = document.getElementById("lineas-carrito");
    if (!root) return;

    root.innerHTML = lines.map(({ product, cantidad }) => `
        <article class="cart-line">
            <img src="${escapeHtml(catalogImage(product))}" alt="${escapeHtml(product.nombre)}" loading="lazy">
            <div><h2><button class="product-title" type="button" data-command="view-product" data-id="${product.id}">${escapeHtml(product.nombre)}</button></h2><p>${formatMoney(product.precio)} por unidad</p></div>
            <div class="quantity-control"><button type="button" data-command="cart-minus" data-id="${product.id}" aria-label="Quitar una unidad">−</button><span>${cantidad}</span><button type="button" data-command="cart-plus" data-id="${product.id}" aria-label="Agregar una unidad">+</button></div>
            <button class="remove-line" type="button" data-command="cart-remove" data-id="${product.id}">Quitar</button>
        </article>`).join("") || emptyMessage("Tu carrito está vacío. Agregá productos desde el catálogo.");

    root.querySelectorAll("img").forEach(image => image.addEventListener("error", () => image.remove(), { once: true }));
    const total = lines.reduce((sum, line) => sum + Number(line.product.precio) * line.cantidad, 0);
    document.getElementById("subtotal-carrito").textContent = formatMoney(total);
    document.getElementById("total-carrito").textContent = formatMoney(total);
    document.getElementById("cart-count").textContent = state.cart.reduce((sum, item) => sum + item.cantidad, 0);
}

async function createOrder() {
    if (!state.cart.length) return showMessage("Agregá un adorno navideño al carrito antes de continuar.", "warning");
    const usuarioId = Number(document.getElementById("usuario-carrito").value);
    if (!usuarioId) return showMessage("Seleccioná o registrá un usuario para el pedido.", "warning");
    try {
        const pedido = await request("/pedidos", {
            method: "POST",
            body: { usuarioId, lineas: state.cart.map(item => ({ productoId: item.productoId, cantidad: item.cantidad })) }
        });
        state.cart = [];
        saveCart();
        renderCart();
        showMessage(`Pedido #${pedido.id} creado y guardado como pendiente.`);
        await switchView("pedidos");
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function loadHistory() {
    const usuarioId = document.getElementById("usuario-historial").value;
    const root = document.getElementById("historial-pedidos");
    if (!usuarioId) {
        root.innerHTML = emptyMessage("Registrá o seleccioná un usuario para ver sus pedidos.");
        return;
    }
    const orders = await request(`/usuarios/${usuarioId}/pedidos`);
    root.innerHTML = renderOrders(orders, false);
}

async function loadAdminOrders() {
    const orders = await request("/pedidos");
    document.getElementById("admin-pedidos").innerHTML = renderOrders(orders, true);
}

function renderOrders(orders, canManage) {
    if (!orders.length) return emptyMessage("Todavía no hay pedidos registrados.");
    return orders.map(order => {
        const transitions = nextStates(order.estado);
        const lines = (order.lineas || []).map(line => `<li>${escapeHtml(line.producto?.nombre || "Producto")} × ${line.cantidad}</li>`).join("");
        const updateControl = canManage && transitions.length ? `
            <div class="order-manage"><label class="visually-hidden" for="estado-${order.id}">Cambiar estado del pedido ${order.id}</label>
            <select id="estado-${order.id}" data-order-select="${order.id}">${transitions.map(status => `<option value="${status}">${statusLabel(status)}</option>`).join("")}</select>
            <button class="button button-quiet" type="button" data-command="order-status" data-id="${order.id}">Actualizar estado</button></div>` : "";
        return `<article class="order-card"><div class="order-top"><div><h2>Pedido #${order.id}</h2><div class="order-meta">${escapeHtml(order.usuario?.nombre || "Cliente")} · ${formatDate(order.fecha)}</div></div><span class="status-badge" data-status="${escapeHtml(order.estado)}">${statusLabel(order.estado)}</span></div><ul class="order-items">${lines}</ul><div class="order-total">Total ${formatMoney(order.total)}</div>${updateControl}</article>`;
    }).join("");
}

function nextStates(current) {
    const transitions = {
        PENDIENTE: ["CONFIRMADO", "CANCELADO"],
        CONFIRMADO: ["ENVIADO", "CANCELADO"],
        ENVIADO: ["ENTREGADO"],
        ENTREGADO: [],
        CANCELADO: []
    };
    return transitions[current] || [];
}

function statusLabel(status) {
    return ({ PENDIENTE: "Pendiente", CONFIRMADO: "Confirmado", ENVIADO: "Enviado", ENTREGADO: "Entregado", CANCELADO: "Cancelado" })[status] || status;
}

async function changeOrderStatus(id) {
    const target = document.querySelector(`[data-order-select="${id}"]`);
    if (!target) return;
    await request(`/pedidos/${id}/estado`, { method: "PATCH", body: { estado: target.value } });
    await Promise.all([loadAdminOrders(), loadStockAlerts(), loadProducts()]);
    showMessage("Estado del pedido actualizado.");
}

async function addCategory(event, inputId) {
    event.preventDefault();
    const input = document.getElementById(inputId);
    try {
        await request("/categorias", { method: "POST", body: { nombre: input.value.trim() } });
        input.value = "";
        await loadCategories();
        showMessage("Categoría agregada.");
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function deleteCategory(id) {
    if (!confirm("¿Eliminar esta categoría? Los productos existentes conservan su categoría.")) return;
    await request(`/categorias/${id}`, { method: "DELETE" });
    await loadCategories();
    showMessage("Categoría eliminada.");
}

function openProductForm(product = null) {
    const form = document.getElementById("form-producto");
    form.hidden = false;
    form.reset();
    renderProductCategorySelect(product?.categoria || "");
    if (product) {
        document.getElementById("producto-id").value = product.id;
        document.getElementById("producto-nombre").value = product.nombre || "";
        document.getElementById("producto-descripcion").value = product.descripcion || "";
        document.getElementById("producto-precio").value = product.precio;
        document.getElementById("producto-stock").value = product.stock;
        document.getElementById("producto-imagen").value = product.imagen || "";
    }
    form.scrollIntoView({ behavior: "smooth", block: "center" });
    document.getElementById("producto-nombre").focus();
}

function closeProductForm() {
    const form = document.getElementById("form-producto");
    form.reset();
    document.getElementById("producto-id").value = "";
    form.hidden = true;
}

async function saveProduct(event) {
    event.preventDefault();
    const id = document.getElementById("producto-id").value;
    const product = {
        nombre: document.getElementById("producto-nombre").value.trim(),
        descripcion: document.getElementById("producto-descripcion").value.trim(),
        precio: Number(document.getElementById("producto-precio").value),
        categoria: document.getElementById("producto-categoria").value,
        imagen: document.getElementById("producto-imagen").value.trim(),
        stock: Number(document.getElementById("producto-stock").value)
    };
    if (!product.nombre || !product.categoria || product.precio < 0 || !Number.isInteger(product.stock) || product.stock < 0) {
        return showMessage("Revisá los campos: precio y stock deben ser valores válidos y no negativos.", "warning");
    }
    try {
        await request(id ? `/productos/${id}` : "/productos", { method: id ? "PUT" : "POST", body: product });
        closeProductForm();
        await Promise.all([loadProducts(), loadStockAlerts()]);
        showMessage(id ? "Producto actualizado." : "Producto agregado al catálogo.");
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function deleteProduct(id) {
    if (!confirm("¿Eliminar este producto del catálogo?")) return;
    await request(`/productos/${id}`, { method: "DELETE" });
    state.cart = state.cart.filter(item => String(item.productoId) !== String(id));
    saveCart();
    await Promise.all([loadProducts(), loadStockAlerts()]);
    renderCart();
    showMessage("Producto eliminado.");
}

async function createUser(event) {
    event.preventDefault();
    const id = document.getElementById("usuario-id").value;
    const nombre = document.getElementById("usuario-nombre").value.trim();
    const email = document.getElementById("usuario-email").value.trim();
    try {
        const user = await request(id ? `/usuarios/${id}` : "/usuarios", {
            method: id ? "PUT" : "POST",
            body: { nombre, email: email || null }
        });
        resetUserForm();
        await loadUsers();
        if (!id) {
            document.getElementById("usuario-carrito").value = user.id;
            document.getElementById("usuario-historial").value = user.id;
        }
        showMessage(id ? "Usuario actualizado." : "Usuario registrado.");
    } catch (error) {
        showMessage(error.message, "error");
    }
}

function editUser(id) {
    const user = state.users.find(item => String(item.id) === String(id));
    if (!user) return;
    document.getElementById("usuario-id").value = user.id;
    document.getElementById("usuario-nombre").value = user.nombre || "";
    document.getElementById("usuario-email").value = user.email || "";
    document.getElementById("guardar-usuario").textContent = "Guardar cambios";
    document.getElementById("cancelar-usuario").hidden = false;
    document.getElementById("usuario-nombre").focus();
}

function resetUserForm() {
    document.getElementById("form-usuario").reset();
    document.getElementById("usuario-id").value = "";
    document.getElementById("guardar-usuario").textContent = "Agregar";
    document.getElementById("cancelar-usuario").hidden = true;
}

async function deleteUser(id) {
    if (!confirm("¿Eliminar este usuario? Los usuarios con pedidos históricos no se pueden eliminar.")) return;
    await request(`/usuarios/${id}`, { method: "DELETE" });
    await loadUsers();
    showMessage("Usuario eliminado.");
}

function formatMoney(value) {
    const amount = Number(value);
    return Number.isFinite(amount) ? money.format(amount) : "$ 0";
}

function formatDate(value) {
    const date = new Date(value);
    return Number.isNaN(date.valueOf()) ? "Fecha no disponible" : date.toLocaleString("es-AR", { dateStyle: "medium", timeStyle: "short" });
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
    })[character]);
}

function emptyMessage(text) {
    return `<p class="empty-state">${escapeHtml(text)}</p>`;
}

let messageTimer;
function showMessage(text, type = "success") {
    const notice = document.getElementById("mensaje");
    notice.textContent = text;
    notice.dataset.type = type;
    notice.hidden = false;
    clearTimeout(messageTimer);
    messageTimer = setTimeout(() => { notice.hidden = true; }, 4500);
}