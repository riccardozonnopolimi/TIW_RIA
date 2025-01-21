/**
 * homePage.js
 * - Carica i dati dal sessionStorage (username, myImages, myAlbums, otherAlbums, ecc.)
 * - Popola la pagina
 * - Gestisce logout
 * - Gestisce upload immagini (POST -> UploadPhoto) => in caso di success => GET -> GetImages
 * - Gestisce creazione album (POST -> CreateAlbum) => in caso di success => GET -> GetAlbums
 */

document.addEventListener("DOMContentLoaded", function () {
    const logoutBtn = document.getElementById("logoutBtn");
    const usernameSpan = document.getElementById("username");

    // Controllo sessionStorage => se non loggato, redirect a login
    const username = sessionStorage.getItem("username");
    if (!username) {
        window.location.href = "login.html";
        return;
    }
    // Utente loggato
    usernameSpan.textContent = username;

    // Recuperiamo dati: myImages, myAlbums, otherAlbums
    const myImagesData = sessionStorage.getItem("myImages");
    const myAlbumsData = sessionStorage.getItem("myAlbums");
    const otherAlbumsData = sessionStorage.getItem("otherAlbums");

    let myImages = [];
    let myAlbums = [];
    let otherAlbums = [];

    if (myImagesData) {
        try {
            myImages = JSON.parse(myImagesData);
        } catch (e) {
            console.error("Error parsing myImages:", e);
        }
    }

    if (myAlbumsData) {
        try {
            myAlbums = JSON.parse(myAlbumsData);
        } catch (e) {
            console.error("Error parsing myAlbums:", e);
        }
    }

    if (otherAlbumsData) {
        try {
            otherAlbums = JSON.parse(otherAlbumsData);
        } catch (e) {
            console.error("Error parsing otherAlbums:", e);
        }
    }

    // Mostriamo i dati recuperati
    populateImages(myImages);
    populateAlbums(myAlbums);
    populateOtherAlbums(otherAlbums);

    // -------------------------------------------
    // Logout
    logoutBtn.addEventListener("click", function () {
        sessionStorage.clear();
        window.location.href = "login.html";
    });

    // -------------------------------------------
    // UPLOAD FORM => "/UploadPhoto"
    const uploadForm = document.getElementById("uploadForm");
    const uploadResult = document.getElementById("uploadResult");
    uploadForm.addEventListener("submit", function (event) {
        event.preventDefault();

        let formData = new FormData(uploadForm);
        makeFormDataCall("POST", "UploadPhoto", formData, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    // Upload OK
                    uploadResult.textContent = "Image uploaded successfully!";

                    // Facciamo una GET a "GetImages" per avere l'elenco aggiornato
                    makeCall("GET", "GetImages", null, function (req2) {
                        if (req2.readyState === XMLHttpRequest.DONE) {
                            if (req2.status === 200) {
                                // Parse JSON => array di ImmagineData
                                let updatedImages = JSON.parse(req2.responseText);
                                // Aggiorno sessionStorage
                                sessionStorage.setItem("myImages", JSON.stringify(updatedImages));
                                // Aggiorno vista
                                populateImages(updatedImages);
                            } else if (req2.status === 401) {
                                uploadResult.textContent = "Unauthorized. Please log in again.";
                            } else {
                                uploadResult.textContent = "Error getting updated images: " 
                                    + req2.status;
                            }
                        }
                    });

                } else if (req.status === 401) {
                    uploadResult.textContent = "Unauthorized: please log in again.";
                } else {
                    let errorMsg = req.responseText || "Upload failed";
                    uploadResult.textContent = "Error: " + errorMsg;
                }
            }
        });
    });

    // -------------------------------------------
    // CREATE ALBUM FORM => "/CreateAlbum"
    const createAlbumForm = document.getElementById("createAlbumForm");
    const createAlbumResult = document.getElementById("createAlbumResult");

    createAlbumForm.addEventListener("submit", function (event) {
        event.preventDefault();

        makeFormCall("POST", "CreateAlbum", createAlbumForm, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    createAlbumResult.textContent = "Album created successfully!";
                    // GET a "GetAlbums" per avere l'elenco aggiornato
                    makeCall("GET", "GetAlbums", null, function (req2) {
                        if (req2.readyState === XMLHttpRequest.DONE) {
                            if (req2.status === 200) {
                                let updatedAlbums = JSON.parse(req2.responseText);
                                // Aggiorniamo sessionStorage
                                sessionStorage.setItem("myAlbums", JSON.stringify(updatedAlbums));
                                // Aggiorniamo la tabella
                                populateAlbums(updatedAlbums);
                            } else if (req2.status === 401) {
                                createAlbumResult.textContent = "Unauthorized. Please log in again.";
                            } else {
                                createAlbumResult.textContent = "Error getting updated albums: "
                                    + req2.status;
                            }
                        }
                    });

                } else if (req.status === 401) {
                    createAlbumResult.textContent = "Unauthorized: please log in again.";
                } else {
                    let errorMessage = req.responseText || "Album creation failed";
                    createAlbumResult.textContent = errorMessage;
                }
            }
        });
    });
});

