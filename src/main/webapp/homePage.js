/**
 * homePage.js
 * - Aggiunge la logica per cliccare sui titoli degli album e mostrare le immagini in un blocco 1x5
 */

document.addEventListener("DOMContentLoaded", function () {
    // ... (tutto invariato come prima, tranne che aggiungiamo la parte per album cliccabili)

    // Riferimenti globali a elementi per la sezione album selezionato
    const selectedAlbumView = document.getElementById("selectedAlbumView");
    const selectedAlbumTitle = document.getElementById("selectedAlbumTitle");
    const selectedAlbumTable = document.getElementById("selectedAlbumTable");
    const prevImagesBtn = document.getElementById("prevImagesBtn");
    const nextImagesBtn = document.getElementById("nextImagesBtn");

    // Variabili per gestire paginazione
    let currentAlbumImages = [];
    let currentAlbumPage = 0;  // indice di pagina (0 => prime 5, 1 => successive 5, etc.)

    // FUNZIONE: Mostra l'album selezionato, parte dalla pagina 0
    function showAlbumImages(album) {
        selectedAlbumView.style.display = "block";
        selectedAlbumTitle.textContent = `Images of Album: ${album.titolo}`;

        // Se l'album ha un array di immagini (album.immagini)
        // In alternativa, potresti fare un AJAX se non hai i dati in anticipo
        currentAlbumImages = album.immagini || [];
        currentAlbumPage = 0;
        renderAlbumImages();
    }

    // FUNZIONE: disegna nella tabella le 5 immagini corrispondenti alla pagina corrente
    function renderAlbumImages() {
        // calcoliamo l'intervallo [start, end)
        const startIndex = currentAlbumPage * 5;
        const endIndex = startIndex + 5;

        // svuotiamo la tabella
        const row = selectedAlbumTable.querySelector("tr");
        // rimuoviamo tutto dentro la <tr>
        while (row.firstChild) {
            row.removeChild(row.firstChild);
        }

        // estraiamo le immagini da visualizzare
        let slice = currentAlbumImages.slice(startIndex, endIndex);

        // riempiamo 5 celle
        for (let i = 0; i < 5; i++) {
            const cell = document.createElement("td");
            if (i < slice.length) {
                // c'è un'immagine
                const imgObj = slice[i];
                // creiamo la miniatura e il titolo
                let thumb = document.createElement("img");
                thumb.src = "DownloadPhoto?imageId=" + imgObj.id_immagine;
                thumb.alt = imgObj.titolo;
                thumb.style.width = "100px";
                thumb.style.height = "100px";

                let titleElem = document.createElement("div");
                titleElem.textContent = imgObj.titolo;

                cell.appendChild(thumb);
                cell.appendChild(titleElem);
            } else {
                // cella vuota (nessuna immagine)
            }
            row.appendChild(cell);
        }

        // abilitiamo o disabilitiamo i bottoni
        prevImagesBtn.disabled = (currentAlbumPage === 0);
        // se siamo all'ultima pagina
        let maxPage = Math.floor((currentAlbumImages.length - 1) / 5);
        nextImagesBtn.disabled = (currentAlbumPage >= maxPage);
    }

    // EVENTI per i bottoni prev/next
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

    // Sostituiamo le funzioni di popolamento album per aggiungere link cliccabili
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
            // Al click => showAlbumImages
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

            // ID
            const idCell = document.createElement("td");
            idCell.textContent = album.id_album;
            row.appendChild(idCell);

            // Title => cliccabile
            const titleCell = document.createElement("td");
            const titleSpan = document.createElement("span");
            titleSpan.classList.add("album-title-link");
            titleSpan.textContent = album.titolo;
            // Al click => showAlbumImages
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

            otherAlbumTableBody.appendChild(row);
        });
    }

    // ================================================================
    // Qui sotto tutto il codice “pre-esistente” (login check, load myImages, 
    // load myAlbums, load otherAlbums, upload logic, createAlbum logic, ecc.)
    // con le versioni aggiornate di populateAlbums() e populateOtherAlbums()
    // ================================================================

    const logoutBtn = document.getElementById("logoutBtn");
    const usernameSpan = document.getElementById("username");
    const uploadForm = document.getElementById("uploadForm");
    const uploadResult = document.getElementById("uploadResult");
    const createAlbumForm = document.getElementById("createAlbumForm");
    const createAlbumResult = document.getElementById("createAlbumResult");

    // Controllo sessionStorage => se non loggato, redirect a login
    const username = sessionStorage.getItem("username");
    if (!username) {
        window.location.href = "login.html";
        return;
    }
    usernameSpan.textContent = username;

    // Carichiamo i dati dall'archivio
    let myImages = JSON.parse(sessionStorage.getItem("myImages") || "[]");
    let myAlbums = JSON.parse(sessionStorage.getItem("myAlbums") || "[]");
    let otherAlbums = JSON.parse(sessionStorage.getItem("otherAlbums") || "[]");

    // Popoliamo la galleria immagini
    populateImages(myImages);
    // Popoliamo la tabella dei nostri album
    populateAlbums(myAlbums);
    // Popoliamo la tabella degli album altrui
    populateOtherAlbums(otherAlbums);

    // Logout
    logoutBtn.addEventListener("click", function () {
        sessionStorage.clear();
        window.location.href = "login.html";
    });

    // Upload Photo => /UploadPhoto, etc. (codice invariato)
    uploadForm.addEventListener("submit", function (event) {
        event.preventDefault();
        let formData = new FormData(uploadForm);
        makeFormDataCall("POST", "UploadPhoto", formData, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    uploadResult.textContent = "Image uploaded successfully!";
                    // GET /GetImages per aggiornare
                    makeCall("GET", "GetImages", null, function (req2) {
                        if (req2.readyState === XMLHttpRequest.DONE) {
                            if (req2.status === 200) {
                                let updatedImages = JSON.parse(req2.responseText);
                                sessionStorage.setItem("myImages", JSON.stringify(updatedImages));
                                populateImages(updatedImages);
                            } else {
                                uploadResult.textContent = "Error getting updated images: " 
                                    + req2.status;
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

    // Create Album => /CreateAlbum, etc. (codice invariato)
    createAlbumForm.addEventListener("submit", function (event) {
        event.preventDefault();
        makeFormCall("POST", "CreateAlbum", createAlbumForm, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    createAlbumResult.textContent = "Album created successfully!";
                    // GET /GetAlbums per aggiornare
                    makeCall("GET", "GetAlbums", null, function (req2) {
                        if (req2.readyState === XMLHttpRequest.DONE) {
                            if (req2.status === 200) {
                                let updatedAlbums = JSON.parse(req2.responseText);
                                sessionStorage.setItem("myAlbums", JSON.stringify(updatedAlbums));
                                populateAlbums(updatedAlbums);
                            } else {
                                createAlbumResult.textContent = "Error getting updated albums: " 
                                    + req2.status;
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
});

