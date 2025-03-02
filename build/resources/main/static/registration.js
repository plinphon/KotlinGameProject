import { showToast } from './script.js';

document.getElementById('registrationForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const name = document.getElementById('name').value;
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        // Register the user
        const registerResponse = await fetch('/api/accounts', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ name, username, password })
        });

        if (!registerResponse.ok) {
            const error = await registerResponse.text();
            throw new Error(error || 'Registration failed');
        }

        // Automatically log in the user
        const loginResponse = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });

        if (!loginResponse.ok) {
            throw new Error('Auto-login failed after registration');
        }

        const { token } = await loginResponse.json();
        
        // Store the token
        localStorage.setItem('token', token);
        
        // Redirect to main page
        window.location.href = '/';
        
    } catch (error) {
        console.error('Registration error:', error);
        showToast(error.message, true);
    }
}); 