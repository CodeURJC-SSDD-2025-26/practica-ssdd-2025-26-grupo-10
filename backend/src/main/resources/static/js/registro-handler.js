// registro-handler.js — corrected.
// Only maintains the matching passwords validation.
// The actual form submission is handled by Spring Boot (action="/registro" method="POST").
document.addEventListener("DOMContentLoaded", function () {
    const registerForm = document.querySelector('#registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function (e) {
            const password = document.getElementById('password')?.value;
            const confirmPassword = document.getElementById('confirmPassword')?.value;

            if (password !== confirmPassword) {
                e.preventDefault(); // Only block if passwords don't match
                alert('Las contraseñas no coinciden');
                return;
            }

            this.classList.add('was-validated');
            // If passwords match, the form is submitted normally to Spring Boot
        });
    }
});
