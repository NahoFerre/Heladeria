# Sistema de Ventas — Heladería / Acai

Plan de implementación, checklist y guía de riesgos. Documento vivo: se actualiza a medida que avanzamos.

## 1. Arquitectura decidida

- **Backend:** Spring Boot (Java) + Spring Data JPA + PostgreSQL
- **Frontend:** Angular (SPA)
- **Red:** un solo punto de venta físico. El backend corre en una PC del local ("servidor"). Acceso remoto vía **Tailscale** (VPN mesh gratuita, cifrado WireGuard) — no se necesita VPS ni dominio ni HTTPS adicional.
- **Roles:** dos roles — **ADMIN** (acceso completo) y **CAJERO** (solo la pantalla de Venta, sin ver costos, ganancias, ni gestión de productos/usuarios). Aplicado tanto en el frontend (menú y rutas) como en el backend (cada endpoint valida el rol independientemente del frontend).
- **Usuarios reales:** `root` (único ADMIN), y dos CAJERO — `Monse` y `Dulce` (las encargadas del negocio). Cada venta queda asociada al usuario que la registró (columna `usuario_id` en `ventas`, se ve en el reporte "Ventas por vendedor" del Dashboard).
- **Marca:** logo propio (ilustración del negocio, recorte cuadrado centrado sin deformar, fondo transparente) en el login y en el menú lateral — `frontend/public/logo.png` (generado a partir de `Descargas/ChatGPT Image 30 ago 2026, 05_54_09 p.m..png`), reutilizado también como favicon.
- **Menú:** sidebar vertical fijo a la izquierda (fondo degradado oscuro a rojo, íconos por sección), colapsable a solo íconos con un botón (‹/›) — el estado elegido se recuerda entre sesiones (`localStorage`). En pantallas angostas (celular) pasa a barra horizontal fija, sin el botón de colapsar.
- **Costo de infraestructura:** $0/mes. Único requisito: la PC del local debe permanecer encendida y con internet para que el acceso remoto funcione.

## 2. Modelo de datos (borrador)

| Tabla | Campos clave | Notas |
|---|---|---|
| `productos` | id, nombre, categoria, costo_unitario, precio_venta, unidad_medida, activo | |
| `productos` (stock) | stock_actual, stock_minimo | Se puede empezar con el stock en la misma tabla; separar a `inventario` si crece la complejidad. |
| `movimientos_inventario` | id, producto_id, tipo (entrada/salida/ajuste), cantidad, motivo, fecha | Auditoría de por qué cambió el stock (compra, merma, ajuste manual). |
| `ventas` | id, fecha, total, metodo_pago | |
| `venta_items` | id, venta_id, producto_id, cantidad, precio_unitario_momento, costo_unitario_momento | **Importante:** guardar el costo y precio "congelados" al momento de la venta, para que si luego cambian los precios/costos, los reportes históricos de ganancia no se distorsionen. |

Ganancia por venta = `sum((precio_unitario_momento - costo_unitario_momento) * cantidad)` por `venta_items`.

## 3. Fases de desarrollo

**Fase 0 — Infraestructura de red**
- Instalar y validar Tailscale.

**Fase 1 — Backend base**
- Scaffold Spring Boot (Web, Data JPA, PostgreSQL driver, Validation).
- Conexión a Postgres, migraciones (Flyway o Liquibase recomendado desde el día 1 para no perder el historial de cambios de esquema).
- Entidades: Producto, Venta, VentaItem, MovimientoInventario.

**Fase 2 — CRUD de inventario**
- Alta/baja/edición de productos, costo y precio.
- Registrar movimientos de stock (compra, ajuste, merma).

**Fase 3 — Registro de ventas**
- Endpoint para crear venta con ítems, descuenta stock automáticamente.
- Validación: no permitir vender más de lo disponible (control de concurrencia, ver riesgos).

