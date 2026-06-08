package it.diem.unisa.musicmanager.specification;

/**
 * Un criterio booleano componibile su un candidato di tipo T.
 * I combinatori sono metodi di default: ogni nuova specification li eredita
 * gratis, quindi la logica di composizione vive qui una volta sola.
 */
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);

    default Specification<T> and(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    default Specification<T> or(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }
}
