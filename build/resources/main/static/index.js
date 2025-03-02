import CharactersTab from './js/characters.js';
import MatchesTab from './js/matches.js';
import LeaderboardTab from './js/leaderboard.js';
import { checkAuth, showToast, fetchWithAuth } from './script.js';

// Add function to load user details
async function loadUserAccount() {
    try {
        const account = await fetchWithAuth('/api/accounts');
        const userInfo = document.getElementById('userInfo');
        if (userInfo && account) {
            userInfo.className = 'user-info-container';
            userInfo.innerHTML = `
                <span class="user-name">
                    <i class="fas fa-user-astronaut"></i>
                    ${account.name}
                </span>
                <button onclick="handleLogout()" class="btn btn-cosmic-outline">
                    <i class="fas fa-door-open"></i>
                    Exit Portal
                </button>
            `;
        }
    } catch (error) {
        console.error('Error loading user account:', error);
    }
}

// Initialize tabs when DOM is loaded
document.addEventListener('DOMContentLoaded', async () => {
    if (!checkAuth()) return;

    try {
        // Load user account first
        await loadUserAccount();

        // Initialize all tabs
        const charactersTab = new CharactersTab();
        const matchesTab = new MatchesTab();
        const leaderboardTab = new LeaderboardTab();

        // Show characters tab by default
        const charactersTabEl = document.getElementById('characters-tab');
        const tab = new bootstrap.Tab(charactersTabEl);
        tab.show();
    } catch (error) {
        console.error('Error initializing application:', error);
        showToast('Failed to initialize application', true);
    }
}); 