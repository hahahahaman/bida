package org.geotools;

import java.lang.*;
import java.io.*;
// import jts.geom.Coordinate;
import com.vividsolutions.jts.geom.*;
import java.util.*;

import org.geotools.geometry.jts.JTSFactoryFinder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;


class SearchForwardThread extends Thread {

    public HexMesh mesh1, mesh2;
    public Stack<HexCell> selected, toVisit;
    public Coordinate s, t, tcenter;
    public double radius;
    // public ArrayList<HexCell> path;

    public HashMap<String, HexCell> grid;
    public boolean isMeshConnected = false;

    @Override
    public void run()
    {
        HexCell c = null;
        if(mesh1.nodes.isEmpty()){
            // create index grid index 0,0
            c = new HexCell(0, 0);

            toVisit.push(c);
            mesh1.nodes.put(c.index.toString(), c);

            // TODO draw node

        } else if(!toVisit.empty()){
            c = toVisit.pop();

            String name = c.index.toString();
            mesh1.nodes.put(name, c);

            if(mesh1.inPath.containsKey(name)){
                // already used
                return;
            } else if(false){
                //TODO meet obstacle
                return;
            } else {

                int x = c.index.getFirst(), y = c.index.getSecond();

                // make the children
                Pair<Integer, Integer> n = new Pair<>(x, y+1),
                    ne = new Pair<>(x+1, y+1),
                    se = new Pair<>(x+1, y),
                    s = new Pair<>(x, y-1),
                    sw = new Pair<>(x-1, y),
                    nw = new Pair<>(x-1, y+1);

                ArrayList<Pair<Integer,Integer>> dir =
                    new ArrayList<Pair<Integer, Integer>>();
                dir.add(n);
                dir.add(ne);
                dir.add(se);
                dir.add(s);
                dir.add(sw);
                dir.add(nw);


                int otherEndX = (int)tcenter.x,
                    otherEndY = (int)tcenter.y;
                if(!mesh2.path.isEmpty()){
                    // most recent node in the other path
                    HexCell otherEnd = mesh2.path.get(mesh2.path.size()-1);
                    otherEndX = otherEnd.index.getFirst();
                    otherEndY = otherEnd.index.getSecond();
                }

                int diffx = c.index.getFirst()- otherEndX,
                    diffy = c.index.getSecond()-otherEndY;

                double angle = Math.atan((double)diffy/diffx);
                if(diffx >= 0 && diffy >= 0){ // first quadrant
                } else if(diffx < 0 && diffy >= 0){ //second quadrant
                    angle = Math.PI + angle;
                } else if(diffx < 0 && diffy < 0){
                    angle = Math.PI + angle;
                } else {
                    angle = 2* Math.PI + angle;
                }

                int starting_point = 0;

                if(angle >= Math.PI/3 && angle <= 2*Math.PI/3){
                    starting_point = 0;
                } else if(angle >= 0 && angle <= Math.PI/3){
                    starting_point = 1;
                } else if(angle >= 5*Math.PI/3 && angle <= 2*Math.PI){
                    starting_point = 2;
                } else if(angle >= 4*Math.PI/3 && angle <= 5*Math.PI/3){
                    starting_point = 3;
                } else if(angle >= Math.PI && angle <= 4*Math.PI/3){
                    starting_point = 4;
                } else {
                    starting_point = 5;
                }

                if(!grid.containsKey(dir.get(starting_point).toString()+":"+1)){
                    HexCell currentCell = new HexCell(dir.get(starting_point));
                    currentCell.parent = c;
                    currentCell.cost = c.cost+1;
                    c.children.add(currentCell);

                    grid.put(dir.get(starting_point).toString()+":"+1,
                             currentCell);

                    if(grid.containsKey(dir.get(starting_point).toString()+":"+2)){
                        // the meshes are connected
                        isMeshConnected = true;
                    }

                    toVisit.push(currentCell);
                }

                for(int i = 1; i <= 3; i++){
                    int l = starting_point - i;
                    int r = starting_point + i;
                    if(l < 0) l += 6;
                    if(r > 5) r -= 6;

                    if(!grid.containsKey(dir.get(l).toString()+":"+1)){
                        HexCell currentCell = new HexCell(dir.get(l));
                        currentCell.parent = c;
                        currentCell.cost = c.cost+1;
                        c.children.add(currentCell);

                        grid.put(dir.get(l).toString()+":"+1,
                                 currentCell);

                        if(grid.containsKey(dir.get(l).toString()+":"+2)){
                            // the meshes are connected
                            isMeshConnected = true;
                        }

                        //TODO draw the children

                        toVisit.push(currentCell);
                    }

                    if(!grid.containsKey(dir.get(r).toString()+":"+1)){
                        HexCell currentCell = new HexCell(dir.get(r));
                        currentCell.parent = c;
                        currentCell.cost = c.cost+1;
                        c.children.add(currentCell);

                        grid.put(dir.get(r).toString()+":"+1,
                                 currentCell);

                        if(grid.containsKey(dir.get(r).toString()+":"+2)){
                            // the meshes are connected
                            isMeshConnected = true;
                        }

                        //TODO draw the children

                        toVisit.push(currentCell);
                    }
                }

                // add current node to stack and path
                selected.push(c);
                mesh1.path.add(c);

                mesh1.inPath.put(name, true);
            }


            if(c.index.getFirst() == (int)tcenter.x &&
               c.index.getSecond() == (int)tcenter.y){
                isMeshConnected = true;
            }
        } else {
            isMeshConnected = true; // stop the loop, nowhere left to go
        }
    }
}

