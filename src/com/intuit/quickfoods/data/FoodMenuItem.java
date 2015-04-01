package com.intuit.quickfoods.data;

import android.graphics.Color;

import java.util.Random;

/**
 * Created by salse on 3/27/15.
 */
public class FoodMenuItem {
    public String name;
    public FoodMenuItem[] subMenuItems;
    public int color = -1;

    private FoodMenuItem search(FoodMenuItem item, String key){
        FoodMenuItem node = null;
        if(item.name == key) {
            node = item;
        }
        else if(item.subMenuItems != null){
            for(FoodMenuItem subFoodMenuItem : item.subMenuItems){
                node = search(subFoodMenuItem,key);
                if(node != null)
                    return node;
            }
        }
        return node;
    }

    private void setRandomColors(FoodMenuItem child){
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        // if color has already been set then don't set it again
        if(child.color == -1)
            child.color = color;
        if(child.subMenuItems != null) {
            for (FoodMenuItem item : child.subMenuItems) {
                setRandomColors(item);
            }
        }

    }
    // call this method in the root
    public void setRandomColors(){
        for(FoodMenuItem item: this.subMenuItems){
           setRandomColors(item);
        }
    }
    public FoodMenuItem search(String key){
        return search(this, key);
    }
}
