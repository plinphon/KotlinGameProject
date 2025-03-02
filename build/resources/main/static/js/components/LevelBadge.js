export class LevelBadge extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.render();
    }

    static get observedAttributes() {
        return ['level'];
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (name === 'level' && oldValue !== newValue) {
            this.render();
        }
    }

    render() {
        const level = this.getAttribute('level');
        this.innerHTML = `
            <span class="character-level">
                <i class="fas fa-star"></i>
                <span class="level-value">Level ${level}</span>
            </span>
        `;
    }
}

customElements.define('level-badge', LevelBadge); 