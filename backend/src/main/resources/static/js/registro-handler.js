// registro-handler.js — corregido.
// Solo mantiene la validación de contraseñas coincidentes.
// El envío real del formulario lo gestiona Spring Boot (action="/registro" method="POST").
document.addEventListener("DOMContentLoaded", function () {
    const registerForm = document.querySelector('#registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function (e) {
            const password = document.getElementById('password')?.value;
            const confirmPassword = document.getElementById('confirmPassword')?.value;

            if (password !== confirmPassword) {
                e.preventDefault(); // Solo bloqueamos si las contraseñas no coinciden
                alert('Las contraseñas no coinciden');
                return;
            }

            this.classList.add('was-validated');
            // Si las contraseñas coinciden, el formulario se envía normalmente a Spring Boot
        });
    }
});
