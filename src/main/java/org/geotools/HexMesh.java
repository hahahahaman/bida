package org.geotools;

import java.util.*;

public class HexMesh{
    // public HashMap<String, HexCell> parent;
    // public HashMap<String, ArrayList<HexCell>> children;

    public HashMap<String, HexCell> nodes;
    public HashMap<String, Boolean> inPath;
    public ArrayList<HexCell> path;

    public HexMesh(){
        // parent = new HashMap<>();
        // children = new HashMap<>();

        nodes = new HashMap<>();
        inPath = new HashMap<>();
        path = new ArrayList<>();
    }
}
