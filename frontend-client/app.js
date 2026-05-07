const API_URL = "http://localhost:8080/api/v1";

// -----------------------------------------------------------------------------
// Authentication: Intercept the form and request the JWT token
// -----------------------------------------------------------------------------
document.getElementById("login-form").addEventListener("submit", async (e) => {
    e.preventDefault(); // Prevent page reload
    
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
            throw new Error("Invalid credentials. Please check your email and password.");
        }

        const data = await response.json();
        const token = data.token; // Extract the JWT from the AuthResponse

        // 1. Store the token securely in Local Storage
        localStorage.setItem("jwtToken", token);

        // 2. UI Transition
        document.getElementById("login-container").style.display = "none";
        document.getElementById("dashboard-container").style.display = "block";

        // 3. Request protected data
        loadDashboard();

    } catch (error) {
        errorDiv.textContent = error.message;
        console.error("[Auth] Login error:", error);
    }
});

// -----------------------------------------------------------------------------
// Protected Access: Send the token in HTTP headers
// -----------------------------------------------------------------------------
async function loadDashboard() {
    const token = localStorage.getItem("jwtToken");
    const contentDiv = document.getElementById("dashboard-content");

    if (!token) {
        contentDiv.innerHTML = "<p class='error'>Access denied: JWT token not found.</p>";
        return;
    }

    try {
        // [CRITICAL] Bearer token injection into the Authorization header
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
                throw new Error("Session expired or invalid token. Please log in again.");
            }
            throw new Error(`Server error: HTTP ${response.status}`);
        }

        // Parse ChartDataDTO (labels, data)
        const chartData = await response.json();
        
        // Render the data into a dynamic table
        let html = `<table>
            <thead>
                <tr>
                    <th>Waste Category</th>
                    <th>CO2 Saved (kg)</th>
                </tr>
            </thead>
            <tbody>`;
            
        // Combine parallel arrays
        if (chartData.labels && chartData.labels.length > 0) {
            for (let i = 0; i < chartData.labels.length; i++) {
                html += `<tr>
                    <td><strong>${chartData.labels[i]}</strong></td>
                    <td style="color: #10b981; font-weight: bold;">${chartData.data[i]} kg</td>
                </tr>`;
            }
        } else {
            html += `<tr><td colspan="2" style="text-align: center;">No impact data available.</td></tr>`;
        }
        
        html += `</tbody></table>`;
        contentDiv.innerHTML = html;

    } catch (error) {
        contentDiv.innerHTML = `<p class="error">${error.message}</p>`;
        console.error("[API] Error fetching dashboard data:", error);
    }
}

// -----------------------------------------------------------------------------
// Logout: Destroy the token
// -----------------------------------------------------------------------------
function logout() {
    localStorage.removeItem("jwtToken"); // Remove token
    document.getElementById("login-container").style.display = "block";
    document.getElementById("dashboard-container").style.display = "none";
    document.getElementById("password").value = ""; // Clear form
}

// -----------------------------------------------------------------------------
// Initialization: Check if an active session already exists on load
// -----------------------------------------------------------------------------
window.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("jwtToken");
    if (token) {
        document.getElementById("login-container").style.display = "none";
        document.getElementById("dashboard-container").style.display = "block";
        loadDashboard();
    }
});
