// ── Session helpers ───────────────────────────────────────────────────────────

function saveSession(user) {
  localStorage.setItem('sb_user', JSON.stringify(user));
}

function getSession() {
  try { return JSON.parse(localStorage.getItem('sb_user')); } catch { return null; }
}

function clearSession() {
  localStorage.removeItem('sb_user');
}

function isLoggedIn() {
  return !!getSession();
}

function isAdmin() {
  const u = getSession();
  return u && u.role && u.role.name === 'ADMIN';
}

function isCustomer() {
  const u = getSession();
  return u && u.role && u.role.name === 'CUSTOMER';
}

function currentUserId() {
  const u = getSession();
  return u ? u.id : null;
}

function currentUserName() {
  const u = getSession();
  return u ? u.name : '';
}

// ── Route guard ───────────────────────────────────────────────────────────────
// Call at top of every protected page.
// adminOnly = true  → redirect non-admins to user-dashboard
function requireAuth(adminOnly = false) {
  if (!isLoggedIn()) {
    window.location.href = 'login.html';
    return false;
  }
  if (adminOnly && !isAdmin()) {
    window.location.href = 'user-dashboard.html';
    return false;
  }
  return true;
}

// ── Logout ────────────────────────────────────────────────────────────────────
function logout() {
  clearSession();
  window.location.href = 'login.html';
}

// ── Sidebar builder ───────────────────────────────────────────────────────────
// Injects the correct sidebar + topbar user info into every page.
function buildSidebar(activePage) {
  const user = getSession();
  if (!user) return;

  const admin = isAdmin();

  const adminLinks = `
    <a href="index.html"        class="nav-item ${activePage==='dashboard'?'active':''}"><span class="icon">🏠</span> Dashboard</a>
    <a href="users.html"        class="nav-item ${activePage==='users'?'active':''}"><span class="icon">👥</span> Users</a>
    <a href="services.html"     class="nav-item ${activePage==='services'?'active':''}"><span class="icon">🛠️</span> Services</a>
    <a href="categories.html"   class="nav-item ${activePage==='categories'?'active':''}"><span class="icon">🏷️</span> Categories</a>
    <a href="appointments.html" class="nav-item ${activePage==='appointments'?'active':''}"><span class="icon">📋</span> Appointments</a>
    <a href="locations.html"    class="nav-item ${activePage==='locations'?'active':''}"><span class="icon">📍</span> Locations</a>`;

  const userLinks = `
    <a href="user-dashboard.html"    class="nav-item ${activePage==='dashboard'?'active':''}"><span class="icon">🏠</span> Home</a>
    <a href="user-appointments.html" class="nav-item ${activePage==='my-appointments'?'active':''}"><span class="icon">📋</span> My Appointments</a>
    <a href="user-services.html"     class="nav-item ${activePage==='browse-services'?'active':''}"><span class="icon">🛠️</span> Browse Services</a>`;

  const sidebar = document.getElementById('sidebar');
  if (sidebar) {
    sidebar.innerHTML = `
      <div class="sidebar-brand">
        <h2>📅 ServiceBook</h2>
        <p>${admin ? 'Admin Panel' : 'My Account'}</p>
      </div>
      <nav class="sidebar-nav">${admin ? adminLinks : userLinks}</nav>
      <div class="sidebar-footer">
        <div class="sidebar-user">
          <div class="su-avatar">${user.name.charAt(0).toUpperCase()}</div>
          <div class="su-info">
            <strong>${user.name}</strong>
            <span>${user.role?.name || ''}</span>
          </div>
        </div>
        <button class="btn-logout" onclick="logout()">⏻ Logout</button>
      </div>`;
  }

  // Topbar user chip
  const topbarUser = document.getElementById('topbar-user');
  if (topbarUser) {
    topbarUser.innerHTML = `
      <span class="role-chip ${admin?'chip-admin':'chip-user'}">${user.role?.name || 'USER'}</span>
      <span class="topbar-name">${user.name}</span>`;
  }
}
