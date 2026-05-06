# Guía de Pruebas Automatizadas con Postman

Esta guía detalla los pasos necesarios para configurar una colección profesional de Postman para la API B2B de EcoMóstoles, automatizando el flujo de autenticación con JWT, tal y como se exige en el **Ítem 20** de la rúbrica de evaluación.

## Paso 1: Configuración de Variables de Colección

Para evitar repetir la misma URL en cada petición y poder inyectar el token dinámicamente, configuraremos las **Collection Variables**:

1. Haz clic en tu colección en la barra lateral izquierda.
2. Ve a la pestaña **"Variables"**.
3. Añade una variable llamada `baseUrl` con el valor inicial y actual: `http://localhost:8080/api/v1`.
4. Añade una segunda variable llamada `token` y deja sus valores en blanco.
5. Haz clic en **Save**.

Tus rutas a partir de ahora deben escribirse usando la variable. Ejemplo: `{{baseUrl}}/offers`.

## Paso 2: Automatización del Login (Extracción del JWT)

Para no tener que copiar y pegar el JWT a mano cada vez que iniciamos sesión o el token expira, vamos a usar los scripts integrados de Postman:

1. Selecciona tu petición **POST Login** (URL: `{{baseUrl}}/auth/login`).
2. Ve a la pestaña **"Tests"** de la petición.
3. Pega el siguiente código JavaScript:

```javascript
// Validar que el login ha sido exitoso (HTTP 200 OK)
pm.test("Login OK: Status 200", function () {
    pm.response.to.have.status(200);
});

// Extraer el JSON del cuerpo de la respuesta
var jsonData = pm.response.json();

// Validar que el servidor nos devuelve el atributo 'token'
pm.test("Response contiene JWT", function () {
    pm.expect(jsonData).to.have.property("token");
});

// Guardar automáticamente el token en las variables de la colección
if (jsonData.token) {
    pm.collectionVariables.set("token", jsonData.token);
    console.log("El token JWT se ha guardado correctamente.");
}
```

4. Guarda la petición y haz clic en **Send**. Si las credenciales son válidas, la variable global se actualizará sola.

## Paso 3: Seguridad Global (Heredada)

Ahora indicaremos a la Colección que inyecte este token en todas las llamadas:

1. Haz clic en el nombre de la colección principal.
2. Ve a la pestaña **"Authorization"**.
3. En el desplegable "Type", selecciona **"Bearer Token"**.
4. En el campo "Token", escribe exactamente `{{token}}`.
5. Guarda los cambios de la colección.

*Nota:* Asegúrate de que todas tus demás peticiones (GET, PUT, DELETE) tengan su Authorization configurada como **"Inherit auth from parent"** (es la opción por defecto).

## Paso 4: Exportación para Entrega

Para que la colección sea evaluable:

1. Haz clic en los tres puntos `...` junto a tu Colección.
2. Selecciona **"Export"**.
3. Elige la versión "Collection v2.1 (recommended)".
4. Guarda el archivo resultante con el nombre `api.postman_collection.json` dentro del directorio `docs/` de tu proyecto.
