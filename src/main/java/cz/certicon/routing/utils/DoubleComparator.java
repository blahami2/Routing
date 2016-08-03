/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

/**
 * Utility class for comparing double numbers.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DoubleComparator {

    private static final double EPS_DEFAULT = 10E-15;

    /**
     * Compares two double numbers with the given precision - if the difference
     * of two numbers it lower than a given precision, those numbers are
     * considered equal to each other.
     *
     * @param a first number
     * @param b second number
     * @param precision determines the area of equivalence
     * @return 1 for (a &gt; b), -1 for (a &lt; b), 0 for (a == b)
     */
    public static int compare( double a, double b, double precision ) {
        if ( a < b - precision ) {
            return -1;
        }
        if ( a > b + precision ) {
            return 1;
        }
        return 0;
    }

    /**
     * Determines, whether a is greater than b considering the given precision -
     * if the difference of two numbers it lower than a given precision, those
     * numbers are considered equal to each other.
     *
     * @param a first number
     * @param b second number
     * @param precision determines the area of equivalence
     * @return true for (a &gt; b), false otherwise
     */
    public static boolean isGreaterThan( double a, double b, double precision ) {
        return compare( a, b, precision ) > 0;
    }

    /**
     * Determines, whether a is lower than b considering the given precision -
     * if the difference of two numbers it lower than a given precision, those
     * numbers are considered equal to each other.
     *
     * @param a first number
     * @param b second number
     * @param precision determines the area of equivalence
     * @return true for (a &lt; b), false otherwise
     */
    public static boolean isLowerThan( double a, double b, double precision ) {
        return compare( a, b, precision ) < 0;
    }

    /**
     * Determines, whether a is greater than or equal to b considering the given precision -
     * if the difference of two numbers it lower than a given precision, those
     * numbers are considered equal to each other.
     *
     * @param a first number
     * @param b second number
     * @param precision determines the area of equivalence
     * @return true for (a &gt;= b), false otherwise
     */
    public static boolean isGreaterOrEqualTo( double a, double b, double precision ) {
        return compare( a, b, precision ) >= 0;
    }

    /**
     * Determines, whether a is lower than or equal to  b considering the given precision -
     * if the difference of two numbers it lower than a given precision, those
     * numbers are considered equal to each other.
     *
     * @param a first number
     * @param b second number
     * @param precision determines the area of equivalence
     * @return true for (a &lt;= b), false otherwise
     */
    public static boolean isLowerOrEqualTo( double a, double b, double precision ) {
        return compare( a, b, precision ) <= 0;
    }

    /**
     * Determines, whether a is equal to b considering the given precision -
     * if the difference of two numbers it lower than a given precision, those
     * numbers are considered equal to each other.
     *
     * @param a first number
     * @param b second number
     * @param precision determines the area of equivalence
     * @return true for (a == b), false otherwise
     */
    public static boolean isEqualTo( double a, double b, double precision ) {
        return compare( a, b, precision ) == 0;
    }
}
