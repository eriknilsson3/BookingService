const token = localStorage.getItem("token");

if (!token) {
    window.location.href = "login.html";
}

// Booking-service kör på port 8080
const BOOKING_API = APP_CONFIG.BOOKING_API;

// ---------------------------
// 1. SÖK LEDIGA RUM
// ---------------------------
document.getElementById("date-form").addEventListener("submit", async function(e) {
    e.preventDefault();

    const errorBox = document.getElementById("date-error");
    errorBox.textContent = "";

    const startDateValue = document.getElementById("startDate").value;
    const endDateValue = document.getElementById("endDate").value;

    if (!startDateValue || !endDateValue) {
        errorBox.textContent = "Please select both dates.";
        return;
    }

    const startDate = new Date(startDateValue);
    const endDate = new Date(endDateValue);
    const today = new Date();
    today.setHours(0,0,0,0);

    if (startDate < today) {
        errorBox.textContent = "Start date cannot be in the past!";
        return;
    }

    if (endDate <= startDate) {
        errorBox.textContent = "End date must be after start date!";
        return;
    }

    try {
        const response = await fetch(
            `${BOOKING_API}/bookings/available-range?start=${startDateValue}&end=${endDateValue}`,
            {
                headers: {
                    "Authorization": "Bearer " + token
                }
            }
        );

        if (!response.ok) {
            errorBox.textContent = "Could not fetch available rooms.";
            return;
        }

        const rooms = await response.json();
        console.log("ROOMS", rooms)
        rooms.sort((a, b) => a.pricePerNight - b.pricePerNight);

        const roomList = document.getElementById("available-rooms");
        roomList.innerHTML = "<h3>Available rooms</h3>";

        rooms.forEach(room => {
            const label = document.createElement("label");
            label.classList.add("room-option");

            label.innerHTML = `
                <input type="radio" name="roomId" value="${room.id}"
                    data-type="${room.type}" data-price="${room.pricePerNight}">
                Room ${room.roomNumber} – ${room.type} – ${room.pricePerNight} SEK/night
            `;

            roomList.appendChild(label);
        });

        roomList.classList.remove("hidden");

    } catch (err) {
        console.error("Fetch error:", err);
        errorBox.textContent = "Network error while fetching rooms.";
    }
});

// ---------------------------
// 2. VISA/DÖLJ EXTRASÄNG
// ---------------------------
document.addEventListener("change", function(e) {
    if (e.target.name === "roomId") {
        const roomType = e.target.getAttribute("data-type");
        const extraBedSection = document.getElementById("extra-bed-section");

        if (roomType === "DOUBLE") {
            extraBedSection.classList.remove("hidden");
        } else {
            extraBedSection.classList.add("hidden");
        }

        document.getElementById("confirm-form").classList.remove("hidden");
    }
});

// ---------------------------
// 3. BEKRÄFTA BOKNING
// ---------------------------
document.getElementById("confirm-form").addEventListener("submit", async function(e) {
    e.preventDefault();

    const selectedRoom = document.querySelector("input[name='roomId']:checked");
    if (!selectedRoom) {
        alert("Please select a room.");
        return;
    }

    const bookingData = {
        roomId: selectedRoom.value,
        startDate: document.getElementById("startDate").value,
        endDate: document.getElementById("endDate").value,
        extraBed: document.querySelector("input[name='extraBed']").checked
    };

    try {
        const response = await fetch(`${BOOKING_API}/bookings`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify(bookingData)
        });

        if (response.ok) {
            document.getElementById("success-message").classList.remove("hidden");
        } else {
            const text = await response.text();
            alert(text || "Something went wrong with your booking.");
        }

    } catch (err) {
        console.error("Booking error:", err);
        alert("Network error while creating booking.");
    }
});

// ---------------------------
// 4. BACK TO DASHBOARD
// ---------------------------
document.getElementById("back-to-dashboard").addEventListener("click", function() {
    window.location.href = "dashboard.html";
});

// ---------------------------
// 5. CANCEL BOOKING (UI RESET)
// ---------------------------
document.getElementById("cancel-booking").addEventListener("click", function() {
    document.getElementById("confirm-form").classList.add("hidden");
    document.getElementById("extra-bed-section").classList.add("hidden");

    const selected = document.querySelector("input[name='roomId']:checked");
    if (selected) selected.checked = false;
});
