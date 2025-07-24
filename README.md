# Sistema POS Backend - A&S Soluciones Generales E.I.R.L

## Descripción
Sistema de gestión de inventario desarrollado con Spring Boot siguiendo los principios de Domain-Driven Design (DDD). El sistema incluye gestión de usuarios (IAM) y gestión completa de inventario.

## Arquitectura

### Bounded Contexts
1. **IAM (Identity and Access Management)**: Gestión de usuarios, roles y autenticación
2. **Inventory**: Gestión de productos, stock y ubicaciones

### Patrón DDD Implementado
- **Domain Layer**: Value Objects, Entities, Aggregates, Commands, Queries, Events, Domain Services
- **Application Layer**: Command Services, Query Services, Outbound Services
- **Infrastructure Layer**: Repositories, External Services, Configuration
- **Interface Layer**: REST Controllers, DTOs, Transformers

## Tecnologías Utilizadas

### Backend
- **Spring Boot 3.5.4**
- **Spring Security** con JWT
- **Spring Data JPA** con Hibernate
- **MySQL** como base de datos
- **SpringDoc OpenAPI** para documentación
- **Lombok** para reducción de código boilerplate
- **JJWT** para manejo de tokens JWT

### Arquitectura
- **Domain-Driven Design (DDD)**
- **Clean Architecture**
- **Event-Driven Architecture**
- **RESTful APIs**

## Estructura del Proyecto

```
src/main/java/com/las4as/POSBackend/
├── IAM/                           # Bounded Context de Identidad y Acceso
│   ├── Domain/
│   │   ├── model/
│   │   │   ├── aggregates/        # User
│   │   │   ├── entities/          # Role
│   │   │   ├── valueobjects/      # Email, Password, Username
│   │   │   ├── commands/          # CreateUserCommand, etc.
│   │   │   ├── queries/           # UserQuery
│   │   │   └── events/            # UserCreatedEvent, etc.
│   │   └── services/              # UserDomainService
│   ├── Application/
│   │   ├── commandServices/       # CreateUserCommandService
│   │   ├── queryServices/         # UserQueryService
│   │   └── outboundServices/      # HashingService, TokenService
│   ├── Infrastructure/
│   │   ├── persistence/           # Repositories
│   │   ├── hashing/               # BCryptHashingService
│   │   ├── tokens/                # JwtTokenService
│   │   └── authorization/         # SecurityConfiguration
│   └── Interfaces/
│       ├── resources/             # UserResource, AuthResource
│       └── transform/             # UserDTO, UserTransformer
├── Inventory/                     # Bounded Context de Inventario
│   ├── Domain/
│   │   ├── model/
│   │   │   ├── aggregates/        # Product
│   │   │   ├── entities/          # Category, Location, Supplier, StockItem
│   │   │   ├── valueobjects/      # SKU, Price, Quantity
│   │   │   ├── commands/          # CreateProductCommand, etc.
│   │   │   └── events/            # ProductCreatedEvent, etc.
│   │   └── services/              # InventoryDomainService
│   ├── Application/
│   │   ├── commandServices/       # CreateProductCommandService, etc.
│   │   └── queryServices/         # ProductQueryService
│   ├── Infrastructure/
│   │   └── persistence/           # Repositories
│   └── Interfaces/
│       ├── resources/             # ProductResource
│       └── transform/             # ProductDTO, ProductTransformer
└── shared/                        # Código compartido entre bounded contexts
    ├── domain/
    │   └── model/
    │       ├── aggregates/        # AuditableAbstractAggregateRoot
    │       └── entities/          # AuditableModel
    ├── infrastructure/
    │   ├── documentation/         # OpenAPI Configuration
    │   └── persistance/           # JPA Configuration
    └── interfaces/
        └── rest/                  # Shared REST Resources
```

## Configuración

### Requisitos Previos
- Java 21
- MySQL 8.0+
- Maven 3.6+

