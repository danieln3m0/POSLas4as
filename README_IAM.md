# Sistema IAM (Identity and Access Management)

## Descripción
El sistema IAM implementa la gestión de identidades y acceso para el sistema POS Backend, siguiendo los principios de Domain-Driven Design (DDD).

## Arquitectura

### Capa de Dominio
- **Value Objects**: `Email`, `Password`, `Username`
- **Entities**: `Role`
- **Aggregates**: `User`
- **Commands**: `CreateUserCommand`, `ChangePasswordCommand`
- **Queries**: `UserQuery`
- **Events**: `UserCreatedEvent`, `UserPasswordChangedEvent`
- **Services**: `UserDomainService`

### Capa de Aplicación
- **Command Services**: `CreateUserCommandService`
- **Query Services**: `UserQueryService`
- **Outbound Services**: `HashingService`, `TokenService`

### Capa de Infraestructura
- **Repositories**: `UserRepository`, `RoleRepository`
- **Services**: `BCryptHashingService`, `JwtTokenService`
- **Configuration**: `SecurityConfiguration`

### Capa de Interfaces
- **Resources**: `UserResource`, `AuthResource`
- **Transformers**: `UserTransformer`
- **DTOs**: `UserDTO`

## Endpoints Disponibles

### Autenticación
- `POST /api/v1/auth/login` - Iniciar sesión
- `POST /api/v1/auth/validate` - Validar token

### Gestión de Usuarios
- `POST /api/v1/users` - Crear usuario
- `GET /api/v1/users/{userId}` - Obtener usuario por ID
- `GET /api/v1/users` - Listar usuarios activos
- `GET /api/v1/users/check-username/{username}` - Verificar disponibilidad de username
- `GET /api/v1/users/check-email/{email}` - Verificar disponibilidad de email

## Configuración

### Base de Datos
El sistema utiliza MySQL como base de datos. Configuración en `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pos_backend
spring.datasource.username=root
spring.datasource.password=password
```

### JWT
```properties
jwt.secret=miClaveSecretaSuperSeguraParaElSistemaPOS2024
jwt.expiration=86400000
```

## Usuario por Defecto
Al iniciar el sistema por primera vez, se crea automáticamente un usuario administrador:
- **Username**: admin
- **Email**: admin@pos.com
- **Password**: Admin123!

## Roles del Sistema
1. **ADMIN**: Acceso completo al sistema
2. **SELLER**: Acceso a ventas e inventario básico
3. **INVENTORY**: Gestión completa de inventario

## Documentación API
La documentación de la API está disponible en Swagger UI:
- URL: http://localhost:8080/swagger-ui.html

## Seguridad
- Autenticación basada en JWT
- Contraseñas hasheadas con BCrypt
- Validación de value objects en el dominio
- CORS configurado para desarrollo 