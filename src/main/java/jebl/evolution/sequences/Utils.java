/*
 * Utils.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Utils.java 918 2008-06-04 01:28:08Z twobeers $
 */
public class Utils {
    private Utils() { }  // make class uninstantiable

    /**
     * Translates a given {@link Sequence} to a corresponding {@link Sequence} under the given genetic code.
     * Simply a utility function that calls AminoAcidState[] translate(final State[] states, GeneticCode geneticCode)
     *
     * @param sequence the Sequence.
     * @param geneticCode
     * @return
     */
    public static Sequence translate(final Sequence sequence, GeneticCode geneticCode) {
        return new BasicSequence(SequenceType.AMINO_ACID, sequence.getTaxon(), translate(sequence.getStates(), geneticCode));
    }

    /**
     * Translates a given {@link Sequence} to a corresponding {@link Sequence} under the given genetic code.
     * Simply a utility function that calls AminoAcidState[] translate(final State[] states, GeneticCode geneticCode)
     *
     * @param sequence the Sequence.
     * @param geneticCode
     * @param readingFrame
     * @return
     */
    public static Sequence translate(final Sequence sequence, GeneticCode geneticCode, int readingFrame) {
        return new BasicSequence(SequenceType.AMINO_ACID, sequence.getTaxon(), translate(sequence.getStates(), geneticCode, readingFrame));
    }

    /**
     * Translates each of a given sequence of {@link NucleotideState}s or {@link CodonState}s
     * to the {@link AminoAcidState} corresponding to it under the given genetic code.
     *
     * Translation doesn't stop at stop codons; these are translated to {@link AminoAcids#STOP_STATE}.
     * If translating from {@link jebl.evolution.sequences.NucleotideState} and the number
     * of states is not a multiple of 3, then the excess states at the end are silently dropped.
     *
     * @param states States to translate; must all be of the same type, either NucleotideState
     *        or CodonState.
     * @param geneticCode
     * @return
     */
    public static AminoAcidState[] translate(final State[] states, GeneticCode geneticCode) {
        return translate(states, geneticCode, 1);
    }

    /**
     * Translates each of a given sequence of {@link NucleotideState}s or {@link CodonState}s
     * to the {@link AminoAcidState} corresponding to it under the given genetic code.
     *
     * Translation doesn't stop at stop codons; these are translated to {@link AminoAcids#STOP_STATE}.
     * If translating from {@link jebl.evolution.sequences.NucleotideState} and the number
     * of states is not a multiple of 3, then the excess states at the end are silently dropped.
     *
     * @param states States to translate; must all be of the same type, either NucleotideState
     *        or CodonState.
     * @param geneticCode
     * @param readingFrame
     * @return
     */
    public static AminoAcidState[] translate(final State[] states, GeneticCode geneticCode, int readingFrame) {
        if (states == null) throw new NullPointerException("States array is null");
        if (states.length == 0) return new AminoAcidState[0];

        if (readingFrame < 1 || readingFrame > 3) {
            throw new IllegalArgumentException("Reading frame should be between 1 and 3");
        }

        if (states[0] instanceof NucleotideState) {
            int offset = readingFrame - 1;
            int length = states.length - offset;
            if (length == 0) return new AminoAcidState[0];

            AminoAcidState[] translation = new AminoAcidState[length / 3];
            for (int i = 0; i < translation.length; i++) {
                translation[i] = geneticCode.getTranslation((NucleotideState)states[i * 3 + offset],
                        (NucleotideState)states[(i * 3) + offset + 1],
                        (NucleotideState)states[(i * 3) + offset + 2]);
            }
            return translation;
        } else if (states[0] instanceof CodonState) {
            if (readingFrame != 1) {
                throw new IllegalArgumentException("Can't translate codon sequences in anything other than reading frame 1");
            }
            AminoAcidState[] translation = new AminoAcidState[states.length];
            for (int i = 0; i < translation.length; i++) {
                translation[i] = geneticCode.getTranslation((CodonState)states[i]);
            }
            return translation;
        } else {
            throw new IllegalArgumentException("Given states are not nucleotides or codons so cannot be translated");
        }
    }