### Configuración de Base de Datos
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pos_backend?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=password
```

### Configuración de JWT
```properties
jwt.secret=miClaveSecretaSuperSeguraParaElSistemaPOS2024
jwt.expiration=86400000
```

## Instalación y Ejecución

### 1. Clonar el repositorio
```bash
git clone <repository-url>
cd POSBackend
```

### 2. Configurar base de datos
- Crear base de datos MySQL: `pos_backend`
- Actualizar credenciales en `application.properties`

### 3. Ejecutar la aplicación
```bash
mvn spring-boot:run
```

### 4. Acceder a la documentación
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## Usuario por Defecto
Al iniciar el sistema por primera vez, se crea automáticamente:
- **Username**: admin
- **Email**: admin@pos.com
- **Password**: Admin123!

## Endpoints Principales

### Autenticación (IAM)
- `POST /api/v1/auth/login` - Iniciar sesión
- `POST /api/v1/auth/validate` - Validar token
- `POST /api/v1/users` - Crear usuario
- `GET /api/v1/users` - Listar usuarios

### Inventario
- `POST /api/v1/inventory/products` - Crear producto
- `GET /api/v1/inventory/products` - Listar productos
- `POST /api/v1/inventory/products/{id}/stock` - Actualizar stock
- `GET /api/v1/inventory/products/low-stock` - Productos con stock bajo

## Características Implementadas

### Sistema IAM
- ✅ Gestión de usuarios y roles
- ✅ Autenticación con JWT
- ✅ Autorización basada en roles
- ✅ Validación de contraseñas seguras
- ✅ Auditoría de cambios

### Sistema de Inventario
- ✅ Gestión completa de productos
- ✅ Control de stock en tiempo real
- ✅ Gestión de ubicaciones múltiples
- ✅ Alertas de stock bajo
- ✅ Sugerencias de reorden
- ✅ Trazabilidad con lotes
- ✅ Códigos de barras y QR

## Historias de Usuario Implementadas

### Épica 1: Gestión de Inventario
- ✅ **HU1.1**: Registrar, editar y eliminar productos
- ✅ **HU1.2**: Ver stock actual en tiempo real
- ✅ **HU1.3**: Registrar entradas y salidas de mercancía
- ✅ **HU1.4**: Asignar productos a ubicaciones y transferir stock
- ✅ **HU1.5**: Alertas automáticas de stock bajo
- ✅ **HU1.6**: Sugerencias de órdenes de compra

## Seguridad

### Autenticación
- JWT tokens con expiración configurable
- Contraseñas hasheadas con BCrypt
- Validación de tokens en cada request

### Autorización
- Roles: ADMIN, SELLER, INVENTORY
- Permisos granulares por funcionalidad
- Validación de acceso en endpoints

### Validaciones
- Value Objects con validaciones de dominio
- Validación de entrada en todos los endpoints
- Manejo de errores consistente

## Eventos de Dominio

### IAM Events
- `UserCreatedEvent`: Usuario creado
- `UserPasswordChangedEvent`: Contraseña cambiada

### Inventory Events
- `ProductCreatedEvent`: Producto creado
- `ProductStockUpdatedEvent`: Stock actualizado
- `LowStockAlertEvent`: Alerta de stock bajo

## Próximos Pasos

### Funcionalidades Pendientes
1. Sistema de notificaciones por email
2. Reportes avanzados de inventario
3. Integración con proveedores
4. Dashboard en tiempo real
5. API para integración con frontend

### Mejoras Técnicas
1. Implementar CQRS para consultas complejas
2. Agregar Event Sourcing
3. Implementar Saga Pattern para transacciones distribuidas
4. Agregar métricas y monitoreo
5. Implementar cache distribuido

## Contribución

### Estándares de Código
- Seguir principios DDD
- Usar nombres descriptivos en español
- Documentar APIs con Swagger
- Escribir tests unitarios
- Seguir convenciones de Spring Boot

### Estructura de Commits
```
feat: agregar funcionalidad de gestión de productos
fix: corregir validación de SKU
docs: actualizar documentación de API
refactor: mejorar servicio de dominio
```

## Licencia
Este proyecto es propiedad de A&S Soluciones Generales E.I.R.L

## Contacto
- **Empresa**: A&S Soluciones Generales E.I.R.L
- **Desarrollador**: Daniel
- **Email**: admin@pos.com 