# [Nombre de la Aplicación]

## 👥 Miembros del Equipo

| Nombre y Apellidos | Correo URJC                        | Usuario GitHub |
| :----------------- | :--------------------------------- | :------------- |
| [Guillermo]        | [g.dominguez.2022@alumnos.urjc.es] | [User1]        |
| [Daniel]           | [d.nieto.2021@alumnos.urjc es]     | [User2]        |
| [Alberto]          | [a.sastre.2022@alumnos.urjc.es]    | [User3]        |
| [Javier]           | [j.delacasa.2022@alumnos.urjc es]  | [User4]        |

---

## 🎭 **Preparación: Definición del Proyecto**

### **Descripción del Tema**

El proyecto consiste en una Plataforma de Simbiosis Industrial y Economía Circular (B2B) enfocada en el ámbito local (polígonos industriales). La aplicación web permite el intercambio de recursos entre empresas: lo que para una es un residuo, para otra es materia prima. Además, facilita el alquiler de maquinaria industrial infrautilizada. El objetivo es reducir costes, optimizar recursos y ofrecer trazabilidad digital cumpliendo normativas medioambientales.

### **Entidades**

1. **Usuario (Empresa)**: Representa a los actores del sistema (PYMES industriales). Almacena credenciales, datos fiscales (CIF), ubicación y sector.
2. **Activo (Recurso)**: El objeto de intercambio. Puede ser un Residuo (oferta de material) o Maquinaria (oferta de alquiler). Contiene atributos como tipo, cantidad, estado y precio.
3. **Solicitud (Demanda)**: Representa una necesidad del mercado. Un usuario publica qué necesita si no lo encuentra en el catálogo.
4. **Acuerdo (Transacción)**: Formalización del intercambio. Vincula al Vendedor, Comprador y el Activo, almacenando el estado de la negociación y fecha.

**Relaciones entre entidades:**

- Usuario - Activo (1:N): Una empresa puede publicar múltiples activos (residuos o máquinas).
- Usuario - Solicitud (1:N): Una empresa puede crear múltiples solicitudes de materiales que necesita.
- Usuario - Acuerdo (1:N): Un acuerdo vincula a dos usuarios (comprador y vendedor).
- Activo - Acuerdo (1:1): Un activo específico se vincula a un acuerdo cuando se cierra la transacción.

### **Permisos de los Usuarios**

Los permisos se gestionan mediante un sistema RBAC (Control de Acceso Basado en Roles):

- **Usuario Anónimo (Visitante)**:
  - Permisos: Acceso en modo "lectura restringida". Puede visualizar el catálogo global (escaparate) para ver la oferta del mercado.
  - Restricción: No ve datos de contacto, precios específicos ni ubicación exacta. No puede realizar transacciones.

- **Usuario Registrado (Empresa)**:
  - Permisos: Acceso completo a la operativa. Puede publicar Activos, crear Solicitudes, ver datos de contacto y gestionar Acuerdos. Acceso a Dashboard con métricas.
  - Es dueño de: Sus propios Activos, sus Solicitudes y su Perfil de Empresa. Solo la empresa creadora puede editar o eliminar estos registros.

- **Administrador**:
  - Permisos: Supervisión y mantenimiento. Valida la legitimidad de nuevas empresas (verificación de CIF), gestiona categorías de materiales y modera contenidos ilícitos.
  - Es dueño de: Gestión global de categorías y capacidad de moderación sobre todos los Activos y Usuarios.

### **Imágenes**

La plataforma permitirá la carga de imágenes vinculadas a las siguientes entidades:

- **Actvo**: Múltiples imágenes reales por activo (maquinaria o lotes de residuos) para verificar su estado.
- **Usuario (Empresa)**: Una imagen de logotipo corporativo para el perfil, mejorando la confianza B2B.

### **Gráficos**

Se integrará un Dashboard para aportar valor a la gestión empresarial con los siguientes gráficos:

- **Gráfico de impacto**: Gráfico de barras (Bar Chart) mostrando el volumen de residuos revalorizados (kg) o el ahorro económico estimado mensual.
- **Gráfico de Categorías**: Gráfico de barras o circular mostrando la distribución de activos por tipo de material o categoría.

### **Tecnología Complementaria**

Se implementará un módulo de Generación Documental y Notificaciones:

