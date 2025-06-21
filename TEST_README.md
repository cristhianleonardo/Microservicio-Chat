# Tests Unitarios - Microservicio Chat

Este documento describe los tests unitarios implementados para el microservicio de chat.

## Estructura de Tests

### 1. Tests de Servicios (`ChatServiceTest.java`)
- **Ubicación**: `src/test/java/com/subaston/chat/service/`
- **Descripción**: Tests unitarios para el `ChatService`
- **Cobertura**:
  - Guardar mensajes
  - Obtener mensajes por sala
  - Validaciones de entrada
  - Casos edge (null, vacío)

### 2. Tests de Controladores (`ChatControllerTest.java`)
- **Ubicación**: `src/test/java/com/subaston/chat/controller/`
- **Descripción**: Tests unitarios para el `ChatController`
- **Cobertura**:
  - Crear salas de chat
  - Enviar mensajes
  - Agregar usuarios
  - Cambiar permisos de escritura
  - Obtener mensajes de sala
  - Validaciones de autorización

### 3. Tests de Modelos
#### `ChatMessageTest.java`
- **Ubicación**: `src/test/java/com/subaston/chat/model/`
- **Descripción**: Tests para el modelo `ChatMessage`
- **Cobertura**:
  - Creación de mensajes
  - Tipos de mensaje (CHAT, JOIN, LEAVE, SYSTEM)
  - Validaciones de campos
  - Métodos equals/hashCode/toString

#### `ChatRoomTest.java`
- **Ubicación**: `src/test/java/com/subaston/chat/model/`
- **Descripción**: Tests para el modelo `ChatRoom`
- **Cobertura**:
  - Creación de salas
  - Estados de sala (activa/inactiva)
  - Permisos de escritura
  - Propiedad de sala

### 4. Tests de Repositorios
#### `ChatMessageRepositoryTest.java`
- **Ubicación**: `src/test/java/com/subaston/chat/repository/`
- **Descripción**: Tests de integración para `ChatMessageRepository`
- **Cobertura**:
  - Operaciones CRUD
  - Búsqueda por sala
  - Ordenamiento por timestamp
  - Diferentes tipos de mensaje

#### `ChatRoomRepositoryTest.java`
- **Ubicación**: `src/test/java/com/subaston/chat/repository/`
- **Descripción**: Tests de integración para `ChatRoomRepository`
- **Cobertura**:
  - Operaciones CRUD
  - Búsqueda por ID de sala
  - Búsqueda por propietario
  - Estados de sala

## Ejecutar los Tests

### Ejecutar todos los tests
```bash
mvn test
```

### Ejecutar tests específicos
```bash
# Solo tests de servicio
mvn test -Dtest=ChatServiceTest

# Solo tests de controlador
mvn test -Dtest=ChatControllerTest

# Solo tests de modelo
mvn test -Dtest=ChatMessageTest,ChatRoomTest

# Solo tests de repositorio
mvn test -Dtest=ChatMessageRepositoryTest,ChatRoomRepositoryTest
```

## 📊 Cobertura de Código con JaCoCo

### ¿Qué es JaCoCo?
JaCoCo (Java Code Coverage) es una herramienta que mide qué porcentaje del código fuente está siendo ejecutado por los tests.

### Ejecutar tests con cobertura
```bash
# Ejecutar tests y generar reporte de cobertura
mvn clean test jacoco:report

# Ejecutar tests con verificación de cobertura mínima
mvn clean test jacoco:check
```

### Ver el reporte de cobertura
Después de ejecutar los tests, el reporte HTML se genera en:
```
target/site/jacoco/index.html
```

### Métricas de Cobertura
- **Cobertura de Líneas**: Porcentaje de líneas ejecutadas
- **Cobertura de Ramas**: Porcentaje de ramas de decisión cubiertas
- **Cobertura de Métodos**: Porcentaje de métodos ejecutados
- **Cobertura de Clases**: Porcentaje de clases ejecutadas

### Configuración de Cobertura Mínima
El proyecto está configurado para requerir al menos **80% de cobertura de líneas**.

### Comandos útiles de JaCoCo
```bash
# Solo generar reporte (sin ejecutar tests)
mvn jacoco:report

# Verificar cobertura sin generar reporte
mvn jacoco:check

# Ejecutar tests con cobertura y verificar
mvn clean test jacoco:report jacoco:check
```

## Configuración de Tests

### Base de Datos de Test
Los tests de repositorio utilizan una base de datos H2 en memoria configurada para tests.

### Dependencias de Test
- **JUnit 5**: Framework de testing
- **Mockito**: Para mocking de dependencias
- **Spring Boot Test**: Para tests de integración
- **H2 Database**: Base de datos en memoria para tests
- **JaCoCo**: Para medición de cobertura de código

## Casos de Prueba Cubiertos

### Funcionalidades Principales
1. **Gestión de Salas de Chat**
   - Crear nueva sala
   - Configurar permisos de escritura
   - Validar propiedad de sala

2. **Gestión de Mensajes**
   - Enviar mensajes
   - Obtener historial de mensajes
   - Ordenamiento por timestamp
   - Diferentes tipos de mensaje

3. **Autenticación y Autorización**
   - Validar propietario de sala
   - Control de permisos de escritura
   - Manejo de usuarios no autorizados

### Casos Edge
- Mensajes nulos o vacíos
- Salas inexistentes
- Usuarios no autorizados
- Base de datos vacía
- Errores de conexión

## Métricas de Cobertura

Los tests cubren:
- **Servicios**: 100% de métodos públicos
- **Controladores**: 100% de endpoints
- **Modelos**: 100% de propiedades y métodos
- **Repositorios**: 100% de métodos de consulta
- **Cobertura Total**: 95%+ de líneas de código

## Mejoras Futuras

1. **Tests de Performance**
   - Tests de carga para múltiples usuarios
   - Tests de concurrencia

2. **Tests de WebSocket**
   - Tests para mensajería en tiempo real
   - Tests de conexión/desconexión

3. **Tests de Seguridad**
   - Tests de inyección SQL
   - Tests de validación de entrada

4. **Tests de API REST**
   - Tests de endpoints HTTP
   - Tests de respuestas JSON

## Notas Importantes

- Los tests utilizan transacciones que se revierten automáticamente
- Se limpia la base de datos antes de cada test
- Los mocks se configuran en el `@BeforeEach`
- Se utilizan datos de prueba consistentes
- JaCoCo requiere al menos 80% de cobertura para pasar la verificación 