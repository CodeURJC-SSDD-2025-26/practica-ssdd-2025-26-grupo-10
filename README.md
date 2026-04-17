# ECOMÓSTOLES B2B

## 👥 Miembros del Equipo

| Nombre y Apellidos | Correo URJC                      | Usuario GitHub  |
| :----------------- | :------------------------------- | :-------------- |
| Guillermo          | g.dominguez.2022@alumnos.urjc.es | Guilledgg06     |
| Daniel             | d.nieto.2021@alumnos.urjc.es     | nietodiazdaniel |
| Alberto            | a.sastre.2022@alumnos.urjc.es    | ASastre03       |
| Javier             | j.delacasa.2022@alumnos.urjc.es  | Javi1014        |

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

- **Activo**: Múltiples imágenes reales por activo (maquinaria o lotes de residuos) para verificar su estado.
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
- **Descripción**: El sistema analiza periódicamente las nuevas Solicitudes (Demandas) y las compara con los Activos (Ofertas) disponibles. Es decir, calculará un "Índice de Compatibilidad" ponderando: Coincidencia de material/subcategoría, volumen requerido vs disponible (con un margen del 10%) y proximidad geográfica (priorizando mismo polígono). Como salida (output), el sistema computa y propone un listado priorizado de oportunidades de simbiosis sectorial basado en el coeficiente de afinidad calculado.

---

## 🛠 **Práctica 1: Maquetación de páginas web con HTML y CSS**

### **Diagrama de Navegación**

Diagrama que muestra cómo se navega entre las diferentes páginas de la aplicación:

![Diagrama de Navegación](imagenes_paginas/DIAGRAM_F.jpeg)

#### 1. Leyenda Visual y Arquitectura del Mapa

El diagrama de navegación adjunto muestra cómo está organizada la estructura de la plataforma B2B EcoMóstoles y cómo se gestionan los permisos de los usuarios. Para que las zonas de seguridad se entiendan claramente durante la revisión técnica, el mapa utiliza un código de colores en sus flechas. Esta guía visual divide la aplicación en tres áreas principales, marcando por dónde puede moverse el cliente según si ha iniciado sesión o el nivel de permisos que tenga su empresa.

#### 2. Flujo Público (Flechas Amarillas - Navegación Anónima)

El camino marcado por las flechas amarillas muestra la parte pública de la web, diseñada para que cualquiera pueda entrar sin restricciones (no hace falta estar identificado). Este recorrido sirve para recibir a los visitantes. Todo empieza en la página de inicio o Landing Page (index.html), desde donde se puede ir directamente a los apartados de acceso: el panel para entrar con usuario y contraseña (login.html) y el formulario para crear una cuenta nueva (registro.html). Las líneas amarillas muestran que se puede ir y volver libremente entre estas tres páginas antes de identificarse en el sistema.

#### 3. Flujo de Empresa Registrada (Flechas Azules - Core Operativo)

Una vez que el usuario se identifica, las flechas azules marcan el corazón operativo de la plataforma, un área protegida exclusiva para empresas registradas. La organización de esta zona funciona como una rueda: el Panel de Control (Dashboard) es el centro de todas las operaciones. Desde allí salen las rutas azules que permiten a la empresa gestionar por completo (crear, editar, ver y listar) los tres pilares del negocio: las Ofertas, las Demandas y los Acuerdos. Además, esta zona conecta el panel central con apartados clave como el centro de mensajes y la edición de los datos de la empresa.

#### 4. Flujo de Administración (Flechas Verdes - Supervisión Master)

El recorrido de las flechas verdes define las rutas de mayor seguridad, reservadas únicamente para los Administradores. Este camino lleva directamente a su propio panel de gestión interna (admin_panel.html), un entorno cerrado desde el cual los administradores realizan tareas de mantenimiento general: revisar informes y estadísticas, dar de alta o baja a usuarios y configurar los ajustes globales de la plataforma.

> **⚠️ Nota importante sobre los permisos:**
> En cuanto al funcionamiento interno, el rol de Administrador está diseñado con un sistema de "herencia de permisos". Esto significa que, aunque su lugar natural de trabajo sea el marcado por las flechas verdes, su acceso es total. Por lo tanto, un administrador puede entrar y moverse legalmente por cualquier parte de la plataforma, incluyendo todas las funciones de los usuarios normales (es decir, por cualquier ruta marcada con flechas azules), lo que le permite supervisar las operaciones de las empresas en tiempo real.

---

### **Capturas de Pantalla y Descripción de Páginas**

#### **1. Página Principal (index.html)**

![Página Principal](imagenes_paginas/index.html.png)

> Página de inicio de una plataforma B2B de economía circular que conecta empresas locales para el intercambio de residuos industriales y maquinaria. Incluye una interfaz dinámica que muestra ofertas destacadas y adapta la navegación según el estado de la sesión del usuario. Integra efectos visuales avanzados con partículas interactivas para ofrecer una experiencia tecnológica y moderna centrada en la sostenibilidad. Facilita el acceso directo al registro de empresas, mercado de activos y paneles de control para la gestión de recursos.

#### **2. Moderación de Ofertas (admin_ofertas.html)**

![Administrador ofertas](imagenes_paginas/admin_ofertas.png)

> Panel de administración de EcoMóstoles diseñado para la gestión centralizada de ofertas y residuos industriales, permitiendo supervisar publicaciones, estados y denuncias. Incluye un dashboard con estadísticas clave, una tabla interactiva para moderar activos y una barra lateral de navegación para el control total de la plataforma. Presenta herramientas de filtrado y exportación de datos, manteniendo una estética tecnológica con fondos de partículas y un sistema de seguridad que restringe el acceso solo a usuarios autenticados.

#### \*_3. Gestión de Censos y Modificadores (admin_usuarios.html)_

![Administrador usuarios](imagenes_paginas/admin_usuarios.png)

> Esta página funciona como el centro de mando para la administración de usuarios y empresas de EcoMóstoles, permitiendo visualizar el listado completo de entidades registradas, auditar sus datos fiscales y controlar su acceso mediante herramientas para aprobar nuevas solicitudes, editar perfiles existentes o bloquear cuentas de forma inmediata. Gracias a sus filtros inteligentes y buscador integrado, facilita la gestión masiva de las más de 1.200 empresas del ecosistema, ofreciendo además opciones para exportar informes detallados o registrar nuevos usuarios manualmente para mantener el tejido industrial local siempre actualizado.

#### **4. Configuración del Sistema (admin_configuracion.html)**

![Configuración página web](imagenes_paginas/config_plataforma.png)

> Esta página de Configuración de Plataforma actúa como el núcleo operativo del sistema EcoMóstoles, permitiendo a los administradores ajustar los parámetros globales que rigen el funcionamiento del portal. A través de una interfaz intuitiva, el personal autorizado puede activar el modo mantenimiento para restringir el acceso durante actualizaciones, modificar los porcentajes de comisión aplicados a las transacciones B2B y gestionar las categorías de residuos permitidas en el mercado, garantizando que la plataforma se adapte en tiempo real a las necesidades operativas y legales del tejido industrial local.

