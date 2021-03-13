package utils;

import java.util.LinkedList;
import java.util.List;

public class Response {
    private final List<String> datas;

    public Response() {
        datas = new LinkedList<>();
    }

    public void addData(String data) {
        datas.add(data);
    }

    public List<String> getAll() {
        return datas;
    }

    private void removeByIndex(int ind) {
        if (!datas.isEmpty()) {
            datas.remove(ind);
        }
    }

    public void removeFirst() {
        removeByIndex(0);
    }

    public void removeLast() {
        removeByIndex(datas.size() - 1);
    }

    private String getByIndex(int ind) {
        return datas.isEmpty() ? "" : datas.get(ind);
    }

    public String first() {
        return getByIndex(0);
    }

    public String last() {
        return getByIndex(datas.size() - 1);
    }

    public boolean isEmpty() {
        return datas.isEmpty();
    }
}
