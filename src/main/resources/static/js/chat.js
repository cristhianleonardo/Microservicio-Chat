let stompClient = null;
let currentUser = null;
let currentRoom = null;
let isOwner = false;

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    const loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
    loginModal.show();

    // Manejar el formulario de inicio de sesión
    document.getElementById('loginForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const roomId = document.getElementById('roomId').value;
        
        currentUser = username;
        currentRoom = roomId;
        
        connectWebSocket();
        loginModal.hide();
    });

    // Manejar el envío de mensajes
    document.getElementById('messageForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const messageInput = document.getElementById('message');
        const message = messageInput.value.trim();
        
        if (message) {
            sendMessage(message);
            messageInput.value = '';
        }
    });

    // Manejar el botón de control de escritura
    document.getElementById('toggleWriteBtn').addEventListener('click', function() {
        if (isOwner) {
            toggleWritePermission();
        }
    });
});

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        
        // Suscribirse al tópico de la sala
        stompClient.subscribe('/topic/' + currentRoom, function(message) {
            const chatMessage = JSON.parse(message.body);
            displayMessage(chatMessage);
        });

        // Enviar mensaje de unión
        stompClient.send("/app/chat.addUser", {}, JSON.stringify({
            senderId: currentUser,
            roomId: currentRoom,
            type: 'JOIN'
        }));

        // Cargar mensajes anteriores
        loadPreviousMessages();
    });
}

function sendMessage(content) {
    if (stompClient) {
        const chatMessage = {
            senderId: currentUser,
            roomId: currentRoom,
            content: content,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
    }
}

function displayMessage(message) {
    const messageArea = document.getElementById('messageArea');
    const messageElement = document.createElement('div');
    
    messageElement.classList.add('message');
    
    switch(message.type) {
        case 'CHAT':
            messageElement.classList.add(message.senderId === currentUser ? 'sent' : 'received');
            messageElement.innerHTML = `
                <strong>${message.senderId}</strong>
                <p class="mb-0">${message.content}</p>
                <small class="text-muted">${new Date(message.timestamp).toLocaleTimeString()}</small>
            `;
            break;
        case 'JOIN':
        case 'LEAVE':
        case 'SYSTEM':
            messageElement.classList.add('system');
            messageElement.textContent = message.content;
            break;
    }
    
    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function loadPreviousMessages() {
    fetch(`/api/chat/room/${currentRoom}/messages`)
        .then(response => response.json())
        .then(messages => {
            messages.forEach(message => displayMessage(message));
        })
        .catch(error => console.error('Error loading messages:', error));
}

function toggleWritePermission() {
    fetch(`/api/chat/room/${currentRoom}/toggleWrite?ownerId=${currentUser}`, {
        method: 'PUT'
    })
    .then(response => response.json())
    .then(room => {
        const button = document.getElementById('toggleWriteBtn');
        if (room.onlyOwnerCanWrite) {
            button.innerHTML = '<i class="fas fa-lock"></i> Solo Propietario';
            button.classList.add('btn-warning');
        } else {
            button.innerHTML = '<i class="fas fa-lock-open"></i> Todos';
            button.classList.remove('btn-warning');
        }
    })
    .catch(error => console.error('Error toggling write permission:', error));
}

// Manejar la desconexión cuando se cierra la ventana
window.addEventListener('beforeunload', function() {
    if (stompClient) {
        stompClient.disconnect();
    }
}); 