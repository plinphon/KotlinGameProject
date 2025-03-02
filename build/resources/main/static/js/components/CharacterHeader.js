export class CharacterHeader extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.render();
    }

    static get observedAttributes() {
        return ['name', 'level', 'character-class'];
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (oldValue !== newValue) {
            this.render();
        }
    }

    render() {
        const name = this.getAttribute('name');
        const level = this.getAttribute('level');
        const characterClass = this.getAttribute('character-class');
        const classIcon = characterClass === 'WARRIOR' ? '⚔️' : '🔮';

        this.innerHTML = `
            <div class="character-header" data-class="${characterClass}">
                <div class="d-flex align-items-center gap-2">
                    <span class="class-icon">
                        ${classIcon}
                    </span>
                    <h5 class="character-name mb-0">${name}</h5>
                    <span class="character-level">
                        <i class="fas fa-star"></i>
                        <span class="level-value">Level ${level}</span>
                    </span>
                </div>
            </div>
        `;
    }
}

customElements.define('character-header', CharacterHeader); 