class SearchBackwardThread extends Thread{
    public HexMesh mesh1, mesh2;
    public Stack<HexCell> selected, toVisit;
    public Coordinate s, t, tcenter;
    public double radius;
    // public ArrayList<HexCell> path;

    public HashMap<String, HexCell> grid;
    public boolean isMeshConnected = false;
    public int hexi, hexj;

    @Override
    public void run()
    {
        HexCell c = null;
        if(mesh2.nodes.isEmpty()){
            // create index grid index 0,0
            c = new HexCell(hexi, hexj);

            toVisit.push(c);
            mesh2.nodes.put(c.index.toString(), c);

            // TODO draw node

        } else if(!toVisit.empty()){
            c = toVisit.pop();

            String name = c.index.toString();
            mesh2.nodes.put(name, c);

            if(mesh2.inPath.containsKey(name)){
                // already used
                return;
            } else if(false){
                //TODO meet obstacle
                return;
            } else {

                int x = c.index.getFirst(), y = c.index.getSecond();

                // make the children
                Pair<Integer, Integer> n = new Pair<>(x, y+1),
                    ne = new Pair<>(x+1, y+1),
                    se = new Pair<>(x+1, y),
                    s = new Pair<>(x, y-1),
                    sw = new Pair<>(x-1, y),
                    nw = new Pair<>(x-1, y+1);

                ArrayList<Pair<Integer,Integer>> dir =
                    new ArrayList<Pair<Integer, Integer>>();
                dir.add(n);
                dir.add(ne);
                dir.add(se);
                dir.add(s);
                dir.add(sw);
                dir.add(nw);

                int otherEndX = 0,
                    otherEndY = 0;
                if(!mesh1.path.isEmpty()){
                    // most recent node in the other path
                    HexCell otherEnd =
                        mesh1.path.get(mesh1.path.size()-1);
                    otherEndX = otherEnd.index.getFirst();
                    otherEndY = otherEnd.index.getSecond();
                }

                int diffx = c.index.getFirst()- otherEndX,
                    diffy = c.index.getSecond()-otherEndY;

                double angle = Math.atan((double)diffy/diffx);
                if(diffx >= 0 && diffy >= 0){ // first quadrant
                } else if(diffx < 0 && diffy >= 0){ //second quadrant
                    angle = Math.PI + angle;
                } else if(diffx < 0 && diffy < 0){
                    angle = Math.PI + angle;
                } else {
                    angle = 2* Math.PI + angle;
                }

                int starting_point = 0;

                if(angle >= Math.PI/3 && angle <= 2*Math.PI/3){
                    starting_point = 0;
                } else if(angle >= 0 && angle <= Math.PI/3){
                    starting_point = 1;
                } else if(angle >= 5*Math.PI/3 && angle <= 2*Math.PI){
                    starting_point = 2;
                } else if(angle >= 4*Math.PI/3 && angle <= 5*Math.PI/3){
                    starting_point = 3;
                } else if(angle >= Math.PI && angle <= 4*Math.PI/3){
                    starting_point = 4;
                } else {
                    starting_point = 5;
                }

                if(!grid.containsKey(dir.get(starting_point).toString()+"2")){
                    HexCell currentCell = new HexCell(dir.get(starting_point));
                    currentCell.parent = c;
                    currentCell.cost = c.cost+1;
                    c.children.add(currentCell);

                    grid.put(dir.get(starting_point).toString()+":"+2,
                             currentCell);

                    if(grid.containsKey(dir.get(starting_point).toString()+":"+1)){
                        // the meshes are connected
                        isMeshConnected = true;
                    }

                    toVisit.push(currentCell);
                }

                for(int i = 1; i <= 2; i++){
                    int l = starting_point - i;
                    int r = starting_point + i;
                    if(l < 0) l += 6;
                    if(r > 5) r -= 6;

                    if(!grid.containsKey(dir.get(l).toString()+":"+2)){
                        HexCell currentCell = new HexCell(dir.get(l));
                        currentCell.parent = c;
                        currentCell.cost = c.cost+1;
                        c.children.add(currentCell);

                        grid.put(dir.get(l).toString()+":"+2,
                                 currentCell);

                        if(grid.containsKey(dir.get(l).toString()+":"+1)){
                            // the meshes are connected
                            isMeshConnected = true;
                        }

                        //TODO draw the children

                        toVisit.push(currentCell);
                    }

                    if(!grid.containsKey(dir.get(r).toString()+":"+2)){
                        HexCell currentCell = new HexCell(dir.get(r));
                        currentCell.parent = c;
                        currentCell.cost = c.cost+1;
                        c.children.add(currentCell);

                        grid.put(dir.get(r).toString()+":"+2,
                                 currentCell);

                        if(grid.containsKey(dir.get(r).toString()+":"+1)){
                            // the meshes are connected
                            isMeshConnected = true;
                        }

                        //TODO draw the children

                        toVisit.push(currentCell);
                    }
                }

                // add current node to stack and path
                selected.push(c);
                mesh2.path.add(c);

                mesh2.inPath.put(name, true);
            }
            if(c.index.getFirst() == (int)tcenter.x &&
               c.index.getSecond() == (int)tcenter.y){
                isMeshConnected = true;
            }
        } else {
            isMeshConnected = true; // stop the loop, nowhere left to go
        }
    }

}