    /**
     * Is the given NucleotideSequence predominantly RNA?
     * (i.e the more occurrences of "U" than "T")
     * @param sequenceString the sequence string to inspect to determine if it's RNA
     * @param maximumNonGapsToLookAt for performance reasons, only look at a maximum of this many non-gap residues in deciding if the sequence is predominantly RNA. Can be -1 or Integer.MAX_VALUE to look at the entire sequence.
     * @return true if the given NucleotideSequence predominantly RNA
     */
    public static boolean isPredominantlyRNA(final CharSequence sequenceString, int maximumNonGapsToLookAt) {
        int length = sequenceString.length();
        int tCount = 0;
        int uCount = 0;
        if (maximumNonGapsToLookAt==-1) maximumNonGapsToLookAt=Integer.MAX_VALUE;
        for (int i = 0; i < length && maximumNonGapsToLookAt > 0; i++) {
            char c = sequenceString.charAt(i);
            if (c != '-') maximumNonGapsToLookAt--;
            if (c == 'T' || c == 't') tCount++;
            if (c == 'U' || c == 'u') uCount++;
        }
        return uCount > tCount;
    }

    private static String reverseComplement(final String nucleotideSequence, boolean removeGaps) {
        boolean predominantlyRNA = isPredominantlyRNA(nucleotideSequence,-1);
        Sequence seq = new BasicSequence(SequenceType.NUCLEOTIDE, Taxon.getTaxon("x"), nucleotideSequence);
        if( removeGaps ) {
            seq = new GaplessSequence(seq);
        }
        int length=seq.getLength();
        StringBuilder results =new StringBuilder();
        for (int i =  length-1; i >=0; i--) {
            State state= seq.getState(i);
            NucleotideState complementaryState = Nucleotides.COMPLEMENTARY_STATES[state.getIndex()];
            if (predominantlyRNA && complementaryState.equals(Nucleotides.T_STATE)) {
                results.append('U');
            }
            else {
                results.append (complementaryState.getCode());
            }
        }
        return results.toString();
/*
        State[] states = seq.getStates();
        NucleotideState[] nucleotideStates = new NucleotideState[states.length];
        for (int i = 0; i < states.length; i++) {
            nucleotideStates[i] = (NucleotideState) states[i];
        }
        nucleotideStates = reverseComplement(nucleotideStates);
        seq = new BasicSequence(SequenceType.NUCLEOTIDE, Taxon.getTaxon("x"), nucleotideStates);
        return seq.getString();*/
    }

    /* kills gaps */
    public static String reverseComplement(final String nucleotideSequence) {
        return reverseComplement(nucleotideSequence, true);
    }

    public static String reverseComplementWithGaps(final String nucleotideSequence) {
        return reverseComplement(nucleotideSequence, false);
    }

    /**
     * Translates the given nucleotideSequence into an amino acid sequence
     * string, using the given geneticCode. The translation is done triplet by
     * triplet, starting with the triplet that is at index 0..2 in
     * nucleotideSequence, then the one at index 3..5 etc. until there are
     * less than 3 nucleotides left.
     * <p>
     * This method uses {@link #translate(State[],GeneticCode)} to do the
     * translation, hence it shares some properties with that method:
     * 1.) Any excess nucleotides at the end will be silently discarded,
     * 2.) Translation doesn't stop at stop codons; instead, they are
     * translated to "*", which is
     * {@link jebl.evolution.sequences.AminoAcids#STOP_STATE}'s code.
     *
     * @param nucleotideSequence nucleotide sequence to translate
     * @param geneticCode        genetic code to use for the translation
     * @return A string with length nucleotideSequence.length() / 3 (rounded
     *         down), the translation of <code>nucleotideSequence</code> with
     *         the given genetic code
     */
    public static String translateCharSequence(final CharSequence nucleotideSequence, GeneticCode geneticCode) {
        Sequence seq = new BasicSequence(SequenceType.NUCLEOTIDE, Taxon.getTaxon("x"), nucleotideSequence);
        seq = new GaplessSequence(seq);
        State[] states = seq.getStates();

        states = translate(states, geneticCode);
        seq = new BasicSequence(SequenceType.AMINO_ACID, Taxon.getTaxon("x"), states);
        return seq.getString();
    }

