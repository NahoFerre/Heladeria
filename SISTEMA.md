# Qué hace el sistema

Sistema de punto de venta para la heladería/açaí: registro de ventas, control de caja, inventario, compras, gastos, promociones y reportes. Un solo local, dos roles de usuario.

## Roles

- **ADMIN** (`root`): acceso completo — gestión de productos/insumos, usuarios, compras, pérdidas, gastos, promociones, reportes, auditoría e historial de cajas.
- **CAJERO** (`Monse`, `Dulce`): Venta, Caja (incluidos sus propios movimientos de efectivo) e Inicio — sin ver costos, ganancias ni pantallas de gestión.

## Pantallas

**Inicio** — resumen del día (ventas, total vendido, stock bajo, ganancia si es ADMIN) y estado de la caja (abierta/cerrada), con accesos directos a las demás pantallas según el rol. Para ADMIN suma un bloque de **dashboard avanzado**: ventas de hoy/semana/mes con variación % contra el mismo tramo del período anterior; productos destacados del mes (más vendido, mayor ganancia, menor rotación, mayor margen); caja actual con efectivo esperado y ventas por método de pago; y alertas de inventario (stock bajo de productos e insumos, productos activos sin ninguna venta en los últimos 30 días).

**Venta** — grid de productos con buscador/filtro/orden, carrito, y confirmación de venta con método de pago (Efectivo, Tarjeta, Transferencia o QR). Los productos pueden tener **variantes** (ej. tamaños de Açaí, cada una con su propio precio/costo/stock) y **adicionales/toppings** configurables por grupo (mínimo/máximo de selección, con o sin costo, con o sin control de stock propio). Las **promociones** vigentes (ver más abajo) se aplican automáticamente al precio del producto/variante, y el carrito muestra el descuento antes de confirmar — el mismo cálculo que hace el servidor al cobrar, para que el total nunca cambie entre lo que se muestra y lo que se cobra. Con método Efectivo aparece un campo "Monto recibido" que calcula el vuelto en vivo. Descuenta stock automáticamente (stock propio o, si el producto/variante tiene una receta cargada, consumo de insumos). Bloqueada si no hay una caja abierta. Al confirmar, se genera un ticket en pantalla (logo, ítems, promoción aplicada si la hubo, total, vendedor) imprimible o exportable a PDF, y queda un botón para volver a verlo.

**Caja** — apertura de turno con un monto inicial de efectivo, retiro/ingreso/gasto de efectivo durante el turno (con motivo, visibles en una tabla del turno), y cierre contando el efectivo real. El efectivo esperado al cerrar es `monto inicial + ventas en efectivo + ingresos − retiros − gastos`; al cerrar se compara contra lo contado y se muestra la diferencia (faltante/sobrante), además del desglose de ventas del turno por método de pago. Solo puede haber una caja abierta a la vez.

**Registro de ventas** — historial completo de ventas con ver/(re)imprimir ticket. Una venta confirmada puede ser **anulada** (solo ADMIN, con motivo obligatorio): revierte automáticamente el stock exacto que esa venta había descontado (producto, variante, insumos de receta o adicionales), y queda marcada como anulada en el historial sin eliminarse ni afectar retroactivamente cajas ya cerradas ni reportes de ganancia.

**Inventario** *(ADMIN)* — alta, edición y baja de productos, sus variantes y sus grupos de adicionales (nombre, categoría, costo, precio, stock). Historial de movimientos de stock por producto (entrada, salida, ajuste — generados manualmente o automáticamente por ventas, compras y pérdidas). Cada producto/variante puede configurar una **receta** (insumos y cantidades que consume al venderse) en lugar de llevar stock propio.

**Insumos** *(ADMIN)* — materia prima usada en recetas (ej. pulpa de açaí, vasos, granola): nombre, unidad de medida, costo, stock actual y mínimo, proveedor.

**Compras** *(ADMIN)* — registra la compra de insumos y/o productos a un proveedor (número de factura, método de pago, observaciones opcionales). Al confirmar, aumenta el stock del ítem comprado y recalcula su costo unitario (costo promedio ponderado o último costo, configurable en el servidor). Genera movimiento de inventario para los ítems que son productos. Historial paginado con detalle por compra; no se edita ni se anula (append-only).