public class App extends JComponent {

    private static class Line{
        final int x1; 
        final int y1;
        final int x2;
        final int y2;   
        final Color color;

        public Line(int x1, int y1, int x2, int y2, Color color) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
        }               
    }

    private final LinkedList<Line> lines = new LinkedList<Line>();

    public void addLine(int x1, int x2, int x3, int x4) {
        addLine(x1, x2, x3, x4, Color.black);
    }

    public void addLine(int x1, int x2, int x3, int x4, Color color) {
        lines.add(new Line(x1,x2,x3,x4, color));        
        repaint();
    }

    public void clearLines() {
        lines.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Line line : lines) {
            g.setColor(line.color);
            g.drawLine(line.x1, line.y1, line.x2, line.y2);
        }
    }

    public static void main( String[] args )
    {
        double radius = 0.5,
            hexWidth = 2*radius,
            height = 2*radius * Math.sin(Math.toRadians(60)),
            squareWidth = 3*radius/2;

        Coordinate s = new Coordinate(10.0, 10.0),
            t = new Coordinate(s.x -10*hexWidth, s.y - 10*height);


        // s is the center of hexagon (0,0)
        // translate t so that the origin is at the corner of the hexagon
        double tx = t.x - (s.x - hexWidth/2),
            ty = t.y - (s.y - height/2);

        // indices for the square grid tile
        double tilei = Math.floor(tx/squareWidth);

        double yts = ty - (((int)tilei % 2) * (height/2.0));

        double tilej = Math.floor(yts / height);
        double tsqi = ty - tilei * squareWidth,
            tsqj = yts - tilej * height;

        double tilelocalx = tx - tilei * squareWidth,
            tilelocaly = yts - tilej * height;

        double xtile = radius * Math.abs(0.5 - tilelocaly/height);

        int d = (tilelocaly > height/2) ? 1 : 0;

        // hexagon index of point t
        double hexi = (tilelocalx > xtile) ? tilei : tilei-1,
            hexj = (tilelocalx > xtile) ? tilej : tilej - ((int)tilei %2) + d;

        double hexcenterx = s.x + hexi * hexWidth,
            hexcentery = s.y + hexj * height;

        Coordinate tHexCenter = new Coordinate(hexcenterx, hexcentery);

        // System.out.println(tilei + " " + tilej);
        // System.out.println(tilelocalx + " " + tilelocaly);
        // System.out.println(hexi + " " + hexj);
        System.out.println(tHexCenter.x + " " + tHexCenter.y);

        HexMesh mesh1 = new HexMesh(), mesh2 = new HexMesh();
        Stack<HexCell> selected1 = new Stack<HexCell>(),
            toVisit1 = new Stack<HexCell>(),
            selected2 = new Stack<HexCell>(),
            toVisit2 = new Stack<HexCell>();
        ArrayList<HexCell> path1 = new ArrayList<HexCell>(),
            path2 = new ArrayList<HexCell>();
        HexCell cell, parent;

        boolean isMeshConnected = false;

        HashMap<String, HexCell> grid = new HashMap<>();

        while(!isMeshConnected){
            SearchForwardThread sf = new SearchForwardThread();

            sf.s = s;
            sf.t = t;

            sf.mesh1 = mesh1;
            sf.mesh2 = mesh2;

            sf.selected = selected1;
            sf.toVisit = toVisit1;

            sf.tcenter = tHexCenter;
            sf.radius = radius;
            sf.grid = grid;
            sf.isMeshConnected = isMeshConnected;

            SearchBackwardThread sb = new SearchBackwardThread();

            sb.s = s;
            sb.t = t;

            sb.mesh1 = mesh1;
            sb.mesh2 = mesh2;

            sb.selected = selected2;
            sb.toVisit = toVisit2;

            sb.tcenter = tHexCenter;
            sb.radius = radius;
            sb.grid = grid;
            sb.isMeshConnected = isMeshConnected;
            sb.hexi = (int)hexi;
            sb.hexj = (int)hexj;

            // perform an iteration the backwards and forwards search
            sf.start();
            sb.start();

            // join both thread (wait for threads to die)
            try {
                System.out.println("Current Thread: " + Thread.currentThread().getName());
                sf.join();
            } catch(Exception ex) {
                System.out.println("Exception has " + "been caught" + ex);
            }

            try {
                System.out.println("Current Thread: " + Thread.currentThread().getName());
                sb.join();
            } catch(Exception ex) {
                System.out.println("Exception has " + "been caught" + ex);
            }

            isMeshConnected = sf.isMeshConnected ||
                sb.isMeshConnected;
        }

        System.out.println(grid);


        // mergePath(path1, path2);

    }
}
