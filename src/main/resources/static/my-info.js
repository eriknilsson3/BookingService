const customer = JSON.parse(localStorage.getItem("customer"));
if (!customer) {
    window.location.href = "login.html";
}

const CUSTOMER_API = APP_CONFIG.CUSTOMER_API;
const token = localStorage.getItem("token");

document.getElementById("firstname").value = customer.firstName;
document.getElementById("lastname").value = customer.lastName;
document.getElementById("email_address").value = customer.email;
document.getElementById("phone_number").value = customer.phoneNumber;

function showMessage(success, message) {
    const successBox = document.getElementById("successMessage");
    const errorBox = document.getElementById("errorMessage");
    successBox.style.display = "none";
    errorBox.style.display = "none";
    if (success) {
        successBox.innerText = message;
        successBox.style.display = "block";
    } else {
        errorBox.innerText = message;
        errorBox.style.display = "block";
    }
}

document.getElementById("saveBtn").addEventListener("click", async () => {
    const updatedCustomer = {
        id: customer.id,
        firstName: document.getElementById("firstname").value,
        lastName: document.getElementById("lastname").value,
        email: document.getElementById("email_address").value,
        phoneNumber: document.getElementById("phone_number").value
    };

    try {
        const response = await fetch(`${CUSTOMER_API}/customers/${customer.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json", "Authorization": "Bearer " + token },
            body: JSON.stringify(updatedCustomer)
        });

        if (response.ok) {
            localStorage.setItem("customer", JSON.stringify(updatedCustomer));
            showMessage(true, "Information updated successfully.");
        } else {
            const text = await response.text();
            showMessage(false, text || "Could not update customer.");
        }
    } catch {
        showMessage(false, "Network error while updating customer.");
    }
});

document.getElementById("changeBtn").addEventListener("click", async () => {
    const currentPassword = document.getElementById("current_password").value;
    const newPassword = document.getElementById("new_password").value;
    const confirmPassword = document.getElementById("confirm_password").value;

    if (newPassword !== confirmPassword) {
        showMessage(false, "Passwords do not match.");
        return;
    }

    try {
        const response = await fetch(`${CUSTOMER_API}/customers/${customer.id}/change-password`, {
            method: "PUT",
            headers: { "Content-Type": "application/json", "Authorization": "Bearer " + token },
            body: JSON.stringify({ currentPassword, newPassword })
        });

        if (response.ok) {
            showMessage(true, "Password updated successfully.");
            document.getElementById("current_password").value = "";
            document.getElementById("new_password").value = "";
            document.getElementById("confirm_password").value = "";
        } else {
            const text = await response.text();
            showMessage(false, text || "Could not change password.");
        }
    } catch {
        showMessage(false, "Network error while changing password.");
    }
});

document.getElementById("deleteBtn").addEventListener("click", async () => {
    if (!confirm("Are you sure you want to delete your account?")) return;

    try {
        const response = await fetch(`${CUSTOMER_API}/customers/${customer.id}`, {
            method: "DELETE",
            headers: {
                "Authorization": "Bearer " + token
            }
        });

        if (response.ok) {
            showMessage(true, "User deleted successfully.");
            setTimeout(() => {
                localStorage.removeItem("customer");
                window.location.href = "login.html";
            }, 2000);
        } else {
            const text = await response.text();

            let prettyMessage = "Cannot delete user.";

            if (text.includes("active bookings")) {
                prettyMessage = "You can´t delete your account since you have active bookings.";
            }
            else if (text.includes("Booking service is not available")) {
                prettyMessage = "The bookingservice couldnt be reached.";
            }
            else if (text.includes("not found")) {
                prettyMessage = "Could not find user.";
            }

            showMessage(false, prettyMessage);
        }

    } catch {
        showMessage(false, "Network error.");
    }
});


document.getElementById("logoutBtn").addEventListener("click", () => {
    localStorage.removeItem("customer");
    window.location.href = "login.html";
});

document.getElementById("backBtn").addEventListener("click", () => {
    window.location.href = "dashboard.html";
});
