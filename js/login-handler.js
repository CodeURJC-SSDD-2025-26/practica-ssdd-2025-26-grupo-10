document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.querySelector('#loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function (e) {
            e.preventDefault();
            if (this.checkValidity()) {
                localStorage.setItem('isLoggedIn', 'true');
                window.location.href = 'dashboard.html';
            }
            this.classList.add('was-validated');
        });
    }
});
