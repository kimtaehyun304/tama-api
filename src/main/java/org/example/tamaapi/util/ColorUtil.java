package org.example.tamaapi.util;

import java.awt.*;

public class ColorUtil {

    //Next.js <div className={`bg-[${color.hexCode}] w-[18px] h-[18px] inline-block`}></div> -> 배경색 안나옴
    public static String colorToHex(String color) {
        return switch (color) {
            case "화이트" -> "#FFFFFF";
            case "그레이" -> "#BFBFBF";
            case "블랙"  -> "#000000";
            case "레드"  -> "#E30718";
            case "브라운" -> "#A76A33";
            case "옐로우" -> "#F2E646";
            case "그린"  -> "#6AB441";
            case "블루"  -> "#4B7EB7";
            //핑크 #ca4481B7
            default -> throw new IllegalArgumentException("알 수 없는 색상: " + color);
        };
    }

    public static String convertColor(String color) {
        return switch (color) {
            case "화이트" -> "white";
            case "그레이" -> "gray";
            case "블랙" -> "black";
            case "레드" -> "red";
            case "브라운" -> "brown";
            case "핑크" -> "pink";
            case "베이지" -> "beige";
            case "옐로우" -> "yellow";
            case "그린" -> "green";
            case "블루" -> "blue";
            default -> throw new IllegalArgumentException("알 수 없는 색상: " + color);
        };
    }



}