**Fase 4 — Reportes de ganancia**
- Ganancia por día/semana/mes.
- Ranking de productos más vendidos / más rentables.
- Alertas de stock bajo.

**Fase 5 — Frontend Angular**
- Pantalla de venta rápida (grid de productos + carrito).
- Pantalla de inventario.
- Dashboard de ganancias.

**Fase 6 — Persistencia y confiabilidad**
- Backend como servicio de Windows (para que sobreviva reinicios).
- Backups automáticos de Postgres.

## 4. Setup de Tailscale

1. Crear cuenta en tailscale.com (plan Personal, gratis — hasta 3 usuarios / 100 dispositivos).
2. Instalar Tailscale en la PC del local (Windows) → queda corriendo como servicio, se conecta solo al iniciar Windows.
3. Instalar Tailscale en cada dispositivo remoto (Android/iOS/Windows/Mac).
4. Verificar conexión: `tailscale ip -4` en la PC del local da una IP tipo `100.x.x.x`. Desde el dispositivo remoto, `ping 100.x.x.x` debe responder.
5. Configurar Spring Boot para escuchar en todas las interfaces, no solo localhost: `server.address=0.0.0.0` en `application.properties`.
6. Permitir el puerto del backend (8081) en el Firewall de Windows — requiere PowerShell como Administrador:
   ```powershell
   New-NetFirewallRule -DisplayName "Heladeria Backend (8081)" -Direction Inbound -Protocol TCP -LocalPort 8081 -Action Allow -Profile Any
   ```
7. Postgres **no** necesita exponerse a la red — solo el backend, que corre en la misma PC y le habla a Postgres por localhost.

## 4.1. Cómo corre el sistema en producción

Backend y frontend van empaquetados juntos en un solo proceso Java, corriendo como **servicio de Windows** (`HeladeriaBackend`, vía NSSM) — arranca solo con Windows, no depende de ninguna terminal abierta. Accesible en `http://localhost:8081` (local) y `http://100.123.52.115:8081` (vía Tailscale — confirmar con `tailscale ip -4` si la IP cambia).

**Gestionar el servicio** (requiere PowerShell como Administrador):
```powershell
Get-Service HeladeriaBackend        # ver estado
Restart-Service HeladeriaBackend    # reiniciar (ej. despues de un deploy)
Stop-Service HeladeriaBackend
Start-Service HeladeriaBackend
```
Logs del servicio: `logs\service.log` (salida normal) y `logs\service-error.log` (errores) en la raíz del proyecto — para verlos en vivo, `Get-Content logs\service.log -Wait -Tail 50`. NSSM archiva el log anterior con un timestamp cada vez que el servicio se reinicia (`service-<fecha>.log`); `limpiar-logs.ps1` borra automáticamente los archivados de más de 30 días (tarea programada diaria "Heladeria - Limpiar logs", 3:10 — mismo patrón que el backup de la base, ver 5.3).

**Actualizar el sistema después de un cambio de código** (backend y/o frontend):
```powershell
cd "C:\Users\MSI\Desktop\Heladeria"
.\deploy.ps1                          # compila frontend + backend en un JAR nuevo
Restart-Service HeladeriaBackend      # requiere PowerShell como Administrador
```

**Modo desarrollo** (hot-reload, dos procesos separados en `:4200` y `:8081`, para cuando se esté programando en vivo en vez de operando en producción):
```powershell
# Terminal 1 - backend
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
cd "C:\Users\MSI\Desktop\Heladeria\backend"
.\mvnw.cmd spring-boot:run

# Terminal 2 - frontend
$env:PATH = "C:\Program Files\nodejs;" + $env:PATH
cd "C:\Users\MSI\Desktop\Heladeria\frontend"
npx ng serve --host 0.0.0.0
```
En este modo, primero detener el servicio de Windows (`Stop-Service HeladeriaBackend`) para liberar el puerto 8081.