- Generación de PDFs: Al cerrar un acuerdo, el sistema genera automáticamente un Albarán de Recogida o Contrato de Alquiler con los datos de ambas partes para garantizar la trazabilidad.
- Envío de Correos: Envío automatizado del PDF generado al correo electrónico de los usuarios involucrados mediante integración con servidor SMTP.

### **Algoritmo o Consulta Avanzada**

El núcleo inteligente de la aplicación será un Sistema de "Matching" Industrial:

- **Algoritmo/Consulta**: Emparejamiento proactivo de Oferta y Demanda.
- **Descripción**: El sistema analiza periódicamente las nuevas Solicitudes (Demandas) y las compara con los Activos (Ofertas) disponibles. Es decir, calcularáun "Índice de Compatibilidad" ponderando: Coincidencia de material/subcategoría, volumen requerido vs disponible (con un margen del 10%) y proximidad geográfica (priorizando mismo polígono). Y como resultado, sugiere las mejores oportunidades de negocio ordenadas por compatibilidad.

---

## 🛠 **Práctica 1: Maquetación de páginas web con HTML y CSS**

### **Diagrama de Navegación**

Diagrama que muestra cómo se navega entre las diferentes páginas de la aplicación:

![Diagrama de Navegación](images/navigation-diagram.png)

> [Descripción opcional del flujo de navegación: Ej: "El usuario puede acceder desde la página principal a todas las secciones mediante el menú de navegación. Los usuarios anónimos solo tienen acceso a las páginas públicas, mientras que los registrados pueden acceder a su perfil y panel de usuario."]

### **Capturas de Pantalla y Descripción de Páginas**

#### **1. Página Principal / Home**

![Página Principal](images/home-page.png)

> [Descripción breve: Ej: "Página de inicio que muestra los productos destacados, categorías principales y un banner promocional. Incluye barra de navegación y acceso a registro/login para usuarios no autenticados."]

#### **AQUÍ AÑADIR EL RESTO DE PÁGINAS**

### **Participación de Miembros en la Práctica 1**

#### **Alumno 1 - [Guillermo Domínguez Galindo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |                                                                                            Commits                                                                                             |           Files           |
| :-: | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :-----------------------: |
|  1  |                                     [Add mis_activos.html with layout for asset management; include navbar, sidebar, and table for offers.](URL_commit_1)                                      | [Archivo1](URL_archivo_1) |
|  2  |                                                        [Add particles.js library for dynamic particle effects on canvas](URL_commit_2)                                                         | [Archivo2](URL_archivo_2) |
|  3  |                                                   [Add custom navbar styles and button hover effects for improved interaction](URL_commit_3)                                                   | [Archivo3](URL_archivo_3) |
|  4  |                                            [Update styles and enhance dashboard layout with improved navbar, sidebar, and KPI cards](URL_commit_4)                                             | [Archivo4](URL_archivo_4) |
|  5  | [Refactor navigation and layout in dashboard, mensajes, mis_activos, mercado, and perfil_empresa pages; enhance user experience with consistent styling and improved structure.](URL_commit_5) | [Archivo5](URL_archivo_5) |
|  6  |    [Refactor layout in crear_activo, dashboard, mensajes, and mis_activos pages for improved structure and user experience; adjust sidebar styling for better responsiveness](URL_commit_6)    | [Archivo6](URL_archivo_6) |
|  7  |                             [Add admin_reportes page and update navigation links in admin_ofertas, admin_panel, and admin_usuarios for consistency](URL_commit_7)                              | [Archivo7](URL_archivo_7) |
|  8  |                                                        [Add edit demand page and my demands page with updated layout and functionality                                                         |

- Created `editar_solicitud.html` for editing demand requests with form validation and Bootstrap styling.
- Added `mis_demandas.html` to display user's published demands with statistics and action buttons. - Introduced a new favicon in SVG format for branding.
  ](URL_commit_8) | [Archivo8](URL_archivo_8) |

---