#### **5. Dashboard Operativo (dashboard.html)**

![Dashboard](imagenes_paginas/dashboard.png)

> El dashboard de EcoMóstoles funciona como un centro neurálgico de gestión donde la empresa visualiza sus indicadores clave (KPIs), como ofertas activas, acuerdos cerrados y el CO₂ ahorrado. Permite administrar el ciclo de vida de los residuos mediante la publicación de ofertas, el seguimiento de demandas y la gestión de mensajes directos con otros colaboradores. Además, destaca por ofrecer un sistema de Smart Matching que sugiere oportunidades de negocio automáticas y un historial detallado para descargar documentos oficiales de las transacciones completadas.

#### **6. Publicar Demanda (crear_solicitud.html)**

![Demandas](imagenes_paginas/demanda.png)

> Esta página proporciona un formulario estructurado para que las empresas detallen sus necesidades específicas de materiales o recursos y las den a conocer a la comunidad industrial. A través de este panel, el usuario puede especificar la categoría del material, la cantidad necesaria, el presupuesto máximo y el nivel de urgencia, facilitando que otros proveedores locales identifiquen oportunidades de colaboración. Su propósito es simplificar la entrada de datos críticos —como la zona de recogida y la vigencia del anuncio— para asegurar que las solicitudes sean precisas, profesionales y eficaces dentro de la red de economía circular de la plataforma.

#### **7. Tablón de Demandas (solicitudes.html)**

![Tablón de demandas](imagenes_paginas/demandas.png)

> Esta página funciona como un mercado abierto donde las empresas pueden visualizar y filtrar las necesidades de materiales de otras organizaciones locales para ofrecer sus propios recursos sobrantes. A diferencia del panel de gestión personal, esta sección está orientada a la prospección y la interacción B2B, permitiendo a los usuarios buscar solicitudes específicas por categoría o nivel de urgencia, consultar los detalles de cada petición y contactar directamente con los demandantes para cerrar acuerdos de colaboración que fomenten la economía circular en la zona.

#### **8. Detalle de la Oferta (detalle_activo.html)**

![Detalles de la oferta](imagenes_paginas/detalle_oferta.png)

> Esta página actúa como la ficha técnica y comercial de un recurso específico disponible en el mercado de EcoMóstoles. En este caso, presenta una oferta de "Viruta de Acero Industrial", permitiendo a los compradores potenciales evaluar rápidamente la viabilidad del material para sus procesos productivos.

#### **9. Editar Activo (editar_activo.html)**

![Editar activo](imagenes_paginas/editar_activo.png)

> Esta página es el centro operativo para que las empresas gestionen la información de sus recursos publicados en el ecosistema de EcoMóstoles. En este caso, permite actualizar los detalles del activo #REF-2024-001, correspondiente a bobinas de cobre.

#### **10. Editar Demanda (editar_solicitud.html)**

![Editar demanda](imagenes_paginas/editar_demanda.png)

> Esta página es la interfaz de gestión donde las empresas de EcoMóstoles ajustan sus necesidades de aprovisionamiento de materiales o servicios. A diferencia de la edición de activos (ofertas), este formulario se centra en especificaciones técnicas y urgencia logística.

#### **11. Error No Encontrado (404.html)**

![Error 404](imagenes_paginas/error404.png)

> Esta página está diseñada específicamente para manejar situaciones donde un usuario intenta acceder a un enlace inexistente o a un recurso que ya ha sido retirado del mercado.

#### **12. Autenticación (login.html)**

![Login](imagenes_paginas/login.html.png)

> Esta página es la puerta de entrada para que las empresas gestionen sus activos y demandas en la plataforma. A diferencia de las páginas de contenido, aquí se prioriza una estética limpia y funcional para minimizar distracciones durante el proceso de autenticación.

#### **13. Buzón de Mensajería (mensajes.html)**

![Buzón de mensajes](imagenes_paginas/mensajes.png)

> Esta página está diseñada como un centro de negociación directa entre empresas. A diferencia de las páginas anteriores, aquí el diseño se vuelve más complejo para soportar una comunicación fluida en tiempo real.

#### **14. Mis Demandas (mis_demandas.html)**

![Demandas publicadas](imagenes_paginas/mis_demandas.png)

> Esta página funciona como un panel de gestión estratégica dentro de la plataforma EcoMóstoles, diseñado específicamente para que las empresas supervisen sus necesidades de materiales y maquinaria. A través de una interfaz limpia y profesional, el usuario puede visualizar de forma inmediata el rendimiento de sus solicitudes mediante tarjetas de estadísticas (demandas activas, cerradas y ofertas recibidas) y administrar su inventario de peticiones mediante una tabla interactiva que prioriza el nivel de urgencia y el estado de cada publicación.

#### **15. Mis Ofertas (mis_activos.html)**

![Mis ofertas](imagenes_paginas/mis_ofertas.png)

> Esta página actúa como el centro de gestión de activos y residuos para las empresas dentro del ecosistema de EcoMóstoles. La página presenta un diseño estructurado mediante una cuadrícula de tarjetas visuales que permiten una administración ágil de los recursos publicados, tales como metales, madera o productos químicos, detallando para cada uno su estado actual (activo, pausado o en negociación), métricas de visibilidad y especificaciones técnicas como peso o ubicación.

#### **16.Mercado B2B Global (mercado.html)**

![Mercado](imagenes_paginas/ofertas.png)

> Esta página funciona como un catálogo interactivo de ofertas que permite a los usuarios buscar, filtrar y visualizar activos industriales disponibles, como residuos, maquinaria o espacios, mediante un sistema de búsqueda por texto, tipo de material y polígono industrial. A través de una interfaz de tarjetas detalladas, facilita la consulta de información específica de cada producto y la navegación hacia otras secciones clave como el panel de control, la publicación de nuevos activos o la gestión del perfil de la empresa.

#### **17. Perfil y Configuración (perfil_empresa.html)**

![Perfil de empresa](imagenes_paginas/perfil_empresa.png)

> Esta página es donde las empresas pueden actualizar su identidad pública, incluyendo la carga del logotipo, la descripción de su actividad y datos de contacto como email, teléfono y dirección. Además de permitir la edición de información estratégica y sectorial para mejorar la visibilidad ante otros usuarios, integra opciones de seguridad para el cambio de contraseña y un menú lateral de navegación para gestionar ofertas, demandas y mensajes internos.

#### **18. Publicar Activo (crear_activo.html)**

![Publicar oferta](imagenes_paginas/publicar_oferta.png)

> Esta página funciona como un formulario de creación y publicación de ofertas, diseñado para que las empresas den de alta activos industriales especificando detalles como el título, el tipo de material o servicio (residuos metálicos, madera, maquinaria, etc.), una descripción detallada, la cantidad disponible y el precio. Además, permite adjuntar una fotografía del activo y definir su disponibilidad temporal, integrando un sistema de validación de datos para asegurar que toda la información obligatoria esté completa antes de su publicación en el mercado de la plataforma.

#### **19. Registro de Entidad (registro.html)**

