const form = document.getElementById("customer-form");
const firstNameInput = document.getElementById("firstName");
const firstNameError = document.getElementById("firstNameError");

const lastNameInput = document.getElementById("lastName");
const lastNameError = document.getElementById("lastNameError");

const emailInput = document.getElementById("email");
const emailError = document.getElementById("emailError");

const numberInput = document.getElementById("phoneNumber");
const numberError = document.getElementById("phoneNumberError");

const passwordInput = document.getElementById("password");
const passwordError = document.getElementById("passwordError");


firstNameInput.addEventListener("blur", validateFirstName);
lastNameInput.addEventListener("blur", validateLastName);
emailInput.addEventListener("blur", validateEmail);
numberInput.addEventListener("blur", validateNumber);
passwordInput.addEventListener("blur", validatePassword);


function displayError(el, message) {
    el.innerHTML = message;
}

function clearError(el) {
    el.innerHTML = "";
}

function validateFirstName() {
    let value = firstNameInput.value.trim();
    if (value.length < 2) {
        displayError(firstNameError, "Minst 2 bokstäver");
        return false;
    }
    clearError(firstNameError);
    return true;
}

function validateLastName() {
    let value = lastNameInput.value.trim();
    if (value.length < 2) {
        displayError(lastNameError, "Minst 2 bokstäver");
        return false;
    }
    clearError(lastNameError);
    return true;
}

function validateEmail() {
    let value = emailInput.value.trim();
    const check = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!check.test(value) || value.length > 50) {
        displayError(emailError, "Fyll i giltig mail");
        return false;
    }
    clearError(emailError);
    return true;
}

function validateNumber() {
    let value = numberInput.value.trim();
    const check = /^[0-9\-()+]{1,20}$/;

    if (!check.test(value)) {
        displayError(numberError, "Fyll i giltigt nummer");
        return false;
    }
    clearError(numberError);
    return true;
}

function validatePassword() {
    let value = passwordInput.value.trim();
    if (value.length < 2) {
        displayError(passwordError, "Minst 2 tecken");
        return false;
    }
    clearError(passwordError);
    return true;
}

function validateForm() {
    let okFirstName = validateFirstName();
    let okLastName = validateLastName();
    let okEmail = validateEmail();
    let okNumber = validateNumber();
    let okPassword = validatePassword();

    return okFirstName && okLastName && okEmail && okNumber && okPassword;
}

form.addEventListener("submit", function (event) {

    if (!validateForm()) {
        event.preventDefault();
    }
});
