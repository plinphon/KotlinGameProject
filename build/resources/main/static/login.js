import { resetUserContext } from './script.js';

// Reset user context when login page loads
document.addEventListener('DOMContentLoaded', () => {
    resetUserContext();
    
    // Check for auth error parameter
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('authError')) {
        showLoginError('Your session has expired. Please log in again.');
    }
});

window.addEventListener('unhandledrejection', event => {
    if (event.reason?.status === 401) {
        // Clear auth token and redirect to login with error flag
        window.sessionStorage.removeItem('auth');
        window.location.href = '/login?authError=true';
    }
});

document.getElementById('loginForm').addEventListener('submit', async (event) => {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const authToken = btoa(`${username}:${password}`);

    try {
        // Store auth token in session storage
        window.sessionStorage.setItem('auth', authToken);

        // Add loading state to button
        const submitButton = event.target.querySelector('button[type="submit"]');
        submitButton.disabled = true;
        submitButton.innerHTML = `
            <i class="fas fa-spinner fa-spin"></i> Entering the Cosmos...
        `;

        // Redirect to main page
        window.location.href = '/';
    } catch (error) {
        console.error('Login error:', error);
        showError('Failed to connect to server');
    }
});

function showError(message) {
    const form = document.getElementById('loginForm');

    // Remove any existing error messages
    const existingError = form.querySelector('.login-error');
    if (existingError) {
        existingError.remove();
    }

    // Create and add new error message
    const errorDiv = document.createElement('div');
    errorDiv.className = 'login-error mt-3 text-center';
    errorDiv.innerHTML = `
        <i class="fas fa-exclamation-circle"></i>
        <span>${message}</span>
    `;
    form.appendChild(errorDiv);
}