![Registrarse](imagenes_paginas/registro.png)

> Esta página funciona como el portal de registro de nuevas empresas en la plataforma. Está diseñada para capturar la información jurídica y operativa esencial de las organizaciones interesadas en unirse a la red de economía circular de Móstoles.

#### **20. Reportes y Estadísticas (admin_reportes.html)**

![Reportes para el administrador](imagenes_paginas/reportes.png)

> Esta página constituye el módulo de Reportes y Estadísticas exclusivo para el perfil de administrador. Su función principal es ofrecer una visión analítica y cuantitativa del impacto de la plataforma EcoMóstoles en el ecosistema industrial local.

#### **21. Panel Master Administrativo (admin_panel.html)**

![Vista general](imagenes_paginas/vista_general.png)

> Esta página funciona como el centro de control administrativo de EcoMóstoles, diseñado para supervisar y gestionar el ecosistema de simbiosis industrial entre empresas locales. Su función principal es permitir la validación de nuevos usuarios, el seguimiento de métricas operativas (como el total de empresas registradas y ofertas activas) y la moderación del mercado de residuos y maquinaria. Además, ofrece herramientas para generar reportes estadísticos, gestionar incidencias técnicas y configurar los parámetros globales de la plataforma B2B. En definitiva, es la herramienta que garantiza el correcto funcionamiento operativo y la seguridad en el intercambio de recursos industriales.

#### **22. Crear Acuerdo (crear_acuerdo.html)**

![Crear Acuerdo](imagenes_paginas/crear_acuerdo.png)

> Formulario convergente que permite formalizar un intercambio B2B. Implementa selección de entidades vinculadas y define el umbral temporal de la simbiosis industrial asegurando coherencia en la base de datos distribuida.

#### **23. Detalle del Acuerdo (detalle_acuerdo.html)**

![Detalle del Acuerdo](imagenes_paginas/detalles_acuerdo.png)

> Ficha transaccional privada que consolida jurídicamente un pacto B2B. Muestra cronogramas de estado, identidades verificadas de ambas empresas e incluye enrutamiento para la exportación o alteración del pacto.

#### **24. Detalle de la Demanda (detalle_solicitud.html)**

![Detalle de la Demanda](imagenes_paginas/detalle_solicitud.png)

> Visor público de requerimientos emitidos por entidades corporativas. Diseñado para maximizar la legibilidad de la necesidad (fecha límite, volumen exigido y umbral económico) e incentivar la oferta cruzada mediante un botón de postulación.

#### **25. Editar Acuerdo (editar_acuerdo.html)**

![Editar Acuerdo](imagenes_paginas/editar_acuerdo.png)

> Interfaz bidireccional donde una de las partes puede proponer alteraciones a los términos transaccionales de un pacto (ej. retraso logístico o modulación de precios) forzando re-validación de estado.

#### **26. Mis Acuerdos (mis_acuerdos.html)**

![Mis Acuerdos](imagenes_paginas/mis_acuerdos.png)

> Visor de contratos consolidados. Su interfaz tabular prioriza el seguimiento del estado de la cadena logística e integra un esquema de colores semánticos (verde: formalizado, amarillo: negociando).

### **Participación de Miembros en la Práctica 1**

#### **Alumno 1 - [Guillermo Domínguez Galindo]**

[Guillermo Domínguez ha ejercido como Responsable de Desarrollo Frontend y Diseño UI/UX, asumiendo la creación y optimización de la arquitectura visual de la plataforma. Sus tareas principales incluyeron el diseño de paneles de gestión de activos y demandas mediante el uso de tablas, barras laterales responsivas y tarjetas de indicadores clave (KPIs), además de la implementación de la librería Particles.js para efectos dinámicos. Fue el encargado de refactorizar la navegación y la estructura de múltiples páginas (mensajería, mercado, perfil de empresa) para garantizar una experiencia de usuario fluida y coherente, integrando formularios validados con Bootstrap, gestionando la identidad visual con nuevos recursos como favicons en formato SVG y asegurando la consistencia estética en todos los niveles administrativos del proyecto.]

