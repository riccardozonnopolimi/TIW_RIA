document.addEventListener("DOMContentLoaded", function () {
    // -------------- Elementi base ---------------
    const logoutBtn = document.getElementById("logoutBtn");
    const usernameSpan = document.getElementById("username");

    const uploadForm = document.getElementById("uploadForm");
    const uploadResult = document.getElementById("uploadResult");

    const createAlbumForm = document.getElementById("createAlbumForm");
    const createAlbumResult = document.getElementById("createAlbumResult");

    // Sezione album selezionato
    const selectedAlbumView = document.getElementById("selectedAlbumView");
    const selectedAlbumTitle = document.getElementById("selectedAlbumTitle");
    const selectedAlbumTable = document.getElementById("selectedAlbumTable");
    const prevImagesBtn = document.getElementById("prevImagesBtn");
    const nextImagesBtn = document.getElementById("nextImagesBtn");
    const notInAlbumContainer = document.getElementById("notInAlbumContainer");

    // Modale
    const modalOverlay = document.getElementById("modalOverlay");
    const imageModal = document.getElementById("imageModal");
    const closeModalBtn = document.getElementById("closeModalBtn");
    const modalImage = document.getElementById("modalImage");
    const modalImageTitle = document.getElementById("modalImageTitle");
    const modalImageDesc = document.getElementById("modalImageDesc");
    const modalComments = document.getElementById("modalComments");
    const modalCommentForm = document.getElementById("modalCommentForm");
    const modalCommentText = document.getElementById("modalCommentText");

    // Variabili globali
    let myImages = [];
    let myAlbums = [];
    let otherAlbums = [];

    // Stato album
    let currentAlbumImages = [];
    let currentAlbumPage = 0;
    let currentAlbumId = null;

    // Check login
    const username = sessionStorage.getItem("username");
    if (!username) {
        window.location.href = "login.html";
        return;
    }
    usernameSpan.textContent = username;

    // Carichiamo i dati
    myImages = JSON.parse(sessionStorage.getItem("myImages") || "[]");
    myAlbums = JSON.parse(sessionStorage.getItem("myAlbums") || "[]");
    otherAlbums = JSON.parse(sessionStorage.getItem("otherAlbums") || "[]");

    // Popolamento iniziale
    populateImages(myImages);
    populateAlbums(myAlbums);
    populateOtherAlbums(otherAlbums);

    // Logout
    logoutBtn.addEventListener("click", function () {
        sessionStorage.clear();
        window.location.href = "login.html";
    });

    // ========================
    // Upload Photo
    // ========================
    uploadForm.addEventListener("submit", function (event) {
        event.preventDefault();
        let formData = new FormData(uploadForm);
        makeFormDataCall("POST", "UploadPhoto", formData, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    uploadResult.textContent = "Image uploaded successfully!";
                    // Ricarichiamo /GetImages
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

    // ========================
    // Create Album
    // ========================
    createAlbumForm.addEventListener("submit", function (event) {
        event.preventDefault();
        makeFormCall("POST", "CreateAlbum", createAlbumForm, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    createAlbumResult.textContent = "Album created successfully!";
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

    // ========================
    // Sezione "Your Images" (orizzontale)
    // ========================
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

            // Se volessimo la modale anche qui: 
            // potremmo aggiungere un onmouseover => showModalImage(img)
            // Oppure lasciamo come da specifica "nell'album"
            div.appendChild(imgTag);
            imagesContainer.appendChild(div);
        });
    }

    // ========================
    // Sezione "My Albums" e "Other Albums"
    // ========================
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
            const titleSpan = document.createElement("span");
            titleSpan.classList.add("album-title-link");
            titleSpan.textContent = album.titolo;
            // click => showAlbumImages
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

            const idCell = document.createElement("td");
            idCell.textContent = album.id_album;
            row.appendChild(idCell);

            const titleCell = document.createElement("td");
            const titleSpan = document.createElement("span");
            titleSpan.classList.add("album-title-link");
            titleSpan.textContent = album.titolo;
            // click => showAlbumImages
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

    // ========================
    // showAlbumImages => blocco 1x5 + notInAlbum
    // ========================
    function showAlbumImages(album) {
        selectedAlbumView.style.display = "block";
        selectedAlbumTitle.textContent = `Images of Album: ${album.titolo}`;

        currentAlbumImages = album.immagini || [];
        currentAlbumPage = 0;
        currentAlbumId = album.id_album;

        renderAlbumImages();

        let notInAlbum = computeNotInAlbumImages(album.immagini || []);
        populateNotInAlbumImages(notInAlbum, album.id_album);
    }

    function computeNotInAlbumImages(albumImages) {
        const albumImageIds = new Set(albumImages.map(i => i.id_immagine));
        return myImages.filter(i => !albumImageIds.has(i.id_immagine));
    }

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
            thumb.style.cursor = "pointer";
            thumb.style.objectFit = "cover";
            thumb.style.borderRadius = "5px";
            thumb.style.marginRight = "5px";

            thumb.addEventListener("click", function () {
                addImageToAlbum(albumId, img.id_immagine);
            });

            notInAlbumContainer.appendChild(thumb);
        });
    }

    function addImageToAlbum(albumId, imageId) {
        let url = "AddImageToAlbum?albumId=" + albumId + "&imageId=" + imageId;
        makeCall("GET", url, null, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    // Ricarichiamo /GetAlbums
                    makeCall("GET", "GetAlbums", null, function (req2) {
                        if (req2.readyState === XMLHttpRequest.DONE) {
                            if (req2.status === 200) {
                                myAlbums = JSON.parse(req2.responseText);
                                sessionStorage.setItem("myAlbums", JSON.stringify(myAlbums));
                                // Cerchiamo l'album
                                let foundAlbum = myAlbums.find(a => a.id_album === albumId);
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

    // ========================
    // Paginazione (1 x 5)
    // ========================
    function renderAlbumImages() {
        const row = selectedAlbumTable.querySelector("tr");
        while (row.firstChild) {
            row.removeChild(row.firstChild);
        }

        let startIndex = currentAlbumPage * 5;
        let endIndex = startIndex + 5;
        let slice = currentAlbumImages.slice(startIndex, endIndex);

        for (let i = 0; i < 5; i++) {
            const cell = document.createElement("td");
            if (i < slice.length) {
                let imgObj = slice[i];
                let thumb = document.createElement("img");
                thumb.src = "DownloadPhoto?imageId=" + imgObj.id_immagine;
                thumb.alt = imgObj.titolo;
                thumb.style.width = "100px";
                thumb.style.height = "100px";
                thumb.style.cursor = "pointer";

                // MOUSEOVER => showModalImage con i dettagli
                thumb.addEventListener("mouseover", function () {
                    // Apriamo modale e mostriamo dettagli
                    showModalImage(imgObj);
                });

                // Se vogliamo che la modale si chiuda al mouseout,
                // potremmo aggiungere un eventListener "mouseout" => hideModal(),
                // ma in genere si usa un "click" per aprire e un bottone "x" per chiudere.

                let titleElem = document.createElement("div");
                titleElem.textContent = imgObj.titolo;

                cell.appendChild(thumb);
                cell.appendChild(titleElem);
            }
            row.appendChild(cell);
        }

        // Bottoni
        prevImagesBtn.disabled = (currentAlbumPage === 0);
        let maxPage = Math.floor((currentAlbumImages.length - 1) / 5);
        nextImagesBtn.disabled = (currentAlbumPage >= maxPage);
    }

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

    // ========================
    // Modal con immagine + commenti + form comment
    // ========================
    function showModalImage(imgObj) {
        // Riempiamo i campi
        modalImage.src = "DownloadPhoto?imageId=" + imgObj.id_immagine;
        modalImageTitle.textContent = imgObj.titolo;
        modalImageDesc.textContent = imgObj.descrizione || "";

        // Puliamo e mostriamo i commenti
        populateModalComments(imgObj.commenti || []);
        
        // Salviamo id_immagine su un attributo data
        modalCommentForm.dataset.imageId = imgObj.id_immagine;

        // Apriamo la modale
        modalOverlay.style.display = "flex";
    }

    function populateModalComments(commentiArray) {
        modalComments.innerHTML = "";
        if (!commentiArray || commentiArray.length === 0) {
            modalComments.textContent = "No comments yet.";
            return;
        }
        commentiArray.forEach(c => {
            let div = document.createElement("div");
            div.classList.add("comment-item");
            div.textContent = c.username + ": " + c.testo; // + data_creazione se vuoi
            modalComments.appendChild(div);
        });
    }

    // Chiudi modale
    closeModalBtn.addEventListener("click", function () {
        hideModal();
    });
    function hideModal() {
        modalOverlay.style.display = "none";
    }

    // ========================
    // Gestione form commenti nella modale
    // ========================
    modalCommentForm.addEventListener("submit", function (e) {
        e.preventDefault();
        let testo = modalCommentText.value.trim();
        if (!testo) {
            alert("Comment cannot be empty!");
            return;
        }
        let imageId = modalCommentForm.dataset.imageId;
        // Serve anche l'albumId => currentAlbumId
        // GET /AddComment?imageId=...&albumId=...&testo=...
        let url = "AddComment?imageId=" + imageId + "&albumId=" + currentAlbumId + 
                  "&testo=" + encodeURIComponent(testo);

        makeCall("GET", url, null, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    // Ricarichiamo /GetAlbums per avere il commento aggiornato
                    makeCall("GET", "GetAlbums", null, function (req2) {
                        if (req2.readyState === XMLHttpRequest.DONE) {
                            if (req2.status === 200) {
                                myAlbums = JSON.parse(req2.responseText);
                                sessionStorage.setItem("myAlbums", JSON.stringify(myAlbums));
                                // Troviamo l'album con currentAlbumId
                                let foundAlbum = myAlbums.find(a => a.id_album === currentAlbumId);
                                if (foundAlbum) {
                                    // Troviamo l'immagine aggiornata
                                    let updatedImg = foundAlbum.immagini.find(i => i.id_immagine == imageId);
                                    if (updatedImg) {
                                        // Ricarichiamo i commenti
                                        populateModalComments(updatedImg.commenti || []);
                                    }
                                }
                            } else {
                                alert("Error reloading albums: " + req2.status);
                            }
                        }
                    });
                    // Puliamo il textarea
                    modalCommentText.value = "";
                } else if (req.status === 401) {
                    alert("Unauthorized, please log in again.");
                } else {
                    alert("Error adding comment: " + req.status);
                }
            }
        });
    });
});
