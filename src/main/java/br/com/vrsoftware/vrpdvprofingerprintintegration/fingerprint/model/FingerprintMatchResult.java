package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model;

/// Represents the result of a fingerprint comparison.
public record FingerprintMatchResult(boolean matched, double score) {

    /// Indicates whether the fingerprints matched according to the threshold.
    @Override
    public boolean matched() {
        return matched;
    }

    /// Returns the raw similarity score produced by the matching algorithm.
    @Override
    public double score() {
        return score;
    }
}
