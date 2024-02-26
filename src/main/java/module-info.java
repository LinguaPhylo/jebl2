/**
 * @author Walter Xie
 */
module jebl {
    requires java.desktop;
    requires java.logging;

    exports jebl.evolution.align;
    exports jebl.evolution.align.scores;
    exports jebl.evolution.alignments;
    exports jebl.evolution.characters;
    exports jebl.evolution.coalescent;
    exports jebl.evolution.distances;
    exports jebl.evolution.graphs;
    exports jebl.evolution.io;
    exports jebl.evolution.parsimony;
    exports jebl.evolution.sequences;
    exports jebl.evolution.substmodel;
    exports jebl.evolution.taxa;
    exports jebl.evolution.treemetrics;
    exports jebl.evolution.trees;
    exports jebl.evolution.treesimulation;
    exports jebl.util;
}