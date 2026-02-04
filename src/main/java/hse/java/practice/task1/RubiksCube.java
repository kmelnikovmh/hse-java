package hse.java.practice.task1;

import java.util.*;

public class RubiksCube {

    final int EDGES_COUNT = 6;
    final int FACE_SIZE = 8;

    final Edge[] edges = new Edge[EDGES_COUNT];
    final int[] state = new int[EDGES_COUNT * FACE_SIZE + 1];

    public RubiksCube() {
        CubeColor[] colors = CubeColor.values();
        for (int i = 0; i < EDGES_COUNT; ++i) {
            edges[i] = new Edge(colors[i]);
        }
        for (int i = 1; i <= EDGES_COUNT * FACE_SIZE; ++i) {
            state[i] = i;
        }
        updateEdges();
    }

    public void front(RotateDirection dir) { rotate(Face.FRONT, dir); }
    public void back(RotateDirection dir)  { rotate(Face.BACK, dir); }
    public void left(RotateDirection dir)  { rotate(Face.LEFT, dir); }
    public void right(RotateDirection dir) { rotate(Face.RIGHT, dir); }
    public void up(RotateDirection dir)    { rotate(Face.UP, dir); }
    public void down(RotateDirection dir)  { rotate(Face.DOWN, dir); }

    public Edge[] getEdges() { return edges; }

    @Override
    public String toString() { return Arrays.toString(edges); }


    
    enum Face { FRONT, BACK, LEFT, RIGHT, UP, DOWN };

    void rotate(Face face, RotateDirection dir) {
        for (int[] c : Permutations.getCycles(face, dir)) {
            rotateCycle(c);
        }
        updateEdges();
    }

    void rotateCycle(int[] cycle) {
        int tmp = state[cycle[cycle.length - 1]];
        for (int i = 0; i < cycle.length; ++i) {
            int cur = state[cycle[i]];
            state[cycle[i]] = tmp;
            tmp = cur;
        }
    }

    void updateEdges() {
        for (int i = 0; i < EDGES_COUNT; ++i) {
            edges[i].setParts(buildEdge(i));
        }
    }

    CubeColor[][] buildEdge(int edgeIdx) {
        CubeColor[][] face = new CubeColor[3][3];
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                if (x == 1 && y == 1) {
                    face[y][x] = CubeColor.values()[edgeIdx];
                } else {
                    face[y][x] = getCellColor(edgeIdx, x, y);
                }
            }
        }
        return face;
    }

    CubeColor getCellColor(int edgeIdx, int x, int y) {
        int pos = x + y * 3 + 1;
        if (5 < pos) {
            --pos;
        }
        int idx = state[edgeIdx * FACE_SIZE + pos];
        return CubeColor.values()[(idx - 1) / FACE_SIZE];
    }

    static class Permutations {

        static final int[][] FRONT_CW = {
            {9,11,16,14}, {10,13,15,12}, {38,17,43,8}, {39,20,42,5}, {40,22,41,3}
        };
        static final int[][] FRONT_CCW = {
            {9,14,16,11}, {10,12,15,13}, {38,8,43,17}, {39,5,42,20}, {40,3,41,22}
        };

        static final int[][] BACK_CW = {
            {25,27,32,30}, {26,29,31,28}, {19,33,6,48}, {21,34,4,47}, {24,35,1,46}
        };
        static final int[][] BACK_CCW = {
            {25,30,32,27}, {26,28,31,29}, {19,48,6,33}, {21,47,4,34}, {24,46,1,35}
        };

        static final int[][] LEFT_CW = {
            {1,3,8,6}, {2,5,7,4}, {33,9,41,32}, {36,12,44,29}, {38,14,46,27}
        };
        static final int[][] LEFT_CCW = {
            {1,6,8,3}, {2,4,7,5}, {33,32,41,9}, {36,29,44,12}, {38,27,46,14}
        };

        static final int[][] RIGHT_CW = {
            {17,19,24,22}, {18,21,23,20}, {48,16,40,25}, {45,13,37,28}, {43,11,35,30}
        };
        static final int[][] RIGHT_CCW = {
            {17,22,24,19}, {18,20,23,21}, {48,25,40,16}, {45,28,37,13}, {43,30,35,11}
        };

        static final int[][] UP_CW = {
            {33,35,40,38}, {34,37,39,36}, {25,17,9,1}, {26,18,10,2}, {27,19,11,3}
        };
        static final int[][] UP_CCW = {
            {33,38,40,35}, {34,36,39,37}, {25,1,9,17}, {26,2,10,18}, {27,3,11,19}
        };

        static final int[][] DOWN_CW = {
            {41,43,48,46}, {42,45,47,44}, {6,14,22,30}, {7,15,23,31}, {8,16,24,32}
        };
        static final int[][] DOWN_CCW = {
            {41,46,48,43}, {42,44,47,45}, {6,30,22,14}, {7,31,23,15}, {8,32,24,16}
        };

        static final Map<Face, CyclePermut> CYCLES = new EnumMap<>(Face.class);

        static {
            CYCLES.put(Face.FRONT, new CyclePermut(FRONT_CW, FRONT_CCW));
            CYCLES.put(Face.BACK, new CyclePermut(BACK_CW, BACK_CCW));
            CYCLES.put(Face.LEFT, new CyclePermut(LEFT_CW, LEFT_CCW));
            CYCLES.put(Face.RIGHT, new CyclePermut(RIGHT_CW, RIGHT_CCW));
            CYCLES.put(Face.UP, new CyclePermut(UP_CW, UP_CCW));
            CYCLES.put(Face.DOWN, new CyclePermut(DOWN_CW, DOWN_CCW));
        }

        static class CyclePermut {
            final int[][] clockwise;
            final int[][] counterClockwise;

            CyclePermut(int[][] clockwise, int[][] counterClockwise) {
                this.clockwise = clockwise;
                this.counterClockwise = counterClockwise;
            }
        }

        static int[][] getCycles(Face face, RotateDirection dir) {
            CyclePermut perm = CYCLES.get(face);
            return dir == RotateDirection.CLOCKWISE ? perm.clockwise : perm.counterClockwise;
        }
    }
}
