const API_URL = "http://localhost:8080/api/v1";

// -----------------------------------------------------------------------------
// Autenticación: Interceptar el formulario y solicitar el token JWT
// -----------------------------------------------------------------------------
document.getElementById("login-form").addEventListener("submit", async (e) => {
    e.preventDefault(); // Evitar recarga de la página
    
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const errorDiv = document.getElementById("error-message");
    
    errorDiv.textContent = "";

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            throw new Error("Credenciales inválidas. Comprueba tu email y contraseña.");
        }

        const data = await response.json();
        const token = data.token; // Extraer el JWT de la respuesta AuthResponse

        // 1. Guardar el token de forma segura en el Local Storage
        localStorage.setItem("jwtToken", token);

        // 2. Transición de UI
        document.getElementById("login-container").style.display = "none";
        document.getElementById("dashboard-container").style.display = "block";

        // 3. Solicitar datos protegidos
        loadDashboard();

    } catch (error) {
        errorDiv.textContent = error.message;
        console.error("[Auth] Error en login:", error);
    }
});

// -----------------------------------------------------------------------------
// Acceso Protegido: Enviar el token en las cabeceras HTTP
// -----------------------------------------------------------------------------
async function loadDashboard() {
    const token = localStorage.getItem("jwtToken");
    const contentDiv = document.getElementById("dashboard-content");

    if (!token) {
        contentDiv.innerHTML = "<p class='error'>Acceso denegado: No se ha encontrado el token JWT.</p>";
        return;
    }

    try {
        // [CRÍTICO] Inyección del token Bearer en la cabecera Authorization
        const response = await fetch(`${API_URL}/charts/impact`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                logout();
                throw new Error("Sesión expirada o token inválido. Por favor, vuelva a iniciar sesión.");
            }
            throw new Error(`Error en el servidor: HTTP ${response.status}`);
        }

        // Parsear el ChartDataDTO (labels, data)
        const chartData = await response.json();
        
        // Volcar los datos en una tabla dinámica
        let html = `<table>
            <thead>
                <tr>
                    <th>Categoría de Residuo</th>
                    <th>CO2 Ahorrado (kg)</th>
                </tr>
            </thead>
            <tbody>`;
            
        // Combinar arrays paralelos
        if (chartData.labels && chartData.labels.length > 0) {
            for (let i = 0; i < chartData.labels.length; i++) {
                html += `<tr>
                    <td><strong>${chartData.labels[i]}</strong></td>
                    <td style="color: #10b981; font-weight: bold;">${chartData.data[i]} kg</td>
                </tr>`;
            }
        } else {
            html += `<tr><td colspan="2" style="text-align: center;">No hay datos de impacto disponibles.</td></tr>`;
        }
        
        html += `</tbody></table>`;
        contentDiv.innerHTML = html;

    } catch (error) {
        contentDiv.innerHTML = `<p class="error">${error.message}</p>`;
        console.error("[API] Error obteniendo el dashboard:", error);
    }
}

// -----------------------------------------------------------------------------
// Cierre de sesión: Destruir el token
// -----------------------------------------------------------------------------
function logout() {
    localStorage.removeItem("jwtToken"); // Eliminar token
    document.getElementById("login-container").style.display = "block";
    document.getElementById("dashboard-container").style.display = "none";
    document.getElementById("password").value = ""; // Limpiar formulario
}

// -----------------------------------------------------------------------------
// Inicialización: Verificar si ya existe una sesión activa al cargar
// -----------------------------------------------------------------------------
window.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("jwtToken");
    if (token) {
        document.getElementById("login-container").style.display = "none";
        document.getElementById("dashboard-container").style.display = "block";
        loadDashboard();
    }
});