    /**
     * A wrapper for {@link #translateCharSequence(CharSequence,GeneticCode)}
     * that takes a nucleotide sequence as a String only rather than a
     * CharSequence. This is to preserve backwards compatibility with
     * existing compiled code.
     *
     * @param nucleotideSequence nucleotide sequence string to translate
     * @param geneticCode        genetic code to use for the translation
     * @return A string with length nucleotideSequence.length() / 3 (rounded
     *         down), the translation of <code>nucleotideSequence</code> with
     *         the given genetic code
     */
    public static String translate(final String nucleotideSequence, GeneticCode geneticCode) {
        return translateCharSequence(nucleotideSequence, geneticCode);
    }

    /**
     * Strips a sequence of gaps
     * @param sequence the sequence
     * @return the stripped sequence
     */
    public static State[] stripGaps(final State[] sequence) {
        if (sequence.length == 0) {
            return new State[] {};
        }

        return stripStates(sequence, Collections.singletonList(sequence[0].getType().getGapState()));
    }

    /**
     * Strips a sequence of any states given
     * @param sequence the sequence
     * @param stripStates the states to strip
     * @return an array of states
     */
    public static State[] stripStates(final State[] sequence, final List<State> stripStates) {
        List<State> stripped = new ArrayList<State>();
        for (State state : sequence) {
            if (!stripStates.contains(state)) {
                stripped.add(state);
            }
        }

        return stripped.toArray(new State[0]);
    }

    /**
     * Searchers and replaces a sequence of any states given
     * @param sequence the sequence
     * @param searchStates the states to search for
     * @return an array of states
     */
    public static State[] replaceStates(final State[] sequence, final List<State> searchStates, State replaceState) {
        List<State> stripped = new ArrayList<State>();
        for (State state : sequence) {
            if (searchStates.contains(state)) {
                stripped.add(replaceState);
            } else {
                stripped.add(state);
            }
        }

        return stripped.toArray(new State[0]);
    }

    public static State[] reverse(final State[] sequence) {
        State[] reversed = new State[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            reversed[i] = sequence[sequence.length - i - 1];
        }
        return reversed;
    }

    public static NucleotideState[] complement(final NucleotideState[] sequence) {
        NucleotideState[] complemented = new NucleotideState[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            complemented[i] = Nucleotides.COMPLEMENTARY_STATES[sequence[i].getIndex()];
        }
        return complemented;
    }

    public static NucleotideState[] reverseComplement(final NucleotideState[] sequence) {
        NucleotideState[] reverseComplemented = new NucleotideState[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            reverseComplemented[i] = Nucleotides.COMPLEMENTARY_STATES[sequence[sequence.length - i - 1].getIndex()];
        }
        return reverseComplemented;
    }

    public static byte[] getStateIndices(final State[] sequence) {
        byte[] indices = new byte[sequence.length];
        int i = 0;
        for (State state : sequence) {
            indices[i] = (byte)state.getIndex();
            i++;
        }

        return indices;
    }

    /**
     * Gets the site location index for this sequence excluding
     * any gaps. The location is indexed from 0.
     * @param sequence the sequence
     * @param gappedLocation the location including gaps
     * @return the location without gaps.
     */
    public static int getGaplessLocation(Sequence sequence, int gappedLocation) {
        int gapless = 0;
        int gapped = 0;
        for (State state : sequence.getStates()) {
            if (gapped == gappedLocation) return gapless;
            if (!state.isGap()) {
                gapless ++;
            }
            gapped ++;
        }
        return gapless;
    }

    /**
     * Gets the site location index for this sequence that corresponds
     * to a location given excluding all gaps. The first non-gapped site
     * in the sequence has a gaplessLocation of 0.
     * @param sequence the sequence
     * @param gaplessLocation
     * @return the site location including gaps
     */
    public static int getGappedLocation(Sequence sequence, int gaplessLocation) {
        int gapless = 0;
        int gapped = 0;
        for (State state : sequence.getStates()) {
            if (gapless == gaplessLocation) return gapped;
            if (!state.isGap()) {
                gapless ++;
            }
            gapped ++;
        }
        return gapped;
    }

