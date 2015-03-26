package com.intuit.quickfoods.data;

/**
 * Created by salse on 3/27/15.
 */
public class MenuItem {
    public String name;
    public MenuItem[] subMenuItems;
    public String color;

    public MenuItem search(MenuItem item, String key){
        MenuItem node = null;
        if(item.name == key) {
            node = item;
        }
        else if(item.subMenuItems != null){
            for(MenuItem subMenuItem: item.subMenuItems){
                node = search(subMenuItem,key);
                if(node != null)
                    return node;
            }
        }
        return node;
    }
    public MenuItem search(String key){
        return search(this, key);
    }
}
