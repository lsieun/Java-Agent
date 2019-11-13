package lsieun.agent.adapter;

import java.util.ArrayList;
import java.util.List;

public class Result {
    public final String className;
    public final List<NameAndDesc> list;

    public Result(String className) {
        this.className = className;
        this.list = new ArrayList();
    }

    public void add(String name, String desc) {
        NameAndDesc item = new NameAndDesc(name, desc);
        this.list.add(item);
    }

}
