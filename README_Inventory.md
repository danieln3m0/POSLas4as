# Sistema de Inventario

## Descripción
El sistema de inventario implementa la gestión completa de productos, stock y ubicaciones para el sistema POS Backend, siguiendo los principios de Domain-Driven Design (DDD).

## Historias de Usuario Implementadas

### HU1.1 - Gestión de Productos
- ✅ Registrar productos con SKU, nombre, descripción, precios, categoría, unidad de medida y códigos
- ✅ Editar información de productos
- ✅ Eliminar productos (desactivación lógica)
- ✅ Validación de SKU único y códigos de barras

### HU1.2 - Stock en Tiempo Real
- ✅ Ver stock actual de cada producto
- ✅ Stock por ubicación específica
- ✅ Stock total consolidado
- ✅ Indicadores de stock bajo y necesidad de reorden

### HU1.3 - Movimientos de Inventario
- ✅ Registrar entradas de mercancía (compras)
- ✅ Registrar salidas (ventas, mermas)
- ✅ Transferencias entre ubicaciones
- ✅ Trazabilidad con lotes y fechas de expiración

### HU1.4 - Gestión de Ubicaciones
- ✅ Asignar productos a diferentes ubicaciones
- ✅ Transferir stock entre ubicaciones
- ✅ Tipos de ubicación: Almacén, Tienda, Centro de Distribución

### HU1.5 - Alertas de Stock Bajo
- ✅ Notificaciones automáticas cuando el stock alcanza el mínimo
- ✅ Eventos de dominio para integración con sistemas externos
- ✅ Configuración de umbrales por producto

### HU1.6 - Sugerencias de Reorden
- ✅ Cálculo automático de cantidades sugeridas
- ✅ Basado en stock mínimo, demanda y plazos
- ✅ Lista de productos que necesitan reorden

## Arquitectura

### Capa de Dominio
- **Value Objects**: `SKU`, `Price`, `Quantity`
- **Entities**: `Category`, `Location`, `Supplier`, `StockItem`
- **Aggregates**: `Product`
- **Commands**: `CreateProductCommand`, `UpdateStockCommand`
- **Events**: `ProductCreatedEvent`, `ProductStockUpdatedEvent`, `LowStockAlertEvent`
- **Services**: `InventoryDomainService`

### Capa de Aplicación
- **Command Services**: `CreateProductCommandService`, `UpdateStockCommandService`
- **Query Services**: `ProductQueryService`

### Capa de Infraestructura
- **Repositories**: `ProductRepository`, `StockItemRepository`, `CategoryRepository`, `LocationRepository`, `SupplierRepository`

### Capa de Interfaces
- **Resources**: `ProductResource`
- **Transformers**: `ProductTransformer`
- **DTOs**: `ProductDTO`

## Endpoints Disponibles

### Gestión de Productos
- `POST /api/v1/inventory/products` - Crear producto
- `GET /api/v1/inventory/products/{productId}` - Obtener producto por ID
- `GET /api/v1/inventory/products/sku/{sku}` - Obtener producto por SKU
- `GET /api/v1/inventory/products` - Listar todos los productos
- `GET /api/v1/inventory/products/category/{categoryId}` - Productos por categoría
- `GET /api/v1/inventory/products/search?q={term}` - Buscar productos

### Gestión de Stock
- `POST /api/v1/inventory/products/{productId}/stock` - Actualizar stock
- `GET /api/v1/inventory/products/{productId}/stock` - Obtener información de stock

### Alertas y Reportes
- `GET /api/v1/inventory/products/low-stock` - Productos con stock bajo
- `GET /api/v1/inventory/products/needing-reorder` - Productos que necesitan reorden

## Modelo de Datos

### Product (Aggregate Root)
- SKU único
- Información básica (nombre, descripción)
- Precios de compra y venta
- Configuración de stock (mínimo, máximo, punto de reorden)
- Relaciones con categoría y proveedor
- Códigos de barras y QR

### StockItem (Entity)
- Cantidad en ubicación específica
- Información de lote y expiración
- Trazabilidad completa

### Location (Entity)
- Tipos: WAREHOUSE, STORE, DISTRIBUTION
- Información de dirección completa
- Gestión de múltiples ubicaciones

## Características Técnicas

### Validaciones de Dominio
- SKU único y formato válido
- Precios no negativos
- Cantidades no negativas
- Stock mínimo <= punto de reorden <= stock máximo

### Eventos de Dominio
- `ProductCreatedEvent`: Cuando se crea un nuevo producto
- `ProductStockUpdatedEvent`: Cuando cambia el stock
- `LowStockAlertEvent`: Cuando el stock está bajo

### Cálculos Automáticos
- Stock total por producto
- Margen de ganancia
- Cantidad sugerida para reorden
- Días hasta expiración

## Configuración

### Base de Datos
El sistema utiliza las mismas configuraciones que el módulo IAM:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pos_backend
spring.jpa.hibernate.ddl-auto=update
```

### Inicialización
Los productos, categorías, ubicaciones y proveedores se pueden crear a través de los endpoints REST.

## Documentación API
La documentación completa está disponible en Swagger UI:
- URL: http://localhost:8080/swagger-ui.html

## Integración con IAM
El sistema de inventario está integrado con el sistema IAM para:
- Autenticación y autorización
- Auditoría de cambios
- Roles específicos para gestión de inventario

## Próximos Pasos
1. Implementar sistema de notificaciones por email
2. Agregar reportes avanzados de inventario
3. Implementar integración con proveedores
4. Agregar funcionalidad de códigos QR dinámicos
5. Implementar dashboard en tiempo real 