## 5. Endpoints disponibles (base: `/api`)

Todos requieren **HTTP Basic Auth** (`Authorization: Basic base64(usuario:contraseña)`, o `-u usuario:contraseña` con curl). Usuarios actuales:
- `root` / `heladeria123` — ADMIN
- `Monse` / `monse2026` — CAJERO
- `Dulce` / `dulce2026` — CAJERO

Gestión de usuarios (alta y baja) desde la pantalla "Usuarios" (solo ADMIN) o vía `/api/usuarios`.

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/productos?soloActivos=true` | Listar productos |
| GET | `/productos/{id}` | Obtener un producto |
| POST | `/productos` | Crear producto (incluye `stockActual` inicial) |
| PUT | `/productos/{id}` | Editar producto (no toca `stockActual`) |
| DELETE | `/productos/{id}` | Baja lógica (`activo=false`) |
| GET | `/productos/{id}/movimientos` | Historial de movimientos de stock de un producto |
| POST | `/productos/{id}/movimientos` | Registrar movimiento (`{"tipo":"ENTRADA\|SALIDA\|AJUSTE","cantidad":N,"motivo":"..."}`) |
| GET | `/ventas` | Listar ventas |
| GET | `/ventas/{id}` | Obtener una venta con sus ítems |
| POST | `/ventas` | Registrar venta (`{"metodoPago":"EFECTIVO","items":[{"productoId":1,"cantidad":3}]}`) — descuenta stock automáticamente, responde 409 si no alcanza |
| GET | `/reportes/ganancias?desde=YYYY-MM-DD&hasta=YYYY-MM-DD` | Ganancia bruta del período (default: mes en curso) |
| GET | `/reportes/productos-mas-rentables?desde=&hasta=` | Ranking de productos por ganancia generada |
| GET | `/reportes/stock-bajo` | Productos activos con `stockActual <= stockMinimo` |
| GET | `/usuarios/yo` | Datos del usuario autenticado (usado por el frontend para armar el menú) — cualquier rol |
| GET | `/usuarios` | Listar usuarios — solo ADMIN |
| POST | `/usuarios` | Crear usuario (`{"username":"...","password":"...","rol":"ADMIN\|CAJERO"}`) — solo ADMIN |
| DELETE | `/usuarios/{id}` | Eliminar usuario — solo ADMIN (no se puede eliminar a uno mismo ni al único ADMIN restante) |
| GET | `/reportes/ventas-por-usuario?desde=&hasta=` | Cantidad de ventas y total vendido por vendedor — solo ADMIN |
| GET | `/reportes/resumen-hoy` | Ventas de hoy, total vendido hoy, cantidad de productos con stock bajo (sin costos/ganancia) — ADMIN y CAJERO |

**Permisos por rol:** `GET /productos*` y `/ventas/**` → ADMIN y CAJERO. Todo lo demás bajo `/productos/**` (crear/editar/dar de baja, movimientos de stock), `/reportes/**` y `/usuarios/**` (salvo `/usuarios/yo`) → solo ADMIN. Se valida en el backend (`SecurityConfig`), no solo en el frontend.

Todo probado manualmente con `curl` (creación de producto, venta con descuento de stock, venta con stock insuficiente → 409, movimiento manual de entrada, reportes de ganancia y ranking, alerta de stock bajo).

## 5.1. Frontend Angular

Proyecto en `frontend/` (Angular 22, standalone components + signals, sin NgModules). Pantallas: **Login**, **Inicio** (landing tras el login — resumen del día y accesos directos, ADMIN y CAJERO con distinto contenido), **Venta** (tabla con buscador/filtro/orden/paginación + carrito, modal para elegir cantidad), **Inventario** (tabla de productos + modal de alta/edición + modal de movimientos de stock — solo ADMIN), **Dashboard "Ganancias"** (ganancia por período + ranking + alerta de stock bajo — solo ADMIN), **Usuarios** (alta/baja de usuarios con rol — solo ADMIN). Autenticación vía HTTP Basic (credenciales + rol en `localStorage`, interceptor agrega el header a cada request y desloguea automáticamente ante un 401). El menú y las rutas se adaptan al rol (`adminGuard`), pero la autorización real está en el backend. Idioma de la app configurado en español (`LOCALE_ID: 'es'` + `registerLocaleData`) para que fechas y moneda se vean correctamente.

**Inicio** (`/inicio`, redirect por defecto tras login): tarjetas de "Ventas de hoy", "Total vendido hoy", "Stock bajo" (ambos roles) y "Ganancia de hoy" (solo ADMIN) + lista de productos con stock bajo si hay + accesos directos a las demás pantallas (los de ADMIN solo se ven si sos ADMIN).

**Cómo levantarlo:**
```powershell
$env:PATH = "C:\Program Files\nodejs;" + $env:PATH
cd "C:\Users\MSI\Desktop\Heladeria\frontend"
npx ng serve --host 0.0.0.0
```
Accesible en `http://localhost:4200` y `http://100.123.52.115:4200` (Tailscale). El backend debe estar corriendo (ver sección 4.1). El frontend llama a la API con ruta relativa (`/api/...`); en desarrollo, `ng serve` usa `proxy.conf.json` (configurado en `angular.json`) para reenviar esas llamadas a `http://localhost:8081` — así el mismo código funciona sin cambios tanto en desarrollo (`:4200` → proxy → `:8081`) como en producción (todo en `:8081`, mismo origen).

**Bugs encontrados y corregidos durante la prueba end-to-end con navegador real:**
1. En la pantalla de Venta, el mensaje "Venta #N registrada correctamente" se seteaba y al toque siguiente se borraba solo (`vaciarCarrito()` limpiaba el mensaje que se acababa de mostrar).
2. En Inventario, el modal de "Stock" mostraba la cantidad vieja después de registrar un movimiento (usaba el objeto producto desactualizado en vez de la respuesta fresca del backend).
3. "Ganancia de hoy" en Inicio daba ₲0 aunque hubiera ventas: el cálculo de "hoy" usaba `Date.toISOString()` (convierte a UTC), y cerca de medianoche en Paraguay (UTC-3) ya es el día siguiente en UTC, consultando el día equivocado. Se centralizó en `shared/fecha.ts` (`fechaLocalIso`) usando el calendario local en vez de UTC.
4. Las fechas se mostraban en inglés ("Sunday", "August") porque la app nunca registró el locale español para los pipes de Angular — agregado `LOCALE_ID: 'es'` + `registerLocaleData(localeEs)` en `app.config.ts`.

**Nota de infraestructura:** el backend tiene CORS configurado (`SecurityConfig`) como respaldo, pero no debería hacer falta en la práctica: en desarrollo el proxy de Angular evita el cross-origin (el navegador solo ve `:4200`), y en producción todo se sirve desde el mismo origen (`:8081`).

## 5.2. Empaquetado para producción

`deploy.ps1` (en la raíz del proyecto) hace todo el proceso: compila el frontend (`ng build --configuration production`), copia el resultado a `backend/src/main/resources/static`, y empaqueta el backend en un JAR único (`backend/target/backend-0.0.1-SNAPSHOT.jar`) que sirve tanto la API (`/api/**`) como la aplicación Angular (todo lo demás, con fallback a `index.html` para que las rutas del router de Angular no den 404 al refrescar — configurado en `SpaConfig.java`).

## 5.3. Backup de la base de datos

`backup-db.ps1` corre `pg_dump` contra `heladeria_db` y guarda el resultado en `backups\heladeria_db_<fecha>.sql`, borrando automáticamente los backups de más de 30 días. Programado como tarea de Windows ("Heladeria - Backup DB") todos los días a las 3:00.

Para restaurar un backup:
```powershell
$env:PGPASSWORD = $env:HELADERIA_DB_PASSWORD
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U heladeria_app -h localhost -d heladeria_db -f "backups\heladeria_db_<fecha>.sql"
```

## 5. Riesgos y límites del sistema

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Punto único de falla: todo depende de la PC del local | Si se apaga, se corta la luz o el internet, se cae la venta local **y** el acceso remoto | UPS/batería para la PC; considerar a futuro un modo caja offline básico |
| Sin login/roles | Cualquiera en la red Tailscale ve costos y ganancias | **Resuelto**: login con HTTP Basic Auth + BCrypt, y roles reales (ADMIN/CAJERO) validados tanto en frontend como en backend. Gestión de usuarios desde la pantalla "Usuarios" (solo ADMIN) |
| Sin backups | Falla de disco = pérdida total de datos | `pg_dump` automático diario a otro disco o a la nube (script + Tarea Programada de Windows) |
| Condiciones de carrera en stock | Dos ventas simultáneas del último producto pueden dejar stock negativo | Transacciones con bloqueo (`SELECT FOR UPDATE`) + constraint `CHECK (stock_actual >= 0)` en la tabla |
| Reinicios de Windows (updates) tiran el backend | El sistema deja de responder hasta reiniciar manualmente | Correr el backend como servicio de Windows (NSSM) con arranque automático |
| Dispositivo remoto perdido/robado con Tailscale autorizado | Acceso no autorizado a la red | Revocar el dispositivo desde el admin de Tailscale; activar 2FA en la cuenta Tailscale |
| Escalabilidad futura | Plan gratuito de Tailscale alcanza sobra para 1 local, pero si se suman sucursales conviene replantear a VPS (Opción B descartada por ahora) | Reevaluar cuando/si se abra una segunda sucursal |
| Credenciales en texto plano | `application.properties` tiene la contraseña de la base de datos en texto plano | Aceptable para desarrollo local; **no** commitear ese archivo tal cual a un repo público — usar variables de entorno o `.gitignore` cuando se inicialice git |
| Java no-LTS por defecto en esta PC | El sistema traía Java 26 (no LTS), con riesgo de incompatibilidades con el ecosistema Spring | Resuelto: se instaló Java 21 LTS (Eclipse Temurin) en paralelo, usado específicamente para este proyecto vía `JAVA_HOME` |
| Frontend corriendo con el servidor de desarrollo (`ng serve`) | Angular avisa explícitamente que ese servidor "no fue revisado por temas de seguridad" y no es apto para producción | **Resuelto**: el frontend se compila (`ng build --configuration production`) y se empaqueta dentro del JAR del backend, que lo sirve como archivos estáticos. Ya no hace falta `ng serve` en producción |
| Backup solo corre si hay una sesión de Windows iniciada | La tarea programada quedó en modo "Solo interactivo" — si la PC está apagada o deslogueada a las 3:00, ese día no se genera backup | Aceptable si la PC del local queda siempre con la sesión abierta (como ya se pidió para Tailscale). Si se quiere backup incluso deslogueado, hay que recrear la tarea con `schtasks /RU` y guardar credenciales (requiere admin) |

## 6. Troubleshooting común

- **No conecta desde el dispositivo remoto** → verificar que Tailscale figure "Connected" en ambos extremos; confirmar la IP con `tailscale ip -4`; revisar que el Firewall de Windows no bloquee el puerto.
- **Tailscale conecta pero el backend no responde** → confirmar `server.address=0.0.0.0` en `application.properties` (si quedó en `127.0.0.1` o default, solo acepta conexiones locales).
- **Postgres rechaza conexiones** → normalmente no debería hacer falta tocar `pg_hba.conf`/`postgresql.conf` porque Postgres solo habla con el backend en localhost; si se toca, revisar esos archivos.
- **Stock queda en negativo** → agregar `CHECK (stock_actual >= 0)` y usar transacción con bloqueo pesimista al descontar stock en la venta.
- **El sistema no arranca solo después de un reinicio de Windows** → instalar el backend como servicio con NSSM; Postgres y Tailscale ya arrancan solos por defecto en Windows.
- **No se puede acceder por la IP de Tailscale, pero sí por `localhost`** → el servicio de Windows "Tailscale" puede figurar "Running" aunque la app de Tailscale (el ícono de la bandeja del sistema) esté cerrada — y sin la app abierta, la VPN no conecta de verdad (`tailscale status` devuelve `NoState`). Solución: abrir la app de Tailscale desde el menú de inicio (no alcanza con que el servicio esté corriendo). Pasó una vez en este proyecto por cerrar la app sin querer.
- **"Autorización exigida" / "la conexión no es segura" en un navegador nuevo (PC distinta)** → el navegador intenta conectarse por HTTPS automáticamente (modo "siempre usar conexiones seguras"), pero el servidor solo habla HTTP en el 8081 (no tiene certificado TLS). Solución: escribir la URL completa con `http://` explícito al principio, ej. `http://100.123.52.115:8081`.

## 7. Checklist

- [x] Instalar Tailscale en la PC del local (IP: `100.123.52.115`, hostname `desktop-schuv6m`)
- [x] Instalar Tailscale en el/los dispositivo(s) remoto(s) (iPhone: `iphone-13`, IP `100.71.72.76`)
- [x] Verificar conexión remota (iPhone conectado en `tailscale status`; pendiente probar acceso real al backend cuando esté levantado)
- [x] Crear base de datos Postgres del proyecto (`heladeria_db`, rol dedicado `heladeria_app`, aislados del resto del sistema)
- [x] Definir esquema inicial (productos, ventas, venta_items, movimientos_inventario) con Flyway — `V1__esquema_inicial.sql`, verificado aplicándose correctamente
- [x] Scaffold backend Spring Boot (Spring Boot 4.1.1, Java 21 LTS vía Eclipse Temurin, Maven wrapper — proyecto compila y arranca OK)
- [x] Configurar `server.address=0.0.0.0`
- [x] Abrir el puerto 8081 en el Firewall de Windows
- [x] CRUD de productos (alta/edición/listado/baja lógica) — `stockActual` queda de solo lectura vía este endpoint, se ajustará por movimientos en la Fase 2
- [x] Registro de ventas con descuento de stock (con control de concurrencia — bloqueo pesimista `PESSIMISTIC_WRITE` + orden consistente de lock por id para evitar deadlocks; validado con prueba de stock insuficiente → 409)
- [x] Movimientos manuales de inventario (entrada/salida/ajuste) con auditoría
- [x] Reportes de ganancia (`GET /api/reportes/ganancias`, `/productos-mas-rentables`, `/stock-bajo` — todos probados y correctos)
- [x] Login básico (HTTP Basic Auth + Spring Security, contraseña hasheada con BCrypt — probado: sin credenciales → 401, credenciales incorrectas → 401, correctas → 200). Usuario inicial: `root`
- [x] Scaffold frontend Angular (venta, inventario, dashboard) — Angular 22 (standalone, signals), probado de punta a punta con navegador real: login, venta con descuento de stock, alta de producto, movimientos de stock, dashboard de ganancias, logout, y guard de rutas. Dos bugs reales encontrados y corregidos durante la prueba (ver sección 5.1)
- [x] Backend como servicio de Windows (NSSM) — servicio `HeladeriaBackend`, arranque automático, corriendo en `:8081`
- [x] Backup automático de Postgres — tarea programada diaria a las 3:00, retención de 30 días
- [x] Frontend empaquetado dentro del backend (un solo proceso/puerto, sin `ng serve` en producción)
- [x] Prueba end-to-end desde un dispositivo remoto vía Tailscale (celular accedió a `/api/productos` correctamente)
