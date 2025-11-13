package com.company.reducer;

import com.company.graph.Graph;

/**
 * The Reducer used by the actual application.
 * Delegates to ParallelReducer for best performance.
 */
public class Reducer {

    public static Graph reduce(Graph topology) {
        return ParallelReducer.reduce(topology);
    }
}
