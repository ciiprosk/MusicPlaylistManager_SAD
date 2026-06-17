package it.diem.unisa.musicmanager.specification;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpecificationCompositeTest {

    @Test
    void testAndComposition() {
        Specification<Integer> isEven = n -> n % 2 == 0;
        Specification<Integer> isPositive = n -> n > 0;
        
        Specification<Integer> isEvenAndPositive = isEven.and(isPositive);
        
        assertTrue(isEvenAndPositive.isSatisfiedBy(4));
        assertFalse(isEvenAndPositive.isSatisfiedBy(-4));
        assertFalse(isEvenAndPositive.isSatisfiedBy(3));
        assertFalse(isEvenAndPositive.isSatisfiedBy(-3));
    }

    @Test
    void testOrComposition() {
        Specification<Integer> isEven = n -> n % 2 == 0;
        Specification<Integer> isPositive = n -> n > 0;
        
        Specification<Integer> isEvenOrPositive = isEven.or(isPositive);
        
        assertTrue(isEvenOrPositive.isSatisfiedBy(4));
        assertTrue(isEvenOrPositive.isSatisfiedBy(-4));
        assertTrue(isEvenOrPositive.isSatisfiedBy(3));
        assertFalse(isEvenOrPositive.isSatisfiedBy(-3));
    }

    @Test
    void testNotComposition() {
        Specification<Integer> isEven = n -> n % 2 == 0;
        
        Specification<Integer> isOdd = isEven.not();
        
        assertTrue(isOdd.isSatisfiedBy(3));
        assertFalse(isOdd.isSatisfiedBy(4));
    }
    
    @Test
    void testComplexComposition() {
        Specification<Integer> isEven = n -> n % 2 == 0;
        Specification<Integer> isPositive = n -> n > 0;
        Specification<Integer> isLessThanTen = n -> n < 10;
        
        Specification<Integer> complexSpec = isEven.and(isPositive).and(isLessThanTen.not());
        
        // Even, positive, and NOT less than 10 (so >= 10)
        assertTrue(complexSpec.isSatisfiedBy(12));
        assertFalse(complexSpec.isSatisfiedBy(8)); // Less than 10
        assertFalse(complexSpec.isSatisfiedBy(-12)); // Not positive
        assertFalse(complexSpec.isSatisfiedBy(11)); // Not even
    }
}
