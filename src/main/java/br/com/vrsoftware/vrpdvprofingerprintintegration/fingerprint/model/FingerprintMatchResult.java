package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model;

/**
 * Result of a fingerprint comparison.
 */
public final class FingerprintMatchResult {

    private final boolean matched;
    private final double score;

    public FingerprintMatchResult(boolean matched, double score) {
        this.matched = matched;
        this.score = score;
    }

    /**
     * Indicates whether the fingerprints matched.
     */
    public boolean matched() {
        return matched;
    }

    /**
     * Returns the similarity score produced by the matcher.
     */
    public double score() {
        return score;
    }
}