#### **Alumno 2 - [Daniel Nieto Díaz]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |                                                                                 Commits                                                                                 |           Files           |
| :-: | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :-----------------------: |
|  1  |                                                 [Add login and registration pages with Bootstrap styling](URL_commit_1)                                                 | [Archivo1](URL_archivo_1) |
|  2  |                                    [Add mercado.html for offers display; include navigation, filters, and item cards](URL_commit_2)                                     | [Archivo2](URL_archivo_2) |
|  3  |                                 [Refactor CSS styles for improved readability and organization; add comments for clarity](URL_commit_3)                                 | [Archivo3](URL_archivo_3) |
|  4  |                                 [Enhance registration and login pages with new navbar, footer, and improved form layout](URL_commit_4)                                  | [Archivo4](URL_archivo_4) |
|  5  |                          [Refactor CSS styles for improved organization and readability; fix spotlight effect on card component](URL_commit_5)                          | [Archivo5](URL_archivo_5) |
|  6  | [Enhance detalle_activo.html with Bootstrap integration, improved layout, and new footer; update navbar and content structure for better user experience](URL_commit_6) | [Archivo6](URL_archivo_6) |
|  7  |                         [Add consistent footer across dashboard, mensajes, and detalle_activo pages for improved user experience](URL_commit_7)                         | [Archivo7](URL_archivo_7) |
|  8  |                             [Replace Twitter icon with SVG version and update styling for consistency across multiple pages](URL_commit_8)                              | [Archivo8](URL_archivo_8) |
|  9  |                               [Add new image assets and update footer layout for improved consistency and user experience](URL_commit_9)                                | [Archivo9](URL_archivo_9) |

---

#### **Alumno 3 - [Alberto Sastre Zorrilla]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |                                                                          Commits                                                                          |           Files           |
| :-: | :-------------------------------------------------------------------------------------------------------------------------------------------------------: | :-----------------------: |
|  1  |                                      [Add index.html with initial structure and Bootstrap integration](URL_commit_1)                                      | [Archivo1](URL_archivo_1) |
|  2  |              [Add dashboard.html with initial layout and sidebar navigation; update styles.css for button and sidebar styles](URL_commit_2)               | [Archivo2](URL_archivo_2) |
|  3  |                       [Add detalle_activo.html for offer details display; include navbar, image, and contact button](URL_commit_3)                        | [Archivo3](URL_archivo_3) |
|  4  |                                   [Add spotlight effect to card component with dynamic hover interaction](URL_commit_4)                                   | [Archivo4](URL_archivo_4) |
|  5  |                 [Update styles and finalize layout adjustments in CSS and HTML; enhance particle effects and clean up code](URL_commit_5)                 | [Archivo5](URL_archivo_5) |
|  6  |                              [Refactor perfil_empresa.html and registro.html for improved layout and styling](URL_commit_6)                               | [Archivo6](URL_archivo_6) |
|  7  | [ Refactor mensajes.html and crear_activo.html for improved layout and user experience; integrate particles.js for enhanced visual effects](URL_commit_7) | [Archivo7](URL_archivo_7) |
|  8  |      [Add admin_ofertas and admin_usuarios pages with updated layout and styling; integrate particles.js for enhanced visual effects](URL_commit_8)       | [Archivo8](URL_archivo_8) |

---

#### **Alumno 4 - [Javier de la Casa Muñoz]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |                                                                                         Commits                                                                                         |           Files           |
| :-: | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :-----------------------: |
|  1  |                                                 [Add navbar and hero section to index.html; enhance styles in styles.css](URL_commit_1)                                                 | [Archivo1](URL_archivo_1) |
|  2  |                                       [Add crear_activo.html for asset publication; include form for asset details and navigation](URL_commit_2)                                        | [Archivo2](URL_archivo_2) |
|  3  |                                                  [Add particle effects to index.html and create particles configuration](URL_commit_3)                                                  | [Archivo3](URL_archivo_3) |
|  4  |                               [Refactor login and registration pages for improved layout and styling; add navigation and footer components](URL_commit_4)                               | [Archivo4](URL_archivo_4) |
|  5  |                                   [Add admin panel, create asset, and edit asset pages with Bootstrap integration and enhanced layouts](URL_commit_5)                                   | [Archivo5](URL_archivo_5) |
|  6  |                                                    [Refactor navigation and authentication flow across multiple pages](URL_commit_6)                                                    | [Archivo6](URL_archivo_6) |
|  7  | [Refactor detalle_activo and editar_activo pages for improved layout and user experience; update background theme and integrate particles.js for enhanced visual effects](URL_commit_7) | [Archivo7](URL_archivo_7) |
|  8  |                    [ Update UI elements across multiple admin pages for consistency; replace initials with logo image and enhance button interactions](URL_commit_8)                    | [Archivo8](URL_archivo_8) |

---

## 🛠 **Práctica 2: Web con HTML generado en servidor**

### **Navegación y Capturas de Pantalla**