| Nº  |                                                                                                                                                                                                                                 Commits                                                                                                                                                                                                                                  |                                                                                   Files                                                                                    |
| :-: | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
|  1  |                                                                                                                                    [Add styles.css with initial CSS variables, typography, and layout styles](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/08a4a5b6d5e40dcef2e5bc8da80832a33176554a)                                                                                                                                     |             [styles.css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/08a4a5b6d5e40dcef2e5bc8da80832a33176554a/css/styles.css)             |
|  2  |                                                                                                                      [Add mis_activos.html with layout for asset management; include navbar, sidebar, and table for offers.](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/a0169821bcbb6327777dd05ea1cf143958dcf19b)                                                                                                                      |         [mis_activos.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/a0169821bcbb6327777dd05ea1cf143958dcf19b/mis_activos.html)         |
|  3  |                                                                                                                                         [Add particles.js library for dynamic particle effects on canvas](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/f6020937f60d3f9b1b4356ddca17f882e7e28c9a)                                                                                                                                         |             [scripts.js](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/f6020937f60d3f9b1b4356ddca17f882e7e28c9a/js/scripts.js)              |
|  4  |                                                                                                                                   [Add custom navbar styles and button hover effects for improved interaction](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/9403f06613471f6aadb506d3ee3c317f93571c00)                                                                                                                                    |             [styles.css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/9403f06613471f6aadb506d3ee3c317f93571c00/css/styles.css)             |
|  5  |                                                                                                                             [Update styles and enhance dashboard layout with improved navbar, sidebar, and KPI cards](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/d12764a2e760de0b7446cf83fbbc74071faecf94)                                                                                                                             |             [styles.css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/d12764a2e760de0b7446cf83fbbc74071faecf94/css/styles.css)             |
|  6  |                                                                                 [Refactor navigation and layout in dashboard, mensajes, mis_activos, mercado, and perfil_empresa pages; enhance user experience with consistent styling and improved structure.](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/b70cf71a97196207971802fc088798a08a86c40f)                                                                                  |           [dashboard.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/b70cf71a97196207971802fc088798a08a86c40f/dashboard.html)           |
|  7  |                                                                                    [Refactor layout in crear_activo, dashboard, mensajes, and mis_activos pages for improved structure and user experience; adjust sidebar styling for better responsiveness](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/e5af96c5289843a8bad9268f8d9ee0a5bc5a0bb6)                                                                                     |        [crear_activo.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/e5af96c5289843a8bad9268f8d9ee0a5bc5a0bb6/crear_activo.html)        |
|  8  |                                                                                                              [Add admin_reportes page and update navigation links in admin_ofertas, admin_panel, and admin_usuarios for consistency](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/999699b83072e4a5ce7a6b11880d5d772113f93b)                                                                                                              | [admin_configuracion.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/999699b83072e4a5ce7a6b11880d5d772113f93b/admin_configuracion.html) |
|  9  | [Add edit demand page and my demands page with updated layout and functionality,Created `editar_solicitud.html` for editing demand requests with form validation and Bootstrap styling,,Added `mis_demandas.html` to display user's published demands with statistics and action buttons. - Introduced a new favicon in SVG format for branding.](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/bdbc6610fc9984d57baec258fb3678037906dd78) |                 [404.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/bdbc6610fc9984d57baec258fb3678037906dd78/404.html)                 |

---

#### **Alumno 2 - [Daniel Nieto Díaz]**

[Daniel Nieto ha desempeñado el rol de Desarrollador de Frontend y Maquetador UI, centrándose en la creación y optimización de la experiencia de usuario a través de HTML5 y CSS3 con Bootstrap. Sus tareas principales incluyeron el desarrollo de los módulos de autenticación (login y registro) y la página de mercado con sistemas de filtrado por tarjetas, además de la refactorización profunda del código CSS para mejorar su legibilidad y organización. Fue el responsable de estandarizar la navegación y el pie de página (footer) en toda la aplicación, integrar recursos gráficos como iconos SVG y activos de imagen, y corregir efectos visuales avanzados (como el spotlight en componentes), garantizando una interfaz cohesiva, profesional y totalmente adaptada a las necesidades del proyecto.]

| Nº  |                                                                                                                                     Commits                                                                                                                                      |                                                                                   Files                                                                                    |
| :-: | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
|  1  |                      [Update README.md with detailed project description, entity definitions, user permissions, and system features](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/59723ebf426ad6b05c2c5bfcf7a033e79b17bc3d)                      |                [README.md](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/59723ebf426ad6b05c2c5bfcf7a033e79b17bc3d/README.md)                |
|  2  |                                             [Add initial CSS and JavaScript files; Add also basic index.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/db5aec325c7c2f45a76f0dc2eaf72b8deac0fcfb)                                             |                      [css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/db5aec325c7c2f45a76f0dc2eaf72b8deac0fcfb/css)                      |
|  3  |                                                 [Add login and registration pages with Bootstrap styling](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/34127956721047d9fc01ac97e3064ebb2bef5dbb)                                                 |               [login.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/34127956721047d9fc01ac97e3064ebb2bef5dbb/login.html)               |
|  4  |                                    [Add mercado.html for offers display; include navigation, filters, and item cards](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/887dcca26751ddb98e82a852ed5e63a97d6fc62c)                                     |             [mercado.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/887dcca26751ddb98e82a852ed5e63a97d6fc62c/mercado.html)             |
|  5  |                                 [Refactor CSS styles for improved readability and organization; add comments for clarity](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/fe9a9c6aa3edc8561c963149c042c8cd3705aa23)                                 |           [css/styles.css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/fe9a9c6aa3edc8561c963149c042c8cd3705aa23/css/styles.css)           |
|  6  |                                 [Enhance registration and login pages with new navbar, footer, and improved form layout](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/8f624fc683f4dd9fea881de20023f11d65a59888)                                  |            [registro.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/8f624fc683f4dd9fea881de20023f11d65a59888/registro.html)            |
|  7  |                          [Refactor CSS styles for improved organization and readability; fix spotlight effect on card component](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/5774d12bf7b7c3400db03cf453b1795f8c61ada4)                          |            [css/styles.](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/5774d12bf7b7c3400db03cf453b1795f8c61ada4/css/styles.css)             |
|  8  | [Enhance detalle_activo.html with Bootstrap integration, improved layout, and new footer; update navbar and content structure for better user experience](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/6070acf46bd7195f684c9ebbfd7861a9c0da77fd) |      [detalle_activo.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/6070acf46bd7195f684c9ebbfd7861a9c0da77fd/detalle_activo.html)      |
|  9  |                         [Add consistent footer across dashboard, mensajes, and detalle_activo pages for improved user experience](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/e209605322a63d9baab27e5a1fe9b9fa6ac59f16)                         |      [detalle_activo.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/e209605322a63d9baab27e5a1fe9b9fa6ac59f16/detalle_activo.html)      |
| 10  |                             [Replace Twitter icon with SVG version and update styling for consistency across multiple pages](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/d3deb9068595e0a480249868c22684f84df380a2)                              |         [admin_panel.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/d3deb9068595e0a480249868c22684f84df380a2/admin_panel.html)         |
| 11  |                               [Add new image assets and update footer layout for improved consistency and user experience](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/0ce8627da156068355269497b3a282676cf26fb2)                                | [admin_configuracion.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/0ce8627da156068355269497b3a282676cf26fb2/admin_configuracion.html) |
| 12  |                        [Add authentication and form handling scripts; implement particle effects for login and registration pages](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/95564bd80a017502fabf9a5ab436a9af6de199b8)                        |       [crear_acuerdo.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/95564bd80a017502fabf9a5ab436a9af6de199b8/crear_acuerdo.html)       |

---

#### **Alumno 3 - [Alberto Sastre Zorrilla]**

[Alberto Sastre ha actuado como Desarrollador Frontend y Diseñador UI/UX, encargándose de la creación integral de la interfaz mediante HTML, CSS y Bootstrap. Sus responsabilidades incluyeron la maquetación de la página principal, el panel de control (dashboard) y las vistas de administración de usuarios y ofertas, además de la implementación de efectos visuales avanzados como Particles.js e interacciones dinámicas en tarjetas. Su labor abarcó desde el diseño de formularios de registro y mensajería hasta la refactorización técnica y visual de los estilos globales, garantizando una navegación coherente, responsiva y estéticamente moderna en toda la plataforma.]

| Nº  |                                                                                                                              Commits                                                                                                                               |                                                                              Files                                                                               |
| :-: | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :--------------------------------------------------------------------------------------------------------------------------------------------------------------: |
|  1  |                                      [Add index.html with initial structure and Bootstrap integration](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/eeccf5b3721c90a7dd18cf7c873bbab7d9ff473e)                                      |          [index.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/eeccf5b3721c90a7dd18cf7c873bbab7d9ff473e/index.html)          |
|  2  |              [Add dashboard.html with initial layout and sidebar navigation; update styles.css for button and sidebar styles](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/54372659b08eb3b0bc49cbc28e604569f32cab4e)               |      [dashboard.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/54372659b08eb3b0bc49cbc28e604569f32cab4e/dashboard.html)      |
|  3  |                       [Add detalle_activo.html for offer details display; include navbar, image, and contact button](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/51b7f34d0ab5313c90172c2b4a53b0ab0e877881)                        | [detalle_activo.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/51b7f34d0ab5313c90172c2b4a53b0ab0e877881/detalle_activo.html) |
|  4  |                                   [Add spotlight effect to card component with dynamic hover interaction](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/8cb8d8ab8c9012ee03359e70c1333d630370223c)                                   |        [styles.css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/8cb8d8ab8c9012ee03359e70c1333d630370223c/css/styles.css)        |
|  5  |                 [Update styles and finalize layout adjustments in CSS and HTML; enhance particle effects and clean up code](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/a54d9a078ddff3dce105898dab094a0b35a33741)                 |        [styles.css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/a54d9a078ddff3dce105898dab094a0b35a33741/css/styles.css)        |
|  6  |                              [Refactor perfil_empresa.html and registro.html for improved layout and styling](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/0ca01e854e85f31d5de842f573d9a39b94559068)                               | [perfil_empresa.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/0ca01e854e85f31d5de842f573d9a39b94559068/perfil_empresa.html) |
|  7  | [ Refactor mensajes.html and crear_activo.html for improved layout and user experience; integrate particles.js for enhanced visual effects](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/8b78396d720abefce95884924d19b3ccfdc8a95a) |       [mensajes.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/8b78396d720abefce95884924d19b3ccfdc8a95a/mensajes.html)       |
|  8  |      [Add admin_ofertas and admin_usuarios pages with updated layout and styling; integrate particles.js for enhanced visual effects](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/fc7d70cb8f4fa394234c1094c5cd07294f73d834)       |  [admin_ofertas.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/fc7d70cb8f4fa394234c1094c5cd07294f73d834/admin_ofertas.html)  |
|  9  |      [Add admin_ofertas and admin_usuarios pages with updated layout and styling; integrate particles.js for enhanced visual effects](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/079d13d289041a207fd06a1123f8ee7c7e9519ac)       |   [crear_activo.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/079d13d289041a207fd06a1123f8ee7c7e9519ac/crear_activo.html)   |

---

#### **Alumno 4 - [Javier de la Casa Muñoz]**

[Javier de la Casa ha desempeñado el rol de Desarrollo Frontend y Diseño de Interfaz (UI/UX), asumiendo la responsabilidad de construir y optimizar la arquitectura visual de la plataforma mediante el uso de HTML5, CSS3 y el framework Bootstrap. Sus tareas principales incluyeron el diseño de la landing page y los flujos de autenticación, así como la creación integral de paneles de administración para la gestión, edición y visualización de activos. Además de la maquetación inicial, el alumno lideró la refactorización de componentes globales como el navbar y el footer para asegurar la coherencia estética, integrando librerías dinámicas como Particles.js para mejorar la experiencia visual y actualizando elementos de interacción (botones y logotipos) para elevar la usabilidad y el acabado profesional del proyecto.]

| Nº  |                                                                                                                                             Commits                                                                                                                                              |                                                                                   Files                                                                                    |
| :-: | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
|  1  |                                                 [Add navbar and hero section to index.html; enhance styles in styles.css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/e328329a747073af7895f951542ca7590a783595)                                                 |               [index.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/e328329a747073af7895f951542ca7590a783595/index.html)               |
|  2  |                                       [Add crear_activo.html for asset publication; include form for asset details and navigation](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/0c9fdb9148b124226220e2a6720427d83467fe62)                                        |        [crear_activo.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/0c9fdb9148b124226220e2a6720427d83467fe62/crear_activo.html)        |
|  3  |                                                  [Add particle effects to index.html and create particles configuration](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/aca7ccaa7ac6a7396a194b82ed4b0e53a0f418cb)                                                  |               [index.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/aca7ccaa7ac6a7396a194b82ed4b0e53a0f418cb/index.html)               |
|  4  |                               [Refactor login and registration pages for improved layout and styling; add navigation and footer components](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/ce0e18569d02fa3f30e481d1d30b5aab5e4991b4)                               |            [registro.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/ce0e18569d02fa3f30e481d1d30b5aab5e4991b4/registro.html)            |
|  5  |                                   [Add admin panel, create asset, and edit asset pages with Bootstrap integration and enhanced layouts](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/281838807605d9c6930ad2eb781b15096c81dfb6)                                   |         [admin_panel.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/281838807605d9c6930ad2eb781b15096c81dfb6/admin_panel.html)         |
|  6  |                                                    [Refactor navigation and authentication flow across multiple pages](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/a4b58da06e8176b35e89328f7592f53c585bf32e)                                                    |               [index.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/a4b58da06e8176b35e89328f7592f53c585bf32e/index.html)               |
|  7  | [Refactor detalle_activo and editar_activo pages for improved layout and user experience; update background theme and integrate particles.js for enhanced visual effects](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/e2046bec900a2d971278eab18df9d91dd3820d34) |      [detalle_activo.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/e2046bec900a2d971278eab18df9d91dd3820d34/detalle_activo.html)      |
|  8  |                    [ Update UI elements across multiple admin pages for consistency; replace initials with logo image and enhance button interactions](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/tree/b9f46816195a6b93bae30ec04f4f99cc07289599)                    | [admin_configuracion.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-10/blob/b9f46816195a6b93bae30ec04f4f99cc07289599/admin_configuracion.html) |

---

## 🛠 **Práctica 2: Web con HTML generado en servidor**

### **Navegación y Capturas de Pantalla**

#### **Diagrama de Navegación**

Solo si ha cambiado.

#### **Capturas de Pantalla Actualizadas**

#### **1. Página Principal (index.html)**

![Página Principal](backend/src/main/resources/static/pages_images/index.png)

> Esta vista representa la página de inicio de la plataforma EcoMóstoles, diseñada para actuar como una pasarela interactiva que conecta empresas locales para fomentar la economía circular. Utiliza el motor de plantillas Mustache para renderizar dinámicamente una sección "Hero" con animaciones de partículas, un escaparate con las ofertas de materiales más recientes publicadas por las empresas y un panel estadístico que muestra el impacto real del sistema en términos de participación, recursos recuperados y ahorro de emisiones de CO2. Estructuralmente, se apoya en fragmentos reutilizables para la cabecera, el pie de página y los metas, empleando clases de Bootstrap 5 para garantizar un diseño profesional, moderno y totalmente adaptativo.

#### **2. Acceso de Usuarios (login.html)**

![Acceso de Usuarios](backend/src/main/resources/static/pages_images/login.png)

> Interfaz de autenticación centralizada que actúa como puerta de entrada segura para las empresas registradas en la plataforma. Presenta un diseño minimalista y enfocado, utilizando una tarjeta central con sombras suaves y bordes redondeados que contiene un formulario de inicio de sesión compatible con Spring Security y protección CSRF. La vista está optimizada para proporcionar feedback inmediato al usuario mediante alertas visuales que gestionan estados de error, confirmaciones de registro o actualizaciones de credenciales, manteniendo la coherencia visual del proyecto y facilitando la navegación hacia el registro de nuevas cuentas o el retorno a la página principal.

#### **3. Registro de Instituciones (registro.html)**

![Registro de Instituciones](backend/src/main/resources/static/pages_images/registro.png)

> Interfaz diseñada para la gestión del alta de nueva empresa en la red de simbiosis de EcoMóstoles, permitiendo capturar datos comerciales, ubicación en polígonos y logotipos corporativos. Integra validaciones avanzadas del backend para asegurar la integridad de la información y un sistema dinámico de selección de direcciones. Su diseño profesional facilita una incorporación de usuarios fluida y segura, manteniendo la estética moderna del proyecto.

#### **4. Marco Legal y Normativo (terminos.html)**

![Marco Legal y Normativo](backend/src/main/resources/static/pages_images/terminos.png)

> Espacio dedicado a detallar los Términos y Condiciones que regulan el uso de la plataforma EcoMóstoles y la aceptación de sus políticas. Define las responsabilidades de intermediación, las obligaciones legales de las empresas usuarias en materia de residuos y la naturaleza de los acuerdos comerciales generados. Presenta un diseño limpio y optimizado para la lectura, garantizando que el marco legal del proyecto sea accesible y transparente para todos los participantes de la red industrial.

#### **5. Protección de Datos (privacidad.html)**

![Protección de Datos](backend/src/main/resources/static/pages_images/privacidad.png)

> Documento informativo que detalla el Aviso Legal y Privacidad aplicado a la gestión de datos sensibles de las empresas participantes. Describe los protocolos de recopilación de información corporativa, el uso de datos para el cálculo del impacto medioambiental y los derechos de acceso y rectificación de los usuarios. La vista mantiene la armonía visual del proyecto, ofreciendo una presentación estructurada que asegura el cumplimiento normativo y refuerza la confianza en el tratamiento de la información dentro del ecosistema industrial.

#### **6. Panel de Control de Empresas (dashboard.html)**

![Panel de Control de Empresas](backend/src/main/resources/static/pages_images/dashboard.png)

> Centro neurálgico para usuarios autenticados donde se gestiona la actividad dentro de la red EcoMóstoles. Incluye un sistema de indicadores clave (KPIs) para monitorizar el balance circular, los acuerdos activos y el ahorro de CO2, junto con gráficos dinámicos que visualizan la actividad de la empresa. Integra un motor de sugerencias inteligentes para conectar excedentes con necesidades y ofrece herramientas directas para la publicación de nuevos activos industriales, consolidando toda la inteligencia operativa en una interfaz moderna y eficiente.

#### **7. Gestión de Identidad Corporativa (perfil_empresa.html)**

![Gestión de Identidad Corporativa](backend/src/main/resources/static/pages_images/perfil_empresa.png)

> Panel dedicado a la gestión y actualización del perfil de empresa, permitiendo administrar la identidad pública de la entidad en la plataforma. Ofrece herramientas para la carga de logotipos personalizados, edición de datos de contacto y descripción de la actividad industrial, contando además con una modalidad de inspección para administradores. Su diseño modular garantiza que la información corporativa se mantenga veraz y actualizada, facilitando la transparencia y la conexión efectiva entre los miembros de la red de simbiosis industrial.

#### **8. Catálogo de Mercado Industrial (mercado.html)**

![Catálogo de Mercado Industrial](backend/src/main/resources/static/pages_images/mercado.png)

> Centro de descubrimiento de activos que alberga el catálogo completo de residuos industriales disponibles para el intercambio en la red EcoMóstoles. Organiza las ofertas en una cuadrícula interactiva con detalles sobre la disponibilidad, cantidad y procedencia de cada material, integrando un sistema de paginación para facilitar la navegación por grandes volúmenes de datos. Esta vista actúa como el núcleo comercial del proyecto, permitiendo a las empresas explorar recursos infrautilizados y establecer conexiones estratégicas para fomentar la economía circular de forma eficiente.

#### **9. Análisis Detallado de Residuos (detalle_activo.html)**

![Análisis Detallado de Residuos](backend/src/main/resources/static/pages_images/detalle_activo.png)

> Vista exhaustiva que presenta toda la información técnica y comercial de un activo específico publicado en el mercado. Permite visualizar imágenes en alta resolución del material, consultar métricas de precio y disponibilidad, y conocer el perfil de la empresa proveedora de forma inmediata. La página integra un canal de mensajería segura B2B para facilitar la negociación directa entre interesados y el propietario, además de herramientas para compartir el recurso y una lógica de control que diferencia las opciones de edición para los propietarios frente a las acciones de contacto para los potenciales compradores.

#### **10. Tablón de Necesidades Industriales (solicitudes.html)**

![Tablón de Necesidades Industriales](backend/src/main/resources/static/pages_images/solicitudes.png)

> Espacio colaborativo que centraliza las peticiones de materiales y activos que las empresas de la red necesitan adquirir para sus procesos. Permite a los usuarios explorar las demandas activas, conocer los requerimientos específicos de otras entidades y responder a ellas para establecer acuerdos de simbiosis. La vista utiliza un sistema de tarjetas estructurado con paginación integrada, facilitando la identificación rápida de oportunidades de suministro y promoviendo una economía circular inversa donde la oferta se adapta a las necesidades reales de la comunidad industrial.

#### **11. Análisis de Necesidades (detalle_solicitud.html)**

![Análisis de Necesidades](backend/src/main/resources/static/pages_images/detalle_solicitud.png)

> Vista pormenorizada de una solicitud de material específica, diseñada para que los proveedores potenciales comprendan los requisitos exactos de una empresa solicitante. Expone datos críticos como el presupuesto estimado, la urgencia de la petición, la zona de recogida preferida y el plazo de vigencia, permitiendo además el envío de propuestas directas a través de un canal de mensajería verificado. Su estructura visual prioriza la claridad de los términos de la demanda, facilitando que las empresas con excedentes adecuados puedan responder de manera ágil y profesional a las necesidades de la comunidad.

#### **12. Gestión de Inventario Circular (mis_activos.html)**

![Gestión de Inventario Circular](backend/src/main/resources/static/pages_images/mis_activos.png)

> Panel de administración personalizado para la gestión integral de los residuos y materiales que la empresa ha publicado en la plataforma. Ofrece un resumen analítico de métricas clave, incluyendo el estado de las publicaciones (activas, pausadas o en negociación) y el alcance de las mismas mediante un contador de visitas totales. La interfaz permite monitorizar el ciclo de vida de cada activo, facilitando acciones rápidas de edición, eliminación o supervisión histórica, lo que asegura que el inventario de la empresa se mantenga optimizado y alineado con los objetivos de simbiosis industrial de la red.

#### **13. Generación de Ofertas (crear_activo.html)**

![Generación de Ofertas](backend/src/main/resources/static/pages_images/crear_activo.png)

> Interfaz operativa diseñada para capturar y procesar el alta de nueva oferta de materiales industriales en el mercado de EcoMóstoles. El formulario integra campos validados para la descripción técnica, cuantificación en diversas unidades de medida, fijación de precios y niveles de disponibilidad, exigiendo además la carga de una imagen obligatoria para asegurar la transparencia del activo. Esta herramienta es fundamental para la alimentación del catálogo de simbiosis, permitiendo a las empresas transformar sus residuos en recursos visibles y listos para ser negociados por otros miembros de la red.

#### **14. Modificación de Recursos (editar_activo.html)**

![Modificación de Recursos](backend/src/main/resources/static/pages_images/editar_activo.png)

> Interfaz de gestión diseñada para la actualización de los parámetros técnicos y comerciales de una oferta existente en la plataforma. Permite modificar aspectos críticos como el título, la categoría de residuo, la cantidad disponible y el precio, incorporando además un selector para cambiar el estado operativo del activo. La vista facilita la optimización continua de los anuncios mediante la previsualización de la imagen actual y la opción de sustituirla, asegurando que la información presentada en el mercado sea siempre precisa y refleje fielmente el estado real de los excedentes industriales.

#### **15. Gestión de Requerimientos (mis_demandas.html)**

![Gestión de Requerimientos](backend/src/main/resources/static/pages_images/mis_demandas.png)

> Panel de control personalizado para la supervisión y administración de las peticiones de material que la empresa ha lanzado a la comunidad industrial. Permite monitorizar el estado de cada solicitud (activa, pausada o finalizada), controlar los presupuestos asignados y visualizar el impacto de las demandas mediante un contador de visitas y días de vigencia restantes. La interfaz centraliza las herramientas de edición y eliminación, proporcionando una visión clara de las necesidades de abastecimiento de la corporación y facilitando la gestión de los recursos requeridos para sus procesos productivos dentro de la red.

#### **16. Generación de Requerimientos (crear_solicitud.html)**

![Generación de Requerimientos](backend/src/main/resources/static/pages_images/crear_solicitud.png)

> Interfaz operativa diseñada para capturar y procesar el alta de nuevas demandas de materiales dentro del ecosistema de EcoMóstoles. El formulario permite especificar detalladamente la naturaleza del recurso buscado, definiendo parámetros críticos como la cantidad, el presupuesto máximo disponible, el nivel de urgencia y la zona de recogida preferida. Esta herramienta facilita que las empresas comuniquen sus necesidades de abastecimiento de forma estructurada, permitiendo que el sistema conecte estas peticiones con potenciales proveedores y dinamice el intercambio de recursos bajo el modelo de economía circular.

#### **17. Actualización de Peticiones (editar_solicitud.html)**

![Actualización de Peticiones](backend/src/main/resources/static/pages_images/editar_solicitud.png)

> Interfaz de gestión orientada a la modificación y refinamiento de las solicitudes de materiales previamente publicadas en la red. Permite ajustar parámetros operativos como la cantidad requerida, el presupuesto máximo y la zona de recogida, además de ofrecer la capacidad de actualizar el estado de vigencia y la urgencia de la petición. Esta vista asegura que las peticiones de abastecimiento de las empresas se mantengan alineadas con sus necesidades productivas reales, facilitando una intermediación precisa y eficiente dentro del tablero de demandas de la plataforma.

#### **18. Centro de Comunicaciones B2B (mensajes.html)**

![Centro de Comunicaciones B2B](backend/src/main/resources/static/pages_images/mensajes.png)

> Hub centralizado de mensajería que facilita la comunicación directa y segura entre las empresas participantes de la red EcoMóstoles. Implementa un diseño de panel dividido que organiza las conversaciones en bandejas de entrada y salida, permitiendo supervisar hilos sobre ofertas y demandas específicas. La vista incluye indicadores de mensajes nuevos y un historial detallado de interacciones, actuando como el motor de negociación del proyecto donde se concretan los acuerdos de intercambio y se resuelven dudas técnicas sobre los subproductos industriales.

#### **19. Interfaz de Lectura B2B (detalle_mensaje.html)**

![Interfaz de Lectura B2B](backend/src/main/resources/static/pages_images/detalle_mensaje.png)

> Vista especializada diseñada para la lectura y gestión individual de las comunicaciones corporativas dentro de la red. Presenta el contenido del mensaje en un entorno limpio y tipográficamente optimizado, facilitando la identificación inmediata del remitente mediante avatares visuales y metadatos de envío. La interfaz permite realizar acciones directas como la redacción de respuestas preconfiguradas o la eliminación de hilos, asegurando un flujo de trabajo ágil para las empresas que negocian acuerdos de simbiosis y manteniendo un registro ordenado de las interacciones estratégicas en su buzón.

#### **20. Emisión de Propuestas (redactar_mensaje.html)**

![Emisión de Propuestas](backend/src/main/resources/static/pages_images/redactar_mensaje.png)

> Interfaz de composición dirigida a formalizar el contacto inicial o la respuesta entre empresas dentro del ecosistema de EcoMóstoles. El formulario está optimizado para capturar el asunto y el contenido detallado de la propuesta, manteniendo el destinatario vinculado de forma segura para evitar errores en la comunicación. Esta vista es clave para el inicio de cualquier negociación de simbiosis, ofreciendo un entorno profesional y protegido donde las corporaciones pueden intercambiar información técnica y comercial relevante para concretar el aprovechamiento de subproductos industriales.

#### **21. Seguimiento de Transacciones (mis_acuerdos.html)**

![Seguimiento de Transacciones](backend/src/main/resources/static/pages_images/mis_acuerdos.png)

> Panel de control administrativo diseñado para centralizar la gestión de todos los acuerdos de intercambio industrial formalizados por la empresa. Permite realizar un seguimiento exhaustivo de los estados de recogida, las empresas colaboradoras implicadas y los términos económicos acordados, integrando además métricas rápidas sobre acuerdos pendientes y completados. Esta herramienta actúa como el libro de registro operativo de la corporación dentro de la red EcoMóstoles, facilitando la auditoría de transacciones passadas y la coordinación logística de las activas para asegurar el éxito en la reintroducción de materiales en el ciclo productivo.

#### **22. Formalización Comercial (crear_acuerdo.html)**

![Formalización Comercial](backend/src/main/resources/static/pages_images/crear_acuerdo.png)

> Interfaz transaccional diseñada para el registro oficial de los términos pactados entre dos empresas tras una negociación de materiales. Permite vincular una oferta existente con la entidad receptora, especificando la cantidad final del recurso, el precio acordado y la fecha prevista para la logística de recogida. Esta vista es esencial para consolidar la confianza en la red de simbiosis industrial, ya que permite documentar los hitos económicos y operativos de cada intercambio, sirviendo como base para el seguimiento de compromisos y la construcción del historial de sostenibilidad de los participantes.

#### **23. Auditoría de Intercambios (detalle_acuerdo.html)**

![Auditoría de Intercambios](backend/src/main/resources/static/pages_images/detalle_acuerdo.png)

> Vista consolidada que ofrece un desglose técnico y administrativo de un acuerdo de simbiosis industrial ya formalizado. Centraliza la información crítica del intercambio, incluyendo la trazabilidad del material, los términos económicos y la logística de recogida, presentando de forma paralela los perfiles de la empresa de origen y de destino. Esta página constituye el documento digital de referencia para ambas partes, permitiendo además la generación y descarga de albaranes en formato PDF, lo que asegura que cada movimiento de recursos dentro de la red EcoMóstoles cuente con un respaldo documental claro para fines de cumplimiento y auditoría medioambiental.

#### **24. Rectificación Contractual (editar_acuerdo.html)**

![Rectificación Contractual](backend/src/main/resources/static/pages_images/editar_acuerdo.png)

> Vista técnica que permite la modificación de los términos y condiciones de un acuerdo industrial ya registrado en la plataforma. La interfaz habilita el ajuste de campos dinámicos como la cantidad final de material, el precio pactado, la fecha de recogida y el estado operativo de la transacción, manteniendo el concepto del material como un campo de solo lectura para preservar la coherencia del acuerdo original. Esta herramienta es fundamental para adaptar los intercambios a cambios imprevistos en la logística o en los requerimientos de las empresas, garantizando que el registro digital sea un reflejo fiel de la realidad de la operación comercial en todo momento.

#### **25. Supervisión Centralizada (admin_panel.html)**

![Supervisión Centralizada](backend/src/main/resources/static/pages_images/admin_panel.png)

> Centro de mando administrativo diseñado para proporcionar una visión global y en tiempo real del estado operativo de EcoMóstoles. El panel integra indicadores clave de rendimiento (KPIs) que monitorizan el volumen de usuarios, la actividad del mercado y los ingresos acumulados por comisiones, complementados con gráficos interactivos que visualizan la eficacia en la resolución de acuerdos. Esta interfaz permite a los administradores supervisar la salud de la infraestructura y auditar el rendimiento del ecosistema, facilitando la toma de decisiones estratégicas para optimizar el flujo de materiales y la estabilidad financiera de la red.

#### **26. Administración de la Comunidad (admin_usuarios.html)**

![Administración de la Comunidad](backend/src/main/resources/static/pages_images/admin_usuarios.png)

> Herramienta de control centralizada para la supervisión y mantenimiento de la base de datos de empresas que integran la plataforma. Permite a los administradores buscar perfiles específicos mediante filtros de identificación fiscal o contacto, facilitando la auditoría de la integridad de los datos corporativos. La vista ofrece funcionalidades de edición y eliminación jerárquica, asegurando que solo las entidades activas y legítimas operen en la red, mientras protege las cuentas de nivel administrativo con un sistema de inmunidad, garantizando así un entorno de colaboración estrictamente verificado y profesional.

#### **27. Supervisión de Inventario (admin_ofertas.html)**

![Supervisión de Inventario](backend/src/main/resources/static/pages_images/admin_ofertas.png)

> Panel de control administrativo para la supervisión global de todos los materiales y residuos publicados en el mercado de EcoMóstoles. Incluye herramientas avanzadas de filtrado por estado y un resumen estadístico del volumen de activos, permitiendo a los administradores monitorizar la fluidez de las ofertas y garantizar la calidad del catálogo industrial. La interfaz facilita la intervención directa sobre las publicaciones mediante acciones de inspección, edición o eliminación, actuando como el filtro de calidad que asegura que los recursos disponibles cumplan con los estándares de la plataforma y las normativas de intercambio de subproductos.

#### **28. Supervisión de Requerimientos (admin_demandas.html)**

![Supervisión de Requerimientos](backend/src/main/resources/static/pages_images/admin_demandas.png)

> Panel de control administrativo para la supervisión global de las solicitudes de materiales publicadas por las empresas en la red de EcoMóstoles. Incluye un resumen estadístico de la demanda total segmentada por estados operativos, permitiendo a los administradores identificar tendencias de necesidad en el mercado industrial. La interfaz proporciona herramientas de inspección, edición y eliminación de requerimientos, garantizando que el tablón de demandas se mantenga actualizado, libre de duplicados y alineado con los objetivos de abastecimiento circular y profesionalismo que promueve la plataforma.

#### **29. Auditoría de Transacciones (admin_acuerdos.html)**

![Auditoría de Transacciones](backend/src/main/resources/static/pages_images/admin_acuerdos.png)

> Registro maestro administrativo que centraliza el historial global de transacciones y contratos comerciales formalizados en EcoMóstoles. Esta vista permite a los administradores supervisar la relación entre compradores y vendedores de excedentes industriales, monitorizando las cantidades intercambiadas y el beneficio generado para la plataforma a través de comisiones. Actúa como un libro de auditoría en tiempo real que garantiza la trazabilidad económica y operativa del ecosistema, ofreciendo herramientas para inspeccionar los detalles pormenorizados de cada colaboración y asegurar el cumplimiento de las condiciones pactadas por las empresas participantes.

#### **30. Analítica de Sostenibilidad (admin_reportes.html)**

![Analítica de Sostenibilidad](backend/src/main/resources/static/pages_images/admin_reportes.png)

> Módulo analítico avanzado diseñado para la visualización y exportación del impacto ambiental colectivo generado por la red EcoMóstoles. Centraliza métricas críticas de sostenibilidad, como el ahorro total de CO₂ y el volumen de transacciones circulares, presentando estos datos mediante gráficos dinámicos que identifican a las empresas líderes en el ranking de reciclaje. Esta herramienta permite a los administradores generar informes ejecutivos en PDF para auditorías externas y toma de decisiones estratégicas, proporcionando una base cuantitativa para validar el éxito del modelo de simbiosis industrial y su contribución real a la reducción de la huella de carbono en la zona.

#### **31. Gobernanza del Sistema (admin_configuracion.html)**

![Gobernanza del Sistema](backend/src/main/resources/static/pages_images/admin_configuracion.png)

> Interfaz de administración avanzada diseñada para la gestión de los parámetros globales y las reglas de negocio que rigen el comportamiento de EcoMóstoles. Permite a los administradores ajustar el margen de comisión aplicado a las transacciones, configurar el correo de soporte corporativo y mantener actualizados los diccionarios maestros del sistema, como las categorías de residuos, unidades de medida y sectores industriales habilitados. Esta vista es vital para la escalabilidad del proyecto, ya que proporciona un control total sobre las taxonomías y variables críticas de la plataforma sin necesidad de intervenciones técnicas, asegurando que el ecosistema pueda adaptarse dinámicamente a las nuevas necesidades de la economía circular.

#### **32. Gestión de Excepciones (custom_error.html)**

![Gestión de Excepciones](backend/src/main/resources/static/pages_images/custom_error.png)

> Vista unificada diseñada para gestionar y comunicar de manera amigable cualquier incidencia técnica o restricción de acceso dentro de la plataforma. Presenta información diagnóstica clara, incluyendo el código de estado y un mensaje explicativo personalizado, integrando además canales de soporte para la resolución de conflictos. Su arquitectura inteligente detecta el rol del usuario para ofrecer rutas de navegación contextuales de retorno, ya sea hacia el panel de administración, el dashboard operativo o la interfaz de inicio de sesión, asegurando que la experiencia del usuario se mantenga fluida y profesional incluso ante situaciones de error inesperadas.

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

#### **Alumno 1 - Guillermo Domínguez Galindo**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---

#### **Alumno 2 - Daniel Nieto Díaz**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---

#### **Alumno 3 - Alberto Sastre Zorrilla**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº  |               Commits                |           Files           |
| :-: | :----------------------------------: | :-----------------------: |
|  1  | [Descripción commit 1](URL_commit_1) | [Archivo1](URL_archivo_1) |
|  2  | [Descripción commit 2](URL_commit_2) | [Archivo2](URL_archivo_2) |
|  3  | [Descripción commit 3](URL_commit_3) | [Archivo3](URL_archivo_3) |
|  4  | [Descripción commit 4](URL_commit_4) | [Archivo4](URL_archivo_4) |
|  5  | [Descripción commit 5](URL_commit_5) | [Archivo5](URL_archivo_5) |

---

#### **Alumno 4 - Javier de la Casa Muñoz**

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
