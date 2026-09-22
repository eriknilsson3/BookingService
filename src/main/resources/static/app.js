// LOGIN HANDLING
const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;

        const response = await fetch(`${APP_CONFIG.CUSTOMER_API}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            const data = await response.json();

            console.log("LOGIN DATA:", data);

            // Spara kunddata
            localStorage.setItem("token", data.token);

            window.location.href = "dashboard.html";
        } else {
            const errorText = await response.text();
            const errorMessage = document.getElementById("errorMessage");

            // Visa snyggt felmeddelande istället för timestamp
            if (errorText.includes("timestamp")) {
                errorMessage.textContent = "Something went wrong.";
            } else {
                errorMessage.textContent = errorText;
            }

            errorMessage.style.display = "block";

            // Dölj felmeddelandet när användaren börjar skriva igen
            document.getElementById("password").addEventListener("input", () => {
                errorMessage.style.display = "none";
            });
        }
    });
}

// REGISTER HANDLING
const registerForm = document.getElementById("registerForm");

if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const firstname = document.getElementById("firstname").value;
        const lastname = document.getElementById("lastname").value;
        const email = document.getElementById("email").value;
        const phone = document.getElementById("phone").value;
        const password = document.getElementById("password").value;
        const errorMessage = document.getElementById("errorMessage");
        const successMessage = document.getElementById("successMessage");
        errorMessage.style.display = "none";
        if (firstname.trim() === "" ||
            lastname.trim() === "" || email.trim() === "" || password.trim() === "") {
            errorMessage.textContent = "Fields cannot be empty";
            errorMessage.style.display = "block";
            return;
        }
        const response = await fetch(`${APP_CONFIG.CUSTOMER_API}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                firstName: firstname,
                lastName: lastname,
                email: email,
                phoneNumber: phone,
                password: password
            })
        });

        errorMessage.style.display = "none";
        successMessage.style.display = "none";

        if (response.ok) {
            const data = await response.json();

            // Spara kunddata
            localStorage.setItem("customer", JSON.stringify(data));
            localStorage.setItem("customerId", data.id);

            successMessage.innerText = "User created successfully!";
            successMessage.style.display = "block";

            setTimeout(() => {
                window.location.href = "login.html";
            }, 2000);

        } else {
            const text = await response.text();
            errorMessage.innerText = text;
            errorMessage.style.display = "block";
        }
    });
}


