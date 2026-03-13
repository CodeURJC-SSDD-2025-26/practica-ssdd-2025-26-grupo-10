document.addEventListener("DOMContentLoaded", function () {
    const registerForm = document.querySelector('#registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const password = document.getElementById('password')?.value;
            const confirmPassword = document.getElementById('confirmPassword')?.value;

            if (password !== confirmPassword) {
                alert('Las contraseñas no coinciden');
                return;
            }

            if (this.checkValidity()) {
                window.location.href = 'login.html';
            }
            this.classList.add('was-validated');
        });
    }
});
