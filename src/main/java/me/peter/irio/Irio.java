/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.peter.irio;

import me.peter.irio.game.Game;

import java.awt.Canvas;

/**
 *
 * @author 020326pm
 */
public class Irio extends Canvas {
    private final static Irio instance = new Irio();
    
    //Entrypoint
    public static void main(String[] args) {
        //instance.init();
        Game.main(args);
    }
    
    //Main game initiator
    private void init() {
        
    }
    
    public static Irio getInstance(){
        return instance;
    }
}
