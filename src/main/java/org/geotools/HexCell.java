package org.geotools;

import java.util.*;

public class HexCell {
    //children, parent, index
    public Pair<Integer, Integer> index;

    public HexCell parent = null;
    public ArrayList<HexCell> children;
    public int cost = 0;


    public HexCell(int i, int j){
        index = new Pair<>(i, j);
        children = new ArrayList<>();
    }

    public HexCell(Pair<Integer, Integer> p){
        index = p;
        children = new ArrayList<>();
    }
}
