const API_BASE = 'http://localhost:8081/api';

async function apiFetch(url, options = {}) {
  try {
    const res = await fetch(API_BASE + url, {
      headers: { 'Content-Type': 'application/json', ...options.headers },
      ...options
    });
    const text = await res.text();
    let data;
    try { data = JSON.parse(text); } catch { data = text; }
    return { ok: res.ok, status: res.status, data };
  } catch (e) {
    return { ok: false, status: 0, data: 'Network error: ' + e.message };
  }
}

function showAlert(el, message, type = 'success') {
  el.className = `alert alert-${type} show`;
  el.textContent = message;
  setTimeout(() => el.classList.remove('show'), 4000);
}

function formatDateTime(dt) {
  if (!dt) return '—';
  return new Date(dt).toLocaleString();
}

function statusBadge(status) {
  const s = (status || 'pending').toLowerCase();
  return `<span class="status status-${s}">${status || 'PENDING'}</span>`;
}

function confirmDelete(name) {
  return confirm(`Delete "${name}"? This cannot be undone.`);
}
