export class MatchOutcome extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.render();
    }

    static get observedAttributes() {
        return ['outcome'];
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (oldValue !== newValue) {
            this.render();
        }
    }

    render() {
        const outcome = this.getAttribute('outcome');
        let badge = '';

        switch (outcome) {
            case 'CHALLENGER_WON':
                badge = '<span class="badge bg-success">Victory</span>';
                break;
            case 'OPPONENT_WON':
                badge = '<span class="badge bg-danger">Defeat</span>';
                break;
            case 'DRAW':
                badge = '<span class="badge bg-warning">Draw</span>';
                break;
            default:
                badge = '<span class="badge bg-secondary">Unknown</span>';
        }

        this.innerHTML = badge;
    }
}

customElements.define('match-outcome', MatchOutcome); 