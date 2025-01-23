function validateEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

document.addEventListener("DOMContentLoaded", function () {
    // GESTIONE LOGIN 
    const loginForm = document.getElementById("loginForm");
    const loginResult = document.getElementById("loginResult");

    loginForm.addEventListener("submit", function (event) {
        event.preventDefault();

        const email = document.getElementById("loginEmail").value;
        const password = document.getElementById("loginPassword").value;

        
        if (!validateEmail(email)) {
            loginResult.innerText = "Please enter a valid email address.";
            return;
        }
        if (!password.trim()) {
            loginResult.innerText = "Password cannot be empty.";
            return;
        }

        
        makeFormCall("POST", "Login", loginForm, function (req) {
            if (req.status === 200) {
                try {
                    let jsonResponse = JSON.parse(req.responseText);

                    if (jsonResponse.success === true) {
						loginForm.reset();
                        loginResult.innerText = "Login successful. Welcome " 
                            + jsonResponse.username + "!";
                        sessionStorage.setItem("userId", jsonResponse.userId);
                        sessionStorage.setItem("username", jsonResponse.username);
                        sessionStorage.setItem("myAlbums", JSON.stringify(jsonResponse.myAlbums));
                        sessionStorage.setItem("otherAlbums", JSON.stringify(jsonResponse.otherAlbums));
                        sessionStorage.setItem("myImages", JSON.stringify(jsonResponse.myImages));
                        window.location.href = "homePage.html";

                    } else {
                        loginResult.innerText = "Login failed: " + jsonResponse.message;
                    }
                } catch (err) {
                    console.error("JSON parse error:", err);
                    loginResult.innerText = "Error parsing server response.";
                }
            } else {
                loginResult.innerText = "Server error (Login): " + req.status;
            }
        }, false);
    });

    // GESTIONE REGISTRAZIONE
	const registerForm = document.getElementById("registerForm");
    const registerResult = document.getElementById("registerResult");

    registerForm.addEventListener("submit", function (event) {
        event.preventDefault();

        const email = document.getElementById("registerEmail").value;
        const username = document.getElementById("registerUsername").value;
        const password = document.getElementById("registerPassword").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        
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

        
        makeFormCall("POST", "Register", registerForm, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
					registerForm.reset();
                    registerResult.innerText = "Registration successful. You can now log in.";
                } else {
                    let errorMessage = req.responseText 
                        || "Registration failed (unknown error)";
                    registerResult.innerText = errorMessage;
                }
            }
        }, false);
    });
});