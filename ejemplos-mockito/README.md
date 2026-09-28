# Ejemplos prácticos de Mockito — Grupo 10

Auditoría de Sistemas — UMSS
Integrantes: Main Rocabado Jairo Gael · Banda Tellez Fernando · Velasquez Ricaldez Rodrigo

## Sistema de ejemplo

Un **servicio de pedidos** (`PedidoService`) que depende de cuatro componentes externos:

| Dependencia            | En producción sería | En las pruebas     |
|------------------------|---------------------|--------------------|
| `InventarioRepository` | Base de datos       | `@Mock`            |
| `PasarelaPago`         | API de un banco     | `@Mock`            |
| `Notificador`          | Servidor de correo  | `@Mock`            |
| `PedidoRepository`     | Base de datos       | `@Mock`            |
| `CalculadoraDescuentos`| Lógica propia       | real, `@Spy` o `@Mock` |

## Cómo ejecutar

Solo hace falta JDK 21+ (no hace falta Maven):

```bash
./run.sh              # compila y ejecuta las pruebas (65 verdes + 3 omitidas a propósito)
./run.sh cobertura    # además genera target/cobertura/index.html (JaCoCo)
./run.sh rojos        # ejecuta las 3 versiones "en rojo" del paquete pasoapaso
```

Con Maven o desde un IDE (IntelliJ / VS Code / Eclipse): abrir el `pom.xml` y ejecutar `mvn test`.

## Mapa de ejemplos

### `basico/`: cómo se usa Mockito y cómo funciona por dentro
| Archivo | Qué enseña |
|---|---|
| `E01_PrimerMockTest` | `mock()`, valores por defecto, `when().thenReturn()`, respuestas consecutivas, `verify()`, `times()`, `never()` |
| `E02_AnotacionesTest` | `@Mock`, `@InjectMocks`, `MockitoExtension` |
| `E03_ArgumentMatchersYCaptorTest` | `any()`, `eq()`, `anyString()`, `doubleThat()`, `contains()`, `ArgumentCaptor`, `thenAnswer()` |
| `E04_ExcepcionesYVoidTest` | `thenThrow()`, `doThrow()`, `doNothing()`, `doAnswer()` para métodos `void` |
| `E05_SpyTest` | `spy()`, espía parcial, `doReturn().when()` |
| `E06_BDDMockitoTest` | Estilo *Given/When/Then* con `given()` / `then().should()` y `@Spy` |
| `E07_OrdenYStaticTest` | `InOrder`, `verifyNoMoreInteractions()`, `mockStatic()` |
| `E08_StrictStubsTest` | *Strict stubs* y `lenient()` |
| `E09_ArquitecturaInternaTest` | **Componentes internos en acción**: `MockingDetails`, la llamada de ensayo que se descarta, el último stub gana, el mock maker inline, la copia de estado del spy, `MockingProgress` (última invocación) y la pila de matchers |
| `E10_ComponentesExtensiblesTest` | **Componentes extensibles**: `Answer` y `ArgumentMatcher` propios, los 3 `MockMaker` (inline/subclass/proxy), `InvocationListener`, `mockConstruction` |

### `pasoapaso/`: cómo se escribe una prueba, de rojo a verde
Una misma prueba (criterio de aceptación de RF-05) construida versión por versión:

| Archivo | Versión | Resultado |
|---|---|---|
| `Paso1_EstructuraTest` | `@ExtendWith`, `@Mock`, `@Spy`, `@InjectMocks` | verde |
| `Paso2_RojoSinStubsTest` | Arrange-Act-Assert sin stubs | **rojo**: stock = 0 (valor por defecto) |
| `Paso3_RojoStubIncompletoTest` | solo el stub del stock | **rojo**: `cobrar()` devuelve null → NPE |
| `Paso4_VerdeTest` | todos los stubs + oráculo 450 calculado a mano | verde |
| `Paso5_VerificarComportamientoTest` | + `verify` del stock, guardado y correo | verde |
| `Paso6_RojoVerificacionTest` | el tester olvida el descuento (espera 500) | **rojo**: "Wanted but not invoked… 450.0" |

Los rojos tienen `@Disabled` para no romper la suite; se ven con `./run.sh rojos`.

### `cajanegra/`: pruebas de caja negra (a partir de la especificación)
- `CalculadoraDescuentosCajaNegraTest`: **partición en clases de equivalencia** y **valores límite** (`@ParameterizedTest`).
- `PedidoServiceCajaNegraTest`: **tabla de decisión** del caso de uso *Procesar pedido*; los mocks solo proveen entradas (stubs) y no se usa `verify()`.

### `cajablanca/`: pruebas de caja blanca (a partir del código)
- `PedidoServiceCajaBlancaTest`: **camino básico de McCabe**, V(G) = 7, con 7 casos. Los stubs fuerzan cada rama y `verify()` demuestra qué se ejecutó.
- `CalculadoraDescuentosCajaBlancaTest`: **cobertura de decisiones/ramas**, V(G) = 5.

## Resultado

```
68 tests found, 65 successful, 3 skipped (rojos a propósito), 0 failed
PedidoService          → 100 % de ramas (12/12)
CalculadoraDescuentos  → 100 % de ramas (8/8)
```

Ejecutando **solo la caja negra**, `PedidoService` alcanza 11/12 ramas: la rama `pedido == null`
no sale de ningún requisito y **solo la encuentra la caja blanca**. Por eso las dos técnicas se complementan.
