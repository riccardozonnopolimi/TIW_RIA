document.addEventListener("DOMContentLoaded", function () {

    // Riferimenti a elementi nella pagina
    const mainContent = document.getElementById("mainContent");

    // Sezione "selectedAlbumView"
    const selectedAlbumView = document.getElementById("selectedAlbumView");
    const closeAlbumViewBtn = document.getElementById("closeAlbumViewBtn");
    const selectedAlbumTitle = document.getElementById("selectedAlbumTitle");
    const selectedAlbumTable = document.getElementById("selectedAlbumTable");
    const prevImagesBtn = document.getElementById("prevImagesBtn");
    const nextImagesBtn = document.getElementById("nextImagesBtn");

    // Sezione "not in album"
    const notInAlbumContainer = document.getElementById("notInAlbumContainer");

    // Bottone e sezione per riordino
    const modifyOrderBtn = document.getElementById("modifyOrderBtn");
    const reorderSection = document.getElementById("reorderSection");
    const reorderList = document.getElementById("reorderList");
    const saveOrderBtn = document.getElementById("saveOrderBtn");

    // Logout
    const logoutBtn = document.getElementById("logoutBtn");
    const usernameSpan = document.getElementById("username");

    // Form upload
    const uploadForm = document.getElementById("uploadForm");
    const uploadResult = document.getElementById("uploadResult");

    // Form create album
    const createAlbumForm = document.getElementById("createAlbumForm");
    const createAlbumResult = document.getElementById("createAlbumResult");

    // Modale
    const modalOverlay = document.getElementById("modalOverlay");
    const closeModalBtn = document.getElementById("closeModalBtn");
    const imageModal = document.getElementById("imageModal");
    const modalImage = document.getElementById("modalImage");
    const modalImageTitle = document.getElementById("modalImageTitle");
    const modalImageDesc = document.getElementById("modalImageDesc");
    const modalComments = document.getElementById("modalComments");
    const modalCommentForm = document.getElementById("modalCommentForm");
    const modalCommentText = document.getElementById("modalCommentText");
    const deletePhotoBtn = document.getElementById("deletePhotoBtn");


    // Variabili globali e stato
    let myImages = [];
    let myAlbums = [];
    let otherAlbums = [];

    // Stato album selezionato
    let currentAlbumImages = [];
    let currentAlbumPage = 0;
    let currentAlbumId = null;
    let currentAlbumOwnerId = null; 
	let dragSrcEl = null;

    // Controllo login
    const username = sessionStorage.getItem("username");
    const currentUserIdStr = sessionStorage.getItem("userId");
    if (!username || !currentUserIdStr) {
        window.location.href = "login.html";
        return;
    }
    const currentUserId = parseInt(currentUserIdStr, 10);
    usernameSpan.textContent = username;

    // 2. Caricamento dati da sessionStorage
    myImages = JSON.parse(sessionStorage.getItem("myImages") || "[]");
    myAlbums = JSON.parse(sessionStorage.getItem("myAlbums") || "[]");
    otherAlbums = JSON.parse(sessionStorage.getItem("otherAlbums") || "[]");

    // 3. Visualizzazioni iniziali
    populateImages(myImages);
    populateAlbums(myAlbums);
    populateOtherAlbums(otherAlbums);

    // LOGOUT
    logoutBtn.addEventListener("click", function () {
        makeCall("GET", "Logout", null, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    sessionStorage.clear();
                    window.location.href = "login.html";
                } else {
                    alert("Error during logout: " + req.status);
                }
            }
        });
    });

    // UPLOAD PHOTO
    uploadForm.addEventListener("submit", function (event) {
        event.preventDefault();
        let formData = new FormData(uploadForm);
        makeFormDataCall("POST", "UploadPhoto", formData, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    uploadResult.textContent = "Image uploaded successfully!";
                    uploadForm.reset();
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
                    let err = req.status === 401 
                        ? "Unauthorized: please log in again."
                        : (req.responseText || "Upload failed");
                    uploadResult.textContent = err;
                }
            }
        });
    });

    // CREATE ALBUM
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
                    let errMsg = req.status === 401
                        ? "Unauthorized: please log in again."
                        : (req.responseText || "Album creation failed");
                    createAlbumResult.textContent = errMsg;
                }
            }
        });
    });


    // SEZIONE "YOUR IMAGES"
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


    // ALBUMS (myAlbums e otherAlbums)
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
                ? new Date(album.data_creazione).toDateString()
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
                ? new Date(album.data_creazione).toDateString()
                : "";
            row.appendChild(dateCell);

            otherAlbumTableBody.appendChild(row);
        });
    }


    // showAlbumImages => nasconde TUTTO il resto, mostra selectedAlbumView  
    function showAlbumImages(album) {
        mainContent.style.display = "none";
        selectedAlbumView.style.display = "block";
        selectedAlbumTitle.textContent = `Images of Album: ${album.titolo}`;


        currentAlbumOwnerId = album.id_user_proprietario;

        currentAlbumImages = album.immagini || [];
        currentAlbumPage = 0;
        currentAlbumId = album.id_album;

        renderAlbumImages();


        const isOwner = (currentAlbumOwnerId === currentUserId);
        if (isOwner) {
            let notInAlbum = computeNotInAlbumImages(album.immagini || []);
            populateNotInAlbumImages(notInAlbum, album.id_album);
            notInAlbumContainer.style.display = "block";
            modifyOrderBtn.style.display = "inline-block";
            reorderSection.style.display = "none";
        } else {
            notInAlbumContainer.innerHTML = "";
            notInAlbumContainer.style.display = "none";
            modifyOrderBtn.style.display = "none";
            reorderSection.style.display = "none";
        }
    }

	
    closeAlbumViewBtn.addEventListener("click", function () {
        selectedAlbumView.style.display = "none";
        mainContent.style.display = "block";
    });

    function computeNotInAlbumImages(albumImages) {
        let albumImageIds = new Set(albumImages.map(i => i.id_immagine));
        return myImages.filter(img => !albumImageIds.has(img.id_immagine));
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
                    makeCall("GET", "GetAlbums", null, function (req2) {
                        if (req2.readyState === XMLHttpRequest.DONE) {
                            if (req2.status === 200) {
                                myAlbums = JSON.parse(req2.responseText);
                                sessionStorage.setItem("myAlbums", JSON.stringify(myAlbums));
                                populateAlbums(myAlbums);
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

    // Paginazione
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

                // Apertura modale al mouseover
                thumb.addEventListener("mouseover", function () {
                    showModalImage(imgObj);
                });

                let titleElem = document.createElement("div");
                titleElem.textContent = imgObj.titolo;

                cell.appendChild(thumb);
                cell.appendChild(titleElem);
            }
            row.appendChild(cell);
        }

		let maxPage = Math.floor((currentAlbumImages.length - 1) / 5);


		if (currentAlbumPage > 0) {
		    prevImagesBtn.style.display = "inline-block";
		} else {
		    prevImagesBtn.style.display = "none";
		}


		if (currentAlbumPage < maxPage) {
		    nextImagesBtn.style.display = "inline-block";
		} else {
		    nextImagesBtn.style.display = "none";
		}
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
	
	
    // MODALE: immagine, commenti, form, delete
    function showModalImage(imgObj) {

        if (imgObj.id_user_proprietario === currentUserId) {
            deletePhotoBtn.style.display = "inline-block";
        } else {
            deletePhotoBtn.style.display = "none";
        }

        modalImage.src = "DownloadPhoto?imageId=" + imgObj.id_immagine;
        modalImageTitle.textContent = imgObj.titolo;
        modalImageDesc.textContent = imgObj.descrizione || "";
        populateModalComments(imgObj.commenti || []);

        modalCommentForm.dataset.imageId = imgObj.id_immagine;
        deletePhotoBtn.dataset.imageId = imgObj.id_immagine;

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
            div.textContent = c.username + ": " + c.testo;
            modalComments.appendChild(div);
        });
    }

    closeModalBtn.addEventListener("click", function () {
        hideModal();
    });
    function hideModal() {
        modalOverlay.style.display = "none";
    }

    // Form comment
    modalCommentForm.addEventListener("submit", function (e) {
        e.preventDefault();
        let testo = modalCommentText.value.trim();
        if (!testo && testo.len()==0) {
            alert("Comment cannot be empty!");
            return;
        }
        let imageId = modalCommentForm.dataset.imageId;
        let url = "AddComment?imageId=" + imageId 
                + "&albumId=" + currentAlbumId
                + "&testo=" + encodeURIComponent(testo);

        makeCall("GET", url, null, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    makeCall("GET", "GetAlbums", null, function (r2) {
                        if (r2.readyState === XMLHttpRequest.DONE) {
                            if (r2.status === 200) {
                                myAlbums = JSON.parse(r2.responseText);
                                sessionStorage.setItem("myAlbums", JSON.stringify(myAlbums));
                                let foundAlbum = myAlbums.find(a => a.id_album == currentAlbumId);
                                if (foundAlbum) {
                                    let updatedImg = foundAlbum.immagini.find(i => i.id_immagine == imageId);
                                    if (updatedImg) {
                                        populateModalComments(updatedImg.commenti || []);
                                        showAlbumImages(foundAlbum);
                                    }
                                }
                            } else {
                                alert("Error reloading albums: " + r2.status);
                            }
                        }
                    });
					makeCall("GET", "GetOtherAlbums", null, function (r2) {
					                        if (r2.readyState === XMLHttpRequest.DONE) {
					                            if (r2.status === 200) {
					                                otherAlbums = JSON.parse(r2.responseText);
					                                sessionStorage.setItem("otherAlbums", JSON.stringify(otherAlbums));
					                                let foundAlbum = otherAlbums.find(a => a.id_album == currentAlbumId);
					                                if (foundAlbum) {
					                                    let updatedImg = foundAlbum.immagini.find(i => i.id_immagine == imageId);
					                                    if (updatedImg) {
					                                        populateModalComments(updatedImg.commenti || []);
					                                        showAlbumImages(foundAlbum);
															populateOtherAlbums(otherAlbums);
					                                    }
					                                }
					                            } else {
					                                alert("Error reloading albums: " + r2.status);
					                            }
					                        }
					                    });
                    modalCommentText.value = "";
                } else if (req.status === 401) {
                    alert("Unauthorized, please log in again.");
                } else {
                    alert("Error adding comment: " + req.status);
                }
            }
        });
	});

		    

    // Delete Photo
    deletePhotoBtn.addEventListener("click", function () {
        hideModal();
        let imageId = deletePhotoBtn.dataset.imageId;
        let url = "DeletePhoto?albumId=" + currentAlbumId + "&imageId=" + imageId;
        makeCall("GET", url, null, function (req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    makeCall("GET", "GetAlbums", null, function (r2) {
                        if (r2.readyState === XMLHttpRequest.DONE) {
                            if (r2.status === 200) {
                                myAlbums = JSON.parse(r2.responseText);
                                sessionStorage.setItem("myAlbums", JSON.stringify(myAlbums));
                                let foundAlbum = myAlbums.find(a => a.id_album === currentAlbumId);
                                makeCall("GET", "GetImages", null, function (r3) {
                                    if (r3.readyState === XMLHttpRequest.DONE) {
                                        if (r3.status === 200) {
                                            myImages = JSON.parse(r3.responseText);
                                            sessionStorage.setItem("myImages", JSON.stringify(myImages));
                                            populateImages(myImages);
                                            if (foundAlbum) {
                                                showAlbumImages(foundAlbum);
                                            }
                                        } else {
                                            alert("Error reloading images: " + r3.status);
                                        }
                                    }
                                });
                            } else {
                                alert("Error reloading albums: " + r2.status);
                            }
                        }
                    });
                } else if (req.status === 401) {
                    alert("Unauthorized, please log in again.");
                } else {
                    alert("Error deleting photo: " + req.status);
                }
            }
        });
    });

    // GESTIONE RIORDINO ("Modify Order" => drag & drop => "Save Order")
    modifyOrderBtn.addEventListener("click", function() {
        reorderList.innerHTML = "";
        currentAlbumImages.forEach(img => {
            let li = document.createElement("li");
            li.textContent = img.titolo;
            li.dataset.imageId = img.id_immagine;

            li.draggable = true;
            li.addEventListener("dragstart", handleDragStart);
            li.addEventListener("dragover", handleDragOver);
            li.addEventListener("dragleave", handleDragLeave);
            li.addEventListener("drop", handleDrop);
            li.addEventListener("dragend", handleDragEnd);

            reorderList.appendChild(li);
        });

        reorderSection.style.display = "block";
    });

    function handleDragStart(e) {
        dragSrcEl = this;
        e.dataTransfer.effectAllowed = "move";
        e.dataTransfer.setData("text/plain", this.dataset.imageId);
        this.style.opacity = "0.4";
    }
    function handleDragOver(e) {
        e.preventDefault();
        return false;
    }
    function handleDrop(e) {
        e.stopPropagation();
        if (dragSrcEl !== this) {
            let list = this.parentNode;
            list.insertBefore(dragSrcEl, this);
        }
        return false;
    }
    function handleDragLeave(e) {
        this.classList.remove("drag-over");
    }
    function handleDragEnd(e) {
        this.style.opacity = "1";
        let items = reorderList.querySelectorAll("li");
        items.forEach(item => item.classList.remove("drag-over"));
    }

    saveOrderBtn.addEventListener("click", function() {
        let liElements = reorderList.querySelectorAll("li");
        let orderArray = [];
        liElements.forEach(li => {
            orderArray.push(li.dataset.imageId);
        });
        let orderStr = orderArray.join(",");
        let url = "UpdateOrder?albumId=" + currentAlbumId + "&order=" + orderStr;

        makeCall("GET", url, null, function(req) {
            if (req.readyState === XMLHttpRequest.DONE) {
                if (req.status === 200) {
                    makeCall("GET", "GetAlbums", null, function(r2) {
                        if (r2.readyState === XMLHttpRequest.DONE) {
                            if (r2.status === 200) {
                                myAlbums = JSON.parse(r2.responseText);
                                sessionStorage.setItem("myAlbums", JSON.stringify(myAlbums));
                                makeCall("GET", "GetImages", null, function(r3) {
                                    if (r3.readyState === XMLHttpRequest.DONE) {
                                        if (r3.status === 200) {
                                            myImages = JSON.parse(r3.responseText);
                                            sessionStorage.setItem("myImages", JSON.stringify(myImages));
                                            populateImages(myImages);

                                            let foundAlbum = myAlbums.find(a => a.id_album == currentAlbumId);
                                            if (foundAlbum) {
                                                showAlbumImages(foundAlbum);
                                            }
                                        } else {
                                            alert("Error reloading images: " + r3.status);
                                        }
                                    }
                                });
                            } else {
                                alert("Error reloading albums: " + r2.status);
                            }
                        }
                    });
                } else if (req.status === 401) {
                    alert("Unauthorized, please log in again.");
                } else {
                    alert("Error updating order: " + req.status);
                }
            }
        });
    });
});

