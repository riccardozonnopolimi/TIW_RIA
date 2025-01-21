/**
 * login.js
 * - Gestisce form di login e registrazione via AJAX
 * - Login => usa servlet "/Login" che restituisce un JSON basato su UserData
 * - Registrazione => usa servlet "/Register" che restituisce HTTP status code
 */

// Semplice regex per validare l'email
function validateEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

document.addEventListener("DOMContentLoaded", function () {
    // =================================================================
    // GESTIONE LOGIN (risposta JSON via /Login)
    // =================================================================
    const loginForm = document.getElementById("loginForm");
    const loginResult = document.getElementById("loginResult");

    loginForm.addEventListener("submit", function (event) {
        event.preventDefault(); // Evitiamo il comportamento di default del form (submit classico)

        const email = document.getElementById("loginEmail").value;
        const password = document.getElementById("loginPassword").value;

        // Validazione base
        if (!validateEmail(email)) {
            loginResult.innerText = "Please enter a valid email address.";
            return;
        }
        if (!password.trim()) {
            loginResult.innerText = "Password cannot be empty.";
            return;
        }

        // Usiamo makeFormCall (definito in utils.js) per inviare la form a /Login
        makeFormCall("POST", "Login", loginForm, function (req) {
            if (req.status === 200) {
                try {
                    // Il server risponde con un JSON contenente UserData
                    let jsonResponse = JSON.parse(req.responseText);

                    if (jsonResponse.success === true) {
                        // Login riuscito
                        loginResult.innerText = "Login successful. Welcome " 
                            + jsonResponse.username + "!";

                        // Salviamo i dati su sessionStorage
                        // (così potremo recuperarli in homePage.html)
                        sessionStorage.setItem("userId", jsonResponse.userId);
                        sessionStorage.setItem("username", jsonResponse.username);

                        // Convertiamo gli array in stringa JSON
                        sessionStorage.setItem("myAlbums", JSON.stringify(jsonResponse.myAlbums));
                        sessionStorage.setItem("otherAlbums", JSON.stringify(jsonResponse.otherAlbums));
                        sessionStorage.setItem("myImages", JSON.stringify(jsonResponse.myImages));

                        // Esempio: reindirizzamento a homePage
                        // (se non vuoi reindirizzare, puoi lasciarlo commentato)
                        window.location.href = "homePage.html";

                    } else {
                        // Login fallito
                        loginResult.innerText = "Login failed: " + jsonResponse.message;
                    }
                } catch (err) {
                    console.error("JSON parse error:", err);
                    loginResult.innerText = "Error parsing server response.";
                }
            } else {
                // Se la servlet risponde con status != 200, lo consideriamo errore
                loginResult.innerText = "Server error (Login): " + req.status;
            }
        }, false);
    });

    // =================================================================
    // GESTIONE REGISTRAZIONE (status code + plain text da /Register)
    // =================================================================
    const registerForm = document.getElementById("registerForm");
    const registerResult = document.getElementById("registerResult");

    registerForm.addEventListener("submit", function (event) {
        event.preventDefault(); // Evitiamo il comportamento di default

        const email = document.getElementById("registerEmail").value;
        const username = document.getElementById("registerUsername").value;
        const password = document.getElementById("registerPassword").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        // Validazione base
        if (!validateEmail(email)) {
            registerResult.innerText = "Please enter a valid email address.";
            return;
        }
        if (!username.trim()) {
            registerResult.innerText = "Username cannot be empty.";
            return;
        }
        if (!password.trim()) {
            registerResult.innerText = "Password cannot be empty.";
            return;
        }
        if (password !== confirmPassword) {
            registerResult.innerText = "Passwords do not match.";
            return;
        }

        // Chiamata AJAX a /Register
        makeFormCall("POST", "Register", registerForm, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                // Check dello status code
                if (req.status === 200) {
                    // Registrazione ok => mostriamo un messaggio nel login
                    registerResult.innerText = "Registration successful. You can now log in.";
                } else {
                    // Errore (400 o 500)
                    let errorMessage = req.responseText 
                        || "Registration failed (unknown error)";
                    registerResult.innerText = errorMessage;
                }
            }
        }, false);
    });
});

