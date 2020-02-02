package de.dosmike.sponge.oreapi.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

public class OreResultList<T> {

    OrePagination pagination;
    T[] result;

    public OreResultList(JsonObject object, Class<T> resultClass) {
        pagination = new OrePagination(object.get("pagination").getAsJsonObject());
        JsonArray array = object.getAsJsonArray("result");
        result = (T[]) Array.newInstance(resultClass, array.size());
        for (int i = 0; i < array.size(); i++)
            try {
                result[i] = resultClass.getConstructor(JsonObject.class)
                        .newInstance(array.get(i).getAsJsonObject());
            } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e) {
                e.printStackTrace();
            }
    }

    public OrePagination getPagination() {
        return pagination;
    }

    public T[] getResult() {
        return result;
    }

}
