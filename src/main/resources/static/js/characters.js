import { CHARACTER_CLASSES, COMMON_DISPLAY_PROPERTIES, CLASS_SPECIFIC_PROPERTIES, showToast, fetchWithAuth } from '../script.js';

// Add the function here and export it
export function formatLevel(level) {
    // If level is a string like "LEVEL_1", extract the number
    if (typeof level === 'string' && level.startsWith('LEVEL_')) {
        return `Level ${level.split('_')[1]}`;
    }
    // If it's already a number or other format
    return `Level ${level}`;
}

class CharactersTab {
    constructor() {
        this.characterModal = null;
        this.levelUpModal = null;
        this.currentLevelUpCharacter = null;
        
        // Initialize immediately if DOM is ready, otherwise wait for DOMContentLoaded
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.initialize());
        } else {
            this.initialize();
        }
    }

    async initialize() {
        console.log('Initializing characters tab...');
        
        // Initialize modals
        this.levelUpModal = new bootstrap.Modal(document.getElementById('levelUpModal'));
        
        // Add tab change listener
        document.getElementById('characters-tab').addEventListener('shown.bs.tab', () => {
            console.log('Characters tab shown, reloading characters...');
            this.loadCharacters();
        });

        // Load characters immediately
        console.log('Loading initial characters...');
        await this.loadCharacters();
        await this.initializeCreateCharacterModal();
        
        // Add event listener for create character button
        document.getElementById('createCharacterBtn').addEventListener('click', () => {
            const modal = new bootstrap.Modal(document.getElementById('createCharacterModal'));
            modal.show();
        });
    }

    async loadCharacters() {
        console.log('Loading characters...');
        const listContainer = document.getElementById('charactersList');
        try {
            listContainer.innerHTML = '<div class="loading">Loading characters...</div>';
    
            const characters = await fetchWithAuth('/api/characters');
            
            console.log('Characters loaded:', characters);
            this.displayCharacters(characters);
        } catch (error) {
            console.error('Error loading characters:', error);
            listContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-exclamation-circle fa-3x"></i>
                    <p>Failed to load characters</p>
                    <small>${error.message}</small>
                </div>
            `;
            showToast(error.message, true);
        }
    }

    displayCharacters(characters) {
        console.log('Displaying characters:', characters);
        const listContainer = document.getElementById('charactersList');
        listContainer.innerHTML = '';

        if (characters.length === 0) {
            listContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-user-astronaut fa-3x"></i>
                    <p>Your cosmic journey begins here...</p>
                    <small>Create your first character to start your adventure</small>
                </div>
            `;
            return;
        }

        characters.forEach(character => {
            const characterElement = this.createCharacterElement(character);
            listContainer.appendChild(characterElement);
        });
    }

    updatePropertyInputs() {
        const selectedClass = document.getElementById('characterClass').value;
        const propertiesContainer = document.getElementById('dynamicProperties');
        propertiesContainer.innerHTML = '';
    
        if (!selectedClass) return;
    
        const classData = CHARACTER_CLASSES[selectedClass];
        const propertiesRow = document.createElement('div');
        propertiesRow.className = 'd-flex gap-3';
    
        Object.entries(classData.properties).forEach(([propName, config]) => {
            const formGroup = document.createElement('div');
            formGroup.className = 'flex-grow-1';
    
            const label = document.createElement('label');
            label.className = 'form-label';
            label.textContent = this.formatPropertyName(propName);
    
            const input = document.createElement('input');
            input.type = config.type;
            input.className = 'form-control';
            input.id = propName;
            input.name = propName;
            input.required = true;
            input.min = config.min;
            input.max = config.max;
            input.value = config.default;
    
            // Add input event listener for real-time validation
            input.addEventListener('input', () => {
                this.updateRemainingPoints();
                input.classList.toggle('is-invalid', parseInt(input.value) < config.min || parseInt(input.value) > config.max);
            });
    
            formGroup.appendChild(label);
            formGroup.appendChild(input);
            propertiesRow.appendChild(formGroup);
        });
    
        propertiesContainer.appendChild(propertiesRow);
        this.updateRemainingPoints();
    }

    updateRemainingPoints() {
        const selectedClass = document.getElementById('characterClass').value;
        if (!selectedClass) return;
    
        const inputs = document.querySelectorAll('#dynamicProperties input[type="number"]');
        const totalPoints = Array.from(inputs).reduce((sum, input) => sum + parseInt(input.value || 0), 0);
        const maxPoints = this.getPointsForLevel(1); // Level 1 points for new character
        const remainingPoints = maxPoints - totalPoints;
    
        let pointsDisplay = document.getElementById('pointsDisplay');
        if (!pointsDisplay) {
            pointsDisplay = document.createElement('div');
            pointsDisplay.id = 'pointsDisplay';
            document.getElementById('dynamicProperties').appendChild(pointsDisplay);
        }
    
        pointsDisplay.innerHTML = `
            <div class="points-summary ${
                remainingPoints === 0 ? 'points-valid' : 
                remainingPoints < 0 ? 'points-exceeded' : 
                'points-remaining'
            }">
                <div class="points-row">
                    <span>Points: ${totalPoints}/${maxPoints}</span>
                    <span>${remainingPoints >= 0 ? `${remainingPoints} remaining` : `${Math.abs(remainingPoints)} over limit`}</span>
                </div>
            </div>
        `;
    
        // Disable submit button if points don't match exactly
        const submitButton = document.querySelector('#characterForm button[type="submit"]');
        submitButton.disabled = remainingPoints !== 0;
    }

    validateCharacterPoints(properties) {
        const totalPoints = Object.values(properties).reduce((sum, value) => sum + value, 0);
        const level1Points = this.getPointsForLevel(1);
    
        if (totalPoints !== level1Points) {
            throw new Error(`Total character points must equal ${level1Points}. Current total: ${totalPoints}`);
        }
    }

    getPointsForLevel(level) {
        // This should match your backend logic for level points
        const BASE_POINTS = 200;
        const POINTS_PER_LEVEL = 50;
        return BASE_POINTS + (level - 1) * POINTS_PER_LEVEL;
    }

    async handleSubmit(event) {
        event.preventDefault();
        const form = event.target;
        const submitButton = form.querySelector('button[type="submit"]');
        const errorDisplay = document.getElementById('formError');
    
        // Clear previous error
        if (errorDisplay) {
            errorDisplay.remove();
        }
    
        const selectedClass = document.getElementById('characterClass').value;
        if (!selectedClass) return;
    
        const formData = {
            name: document.getElementById('name').value,
            characterClass: selectedClass
        };
    
        // Add class-specific properties
        const properties = {};
        Object.keys(CHARACTER_CLASSES[selectedClass].properties).forEach(prop => {
            properties[prop] = parseInt(document.getElementById(prop).value);
        });
    
        try {
            // Validate total points
            const totalPoints = Object.values(properties).reduce((sum, value) => sum + value, 0);
            const level1Points = this.getPointsForLevel(1);

            if (totalPoints !== level1Points) {
                throw new Error(`Total points must equal ${level1Points}. Current total: ${totalPoints}`);
            }

            // Add properties to form data
            Object.assign(formData, properties);

            submitButton.disabled = true;
            submitButton.textContent = 'Creating...';

            await this.createCharacter(formData);
            this.resetForm();
            showToast('Character created successfully!');
            await this.loadCharacters();
        } catch (error) {
            console.error('Error creating character:', error);
    
            // Display error message
            const errorDiv = document.createElement('div');
            errorDiv.id = 'formError';
            errorDiv.className = 'alert alert-danger mt-3';
            errorDiv.textContent = error.message;
            form.querySelector('.modal-body').appendChild(errorDiv);
    
            showToast(error.message, true);
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = 'Create Character';
        }
    }

    async createCharacter(character) {
        const response = await fetchWithAuth('/api/characters', {
            method: 'POST',
            body: JSON.stringify(character),
        });
    
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Failed to create character');
        }
        return data;
    }

    createCharacterElement(character) {
        console.log('Creating character element:', character);
        const template = document.getElementById('character-template');
        const clone = template.content.cloneNode(true);
        const characterItem = clone.querySelector('.character-item');
        
        // Add character class as data attribute
        characterItem.dataset.class = character.characterClass;
        
        // Set character name and level
        characterItem.querySelector('.character-name').textContent = character.name;
        characterItem.querySelector('.level-value').textContent = formatLevel(character.level);
        
        // Add properties
        const propertiesContainer = characterItem.querySelector('.character-properties');
        
        // Add common properties first
        COMMON_DISPLAY_PROPERTIES.forEach(prop => {
            if (character[prop] !== undefined) {
                const propertyItem = this.createPropertyElement(prop, character[prop]);
                propertiesContainer.appendChild(propertyItem);
            }
        });

        // Add class-specific properties
        const classSpecificProps = CLASS_SPECIFIC_PROPERTIES[character.characterClass] || [];
        classSpecificProps.forEach(prop => {
            if (character[prop] !== undefined) {
                const propertyItem = this.createPropertyElement(prop, character[prop]);
                propertiesContainer.appendChild(propertyItem);
            }
        });

        // Add experience property with level up functionality
        const expPropertyItem = this.createPropertyElement('experience', character.experience);
        const isCharacterOwner = character.isOwner || character.owner;
        
        if (character.shouldLevelUp && isCharacterOwner) {
            expPropertyItem.classList.add('can-level-up');
            const label = expPropertyItem.querySelector('.property-label');
            label.textContent = `${this.formatPropertyName('experience')} ⇧`;
            expPropertyItem.addEventListener('click', () => this.showLevelUpModal(character));
        }
        
        propertiesContainer.appendChild(expPropertyItem);
        
        // Update class display to use icon with tooltip
        const classIcon = document.createElement('span');
        classIcon.className = 'class-icon';
        classIcon.setAttribute('title', CHARACTER_CLASSES[character.characterClass].name);
        classIcon.innerHTML = character.characterClass === 'WARRIOR' ? '⚔️' : '🔮';
        
        // Add the icon before the character name
        const nameElement = characterItem.querySelector('.character-name');
        nameElement.insertBefore(classIcon, nameElement.firstChild);
        
        // Initialize tooltip
        new bootstrap.Tooltip(classIcon);
        
        return characterItem;
    }

    showLevelUpModal(character) {
        const nextLevel = parseInt(character.level.split('_')[1]) + 1;
        this.currentLevelUpCharacter = character;
        
        this.showPointsAssignmentDialog(character, nextLevel, async (properties) => {
            try {
                const response = await fetchWithAuth(`/api/characters/${character.id}`, {
                    method: 'PUT',
                    body: JSON.stringify({
                        properties: {
                            health: parseInt(properties.health),
                            attackPower: parseInt(properties.attackPower),
                            ...(character.characterClass === 'WARRIOR' ? {
                                stamina: parseInt(properties.stamina),
                                defensePower: parseInt(properties.defensePower)
                            } : {
                                mana: parseInt(properties.mana),
                                healingPower: parseInt(properties.healingPower)
                            })
                        }
                    })
                });
                
                if (response.ok) {
                    this.levelUpModal.hide();
                    showToast('Character leveled up successfully!');
                    await this.loadCharacters();
                }
            } catch (error) {
                showToast(error.message, true);
            }
        });
    }

    async showPointsAssignmentDialog(character, nextLevel, callback) {
        try {
            const modal = document.getElementById('levelUpModal');
            const form = modal.querySelector('form');
            
            // Calculate available points based on target level points
            const LEVEL_POINTS = {
                'LEVEL_1': 200,
                'LEVEL_2': 210,
                'LEVEL_3': 230,
                'LEVEL_4': 260,
                'LEVEL_5': 300,
                'LEVEL_6': 350,
                'LEVEL_7': 410,
                'LEVEL_8': 480,
                'LEVEL_9': 560,
                'LEVEL_10': 650
            };

            // Calculate current total assigned points
            const currentAssignedPoints = Object.entries(character).reduce((sum, [key, value]) => {
                // Only sum up numeric values that are properties we care about
                if ([...COMMON_DISPLAY_PROPERTIES, ...CLASS_SPECIFIC_PROPERTIES[character.characterClass]].includes(key)) {
                    return sum + (parseInt(value) || 0);
                }
                return sum;
            }, 0);

            // Calculate points needed to reach next level's total
            const nextLevelPoints = LEVEL_POINTS[`LEVEL_${nextLevel}`] || 0;
            const availablePoints = nextLevelPoints - currentAssignedPoints;

            // Update character info
            const characterHeader = modal.querySelector('.modal-header');
            characterHeader.innerHTML = `
                <div class="character-header">
                    <div class="d-flex align-items-center gap-2">
                        <span class="class-icon">
                            ${character.characterClass === 'WARRIOR' ? '⚔️' : '🔮'}
                        </span>
                        <h5 class="character-name mb-0">${character.name}</h5>
                        <span class="character-level">
                            <i class="fas fa-star"></i> ${nextLevel || character.level}
                        </span>
                    </div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            `;
            
            // Add cosmic background class to modal content
            modal.querySelector('.modal-content').className = 'modal-content cosmic-modal';
            
            // Clear previous form
            const propertiesContainer = modal.querySelector('#levelUpProperties');
            propertiesContainer.innerHTML = '';
            
            // Create properties row with more padding
            const propertiesRow = document.createElement('div');
            propertiesRow.className = 'd-flex gap-3 p-2';  // Added p-2 for inner padding
            
            // Add property inputs
            const commonProps = COMMON_DISPLAY_PROPERTIES;
            const classProps = CLASS_SPECIFIC_PROPERTIES[character.characterClass];
            
            [...commonProps, ...classProps].forEach(prop => {
                const formGroup = document.createElement('div');
                formGroup.className = 'flex-grow-1';
                
                const label = document.createElement('label');
                label.className = 'form-label mb-1';
                label.textContent = this.formatPropertyName(prop);
                
                const input = document.createElement('input');
                input.type = 'number';
                input.className = 'form-control form-control-sm';
                input.name = prop;
                input.value = character[prop] || 0;
                input.min = character[prop] || 0;  // Set minimum to current value
                input.required = true;
                input.placeholder = `Current: ${character[prop] || 0}`;
                
                // Prevent manual input of values below current
                input.addEventListener('change', () => {
                    const currentValue = parseInt(input.value) || 0;
                    const minValue = character[input.name] || 0;
                    if (currentValue < minValue) {
                        input.value = minValue;
                    }
                });
                
                formGroup.appendChild(label);
                formGroup.appendChild(input);
                propertiesRow.appendChild(formGroup);
            });
            
            propertiesContainer.appendChild(propertiesRow);
            
            // Update points display
            const pointsDisplay = document.createElement('div');
            pointsDisplay.className = 'points-summary mt-3 mb-4';
            pointsDisplay.innerHTML = `
                <div class="points-row">
                    <span>Available Points:</span>
                    <span id="availablePoints">${availablePoints}</span>
                </div>
            `;
            modal.querySelector('#pointsDisplay').innerHTML = '';
            modal.querySelector('#pointsDisplay').appendChild(pointsDisplay);
            
            // Update modal footer
            const modalFooter = modal.querySelector('.modal-footer');
            modalFooter.innerHTML = `
                <div class="d-flex gap-2">
                    <button type="button" class="btn btn-cosmic-outline" onclick="autoLevelUp()">
                        <i class="fas fa-magic"></i> Auto Assign
                    </button>
                    <button type="submit" class="btn btn-cosmic" disabled>
                        <i class="fas fa-level-up-alt"></i> Level Up!
                    </button>
                </div>
            `;
            
            // Handle form submission
            form.onsubmit = async (e) => {
                e.preventDefault();
                const formData = new FormData(form);
                const properties = {};
                formData.forEach((value, key) => {
                    properties[key] = parseInt(value);
                });
                
                await callback(properties);
            };
            
            // Update available points display when inputs change
            const inputs = form.querySelectorAll('input[type="number"]');
            
            inputs.forEach(input => {
                input.addEventListener('change', () => {
                    // Calculate total points used
                    let used = 0;
                    inputs.forEach(input => {
                        const currentValue = parseInt(input.value) || 0;
                        const originalValue = parseInt(character[input.name]) || 0;
                        used += currentValue - originalValue;
                    });
                    
                    // Update display and validation
                    const available = availablePoints - used;
                    modal.querySelector('#availablePoints').textContent = available;
                    
                    // Disable submit button unless points are exactly used up
                    const submitButton = form.querySelector('button[type="submit"]');
                    submitButton.disabled = available !== 0;
                    
                    // Update points display styling
                    pointsDisplay.className = `points-summary mt-3 mb-4 ${
                        available === 0 ? 'points-valid' : 
                        available < 0 ? 'points-exceeded' : 
                        'points-remaining'
                    }`;
                });
            });
            
            // Update auto level up function
            window.autoLevelUp = () => {
                const inputCount = inputs.length;
                const basePoints = Math.floor(availablePoints / inputCount);
                let remaining = availablePoints % inputCount;
                
                inputs.forEach(input => {
                    const currentValue = parseInt(character[input.name]) || 0;
                    input.value = currentValue + basePoints + (remaining-- > 0 ? 1 : 0);
                    input.dispatchEvent(new Event('change'));
                });
            };
            
            // Show the modal
            const levelUpModal = new bootstrap.Modal(modal);
            levelUpModal.show();
            
        } catch (error) {
            console.error('Error showing points assignment dialog:', error);
            showToast(error.message, true);
        }
    }

    formatPropertyName(prop) {
        // Convert camelCase to Title Case and remove "Power"
        return prop
            .replace(/Power/g, '')  // Remove "Power" from the property name
            .replace(/([A-Z])/g, ' $1')  // Add space before capital letters
            .replace(/^./, str => str.toUpperCase())  // Capitalize first letter
            .trim();  // Remove any extra spaces
    }

    resetForm() {
        const form = document.getElementById('characterForm');
        form.reset();
    
        // Reset class selection
        const classSelect = document.getElementById('characterClass');
        classSelect.value = '';
    
        // Clear dynamic properties
        const propertiesContainer = document.getElementById('dynamicProperties');
        propertiesContainer.innerHTML = '';
    
        // Remove points display
        const pointsDisplay = document.getElementById('pointsDisplay');
        if (pointsDisplay) {
            pointsDisplay.remove();
        }
    
        // Remove any error messages
        const errorDisplay = document.getElementById('formError');
        if (errorDisplay) {
            errorDisplay.remove();
        }
    
        // Close the modal
        this.characterModal.hide();
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

    handleClassSelection() {
        const selectedClass = document.getElementById('characterClass').value;
        const characterName = document.getElementById('name').value;
        
        if (!selectedClass || !characterName) return;
        
        // Create initial character with default values
        const classData = CHARACTER_CLASSES[selectedClass];
        const character = {
            name: characterName,
            characterClass: selectedClass,
            availablePoints: 10, // Initial points for new character
            level: 1
        };
        
        // Hide the character creation modal
        this.characterModal.hide();
        
        // Update modal title for character creation
        const modal = document.getElementById('levelUpModal');
        const modalTitle = modal.querySelector('.modal-title');
        modalTitle.textContent = 'Assign Initial Character Points';
        
        // Show the points assignment dialog
        this.showPointsAssignmentDialog(character, null, async (properties) => {
            try {
                const formData = {
                    name: character.name,
                    characterClass: character.characterClass,
                    properties: properties
                };
                
                await this.createCharacter(formData);
                this.levelUpModal.hide();
                showToast('Character created successfully!');
                await this.loadCharacters();
            } catch (error) {
                showToast(error.message, true);
            }
        });
    }

    // Add this helper function to create the point distribution form
    createPointDistributionForm(container, character, availablePoints, onSubmit) {
        // Create properties row
        const propertiesRow = document.createElement('div');
        propertiesRow.className = 'd-flex gap-3 p-2';

        // Add property inputs
        const commonProps = COMMON_DISPLAY_PROPERTIES;
        const classProps = CLASS_SPECIFIC_PROPERTIES[character.characterClass];
        
        [...commonProps, ...classProps].forEach(prop => {
            const formGroup = document.createElement('div');
            formGroup.className = 'flex-grow-1';
            
            const label = document.createElement('label');
            label.className = 'form-label mb-1';
            label.textContent = this.formatPropertyName(prop);
            
            const input = document.createElement('input');
            input.type = 'number';
            input.className = 'form-control form-control-sm';
            input.name = prop;
            input.value = character[prop] || 0;
            input.min = 0;
            input.required = true;
            input.placeholder = '0';
            
            formGroup.appendChild(label);
            formGroup.appendChild(input);
            propertiesRow.appendChild(formGroup);
        });
        
        container.appendChild(propertiesRow);
        
        // Add points display
        const pointsDisplay = document.createElement('div');
        pointsDisplay.className = 'points-summary mt-3 mb-4';
        pointsDisplay.innerHTML = `
            <div class="points-row">
                <span>Available Points:</span>
                <span id="availablePoints">${availablePoints}</span>
            </div>
        `;
        container.appendChild(pointsDisplay);
        
        // Add points validation
        const inputs = container.querySelectorAll('input[type="number"]');
        inputs.forEach(input => {
            input.addEventListener('change', () => {
                let used = 0;
                inputs.forEach(input => {
                    used += parseInt(input.value) || 0;
                });
                
                const available = availablePoints - used;
                container.querySelector('#availablePoints').textContent = available;
                
                const submitButton = container.closest('form').querySelector('button[type="submit"]');
                submitButton.disabled = available !== 0;
                
                pointsDisplay.className = `points-summary mt-3 mb-4 ${
                    available === 0 ? 'points-valid' : 
                    available < 0 ? 'points-exceeded' : 
                    'points-remaining'
                }`;
            });
        });

        // Add auto assign button
        const autoAssignBtn = document.createElement('button');
        autoAssignBtn.type = 'button';
        autoAssignBtn.className = 'btn btn-cosmic-outline mb-3';
        autoAssignBtn.innerHTML = '<i class="fas fa-magic"></i> Auto Assign';
        autoAssignBtn.onclick = () => {
            const inputCount = inputs.length;
            const basePoints = Math.floor(availablePoints / inputCount);
            let remaining = availablePoints % inputCount;
            
            inputs.forEach(input => {
                input.value = basePoints + (remaining-- > 0 ? 1 : 0);
                input.dispatchEvent(new Event('change'));
            });
        };
        container.insertBefore(autoAssignBtn, pointsDisplay);

        return { inputs, pointsDisplay };
    }

    initializeCreateCharacterModal() {
        const modal = document.getElementById('createCharacterModal');
        const form = modal.querySelector('form');
        const classSelect = form.querySelector('#characterClass');
        const pointsContainer = form.querySelector('#pointsContainer');
        
        // Add cosmic theme class
        modal.querySelector('.modal-content').className = 'modal-content cosmic-modal';
        
        // Populate class options
        Object.entries(CHARACTER_CLASSES).forEach(([value, data]) => {
            const option = document.createElement('option');
            option.value = value;
            option.textContent = data.name;
            classSelect.appendChild(option);
        });

        // Add point distribution section when class is selected
        classSelect.addEventListener('change', () => {
            const selectedClass = classSelect.value;
            pointsContainer.innerHTML = ''; // Clear previous content

            if (selectedClass) {
                const character = { characterClass: selectedClass };
                this.createPointDistributionForm(pointsContainer, character, 200); // LEVEL_1 points
            }
        });

        // Update form submission to include points
        form.onsubmit = async (e) => {
            e.preventDefault();
            const formData = new FormData(form);
            const data = {
                name: formData.get('name'),
                characterClass: formData.get('characterClass'),
                properties: {}
            };

            // Add all numeric properties
            form.querySelectorAll('input[type="number"]').forEach(input => {
                data.properties[input.name] = parseInt(input.value);
            });

            try {
                await this.createCharacter(data);
                const bsModal = bootstrap.Modal.getInstance(modal);
                bsModal.hide();
                showToast('Character created successfully!');
                await this.loadCharacters();
            } catch (error) {
                showToast(error.message, true);
            }
        };
    }
}

export default CharactersTab; 