**Pérdidas** *(ADMIN)* — registra pérdida de stock de un insumo o producto (producto derretido, vencimiento, rotura, derrame, degustación, error de preparación, uso interno, otro), con cantidad y observación opcional. Descuenta stock y congela el costo del ítem en ese momento. Resumen con costo total y desgloses por ítem/motivo/usuario, más historial paginado.

**Gastos** *(ADMIN)* — registro de gastos operativos del negocio (categoría libre con sugerencias, descripción, monto, método de pago, observación) — no confundir con un retiro/gasto de caja, que es un movimiento de efectivo dentro de un turno para su reconciliación. No toca stock ni caja. Resumen (total + desglose por categoría) e historial paginado.

**Promociones** *(ADMIN)* — descuentos que se aplican automáticamente en Venta, sin que el cajero tenga que hacer nada. Tipos: porcentaje de descuento, monto fijo de descuento, o precio especial. Condiciones opcionales y combinables: fecha inicio/fin, producto específico o categoría entera (sin ninguna de las dos, aplica a todo el catálogo), y cantidad mínima por línea de venta. Si a un producto le calzan varias promociones vigentes a la vez, se aplica la que da el precio más bajo. Alta, edición y baja desde esta pantalla.

**Ganancias** *(ADMIN, pantalla "Ganancias" del menú)* — reportes por rango de fechas: ganancia operativa (ventas − costo de productos − costo de pérdidas − gastos operativos), con margen % y ticket promedio; ranking de productos y de categorías más rentables; ventas por vendedor; y alerta de productos con stock bajo. Exportable a PDF o CSV. Las ventas anuladas quedan excluidas de todos estos cálculos.

**Reportes** *(ADMIN)* — seis reportes con filtros combinables según corresponda a cada uno (fecha desde/hasta, usuario, método de pago, caja, producto, categoría de gasto) y exportación a PDF o CSV: Ventas (listado filtrable, distinto del Registro de ventas operativo), Métodos de pago (totales por período), Inventario (movimientos de stock filtrables por producto), Compras, Pérdidas y Gastos.

**Historial de cajas** *(ADMIN)* — listado de todas las cajas cerradas (apertura, cierre, montos, diferencia, responsables, ingresos/retiros/gastos del turno). Exportable a PDF con el total de cada columna.

**Usuarios** *(ADMIN)* — alta y baja de usuarios, asignando rol ADMIN o CAJERO.

**Auditoría** *(ADMIN)* — registro append-only (sin edición ni borrado posible, ni siquiera para ADMIN) de acciones sensibles: login, venta creada/anulada, producto creado/editado/cambio de precio, stock ajustado manualmente, caja abierta/cerrada, retiro/ingreso/gasto de caja, compra/pérdida/gasto/promoción registrada, usuario creado/eliminado. Filtrable por fecha y usuario, paginado desde el servidor (la única lista del sistema que no pagina en memoria, porque esta tabla crece indefinidamente).

## Comportamiento general

- Notificaciones tipo "toast" para errores y confirmaciones, spinners de carga, estados vacíos con ícono, tablas paginadas.
- Exportación a PDF vía impresión del navegador (con una hoja de estilos que oculta todo lo que no sea la tabla) y a CSV/Excel generado en el propio navegador, sin depender de ningún servicio externo.
- Diseño responsive (sidebar colapsable en escritorio, barra horizontal en mobile).
- Autenticación HTTP Basic; cada acción del backend valida el rol independientemente del frontend.
- Los precios y costos relevantes de cada venta (precio, costo, variante, adicionales, promoción aplicada) quedan "congelados" en el momento de vender, así que los reportes históricos no cambian si después se edita el catálogo.

## Infraestructura

Backend Spring Boot + PostgreSQL, frontend Angular empaquetado dentro del mismo proceso Java (un solo puerto). Corre como servicio de Windows en la PC del local, accesible en red local y remotamente vía Tailscale. Backups diarios automáticos de la base de datos.

Ver `PLAN.md` para arquitectura, endpoints y detalles técnicos, y `TODO.md` para el historial de avance y los próximos pasos del roadmap.
