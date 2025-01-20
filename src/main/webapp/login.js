document.addEventListener("DOMContentLoaded", function () {
    // Login form validation
    const loginForm = document.getElementById("loginForm");
    loginForm.addEventListener("submit", function (e) {
        const email = document.getElementById("loginEmail").value;
        const password = document.getElementById("loginPassword").value;

        if (!validateEmail(email)) {
            e.preventDefault();
            alert("Please enter a valid email address.");
        }

        if (!password.trim()) {
            e.preventDefault();
            alert("Password cannot be empty.");
        }
    });

    // Register form validation
    const registerForm = document.getElementById("registerForm");
    registerForm.addEventListener("submit", function (e) {
        const email = document.getElementById("registerEmail").value;
        const username = document.getElementById("registerUsername").value;
        const password = document.getElementById("registerPassword").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        if (!validateEmail(email)) {
            e.preventDefault();
            alert("Please enter a valid email address.");
        }

        if (!username.trim()) {
            e.preventDefault();
            alert("Username cannot be empty.");
        }

        if (password !== confirmPassword) {
            e.preventDefault();
            alert("Passwords do not match.");
        }
    });

    // Email validation function
    function validateEmail(email) {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    }
});

