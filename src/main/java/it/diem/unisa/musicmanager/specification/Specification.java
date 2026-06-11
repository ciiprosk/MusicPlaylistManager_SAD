package it.diem.unisa.musicmanager.specification;

/**
 * Un criterio booleano componibile su un candidato di tipo T.
 * I combinatori sono metodi di default: ogni nuova specification li eredita
 * gratis, quindi la logica di composizione vive qui una volta sola.
 */
public interface Specification<T> {

    /**
     * Verifica se il candidato specificato soddisfa i criteri della specifica.
     *
     * @param candidate l'oggetto da valutare
     * @return true se i criteri sono soddisfatti, false altrimenti
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * Combina questa specifica con un'altra in AND logico.
     *
     * @param other l'altra specifica da combinare
     * @return una nuova specifica che richiede il soddisfacimento di entrambe
     */
    default Specification<T> and(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }
    /**
     * Combina questa specifica con un'altra in OR logico.
     *
     * @param other l'altra specifica da combinare
     * @return una nuova specifica che richiede il soddisfacimento di almeno una delle due
     */
    default Specification<T> or(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    /**
     * Nega la specifica corrente (NOT logico).
     *
     * @return una nuova specifica che inverte il risultato di quella attuale
     */
    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }
}