/**
 * Popola la sezione "Your Images" con le immagini
 */
function populateImages(imagesArray) {
    const imagesContainer = document.getElementById("imagesContainer");
    imagesContainer.innerHTML = "";

    if (!imagesArray || imagesArray.length === 0) {
        imagesContainer.textContent = "No images found.";
        return;
    }

    imagesArray.forEach(img => {
        const div = document.createElement("div");
        div.classList.add("image-item");

        const titleElem = document.createElement("h4");
        titleElem.textContent = img.titolo;
        div.appendChild(titleElem);

        const descElem = document.createElement("p");
        descElem.textContent = img.descrizione || "";
        div.appendChild(descElem);

        // Invece di img.percorso, costruiamo l'URL alla servlet DownloadPhoto
        // Passiamo l'id_immagine come query param
        const downloadUrl = "DownloadPhoto?imageId=" + img.id_immagine;

        const imgTag = document.createElement("img");
        imgTag.src = downloadUrl; // Carica l'immagine dalla servlet
        imgTag.alt = img.titolo;
        imgTag.classList.add("thumbnail");
        div.appendChild(imgTag);

        imagesContainer.appendChild(div);
    });
}

/**
 * Popola la tabella "Your Albums"
 */
function populateAlbums(albumsArray) {
    const albumTableBody = document.getElementById("albumTableBody");
    albumTableBody.innerHTML = "";

    if (!albumsArray || albumsArray.length === 0) {
        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 4;
        cell.textContent = "No albums found.";
        row.appendChild(cell);
        albumTableBody.appendChild(row);
        return;
    }

    albumsArray.forEach(album => {
        const row = document.createElement("tr");

        const idCell = document.createElement("td");
        idCell.textContent = album.id_album;
        row.appendChild(idCell);

        const titleCell = document.createElement("td");
        titleCell.textContent = album.titolo;
        row.appendChild(titleCell);

        const creatorCell = document.createElement("td");
        creatorCell.textContent = album.usernameCreatore || "";
        row.appendChild(creatorCell);

        const dateCell = document.createElement("td");
        dateCell.textContent = album.data_creazione 
            ? new Date(album.data_creazione).toLocaleString() 
            : "";
        row.appendChild(dateCell);

        albumTableBody.appendChild(row);
    });
}

/**
 * Popola la tabella "Other Albums"
 */
function populateOtherAlbums(albumsArray) {
    const otherAlbumTableBody = document.getElementById("otherAlbumTableBody");
    otherAlbumTableBody.innerHTML = "";

    if (!albumsArray || albumsArray.length === 0) {
        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 4;
        cell.textContent = "No other albums found.";
        row.appendChild(cell);
        otherAlbumTableBody.appendChild(row);
        return;
    }

    albumsArray.forEach(album => {
        const row = document.createElement("tr");

        const idCell = document.createElement("td");
        idCell.textContent = album.id_album;
        row.appendChild(idCell);

        const titleCell = document.createElement("td");
        titleCell.textContent = album.titolo;
        row.appendChild(titleCell);

        const creatorCell = document.createElement("td");
        creatorCell.textContent = album.usernameCreatore || "";
        row.appendChild(creatorCell);

        const dateCell = document.createElement("td");
        dateCell.textContent = album.data_creazione 
            ? new Date(album.data_creazione).toLocaleString() 
            : "";
        row.appendChild(dateCell);

        otherAlbumTableBody.appendChild(row);
    });
}
