document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.getElementById("loginForm");
    const registerForm = document.getElementById("registerForm");

    const loginError = document.getElementById("loginError");
    const registerError = document.getElementById("registerError");

    // Validazione Login
    loginForm.addEventListener("submit", function (e) {
        e.preventDefault();

        const email = document.getElementById("loginEmail").value.trim();
        const password = document.getElementById("loginPassword").value.trim();

        if (!email || !password) {
            loginError.textContent = "Inserisci email e password.";
            loginError.style.display = "block";
            return;
        }

        loginError.style.display = "none";
        alert(`Login effettuato con successo!\nEmail: ${email}`);
    });

    // Validazione Registrazione
    registerForm.addEventListener("submit", function (e) {
        e.preventDefault();

        const email = document.getElementById("registerEmail").value.trim();
        const username = document.getElementById("registerUsername").value.trim();
        const password = document.getElementById("registerPassword").value;
        const confirmPassword = document.getElementById("registerConfirmPassword").value;

        const emailRegex =
            /^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)])$/;

        if (!emailRegex.test(email)) {
            registerError.textContent = "Inserisci un'email valida.";
            registerError.style.display = "block";
            return;
        }

        if (password !== confirmPassword) {
            registerError.textContent = "Le password non corrispondono.";
            registerError.style.display = "block";
            return;
        }

        if (!username || !password) {
            registerError.textContent = "Compila tutti i campi.";
            registerError.style.display = "block";
            return;
        }

        registerError.style.display = "none";
        alert(`Registrazione completata!\nUsername: ${username}`);
    });
});
