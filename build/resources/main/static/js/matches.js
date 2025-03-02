import { showToast, fetchWithAuth, CHARACTER_CLASSES, COMMON_DISPLAY_PROPERTIES, CLASS_SPECIFIC_PROPERTIES } from '../script.js';
import { formatLevel } from './characters.js';

class MatchesTab {
    constructor() {
        this.matchModal = null;
        this.matchResultModal = null;
        this.characters = [];
        this.initialize();
    }

    initialize() {
        console.log('Initializing matches tab...');

        // Initialize modals
        this.matchModal = new bootstrap.Modal(document.getElementById('newMatchModal'));
        this.matchResultModal = new bootstrap.Modal(document.getElementById('matchResultModal'));

        // Add event listeners
        document.getElementById('matches-tab').addEventListener('shown.bs.tab', () => {
            console.log('Matches tab shown, loading matches...');
            this.loadMatches();
            this.loadCharacters(); // Load characters when tab is shown
        });

        // Add event listener for new match modal shown
        document.getElementById('newMatchModal').addEventListener('show.bs.modal', () => {
            this.loadCharacters(); // Reload characters when modal is opened
        });

        document.getElementById('randomMatchButton').addEventListener('click', this.handleRandomMatch.bind(this));
        document.getElementById('fightButton').addEventListener('click', this.handleFight.bind(this));

        // Add change listeners for character selects
        document.getElementById('challengerSelect').addEventListener('change', this.updateFightButton.bind(this));
        document.getElementById('opponentSelect').addEventListener('change', this.updateFightButton.bind(this));
    }

    async loadCharacters() {
        try {
            // Load challengers
            const challengers = await fetchWithAuth('/api/characters/challengers');

            // Load opponents
            const opponents = await fetchWithAuth('/api/characters/opponents');

            // Store characters and populate selects
            this.characters = {
                challengers: challengers,
                opponents: opponents
            };

            this.populateCharacterSelects();
        } catch (error) {
            console.error('Error loading characters:', error);
            showToast(error.message, true);
        }
    }

    populateCharacterSelects() {
        const challengerSelect = document.getElementById('challengerSelect');
        const opponentSelect = document.getElementById('opponentSelect');

        // Add the select classes
        challengerSelect.className = 'character-select';
        opponentSelect.className = 'character-select';

        // Clear existing options except the first one
        challengerSelect.innerHTML = '<option value="">Choose your challenger...</option>';
        opponentSelect.innerHTML = '<option value="">Choose your opponent...</option>';

        // Add challenger options
        this.characters.challengers.forEach(character => {
            const option = document.createElement('option');
            option.value = character.id;
            option.dataset.class = character.characterClass;
            const icon = character.characterClass === 'WARRIOR' ? '⚔️' : '🔮';
            option.textContent = `${icon} ${character.name} (${formatLevel(character.level)})`;
            challengerSelect.appendChild(option.cloneNode(true));
        });

        // Add opponent options
        this.characters.opponents.forEach(character => {
            const option = document.createElement('option');
            option.value = character.id;
            option.dataset.class = character.characterClass;
            const icon = character.characterClass === 'WARRIOR' ? '⚔️' : '🔮';
            option.textContent = `${icon} ${character.name} (${formatLevel(character.level)})`;
            opponentSelect.appendChild(option);
        });
    }

