# TechLab: API de tienda

API REST para la tienda web del directorio `web`. Java 17+, Spring Boot, JPA y validación de datos.

## Probar localmente con H2

No hace falta instalar ni iniciar MySQL para probar. H2 se ejecuta dentro de Spring Boot y guarda la base en `api/data/articulos`; la primera ejecución carga 10 artículos navideños, sus categorías y un usuario de demostración. Al reiniciar, si encuentra los diez productos demo antiguos de tecnología, los transforma en el catálogo navideño conservando sus IDs para no perder referencias en pedidos. Los demás artículos, pedidos y usuarios no se eliminan.

1. En una terminal ubicada en `api`, configura Java si esa terminal no reconoce Maven:

```cmd
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
npm start
```

2. En otra terminal, ubicada en `web`, ejecuta `npm start` (la dependencia se instala una vez con `npm install`).
3. Abrí `http://localhost:5500` y dejá los dos servidores ejecutándose.

Si la API vieja ya está ocupando el puerto 8080, detenela con `Ctrl+C` en su terminal y volvé a iniciar `npm start` desde `api` para cargar los endpoints nuevos.

## Ejecutar el JAR compilado en Windows

Con Java 17 o superior configurado en `JAVA_HOME`, ejecuta `api\iniciar.cmd` desde el Explorador o una terminal. El script usa la carpeta `api` para guardar la base H2, independientemente de dónde se invoque. Para MySQL: `iniciar.cmd --spring.profiles.active=mysql` con las variables de conexión configuradas.

El JAR se genera con `mvn clean verify` y queda en `api/target/michi-navidad-fotos-api-1.0.0.jar`.

## Base MySQL

Para usar MySQL en lugar de H2, instala **MySQL Community Server** desde el [instalador oficial de MySQL para Windows](https://dev.mysql.com/downloads/installer/). Durante la configuración, inicia el servicio MySQL y guarda la contraseña definida para `root`. MySQL Workbench es opcional; sirve para inspeccionar la base.

En una terminal `cmd` ubicada en `api`, configura las credenciales de esa sesión y arranca con el perfil MySQL:

```cmd
set "DB_USER=root"
set "DB_PASSWORD=TU_CONTRASEÑA_DE_MYSQL"
npm run start:mysql
```

La URL por defecto apunta a `localhost:3306/techlab` y crea el esquema si `root` tiene permisos. Se puede cambiar con `DB_URL`, `DB_USER` y `DB_PASSWORD`. Conservá el perfil normal `npm start` para usar H2 sin iniciar MySQL.

## Endpoints

| Método | Ruta | Acción |
| --- | --- | --- |
| GET | `/api/productos` | Listar productos; admite `?nombre=` |
| GET | `/api/productos/{id}` | Ver un producto |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Reemplazar producto |
| PATCH | `/api/productos/{id}` | Actualizar precio y/o stock |
| DELETE | `/api/productos/{id}` | Eliminar producto |
| GET/POST/PUT/DELETE | `/api/categorias` | Gestionar categorías |
| GET/POST/PUT/DELETE | `/api/usuarios` | Listar, registrar, editar y eliminar usuarios |
| GET | `/api/usuarios/{id}/pedidos` | Historial de un usuario |
| GET/POST | `/api/pedidos` | Listar y crear pedidos pendientes validando stock |
| GET | `/api/pedidos/{id}` | Ver detalle del pedido |
| PATCH | `/api/pedidos/{id}/estado` | Cambiar estado y actualizar stock al confirmar/cancelar |
| GET | `/api/productos/stock-bajo?minimo=5` | Reporte de stock bajo |

## Pruebas

```cmd
mvn test
```

Las pruebas utilizan una base H2 en memoria separada y no modifican el catálogo local.

## Comportamiento de pedidos

Al crear un pedido se valida el stock disponible, sumando las cantidades de las líneas que repiten un producto. Si no alcanza, la API responde HTTP 400 y no guarda el pedido. El pedido se registra como PENDIENTE sin descontar stock. Al confirmarlo se vuelve a validar y se descuenta; al cancelar un pedido confirmado se repone. Este flujo sigue el requisito de actualización de stock al confirmar.

Verificación local: compilación y empaquetado correctos; cuatro pruebas de integración aprobadas con H2. La conexión MySQL requiere las credenciales de la base existente mediante DB_URL, DB_USER y DB_PASSWORD. No se ha verificado esa conexión sin dichas credenciales.

La colección navideña incluye 14 productos con imágenes de estilo fotográfico generadas con IA, guardadas localmente en web/assets/navidad. La migración conserva los IDs, el stock existente y los pedidos; solo transforma artículos conocidos del catálogo anterior. database/migracion-navidad.sql permite aplicar el mismo cambio directamente en techlab. Los precios unitarios ya guardados en pedidos históricos se conservan.
