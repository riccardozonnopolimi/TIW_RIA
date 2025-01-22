/**
 * AJAX call management
 */
function makeCall(method, url, formElement, callback, reset = true) {
    let req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === XMLHttpRequest.DONE) {
            callback(req);
        }
    };
    req.open(method, url);
    if (formElement === null) {
        req.send();
    } else {
        req.send(new FormData(formElement));
    }
    if (formElement !== null && reset === true) {
        formElement.reset();
    }
}


function makeFormCall(method, url, formElement, callback, reset = true) {
    let req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === XMLHttpRequest.DONE) {
            callback(req);
        }
    };
    req.open(method, url);
    if (formElement == null) {
        req.send();
    } else {
        let formData = new FormData(formElement);
        let encodedData = new URLSearchParams(formData).toString();
        req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        req.send(encodedData);
    }
    if (formElement !== null && reset === true) {
        formElement.reset();
    }
}


function makeFormDataCall(method, url, formData, callback) {
    let req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === XMLHttpRequest.DONE) {
            callback(req);
        }
    };
    req.open(method, url, true);
    if (formData instanceof FormData) {
        req.send(formData);
    } else {
        req.send();
    }
}
