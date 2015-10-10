/*
 * Copyright (C) 2014 morten
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.laukvik.sql.ddl;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author morten
 */
public class Schema implements Sqlable {

    private final String name;
    private final List<Table> tables;
    private final List<View> views;
    private final List<Function> functions;

    private final List<Function> systemFunctions;
    private final List<Function> timeFunctions;
    private final List<Function> stringFunctions;
    private final List<Function> numericFunctions;

    public Schema() {
        this("");
    }

    public Schema(String name) {
        this.name = name;
        tables = new ArrayList<>();
        views = new ArrayList<>();
        functions = new ArrayList<>();
        systemFunctions = new ArrayList<>();
        timeFunctions = new ArrayList<>();
        stringFunctions = new ArrayList<>();
        numericFunctions = new ArrayList<>();
    }

    public List<Function> getSystemFunctions() {
        return systemFunctions;
    }

    public List<Function> getTimeFunctions() {
        return timeFunctions;
    }

    public List<Function> getStringFunctions() {
        return stringFunctions;
    }

    public List<Function> getNumericFunctions() {
        return numericFunctions;
    }

    public boolean isDefault(){
        return name.isEmpty();
    }

    public void addFunction(Function function){
        functions.add(function);
    }

    public void removeFunction(Function function){
        functions.remove(function);
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public void addView(View view){
        views.add(view);
    }

    public void removeView(View view){
        views.remove(view);
    }

    public List<View> getViews() {
        return views;
    }

    public void addTable(Table table){
        tables.add(table);
    }

    public void removeTable(Table table){
        tables.remove(table);
    }

    public List<Table> getTables() {
        return tables;
    }

    public String getName() {
        return name;
    }

    public void addSystemFunction(Function function){
        systemFunctions.add(function);
    }
    public void addStringFunction(Function function){
        stringFunctions.add(function);
    }
    public void addTimeFunction(Function function){
        timeFunctions.add(function);
    }
    public void addNumericFunction(Function function){
        numericFunctions.add(function);
    }


    @Override
    public String toString() {
        return name;
    }
}