    /**
     * Guess type of sequence from contents.
     * @param seq the sequence
     * @return SequenceType.NUCLEOTIDE or SequenceType.AMINO_ACID, if sequence is believed to be of that type.
     *         If the sequence contains characters that are valid for neither of these two sequence
     *         types, then this method returns null.
     */
    public static SequenceType guessSequenceType(final CharSequence seq) {

        int canonicalNucStates = 0;
        int undeterminedStates = 0;
        // true length, excluding any gaps
        int sequenceLength = seq.length();
        final int seqLen = sequenceLength;

        final int canonicalStateCount = Nucleotides.getCanonicalStateCount();

        boolean onlyValidNucleotides = true;
        boolean onlyValidAminoAcids = true;

        // do not use toCharArray: it allocates an array size of sequence
        for(int k = 0; (k < seqLen) && (onlyValidNucleotides || onlyValidAminoAcids); ++k) {
            final char c = seq.charAt(k);

            final NucleotideState nucState = Nucleotides.getState(c);
            final boolean isNucState = nucState != null;
            final boolean isAminoState = AminoAcids.getState(c) != null;

            onlyValidNucleotides &= isNucState;
            onlyValidAminoAcids &= isAminoState;

            if (onlyValidNucleotides) {
                assert(isNucState);
                if (nucState.getIndex() < canonicalStateCount) {
                    ++canonicalNucStates;
                } else {
                    if (nucState == Nucleotides.GAP_STATE) {
                        --sequenceLength;
                    } else if( nucState == Nucleotides.N_STATE ) {
                        ++undeterminedStates;
                    }
                }
            }
        }

        SequenceType result;
        if (onlyValidNucleotides) {  // only nucleotide states
            // All sites are nucleotides (actual or ambigoues). If longer than 100 sites, declare it a nuc
            if( sequenceLength >= 100 ) {
                result = SequenceType.NUCLEOTIDE;
            } else {
                // if short, ask for 70% of ACGT or N
                final double threshold = 0.7;
                final int nucStates = canonicalNucStates + undeterminedStates;
                // note: This implicitely assumes that every valid nucleotide
                // symbol is also a valid amino acid. This is true since we
                // added support for the 21st amino acid, U (Selenocysteine)
                // in AminoAcids.java.
                result = nucStates >= sequenceLength * threshold ? SequenceType.NUCLEOTIDE : SequenceType.AMINO_ACID;
            }
        } else if (onlyValidAminoAcids) {
            result = SequenceType.AMINO_ACID;
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Counts the number of stop codons in an amino acid sequence
     * @param sequence the sequence string to count stop codons
     * @return the number of stop codons
     */
    public static int getStopCodonCount(final Sequence sequence) {

        if (sequence.getSequenceType() != SequenceType.AMINO_ACID) {
            throw new IllegalArgumentException("Sequence should be an amino acid sequence");
        }

        int count = 0;
        for (State state : sequence.getStates()) {
            if (((AminoAcidState)state).isStop()) {
                count ++;
            }
        }

        return count;
    }


    /**
     * Produce a clean sequence filtered of spaces and digits.
     * @param seq the sequence
     * @param type the sequence type
     * @return An array of valid states of SequenceType (may be shorter than the original sequence)
     */
    public static State[] cleanSequence(final CharSequence seq, final SequenceType type) {
        int count = 0;
        for (int i = 0; i < seq.length(); i++) {
            final char c = seq.charAt(i);
            if (type.getState(c) != null) {
                count++;
            }
        }

        State[] cleaned = new State[count];
        int index = 0;
        for (int i = 0; i < seq.length(); i++) {
            final char c = seq.charAt(i);
            State state = type.getState(c);
            if (state != null) {
                cleaned[index] = state;
                index += 1;
            }
        }

        return cleaned;
    }

    public static String toString(State[] states) {
        StringBuilder builder = new StringBuilder();
        for (State state : states) {
            builder.append(state.getCode());
        }
        return builder.toString();
    }

}
