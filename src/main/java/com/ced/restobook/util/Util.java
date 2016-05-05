package com.ced.restobook.util;

public class Util {

    /**
     * Affiche une distance en m ou km en fonction de si la valeur est inférieur ou supérieur à 1000m
     * @param distance
     * @return
     */
    public static String distanceToString(int distance){
        String s = "";
        if(distance < 1000)
            s = distance+" m";
        else{
            double r = distance / 100;
            r = r / 10.0;
            s = r+" km";
        }
        return s;
    }
}
