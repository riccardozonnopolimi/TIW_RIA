/**
 * homePage.js
 * 
 * - Controlla login via sessionStorage
 * - Carica myImages, myAlbums, otherAlbums
 * - Visualizza "Your Images" in forma orizzontale
 * - Cliccando su un album => visualizza immagini dell'album (paginazione 5/volta)
 * - Visualizza le immagini non ancora nell'album, cliccabili => AddImageToAlbum
 * - Gestisce upload (UploadPhoto) e creazione album (CreateAlbum)
 * - Aggiorna i dati richiamando GetImages / GetAlbums quando serve
 */

document.addEventListener("DOMContentLoaded", function () {
    // Riferimenti a elementi della pagina
    const logoutBtn = document.getElementById("logoutBtn");
    const usernameSpan = document.getElementById("username");

    const uploadForm = document.getElementById("uploadForm");
    const uploadResult = document.getElementById("uploadResult");

    const createAlbumForm = document.getElementById("createAlbumForm");
    const createAlbumResult = document.getElementById("createAlbumResult");

    // Sezione che mostra le immagini di un album selezionato
    const selectedAlbumView = document.getElementById("selectedAlbumView");
    const selectedAlbumTitle = document.getElementById("selectedAlbumTitle");
    const selectedAlbumTable = document.getElementById("selectedAlbumTable");
    const prevImagesBtn = document.getElementById("prevImagesBtn");
    const nextImagesBtn = document.getElementById("nextImagesBtn");

    // Sezione con le immagini "non in album"
    const notInAlbumContainer = document.getElementById("notInAlbumContainer");

    // Variabili globali di stato
    let myImages = [];
    let myAlbums = [];
    let otherAlbums = [];

    // Per la visualizzazione dell'album corrente
    let currentAlbumImages = [];
    let currentAlbumPage = 0;
    let currentAlbumId = null;

    // ==========================
    // 1. Controllo login
    // ==========================
    const username = sessionStorage.getItem("username");
    if (!username) {
        // Non loggato => redirect
        window.location.href = "login.html";
        return;
    }
    usernameSpan.textContent = username;

    // ==========================
    // 2. Caricamento dati da sessionStorage
    // ==========================
    // TUTTE le immagini dell'utente
    myImages = JSON.parse(sessionStorage.getItem("myImages") || "[]");
    // Album dell'utente
    myAlbums = JSON.parse(sessionStorage.getItem("myAlbums") || "[]");
    // Album di altri utenti
    otherAlbums = JSON.parse(sessionStorage.getItem("otherAlbums") || "[]");

    // ==========================
    // 3. Visualizzazioni iniziali
    // ==========================
    populateImages(myImages);
    populateAlbums(myAlbums);
    populateOtherAlbums(otherAlbums);

    // ==========================
    // 4. Logout
    // ==========================
    logoutBtn.addEventListener("click", function () {
        sessionStorage.clear();
        window.location.href = "login.html";
    });

    // ==========================
    // 5. Upload Photo
    // ==========================
    uploadForm.addEventListener("submit", function (event) {
        event.preventDefault();

        let formData = new FormData(uploadForm);
        // POST -> /UploadPhoto
        makeFormDataCall("POST", "UploadPhoto", formData, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    uploadResult.textContent = "Image uploaded successfully!";
                    // Ricarichiamo /GetImages per avere myImages aggiornato
                    makeCall("GET", "GetImages", null, function (req2) {
                        if (req2.readyState === XMLHttpRequest.DONE) {
                            if (req2.status === 200) {
                                myImages = JSON.parse(req2.responseText);
                                sessionStorage.setItem("myImages", JSON.stringify(myImages));
                                populateImages(myImages);
                            } else {
                                uploadResult.textContent =
                                    "Error getting updated images: " + req2.status;
                            }
                        }
                    });
                } else {
                    let errorMsg = req.status === 401
                        ? "Unauthorized: please log in again."
                        : (req.responseText || "Upload failed");
                    uploadResult.textContent = errorMsg;
                }
            }
        });
    });

    // ==========================
    // 6. Create Album
    // ==========================
    createAlbumForm.addEventListener("submit", function (event) {
        event.preventDefault();

        // POST -> /CreateAlbum
        makeFormCall("POST", "CreateAlbum", createAlbumForm, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    createAlbumResult.textContent = "Album created successfully!";
                    // Ricarichiamo /GetAlbums
                    makeCall("GET", "GetAlbums", null, function (req2) {
                        if (req2.readyState === XMLHttpRequest.DONE) {
                            if (req2.status === 200) {
                                myAlbums = JSON.parse(req2.responseText);
                                sessionStorage.setItem("myAlbums", JSON.stringify(myAlbums));
                                populateAlbums(myAlbums);
                            } else {
                                createAlbumResult.textContent =
                                    "Error getting updated albums: " + req2.status;
                            }
                        }
                    });
                } else {
                    let errorMessage = req.status === 401
                        ? "Unauthorized: please log in again."
                        : (req.responseText || "Album creation failed");
                    createAlbumResult.textContent = errorMessage;
                }
            }
        });
    });

    // ==========================
    // Funzione: visualizza "Your Images" in orizzontale
    // ==========================
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

            const imgTag = document.createElement("img");
            imgTag.src = "DownloadPhoto?imageId=" + img.id_immagine;
            imgTag.alt = img.titolo;
            imgTag.classList.add("thumbnail");
            div.appendChild(imgTag);

            imagesContainer.appendChild(div);
        });
    }

    // ==========================
    // Funzione: visualizza la lista "myAlbums"
    // ==========================
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

            // ID
            const idCell = document.createElement("td");
            idCell.textContent = album.id_album;
            row.appendChild(idCell);

            // Title => cliccabile
            const titleCell = document.createElement("td");
            const titleSpan = document.createElement("span");
            titleSpan.classList.add("album-title-link");
            titleSpan.textContent = album.titolo;
            titleSpan.addEventListener("click", function () {
                showAlbumImages(album);
            });
            titleCell.appendChild(titleSpan);
            row.appendChild(titleCell);

            // Creator
            const creatorCell = document.createElement("td");
            creatorCell.textContent = album.usernameCreatore || "";
            row.appendChild(creatorCell);

            // Date
            const dateCell = document.createElement("td");
            dateCell.textContent = album.data_creazione 
                ? new Date(album.data_creazione).toLocaleString() 
                : "";
            row.appendChild(dateCell);

            albumTableBody.appendChild(row);
        });
    }

    // ==========================
    // Funzione: visualizza la lista "otherAlbums"
    // ==========================
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
            const titleSpan = document.createElement("span");
            titleSpan.classList.add("album-title-link");
            titleSpan.textContent = album.titolo;
            titleSpan.addEventListener("click", function () {
                showAlbumImages(album);
            });
            titleCell.appendChild(titleSpan);
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

    // ==========================
    // showAlbumImages => mostra il blocco di immagini (paginazione) + "not in album"
    // ==========================
    function showAlbumImages(album) {
        selectedAlbumView.style.display = "block";
        selectedAlbumTitle.textContent = `Images of Album: ${album.titolo}`;

        currentAlbumImages = album.immagini || [];
        currentAlbumPage = 0;
        currentAlbumId = album.id_album;

        // Renderizziamo la paginazione
        renderAlbumImages();

        // Calcoliamo le immagini "notInAlbum"
        let notInAlbum = computeNotInAlbumImages(album.immagini || []);
        populateNotInAlbumImages(notInAlbum, album.id_album);
    }

    // ==========================
    // computeNotInAlbumImages => myImages - albumImages
    // ==========================
    function computeNotInAlbumImages(albumImages) {
        const albumImageIds = new Set(albumImages.map(img => img.id_immagine));
        return myImages.filter(img => !albumImageIds.has(img.id_immagine));
    }

    // ==========================
    // populateNotInAlbumImages => mostra orizzontalmente e cliccabile
    // ==========================
    function populateNotInAlbumImages(imagesArray, albumId) {
        notInAlbumContainer.innerHTML = "";

        if (!imagesArray || imagesArray.length === 0) {
            let p = document.createElement("p");
            p.textContent = "No images outside this album.";
            notInAlbumContainer.appendChild(p);
            return;
        }

        imagesArray.forEach(img => {
            let thumb = document.createElement("img");
            thumb.src = "DownloadPhoto?imageId=" + img.id_immagine;
            thumb.alt = img.titolo;
            thumb.style.width = "120px";
            thumb.style.height = "120px";
            thumb.style.objectFit = "cover";
            thumb.style.cursor = "pointer";
            thumb.style.borderRadius = "5px";

            thumb.addEventListener("click", function () {
                // AddImageToAlbum
                addImageToAlbum(albumId, img.id_immagine);
            });

            notInAlbumContainer.appendChild(thumb);
        });
    }

    // ==========================
    // addImageToAlbum => GET /AddImageToAlbum?albumId=...&imageId=...
    // ==========================
    function addImageToAlbum(albumId, imageId) {
        const url = "AddImageToAlbum?albumId=" + albumId + "&imageId=" + imageId;
        makeCall("GET", url, null, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    // Ricarichiamo /GetAlbums per avere l'album aggiornato
                    makeCall("GET", "GetAlbums", null, function (req2) {
                        if (req2.readyState === XMLHttpRequest.DONE) {
                            if (req2.status === 200) {
                                let updatedAlbums = JSON.parse(req2.responseText);
                                sessionStorage.setItem("myAlbums", JSON.stringify(updatedAlbums));
                                // Cerchiamo l'albumId
                                let foundAlbum = updatedAlbums.find(a => a.id_album === albumId);
                                if (foundAlbum) {
                                    showAlbumImages(foundAlbum);
                                }
                            } else {
                                alert("Error reloading albums: " + req2.status);
                            }
                        }
                    });
                } else if (req.status === 401) {
                    alert("Unauthorized, please log in again.");
                } else {
                    alert("Error adding image to album: " + req.status);
                }
            }
        });
    }

    // ==========================
    // renderAlbumImages => paginazione 5/volta
    // ==========================
    function renderAlbumImages() {
        const row = selectedAlbumTable.querySelector("tr");
        while (row.firstChild) {
            row.removeChild(row.firstChild);
        }

        const startIndex = currentAlbumPage * 5;
        const endIndex = startIndex + 5;
        let slice = currentAlbumImages.slice(startIndex, endIndex);

        for (let i = 0; i < 5; i++) {
            const cell = document.createElement("td");
            if (i < slice.length) {
                const imgObj = slice[i];

                let thumb = document.createElement("img");
                thumb.src = "DownloadPhoto?imageId=" + imgObj.id_immagine;
                thumb.alt = imgObj.titolo;
                thumb.style.width = "100px";
                thumb.style.height = "100px";

                let titleElem = document.createElement("div");
                titleElem.textContent = imgObj.titolo;

                cell.appendChild(thumb);
                cell.appendChild(titleElem);
            }
            row.appendChild(cell);
        }

        prevImagesBtn.disabled = (currentAlbumPage === 0);
        let maxPage = Math.floor((currentAlbumImages.length - 1) / 5);
        nextImagesBtn.disabled = (currentAlbumPage >= maxPage);
    }

    // Bottoni prev/next per la paginazione
    prevImagesBtn.addEventListener("click", function () {
        if (currentAlbumPage > 0) {
            currentAlbumPage--;
            renderAlbumImages();
        }
    });
    nextImagesBtn.addEventListener("click", function () {
        let maxPage = Math.floor((currentAlbumImages.length - 1) / 5);
        if (currentAlbumPage < maxPage) {
            currentAlbumPage++;
            renderAlbumImages();
        }
    });
});