    updateFightButton() {
        const challengerSelect = document.getElementById('challengerSelect');
        const opponentSelect = document.getElementById('opponentSelect');
        const fightButton = document.getElementById('fightButton');

        const challengerId = challengerSelect.value;
        const opponentId = opponentSelect.value;

        // Disable button if no selection or same character selected
        fightButton.disabled = !challengerId || !opponentId || challengerId === opponentId;

        // Update character stats displays
        if (challengerId) {
            const challenger = this.characters.challengers.find(c => c.id === challengerId);
            if (challenger) {
                this.displayCharacterStats('challenger', challenger);
                challengerSelect.dataset.class = challenger.characterClass;
            }
        } else {
            delete challengerSelect.dataset.class;
        }

        if (opponentId) {
            const opponent = this.characters.opponents.find(c => c.id === opponentId);
            if (opponent) {
                this.displayCharacterStats('opponent', opponent);
                opponentSelect.dataset.class = opponent.characterClass;
            }
        } else {
            delete opponentSelect.dataset.class;
        }

        // Update fight button text
        if (!challengerId || !opponentId) {
            fightButton.innerHTML = '<i class="fas fa-swords"></i> Select Characters';
        } else if (challengerId === opponentId) {
            fightButton.innerHTML = '<i class="fas fa-swords"></i> Select Different Characters';
        } else {
            const challenger = this.characters.challengers.find(c => c.id === challengerId);
            const opponent = this.characters.opponents.find(c => c.id === opponentId);
            if (challenger && opponent) {
                fightButton.innerHTML = `
                    <i class="fas fa-swords"></i>
                    ${challenger.name} vs ${opponent.name}
                `;
            }
        }
    }

    displayCharacterStats(type, character) {
        if (!character) return;

        const statsContainer = document.getElementById(`${type}Stats`);
        statsContainer.innerHTML = '';

        // Add common properties
        COMMON_DISPLAY_PROPERTIES.forEach(prop => {
            if (character[prop] !== undefined) {
                const propertyItem = this.createPropertyElement(prop, character[prop]);
                statsContainer.appendChild(propertyItem);
            }
        });

        // Add class-specific properties
        const classSpecificProps = CLASS_SPECIFIC_PROPERTIES[character.characterClass] || [];
        classSpecificProps.forEach(prop => {
            if (character[prop] !== undefined) {
                const propertyItem = this.createPropertyElement(prop, character[prop]);
                statsContainer.appendChild(propertyItem);
            }
        });
    }

    createPropertyElement(prop, value) {
        const propertyItem = document.createElement('div');
        propertyItem.className = 'property-item';
        propertyItem.dataset.property = prop;

        const label = document.createElement('div');
        label.className = 'property-label';
        label.textContent = this.formatPropertyName(prop);

        const valueElement = document.createElement('div');
        valueElement.className = 'property-value';
        valueElement.textContent = value;

        propertyItem.appendChild(label);
        propertyItem.appendChild(valueElement);

        return propertyItem;
    }

    formatPropertyName(prop) {
        return prop
            .replace(/([A-Z])/g, ' $1')
            .replace(/^./, str => str.toUpperCase());
    }

