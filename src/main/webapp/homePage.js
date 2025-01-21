// Carica il nome utente
document.getElementById("username").textContent = sessionStorage.getItem("currentUserUsername"); // Sostituire dinamicamente

// Logout
document.getElementById('logoutButton').addEventListener('click', () => {
    fetch('/Logout', { method: 'POST' })
        .then(response => {
            if (response.ok) window.location.href = '/login.html';
        });
});

// Upload Image
document.getElementById('uploadForm').addEventListener('submit', (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);

    fetch('/UploadPhoto', { method: 'POST', body: formData })
        .then(response => {
            if (response.ok) {
                alert('Image uploaded successfully!');
                loadImages(); // Aggiorna la galleria
            } else {
                alert('Error uploading image.');
            }
        });
});

// Load Images
function loadImages() {
    fetch('/GetImages')
        .then(response => response.json())
        .then(images => {
            const gallery = document.getElementById('imageGallery');
            gallery.innerHTML = '';
            images.forEach(image => {
                gallery.innerHTML += `
                    <div class="image-item">
                        <img src="/DownloadPhoto?imageId=${image.id}" alt="${image.title}">
                        <p>${image.title}</p>
                    </div>`;
            });
        });
}

// Create Album
document.getElementById('createAlbumForm').addEventListener('submit', (event) => {
    event.preventDefault();
    const albumTitle = document.getElementById('albumTitle').value;

    fetch('/CreateAlbum', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: albumTitle })
    })
        .then(response => {
            if (response.ok) {
                alert('Album created successfully!');
                loadAlbums(); // Aggiorna la tabella degli album
            } else {
                alert('Error creating album.');
            }
        });
});

// Load Albums
function loadAlbums() {
    fetch('/GetAlbums')
        .then(response => response.json())
        .then(albums => {
            const tableBody = document.getElementById('albumsTable').querySelector('tbody');
            tableBody.innerHTML = '';
            albums.forEach(album => {
                tableBody.innerHTML += `
                    <tr>
                        <td>${album.id}</td>
                        <td>${album.title}</td>
                        <td>${album.creator}</td>
                        <td>${album.creationDate}</td>
                    </tr>`;
            });
        });
}

// Initialize Page
document.addEventListener('DOMContentLoaded', () => {
    loadImages();
    loadAlbums();
    document.getElementById('username').textContent = 'User'; // Sostituire con username dinamico
});

