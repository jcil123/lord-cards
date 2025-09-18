let ws = null;
let playerName = "";
let currentGameId = null;

function showPage(pageId) {
  document.querySelectorAll(".page").forEach(p => p.classList.remove("active"));
  document.getElementById(pageId).classList.add("active");
}

// --- Login actions ---
function createGame() {
  playerName = document.getElementById("playerName").value.trim();
  if (!playerName) { alert("Enter a name!"); return; }

  // Connect to create-game endpoint; server should create game and reply with id
  connect(`/lords/game?name=${encodeURIComponent(playerName)}`, true);
}

function joinGame() {
  playerName = document.getElementById("playerName").value.trim();
  const gameId = document.getElementById("gameId").value.trim();
  if (!playerName) { alert("Enter a name!"); return; }
  if (!gameId) { alert("Enter a game id to join!"); return; }

  // Connect to join-game endpoint with id & name
  connect(`/lords/game/${encodeURIComponent(gameId)}?name=${encodeURIComponent(playerName)}`, false);
}

// --- Waiting room + chat ---
function connect(path, expectServerToCreateGame = false) {
  // Close previous socket if any
  if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
    ws.close();
  }

  // Build base URL from the current origin (works when index is served from the same host).
  // If you're using a tunneling service (zrok) and sending the HTML file directly,
  // replace baseUrl with the full wss://<your-public-host>
  const baseUrl = (location.protocol === "https:" ? "wss://" : "ws://") + location.host;
  const url = baseUrl + path;

  ws = new WebSocket(url);

  ws.onopen = () => {
    console.log("✅ Connected as " + playerName);
    showPage("waitingPage");
  };

  ws.onmessage = (event) => {
    const chatBox = document.getElementById("chatBox");
    // If server sends a message like "Game created with id: <id>" capture it:
    const text = event.data;
    chatBox.innerHTML += `<div>${escapeHtml(text)}</div>`;
    chatBox.scrollTop = chatBox.scrollHeight;

    if (expectServerToCreateGame) {
      // attempt to parse an ID in a common "Game created with id: <id>" format
      const m = text.match(/id[: ]+([A-Za-z0-9_-]{4,})/i);
      if (m) {
        currentGameId = m[1];
        // optionally show the ID to user (alert or copy to clipboard)
        alert(`Game created: ${currentGameId} (share this id with others)`);
      }
    }
  };

  ws.onclose = () => {
    console.log("🔌 Disconnected");
    showPage("loginPage");
  };

  ws.onerror = (err) => {
    console.error("❌ WebSocket error:", err);
    // show an error in UI
    const chatBox = document.getElementById("chatBox");
    chatBox.innerHTML += `<div style="color:red">WebSocket error</div>`;
  };
}

function sendChat() {
  const input = document.getElementById("chatInput");
  const message = input.value.trim();
  if (message && ws && ws.readyState === WebSocket.OPEN) {
    // send plain text chat; you can switch to JSON later
    ws.send(`${playerName}: ${message}`);
    input.value = "";
  }
}

function backToLogin() {
  if (ws) ws.close();
  showPage("loginPage");
}

// tiny helper to avoid XSS in chat display
function escapeHtml(s) {
  return s
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}