    async loadMatches() {
        console.log('Loading matches...');
        const listContainer = document.getElementById('matchesList');
        try {
            listContainer.innerHTML = '<p class="text-muted text-center">Loading matches...</p>';

            const matches = await fetchWithAuth('/api/matches');

            this.displayMatches(matches);
        } catch (error) {
            console.error('Error loading matches:', error);
            listContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-exclamation-circle fa-3x"></i>
                    <p>Failed to load matches</p>
                    <small>${error.message}</small>
                </div>
            `;
            showToast(error.message, true);
        }
    }

    displayMatches(matches) {
        const matchesList = document.getElementById('matchesList');
        
        if (!matches || matches.length === 0) {
            matchesList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-sword-cross fa-3x"></i>
                    <p>No matches yet</p>
                    <small>Start a new match to begin your journey!</small>
                </div>
            `;
            return;
        }

        matchesList.innerHTML = `
            <div class="table-responsive">
                <table class="table leaderboard-table">
                    <thead class="cosmic-header">
                        <tr>
                            <th scope="col" style="width: 300px">Challenger</th>
                            <th scope="col" class="text-center" colspan="3">Battle</th>
                            <th scope="col" style="width: 300px">Opponent</th>
                            <th scope="col" class="text-center" style="width: 100px">Rounds</th>
                        </tr>
                    </thead>
                    <tbody class="cosmic-body">
                        ${matches.map(match => {
                            // Determine row class based on match outcome
                            const rowClass = match.matchOutcome === 'DRAW' 
                                ? 'DRAW'
                                : match.matchOutcome === 'CHALLENGER_WON'
                                    ? match.challenger.characterClass
                                    : match.opponent.characterClass;
                            
                            // Determine battle outcome icons
                            let leftIcon = '', middleIcon = '', rightIcon = '';
                            switch (match.matchOutcome) {
                                case 'DRAW':
                                    leftIcon = '<span class="battle-icon"></span>';
                                    middleIcon = '<i class="fas fa-handshake text-warning"></i>';
                                    rightIcon = '<span class="battle-icon"></span>';
                                    break;
                                case 'CHALLENGER_WON':
                                    leftIcon = '<i class="fas fa-trophy text-success"></i>';
                                    middleIcon = '';
                                    rightIcon = '<i class="fas fa-skull text-danger"></i>';
                                    break;
                                case 'OPPONENT_WON':
                                    leftIcon = '<i class="fas fa-skull text-danger"></i>';
                                    middleIcon = '';
                                    rightIcon = '<i class="fas fa-trophy text-success"></i>';
                                    break;
                            }

                            return `
                                <tr class="cosmic-row match-row" data-class="${rowClass}" style="cursor: pointer;">
                                    <td>
                                        <div class="d-flex align-items-center justify-content-between">
                                            <div class="d-flex align-items-center gap-2">
                                                <span class="class-icon" title="${match.challenger.characterClass === 'WARRIOR' ? 'Warrior' : 'Sorcerer'}">
                                                    ${match.challenger.characterClass === 'WARRIOR' ? '⚔️' : '🔮'}
                                                </span>
                                                <span>${match.challenger.name}</span>
                                            </div>
                                            <span class="experience-gained">+${match.challenger.experienceGained}<small>xp</small></span>
                                        </div>
                                    </td>
                                    <td class="text-center battle-column">
                                        <span class="battle-icon">${leftIcon}</span>
                                    </td>
                                    <td class="text-center battle-column">
                                        <span class="battle-icon">${middleIcon}</span>
                                    </td>
                                    <td class="text-center battle-column">
                                        <span class="battle-icon">${rightIcon}</span>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center justify-content-between">
                                            <div class="d-flex align-items-center gap-2">
                                                <span class="class-icon" title="${match.opponent.characterClass === 'WARRIOR' ? 'Warrior' : 'Sorcerer'}">
                                                    ${match.opponent.characterClass === 'WARRIOR' ? '⚔️' : '🔮'}
                                                </span>
                                                <span>${match.opponent.name}</span>
                                            </div>
                                            <span class="experience-gained">+${match.opponent.experienceGained}<small>xp</small></span>
                                        </div>
                                    </td>
                                    <td class="text-center">
                                        ${match.rounds.length}
                                    </td>
                                </tr>
                            `;
                        }).join('')}
                    </tbody>
                </table>
            </div>
        `;

        // Add click handlers for match details
        const matchRows = matchesList.querySelectorAll('.match-row');
        matchRows.forEach((row, index) => {
            row.addEventListener('click', () => this.displayMatchResult(matches[index]));
        });

        // Initialize tooltips
        const tooltips = matchesList.querySelectorAll('[title]');
        tooltips.forEach(el => new bootstrap.Tooltip(el));
    }

    async handleFight(event) {
        event.preventDefault();
        const challengerId = document.getElementById('challengerSelect').value;
        const opponentId = document.getElementById('opponentSelect').value;
        const fightButton = document.getElementById('fightButton');

        if (!challengerId || !opponentId) return;
        const rounds = parseInt(document.getElementById('roundsSelect').value) || 20;

        try {
            fightButton.disabled = true;
            fightButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Fighting...';

            const match = await fetchWithAuth('/api/matches', {
                method: 'POST',
                body: JSON.stringify({
                    challengerId: challengerId,
                    opponentId: opponentId,
                    rounds: rounds
                })
            });

            // Hide the new match modal
            this.matchModal.hide();

            // Show the match result directly from the response
            this.displayMatchResult(match);

            // Reload the matches list
            await this.loadMatches();

            showToast('Match completed successfully!');
        } catch (error) {
            console.error('Error creating match:', error);
            showToast(error.message, true);
        } finally {
            fightButton.disabled = false;
            fightButton.innerHTML = '<i class="fas fa-swords"></i> Fight!';
        }
    }

