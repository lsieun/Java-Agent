package lsieun.agent.adapter;

public class NameAndDesc {
    public final String name;
    public final String desc;

    public NameAndDesc(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", name, desc);
    }
}
