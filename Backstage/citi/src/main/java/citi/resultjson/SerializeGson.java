package citi.resultjson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by zhong on 2018/7/17 10:33
 */
public class SerializeGson {
    public static final Gson gson;
    static {
        gson=new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }
}
