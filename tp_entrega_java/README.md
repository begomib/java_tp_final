# Michi & Co. — Trabajo final de Java

E-commerce de adornos y regalos navideños, con una colección de gatitos decorativos. Backend Java 17 con Spring Boot, JPA, validaciones y MySQL; frontend HTML, CSS y JavaScript.

## Requisitos

- JDK 17 o superior y Maven 3.9 o superior.
- MySQL en ejecución (puerto 3306 en los ejemplos).
- Node.js con npm.

## Importar la base de datos

La entrega debe incluir `database/techlab.sql`, exportado con estructura y datos. En MySQL Workbench:

1. Crear la base si no existe: `CREATE DATABASE IF NOT EXISTS techlab CHARACTER SET utf8mb4;`.
2. Abrir **Server → Data Import**, elegir **Import from Self-Contained File** y seleccionar `database/techlab.sql`.
3. En **Default Target Schema**, seleccionar `techlab` y ejecutar **Start Import**.

Importar en una base de evaluación, ya que el volcado puede reemplazar tablas con los mismos nombres. Las credenciales de MySQL son las del equipo del evaluador; no se incluyen contraseñas en el repositorio.

## Ejecutar la API con MySQL

En PowerShell, desde `api`:

```powershell
# JAVA_HOME debe apuntar al JDK 17 o superior instalado en tu equipo.
$env:DB_USER = 'root'
$env:DB_PASSWORD = 'REEMPLAZAR_POR_LA_CLAVE_LOCAL'
$env:DB_URL = 'jdbc:mysql://localhost:3306/techlab?serverTimezone=UTC'
mvn clean verify
.\iniciar.cmd --spring.profiles.active=mysql
```

También se puede iniciar desde Maven con `npm.cmd run start:mysql`. Mantener la terminal abierta. La API usa http://localhost:8080/api. Si el puerto está ocupado, detener la instancia anterior.

Hibernate crea las tablas que falten mediante `ddl-auto=update`. Al arrancar con el catálogo vacío, se cargan los datos de `api/src/main/resources/productos.json`, las categorías y un usuario demo. Los pedidos se generan desde la aplicación. El arranque también actualiza los productos demo de tecnología de versiones anteriores al catálogo navideño.

## Ejecutar la web

En otra terminal PowerShell, desde `web`:

```powershell
npm.cmd ci
npm.cmd start
```

Abrir http://localhost:5500. Dejar ejecutándose la API y el servidor web. El frontend apunta a la API del puerto 8080.

## Funcionalidades

- Alta, listado, búsqueda por nombre o ID, edición y eliminación de productos.
- Categorías y administración de usuarios.
- Carrito, creación de pedidos y cálculo de totales.
- Historial por usuario y estados pendiente, confirmado, enviado, entregado y cancelado.
- Validación de stock al crear y confirmar; descuento al confirmar y reposición al cancelar un confirmado.
- Reporte de stock bajo.

## Pruebas

Desde `api`, ejecutar `mvn clean verify`. Las cuatro pruebas de integración usan H2 en memoria, separada de MySQL. Verifican creación/búsqueda/validación de productos, confirmación/cancelación y stock insuficiente incluyendo líneas repetidas. La verificación automatizada no sustituye la prueba manual con MySQL y el navegador.

Los endpoints están documentados en `api/README.md`.

## Preparar la entrega

Antes de subir, exportar la base actualizada ejecutando desde la raíz:

```powershell
.\database\exportar.cmd
```

El comando solicita la contraseña sin guardarla en el script y crea `database/techlab.sql`. Usa la instalación local de MySQL en `D:\Programas\MySql`. Si no está disponible, exportar desde Workbench con **Server → Data Export → techlab → Dump Structure and Data → Export to Self-Contained File** y guardar en `database/techlab.sql`.

Subir únicamente `api`, `web`, `database`, `README.md` y `.gitignore`. No incluir carpetas de ejemplos, `.tools`, `node_modules`, `target`, bases H2 ni archivos con credenciales. `.gitignore` ya excluye esos elementos. No es necesario entregar Postman según la consigna proporcionada.

La colección navideña incluye 14 productos con imágenes de estilo fotográfico generadas con IA, guardadas localmente en web/assets/navidad. La migración conserva los IDs, el stock existente y los pedidos; solo transforma artículos conocidos del catálogo anterior. database/migracion-navidad.sql permite aplicar el mismo cambio directamente en techlab. Los precios unitarios ya guardados en pedidos históricos se conservan.