    async handleRandomMatch() {
        // Check if we have enough characters
        if (!this.characters.challengers.length || !this.characters.opponents.length) {
            showToast('Not enough characters available for a random match', true);
            return;
        }

        // Get random challenger and opponent
        const challenger = this.characters.challengers[Math.floor(Math.random() * this.characters.challengers.length)];

        // Filter out the challenger from opponents if it exists there
        const availableOpponents = this.characters.opponents.filter(c => c.id !== challenger.id);

        if (!availableOpponents.length) {
            showToast('No available opponents for this challenger', true);
            return;
        }

        const opponent = availableOpponents[Math.floor(Math.random() * availableOpponents.length)];

        // Set the select values
        document.getElementById('challengerSelect').value = challenger.id;
        document.getElementById('opponentSelect').value = opponent.id;

        // Update the fight button and stats
        this.updateFightButton();
    }

    displayMatchResult(match) {
        const modal = document.getElementById('matchResultModal');

        const getBadgeClass = (isChallenger) => {
            switch (match.matchOutcome) {
                case 'DRAW': return 'bg-warning';
                case 'CHALLENGER_WON': return isChallenger ? 'bg-success' : 'bg-danger';
                case 'OPPONENT_WON': return isChallenger ? 'bg-danger' : 'bg-success';
                default: return 'bg-secondary';
            }
        };

        const getResultText = (isChallenger) => {
            switch (match.matchOutcome) {
                case 'DRAW': return '<i class="fas fa-handshake"></i> Draw';
                case 'CHALLENGER_WON': return isChallenger 
                    ? '<i class="fas fa-trophy"></i> Victor'
                    : '<i class="fas fa-skull"></i> Defeated';
                case 'OPPONENT_WON': return isChallenger
                    ? '<i class="fas fa-skull"></i> Defeated'
                    : '<i class="fas fa-trophy"></i> Victor';
                default: return 'Unknown';
            }
        };

        modal.querySelector('.modal-body').innerHTML = `
            <div class="match-content">
                <div class="match-summary mb-4">
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="challenger" data-class="${match.challenger.characterClass}">
                            <div class="d-flex align-items-center gap-2 mb-1">
                                <span class="class-icon" title="${match.challenger.characterClass === 'WARRIOR' ? 'Warrior' : 'Sorcerer'}">
                                    ${match.challenger.characterClass === 'WARRIOR' ? '⚔️' : '🔮'}
                                </span>
                                <h5 class="mb-0">${match.challenger.name}</h5>
                            </div>
                            <div class="badge ${getBadgeClass(true)}">
                                ${getResultText(true)}
                            </div>
                            <div class="experience-info">
                                <div class="d-flex align-items-center gap-2">
                                    <span title="Total Experience">
                                        <i class="fas fa-star"></i> ${match.challenger.experienceTotal}
                                    </span>
                                    <span class="text-success" title="Experience Gained">
                                        <i class="fas fa-plus"></i>${match.challenger.experienceGained}<small>xp</small>
                                    </span>
                                </div>
                            </div>
                        </div>
                        
                        <div class="match-vs">
                            <span>${match.rounds.length}</span>
                        </div>
                        
                        <div class="opponent" data-class="${match.opponent.characterClass}">
                            <div class="d-flex align-items-center gap-2 mb-1">
                                <span class="class-icon" title="${match.opponent.characterClass === 'WARRIOR' ? 'Warrior' : 'Sorcerer'}">
                                    ${match.opponent.characterClass === 'WARRIOR' ? '⚔️' : '🔮'}
                                </span>
                                <h5 class="mb-0">${match.opponent.name}</h5>
                            </div>
                            <div class="badge ${getBadgeClass(false)}">
                                ${getResultText(false)}
                            </div>
                            <div class="experience-info">
                                <div class="d-flex align-items-center gap-2">
                                    <span title="Total Experience">
                                        <i class="fas fa-star"></i> ${match.opponent.experienceTotal}
                                    </span>
                                    <span class="text-success" title="Experience Gained">
                                        <i class="fas fa-plus"></i>${match.opponent.experienceGained}<small>xp</small>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="rounds-timeline">
                    <h6 class="mb-3">Battle Timeline</h6>
                    <div class="rounds-list"></div>
                </div>
            </div>

            <div class="match-footer">
                <button type="button" class="btn btn-cosmic" data-bs-dismiss="modal">
                    <i class="fas fa-portal-exit"></i> Return to the Cosmic Arena
                </button>
            </div>
        `;

        // Initialize tooltips in the modal
        const tooltips = modal.querySelectorAll('[title]');
        tooltips.forEach(el => new bootstrap.Tooltip(el));

        // Display rounds if they exist
        const roundsList = modal.querySelector('.rounds-list');
        if (match.rounds && Array.isArray(match.rounds)) {
            // Group rounds by round number
            const roundsMap = match.rounds.reduce((acc, round) => {
                if (!acc[round.round]) {
                    acc[round.round] = {};
                }
                const character = round.characterId === match.challenger.id ? 'challenger' : 'opponent';
                acc[round.round][character] = round;
                return acc;
            }, {});

            roundsList.innerHTML = Object.entries(roundsMap).map(([roundNum, roundData]) => `
                <div class="round-item">
                    <div class="round-header">Round ${roundNum}</div>
                    <div class="round-content d-flex justify-content-between align-items-center gap-4">
                        <div class="challenger-stats flex-grow-1">
                            ${roundData.challenger ? `
                                <div class="stats-row d-flex justify-content-between">
                                    <div class="stat-item ${roundData.challenger.healthDelta < 0 ? 'negative' : 'positive'}">
                                        <span class="stat-label">❤️</span>
                                        <span class="stat-value">${roundData.challenger.healthDelta >= 0 ? '+' : ''}${roundData.challenger.healthDelta}</span>
                                    </div>
                                    <div class="stat-item ${roundData.challenger.staminaDelta < 0 ? 'negative' : 'positive'}">
                                        <span class="stat-label">⚡</span>
                                        <span class="stat-value">${roundData.challenger.staminaDelta >= 0 ? '+' : ''}${roundData.challenger.staminaDelta}</span>
                                    </div>
                                    <div class="stat-item ${roundData.challenger.manaDelta < 0 ? 'negative' : 'positive'}">
                                        <span class="stat-label">✨</span>
                                        <span class="stat-value">${roundData.challenger.manaDelta >= 0 ? '+' : ''}${roundData.challenger.manaDelta}</span>
                                    </div>
                                </div>
                            ` : '<div class="no-action">No action</div>'}
                        </div>

                        <div class="round-vs">VS</div>

                        <div class="opponent-stats flex-grow-1">
                            ${roundData.opponent ? `
                                <div class="stats-row d-flex justify-content-between">
                                    <div class="stat-item ${roundData.opponent.healthDelta < 0 ? 'negative' : 'positive'}">
                                        <span class="stat-label">❤️</span>
                                        <span class="stat-value">${roundData.opponent.healthDelta >= 0 ? '+' : ''}${roundData.opponent.healthDelta}</span>
                                    </div>
                                    <div class="stat-item ${roundData.opponent.staminaDelta < 0 ? 'negative' : 'positive'}">
                                        <span class="stat-label">⚡</span>
                                        <span class="stat-value">${roundData.opponent.staminaDelta >= 0 ? '+' : ''}${roundData.opponent.staminaDelta}</span>
                                    </div>
                                    <div class="stat-item ${roundData.opponent.manaDelta < 0 ? 'negative' : 'positive'}">
                                        <span class="stat-label">✨</span>
                                        <span class="stat-value">${roundData.opponent.manaDelta >= 0 ? '+' : ''}${roundData.opponent.manaDelta}</span>
                                    </div>
                                </div>
                            ` : '<div class="no-action">No action</div>'}
                        </div>
                    </div>
                </div>
            `).join('');
        } else {
            roundsList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-exclamation-circle fa-3x"></i>
                    <p>No rounds data available</p>
                </div>
            `;
        }

        // Show the modal
        const matchResultModal = new bootstrap.Modal(modal);
        matchResultModal.show();
    }
}

export default MatchesTab;