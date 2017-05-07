/**
 * Tank class use locally by client to keep track of its own tank.
 */
public class Tank {
    private int id;
    private int x;
    private int y;
    private int dir;

    public Tank() {
        id = -1;
    }

    public Tank(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDir() {
        return dir;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }
}