#### **Diagrama de Navegación**

Solo si ha cambiado.

#### **Capturas de Pantalla Actualizadas**

Solo si han cambiado.

### **Instrucciones de Ejecución**

#### **Requisitos Previos**

- **Java**: versión 21 o superior
- **Maven**: versión 3.8 o superior
- **MySQL**: versión 8.0 o superior
- **Git**: para clonar el repositorio

#### **Pasos para ejecutar la aplicación**

1. **Clonar el repositorio**

   ```bash
   git clone https://github.com/[usuario]/[nombre-repositorio].git
   cd [nombre-repositorio]
   ```

2. **AQUÍ INDICAR LO SIGUIENTES PASOS**

#### **Credenciales de prueba**

- **Usuario Admin**: usuario: `admin`, contraseña: `admin`
- **Usuario Registrado**: usuario: `user`, contraseña: `user`

### **Diagrama de Entidades de Base de Datos**

Diagrama mostrando las entidades, sus campos y relaciones:

![Diagrama Entidad-Relación](images/database-diagram.png)

> [Descripción opcional: Ej: "El diagrama muestra las 4 entidades principales: Usuario, Producto, Pedido y Categoría, con sus respectivos atributos y relaciones 1:N y N:M."]

### **Diagrama de Clases y Templates**

Diagrama de clases de la aplicación con diferenciación por colores o secciones:

![Diagrama de Clases](images/classes-diagram.png)

> [Descripción opcional del diagrama y relaciones principales]

### **Participación de Miembros en la Práctica 2**

#### **Alumno 1 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---

#### **Alumno 2 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---

#### **Alumno 3 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---

#### **Alumno 4 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---

## 🛠 **Práctica 3: API REST, docker y despliegue**

### **Documentación de la API REST**

#### **Especificación OpenAPI**

📄 **[Especificación OpenAPI (YAML)](/api-docs/api-docs.yaml)**

#### **Documentación HTML**

📖 **[Documentación API REST (HTML)](https://raw.githack.com/[usuario]/[repositorio]/main/api-docs/api-docs.html)**

> La documentación de la API REST se encuentra en la carpeta `/api-docs` del repositorio. Se ha generado automáticamente con SpringDoc a partir de las anotaciones en el código Java.

### **Diagrama de Clases y Templates Actualizado**

Diagrama actualizado incluyendo los @RestController y su relación con los @Service compartidos:

![Diagrama de Clases Actualizado](images/complete-classes-diagram.png)

### **Instrucciones de Ejecución con Docker**

#### **Requisitos previos:**

- Docker instalado (versión 20.10 o superior)
- Docker Compose instalado (versión 2.0 o superior)

#### **Pasos para ejecutar con docker-compose:**

1. **Clonar el repositorio** (si no lo has hecho ya):

   ```bash
   git clone https://github.com/[usuario]/[repositorio].git
   cd [repositorio]
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**:

### **Construcción de la Imagen Docker**

#### **Requisitos:**

- Docker instalado en el sistema

#### **Pasos para construir y publicar la imagen:**

1. **Navegar al directorio de Docker**:

   ```bash
   cd docker
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**

### **Despliegue en Máquina Virtual**

#### **Requisitos:**

- Acceso a la máquina virtual (SSH)
- Clave privada para autenticación
- Conexión a la red correspondiente o VPN configurada

#### **Pasos para desplegar:**

1. **Conectar a la máquina virtual**:

   ```bash
   ssh -i [ruta/a/clave.key] [usuario]@[IP-o-dominio-VM]
   ```

   Ejemplo:

   ```bash
   ssh -i ssh-keys/app.key vmuser@10.100.139.XXX
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**:

### **URL de la Aplicación Desplegada**

🌐 **URL de acceso**: `https://[nombre-app].etsii.urjc.es:8443`

#### **Credenciales de Usuarios de Ejemplo**

| Rol                | Usuario | Contraseña |
| :----------------- | :------ | :--------- |
| Administrador      | admin   | admin123   |
| Usuario Registrado | user1   | user123    |
| Usuario Registrado | user2   | user123    |

### **OTRA DOCUMENTACIÓN ADICIONAL REQUERIDA EN LA PRÁCTICA**

### **Participación de Miembros en la Práctica 3**

#### **Alumno 1 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---

#### **Alumno 2 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---

#### **Alumno 3 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---

#### **Alumno 4 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---
